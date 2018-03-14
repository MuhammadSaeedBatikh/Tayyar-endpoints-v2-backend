package backend.apis;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.cmd.Query;

import java.io.IOException;
import java.util.List;

import backend.deliveryRequests.DeliveryRequest;
import backend.deliveryRequests.DeliveryRequestState;
import backend.deliveryRequests.DeliveryRequestView;
import backend.deliveryRequests.clientWrappers.DeliveryTimeline;
import backend.general.ConstantParams;
import backend.general.ErrorMessage;
import backend.general.UserPrivileges;
import backend.helpers.Constants;
import backend.helpers.CursorHelper;
import backend.helpers.FireBaseHelper;
import backend.helpers.returnWrappers.BooleanWrapper;
import backend.helpers.returnWrappers.LongWrapper;
import backend.profiles.Profile;
import backend.profiles.driver.Driver;
import backend.profiles.driver.DriverInfo;
import backend.profiles.driver.UpdatableLocation;
import backend.reviews.Review;
import backend.reviews.ReviewsTypes;
import backend.stats.DriverStats;
import backend.stats.MerchantStats;

import static backend.profiles.Profile.getProfileByID;
import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 19/08/2017.
 */
@Api(name = "driverApi",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE}
)
public class DriverApi {
    private static DriverApi driverApiInstance;

    public DriverApi() {
    }

    public static DriverApi getApiSingleton() {
        if (driverApiInstance == null) {
            driverApiInstance = new DriverApi();
            return driverApiInstance;
        }
        return driverApiInstance;
    }


