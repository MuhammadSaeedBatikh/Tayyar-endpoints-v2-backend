package backend.servlets;

import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.apis.DriverApi;
import backend.deliveryRequests.DeliveryRequest;
import backend.deliveryRequests.DeliveryRequestState;
import backend.general.ConstantParams;
import backend.helpers.FireBaseHelper;
import backend.helpers.UtilityHelper;
import backend.merchants.Merchant;
import backend.profiles.driver.Driver;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 24/08/2017.
 */

public class GetTheNearestDriverServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String deliveryRequestIdStr = req.getParameter("deliveryRequestId");
        Long deliveryRequestId = Long.parseLong(deliveryRequestIdStr);

        // calculate stats


        // getDriver
        getTheNearestDriver(deliveryRequestId);
        System.out.println("done and done");
    }

    DeliveryRequest getTheNearestDriver(Long deliveryRequestID) {
        final DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestID);
        String merchantType = deliveryRequest.merchantType;
        if (merchantType.equalsIgnoreCase("ph") || merchantType.equalsIgnoreCase("pr")) {
            sendRequestToPharmacy(deliveryRequest);
            return deliveryRequest;
        }


        Long cityId = deliveryRequest.cityId;
        System.out.println(cityId);

        Query<Driver> driverQuery = ofy().load().type(Driver.class)
                .filter("idle =", true)
                .filter("cityId =", cityId)
                .filter("generalDriver =", true);

        List<Driver> driverList = driverQuery.list();
        List<Long> driverIDs = new ArrayList<>();
        //getting list of all active drivers' IDs
        for (Driver driver : driverList) {
            driverIDs.add(driver.id);
        }

        List<Long> driversWhoRefusedIDs = deliveryRequest.driversWhoRefusedIDs;
        //filtering out drivers who refused
        for (Long id : driversWhoRefusedIDs) {
            driverIDs.remove(id);
        }


        try {

            //sort according to some criteria ... distance from driver to merchant,
            final ConstantParams constantParams = ConstantParams.getParamsByCityId(cityId);
            final List<Long> specialDriversIds = constantParams.specialDriversIds;
            final List<Long> driversIdsFinal = driverIDs;
            // start a transaction
            Long driverIdOld = ofy().transact(new Work<Long>() {
                @Override
                public Long run() {
                    final Long driverIdOld = getBestDriver(driversIdsFinal, deliveryRequest, constantParams);
                    Driver driver = Driver.getDriverByID(driverIdOld);
                    deliveryRequest.driverId = driverIdOld;
                    deliveryRequest.save();
                    DeliveryRequestState deliveryRequestState = new DeliveryRequestState(deliveryRequest);
                    if (!specialDriversIds.contains(driverIdOld)) {
                        driver.waitForAcceptance(); // not idle
                    }
                    try {
                        FireBaseHelper.sendNotification(driver.regTokenList, deliveryRequestState.toJson());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return driverIdOld;
                }
            });


            if (specialDriversIds.contains(driverIdOld))

            {
                return deliveryRequest;
            }

            //check for error
            int waitingTimeForDriverToAccept = constantParams.waitingTimeForDriverToAccept;
            int checkTime = constantParams.checkTime;
            int checkCount = constantParams.checkCount;
            Thread.sleep(waitingTimeForDriverToAccept);//wait for 60 s

            checkLoop:
            for (int i = 0; i < checkCount; i++) {

                System.out.println("i = " + i);
                DeliveryRequest deliveryRequestNew = DeliveryRequest.getById(deliveryRequestID);
                int stateNew = deliveryRequestNew.getState();
                List<Long> driversWhoRefusedIdsNew = deliveryRequestNew.driversWhoRefusedIDs;
                System.out.println("state = " + stateNew + " delivery.state = " + deliveryRequest.state);
                if (stateNew == 1) {
                    System.out.println("accepted");
                    //accepted
                    break checkLoop;
                } else if (stateNew == 0) {
                    if (driversWhoRefusedIdsNew.contains(driverIdOld)) {
                        //refused
                        System.out.println("driver refused");
                        break checkLoop;

                    } else {
                        //error
                        if (i == checkCount - 1) {
                            System.out.println("force driver to refuse and send another DeliveryRequest");
                            DeliveryRequest deliveryRequest1 = DriverApi.getApiSingleton()
                                    .driverRefuseDeliveryRequest(deliveryRequestID, driverIdOld, "not available");
                            System.out.println("deliveryRequest1 = " + deliveryRequest1);
                            break checkLoop;
                        }
                    }
                }
                Thread.sleep(checkTime);
            }

            return deliveryRequest;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private Long getBestDriver(List<Long> driverIDs, DeliveryRequest deliveryRequest, ConstantParams constantParams) {
        List<Long> specialDriversIds = constantParams.specialDriversIds;

        for (Long specialDriversId : specialDriversIds) {
            driverIDs.remove(specialDriversId);
        }


        Long specialDriverId = constantParams.getBestSpecialDriver();
        int max = constantParams.maxDeliveryRequestsPerDriver;


        Merchant merchant = Merchant.getMerchantByID(deliveryRequest.merchantId);
        int deliveryOption = merchant.deliveryOption;
        Long bestDriverId = specialDriverId;

        if (deliveryOption == 2) { //doesn't want our drivers
            return specialDriverId;
        }

        GeoPt merchantGeoPt = merchant.currentLocationGeoPt;
        double oldDistance = Double.MAX_VALUE;
        for (Long driverID : driverIDs) {
            Driver driver = Driver.getDriverByID(driverID);
            if (driverID == specialDriverId | driver.waitingToAcceptDeliveryRequest | !driver.idle) {
                System.out.println("do nothing");
            } else {
                if (driver.currentlyAcceptedDelReq < max) {
                    GeoPt driverLocation = driver.getDriverLocation();
                    double newDistance = UtilityHelper.distance(driverLocation, merchantGeoPt);
                    if (newDistance < oldDistance) {
                        oldDistance = newDistance;
                        bestDriverId = driverID;
                    }
                }
            }
        }
        return bestDriverId;
    }

    void sendRequestToPharmacy(DeliveryRequest deliveryRequest) {
        Long merchantId = deliveryRequest.merchantId;
        Driver driver = ofy().load().type(Driver.class)
                .filter("associatedMerchants", merchantId)
                .list()
                .get(0);
        deliveryRequest.driverId = driver.id;
        deliveryRequest.save();
        DeliveryRequestState deliveryRequestState = new DeliveryRequestState(deliveryRequest);
        try {
            FireBaseHelper.sendNotification(driver.regTokenList, deliveryRequestState.toJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
