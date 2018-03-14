package backend.profiles.driver;


import backend.general.ConstantParams;
import backend.profiles.Profile;

import com.google.api.server.spi.Constant;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Subclass;

import java.util.ArrayList;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad Saeed on 2/11/2017.
 */
@Subclass(index = true)
public class Driver extends Profile {

    public String vehicle;
    public int rate;
    public String imageURL;

    @Index
    public String password;


    @Index
    public boolean idle = true;

    @Index
    public int currentlyAcceptedDelReq = 0;

    @Index
    public boolean waitingToAcceptDeliveryRequest;

    @Index
    public Long updatableLocationId;

    @Index
    public List<Long> refusedDeliveryRequestIds = new ArrayList<>();

    @Index
    public List<Long> completedDeliveryRequestIds = new ArrayList<>();

    @Index
    public List<Long> acceptedDeliveryRequestIds = new ArrayList<>();

    @Index
    public Long driverStatsId;

    @Index
    public List<Long> associatedMerchants = new ArrayList<>();
    @Index
    public boolean generalDriver = true;
    /*
    * if false, not general driver means something like, a pharmacy driver that only deliver for this particular pharmacy
    * */

    //default constructor for Entity initialization
    public Driver() {
    }

    public Driver(String name, String phone, String password, Long cityId) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.cityId = cityId;

    }

    public void changeDriverState(boolean idle) {
        this.idle = idle;
        saveProfile();
    }

    public void waitForAcceptance() {
        this.idle = false;
        this.waitingToAcceptDeliveryRequest = true;
        saveProfile();
    }

    public GeoPt getDriverLocation() {
        ofy().clear();
        return UpdatableLocation.getById(this.updatableLocationId).geoPt;
    }

    public void driverAcceptsDeliveryRequest(Long deliveryRequestId) {
        this.acceptedDeliveryRequestIds.add(deliveryRequestId);
        this.waitingToAcceptDeliveryRequest = false;
        this.idle = true;
        this.currentlyAcceptedDelReq += 1;
        saveProfile();
    }


    public void driverRefusesDelivery(Long deliveryRequestId) {
        this.refusedDeliveryRequestIds.add(deliveryRequestId);
        this.waitingToAcceptDeliveryRequest = false;
        this.idle = true;
        if (this.currentlyAcceptedDelReq > 0) {
            this.currentlyAcceptedDelReq -= 1;
        }
        saveProfile();
    }

    public void driverCompletedDelivery(Long deliveryRequestId) {
        this.completedDeliveryRequestIds.add(deliveryRequestId);
        this.currentlyAcceptedDelReq -= 1;
        saveProfile();
    }

    public static Driver getDriverByPhone(String phone) throws UnauthorizedException {
        try {
            return ofy().load().type(Driver.class)
                    .filter("phone =", phone)
                    .list().get(0);
        } catch (Exception e) {
            throw new UnauthorizedException("incorrect phone or password");
        }
    }

    public static Driver getDriverByID(Long id) {
        return ofy().load().type(Driver.class).id(id).now();
    }

    public void associateWithMerchant(Long merchantId, boolean generalDriver) {
        this.associatedMerchants.add(merchantId);
        this.generalDriver = generalDriver;
        saveProfile();
    }
    //============

    @Override
    public String toString() {
        return "Driver{" +
                "id=" + id +
                ", idle=" + idle +
                ", updatableLocationId=" + updatableLocationId +
                '}';
    }
}