    @ApiMethod(name = "createDriver")
    public Driver createDriver(User user, @Named("name") String name,
                               @Named("password") String password,
                               @Named("City") Long cityId,
                               @Named("phone") String phone,
                               @Named("longitude") float longitude,
                               @Named("latitude") float latitude) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        return UnexposedApiMethods.createDriver(name, password, cityId, phone, longitude, latitude);
    }

    @ApiMethod(name = "signIn")
    public DriverInfo signIn(
            @Named("phone") String phone,
            @Named("password") String password,
            @Named("regToken") String regToken) {

        Driver driver = null;
        try {
            driver = Driver.getDriverByPhone(phone);
            if (driver.password.equals(password)) {
                driver.addRegToken(regToken);
                return new DriverInfo(driver);
            } else {
                return null;
            }
        } catch (UnauthorizedException e) {
            return null;
        }

    }

    @ApiMethod(name = "signOut")
    public BooleanWrapper signOut(@Named("driverID") Long driverID, @Named("regToken") String regToken) {

        Driver driver = (Driver) Driver.getProfileByID(driverID);
        driver.idle = false;
        driver.removeRegToken(regToken);
        return new BooleanWrapper(true);
    }

    @ApiMethod(name = "getDriverInfo")
    public DriverInfo getDriverInfo(@Named("driverId") Long driverId) {
        return DriverInfo.getDriverInfo(driverId);
    }

    @ApiMethod(name = "updateDriverLocation")
    public LongWrapper updateDriverLocation(@Named("driverId") Long driverId,
                                            @Named("longitude") float longitude,
                                            @Named("latitude") float latitude) {
        Long updateLocationId = UpdatableLocation.updateLocation(driverId, new GeoPt(latitude, longitude));
        Driver driver = Driver.getDriverByID(driverId);
        if (driver.idle == false) {
            driver.changeDriverState(true);
        }
        return new LongWrapper(updateLocationId);
    }

    @ApiMethod(name = "updateDriverState")
    public BooleanWrapper updateDriverState(@Named("driverId") Long driverId,
                                            @Named("idle") boolean idle) {
        Driver driver = Driver.getDriverByID(driverId);
        driver.changeDriverState(idle);
        return new BooleanWrapper(driver.idle);
    }


    @ApiMethod(name = "getDeliveryRequestViewById")
    public DeliveryRequestView getDeliveryRequestViewById(@Named("deliveryRequestId") Long deliveryRequestId) {
        return new DeliveryRequestView(DeliveryRequest.getById(deliveryRequestId));
    }


    @ApiMethod(name = "driverRefuseDeliveryRequest")
    public DeliveryRequest driverRefuseDeliveryRequest(@Named("deliveryRequestId") Long deliveryRequestId,
                                                       @Named("driverId") Long driverId,
                                                       @Named("content") String content) {
        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestId);
        deliveryRequest.addDriverWhoRefused(driverId);
        Driver.getDriverByID(driverId)
                .driverRefusesDelivery(deliveryRequestId); //update driver
        // update driver stats
        // redirect to another Driver if not special driver
        List<Long> specialDriversIds = ConstantParams.getParamsByCityId(deliveryRequest.cityId).specialDriversIds;
        boolean pharmacyRefused = deliveryRequest.merchantType.equalsIgnoreCase("ph") ||
                deliveryRequest.merchantType.equalsIgnoreCase("pr");
        if (specialDriversIds.contains(driverId) || pharmacyRefused) {
            if (pharmacyRefused) {
                deliveryRequest.driverId =null;
            }
            deliveryRequest.cancelDeliveryRequest();
            List<String> customerRegTokens = getProfileByID(deliveryRequest.customerId).getRegTokenList();
            ErrorMessage errorMessage = new ErrorMessage(0, "order has been canceled!", content, deliveryRequestId);
            try {
                FireBaseHelper.sendNotification(customerRegTokens, errorMessage.toJson());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return deliveryRequest;
        }

        final Queue queue = QueueFactory.getQueue("driverQueue");
        queue.add(TaskOptions.Builder.withUrl("/admin/GetTheNearestDriverServlet").
                param("deliveryRequestId", String.valueOf(deliveryRequest.id)));

        return deliveryRequest;

    }

    @ApiMethod(name = "driverArrivesAtCustomer")
    public DeliveryRequestState driverArrivesAtCustomer(@Named("deliveryRequestId") Long deliveryRequestId) {
        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestId);
        deliveryRequest.driverArrivesAtCustomer();
        DriverStats.arrivesAtCustomers(deliveryRequestId);
        List<String> customerRegTokens = getProfileByID(deliveryRequest.customerId).getRegTokenList();
        DeliveryTimeline deliveryTimeline = new DeliveryTimeline(deliveryRequest);

        try {
            FireBaseHelper.sendNotification(customerRegTokens, deliveryTimeline.toJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new DeliveryRequestState(deliveryRequest);
    }

    @ApiMethod(name = "driverAcceptsDeliveryRequest")
    public DeliveryRequestState acceptDeliveryRequest(@Named("deliveryRequestId") Long deliveryRequestId,
                                                      @Named("driverId") Long driverId) throws IOException {
        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestId);
        //some info about time etc
        deliveryRequest.driverAcceptsOrder(driverId); //update delivery request
        Driver.getDriverByID(driverId).driverAcceptsDeliveryRequest(deliveryRequestId); //update driver
        DriverStats.deliveryAccepted(deliveryRequestId);
        MerchantStats.deliveryAccepted(deliveryRequestId); // update merchant stats

        DeliveryTimeline deliveryTimeline = new DeliveryTimeline(deliveryRequest);
        List<String> customerRegTokens = getProfileByID(deliveryRequest.customerId).getRegTokenList();

        FireBaseHelper.sendNotification(customerRegTokens, deliveryTimeline.toJson());

        return new DeliveryRequestState(deliveryRequest);
    }

    @ApiMethod(name = "getRecentDeliveryRequests")
    public CollectionResponse<DeliveryRequestView> getRecentDeliveryRequests(@Named("driverId") Long driverId,
                                                                             @Named("state") int state,
                                                                             @Named("limitNumber") int limitNumber,
                                                                             @Named("cursorStr") String cursorStr
    ) {

        Query<DeliveryRequest> query = ofy().load().type(DeliveryRequest.class)
                .filter("driverId =", driverId)
                .order("creationDate")
                .limit(limitNumber);
        if (state != -1) {
            query = query.filter("state =", state);
        }

        //cursor stuff
        cursorStr = cursorStr.toLowerCase().equals("null") ? null : cursorStr;
        CursorHelper<DeliveryRequest> cursorHelper = new CursorHelper<>(DeliveryRequest.class);
        CollectionResponse<DeliveryRequest> deliveryRequestResponse =
                cursorHelper.queryAtCursor(query, cursorStr);
        List<DeliveryRequest> deliveryRequests = (List<DeliveryRequest>) deliveryRequestResponse.getItems();

        //to view
        List<DeliveryRequestView> deliveryRequestViews = DeliveryRequestView.toViewList(deliveryRequests);
        CollectionResponse<DeliveryRequestView> response = cursorHelper.buildCollectionResponse(deliveryRequestViews);

        return response;
    }


    @ApiMethod(name = "driverConfirmsPickUp")
    public DeliveryRequestState driverConfirmsPickUp(@Named("deliveryRequestId") Long deliveryRequestId) {

        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestId);
        deliveryRequest.driverConfirmsPickUP();
        DriverStats.confirmedPickUp(deliveryRequestId);
        DeliveryTimeline deliveryTimeline = new DeliveryTimeline(deliveryRequest);
        List<String> customerRegTokens = getProfileByID(deliveryRequest.customerId).getRegTokenList();
        try {
            FireBaseHelper.sendNotification(customerRegTokens, deliveryTimeline.toJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new DeliveryRequestState(deliveryRequest);
    }

    @ApiMethod(name = "driverCompletedDelivery")
    public DeliveryRequestState driverCompletedDelivery(@Named("deliveryRequestId") Long deliveryRequestId) throws IOException {
        return UnexposedApiMethods.deliveryCompleted(deliveryRequestId, true);
    }

    @ApiMethod(name = "reviewCustomer")
    public Review reviewCustomer(@Named("customerID") Long customerID,
                                 @Named("Driver") Long driverID,
                                 @Named("anonymous") boolean anonymous,
                                 @Named("rating") int rating, @Named("comment") String comment) {
        return Review.submitReview(ReviewsTypes.DriverReviewedCustomer, driverID, customerID, anonymous, comment, rating);
    }
}
