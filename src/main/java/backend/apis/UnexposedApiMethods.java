package backend.apis;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import backend.cityArea.City;
import backend.cityArea.JCity;
import backend.deliveryRequests.DeliveryRequest;
import backend.deliveryRequests.DeliveryRequestState;
import backend.deliveryRequests.clientWrappers.DeliveryTimeline;
import backend.general.ConstantParams;
import backend.general.UserPrivileges;
import backend.helpers.FireBaseHelper;
import backend.helpers.returnWrappers.BooleanWrapper;
import backend.helpers.returnWrappers.LongWrapper;
import backend.helpers.returnWrappers.StringWrapper;
import backend.merchants.Merchant;
import backend.merchants.dessertsMerchant.DessertsMerchant;
import backend.merchants.inventory.ActualCategory;
import backend.merchants.inventory.Inventory;
import backend.merchants.jsonWrappers.JMerchant;
import backend.merchants.jsonWrappers.JsonRawMerchant;
import backend.merchants.pharmacy.Pharmacy;
import backend.merchants.restaurant.Restaurant;
import backend.merchants.specialMerchant.SpecialMerchant;
import backend.merchants.superMarket.SuperMarket;
import backend.monitor.DeliveryRequestCheckView;
import backend.monitor.DeliveryRequestChecker;
import backend.offers.PromotionMessage;
import backend.profiles.Profile;
import backend.profiles.customer.Customer;
import backend.profiles.driver.Driver;
import backend.profiles.driver.UpdatableLocation;
import backend.stats.DriverStats;
import backend.stats.MerchantStats;

import static backend.profiles.Profile.getProfileByID;

/**
 * Created by Muhammad on 07/01/2018.
 */

public class UnexposedApiMethods {
    public static LongWrapper resendDeliveryRequest(@Named("deliveryRequestId") Long deliveryRequestId) {
        final Queue queue = QueueFactory.getQueue("driverQueue");
        queue.add(TaskOptions.Builder.withUrl("/GetTheNearestDriverServlet").
                param("deliveryRequestId", String.valueOf(deliveryRequestId)));
        return new LongWrapper(deliveryRequestId);
    }

    public static StringWrapper uploadCityData(String dataJson, boolean support) {
        Gson gson = new Gson();
        JCity jcity = gson.fromJson(dataJson, JCity.class);
        System.out.println(jcity);
        jcity.city.supported = support;
        City city = jcity.uploadCity();
        return new StringWrapper(city.toString());
    }

    public static Inventory createInventory(String categoriesJson) {
        Type listType = new TypeToken<ArrayList<ActualCategory>>() {
        }.getType();
        List<ActualCategory> actualCategories = new Gson().fromJson(categoriesJson, listType);
        return Inventory.init(actualCategories);
    }

    public static BooleanWrapper doneCheckingADelivery(Long deliveryRequestId) {
        boolean b = DeliveryRequestChecker.doneCheckingADelivery(deliveryRequestId);
        return new BooleanWrapper(b);
    }

    public static List<DeliveryRequestCheckView> getDeliveryRequestsThatNeedToBeChecked(Long cityId) {
        return DeliveryRequestChecker.getDeliveryRequestsThatNeedToBeChecked(cityId);
    }

    public static StringWrapper uploadJsonMerchant(boolean newMerchant, String jMerchantJSON) throws Exception {
        Gson gson = new Gson();
        JMerchant jMerchant = gson.fromJson(jMerchantJSON, JMerchant.class);
        Merchant merchant = createMerchant(newMerchant, jMerchant);
        JsonRawMerchant jsonRawMerchant = new JsonRawMerchant(merchant.id, jMerchantJSON);

        String merchantId = String.valueOf(merchant.id);

        final Queue queue = QueueFactory.getQueue("DataUploaderQueue");
        queue.add(TaskOptions.Builder.withUrl("/admin/DataUploaderServlet")
                .param("jsonRawMerchantId", String.valueOf(jsonRawMerchant.id))
                .param("merchantId", merchantId)
                .param("newMerchant", String.valueOf(newMerchant))
        );

        return new StringWrapper("uploading ...");
    }

    public static Merchant createMerchant(boolean newMerchant, JMerchant jMerchant) throws Exception {
        Merchant merchant = null;
        String type = jMerchant.type.toLowerCase().trim();
        switch (type) {
            case ("r"):
                merchant = new Restaurant(newMerchant, jMerchant);
                break;
            case ("ph"):
                merchant = new Pharmacy(newMerchant, jMerchant);
                break;
            case ("sm"):
                merchant = new SuperMarket(newMerchant, jMerchant);
                break;
            case ("sp"):
                merchant = new SpecialMerchant(newMerchant, jMerchant);
                break;
            case ("d"):
                merchant = new DessertsMerchant(newMerchant, jMerchant);
                break;
        }
        merchant.saveMerchant();
        return merchant;
    }

    public static StringWrapper deleteMerchant(Long merchantId) {
        final Queue queue = QueueFactory.getQueue("MerchantDeletionQueue");
        queue.add(TaskOptions.Builder.withUrl("/admin/MerchantDeletionServlet").
                param("merchantId", String.valueOf(merchantId)));
        return new StringWrapper("deleting now ...");
    }

    public static ConstantParams setConstantParams(int waitingTimeForDriverToAccept,
                                                   int checkTime,
                                                   int checkCount,
                                                   int timeSliceForCheckingDriverState,
                                                   int maxDeliveryRequestsPerDriver,
                                                   Long cityId,
                                                   double ourPercentagePerDeliveryFromDelivery,
                                                   List<String> customerSupportPhones,
                                                   List<Long> specialDriversIds) {

        ConstantParams.setAll(waitingTimeForDriverToAccept, checkTime, checkCount,
                maxDeliveryRequestsPerDriver, timeSliceForCheckingDriverState,
                cityId, ourPercentagePerDeliveryFromDelivery, customerSupportPhones, specialDriversIds);

        return ConstantParams.getParamsByCityId(cityId);
    }


    public static DeliveryRequestState deliveryCompleted(Long deliveryRequestId, boolean fromDriver) throws IOException {
        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestId);
        List<String> customerRegTokens = getProfileByID(deliveryRequest.customerId).getRegTokenList();
        deliveryRequest.driverCompletedDelivery(); //update delivery request
        // TODO: 05/02/2018  update In Queue
        Driver.getDriverByID(deliveryRequest.driverId) //update driver
                .driverCompletedDelivery(deliveryRequestId);
        Customer.deliveryCompleted(deliveryRequest.customerId, deliveryRequest); //update customer
        MerchantStats.deliveryCompleted(deliveryRequestId); // update MerchantStats
        DriverStats.deliveryCompleted(deliveryRequestId);
        DeliveryTimeline deliveryTimeline = new DeliveryTimeline(deliveryRequest);
        if (fromDriver) {
            FireBaseHelper.sendNotification(customerRegTokens, deliveryTimeline.toJson());
        }
        return new DeliveryRequestState(deliveryRequest);
    }

    public static BooleanWrapper sendNotificationToAllCustomers(Long cityId, String title, String content, int flag,
                                                                Long merchantId) {
        PromotionMessage promotionMessage;
        if (merchantId == null) {
            promotionMessage = new PromotionMessage(flag, title, content);
        } else {
            String imageUrl = Merchant.getMerchantByID(merchantId).imageURL;
            promotionMessage = new PromotionMessage(flag, title, content, imageUrl, merchantId);
        }

        String promotionMessageJson = new Gson().toJson(promotionMessage);
        final Queue queue = QueueFactory.getQueue("SendNotificationQueue");
        queue.add(TaskOptions.Builder.withUrl("/admin/SendNotificationServlet")
                .param("cityId", String.valueOf(cityId))
                .param("promotionMessageJson", promotionMessageJson)
        );
        return new BooleanWrapper(true);
    }

    public static Driver createDriver(String name, String password, Long cityId,
                               String phone,float longitude, float latitude) throws UnauthorizedException {
        Driver driver = null;
        List<Profile> drivers = Driver.getByPhone(phone);
        if (drivers.size() == 0) {
            driver = new Driver(name, phone, password, cityId);
            driver.saveProfile();
            UpdatableLocation updatableLocation = new UpdatableLocation(driver.id, driver.cityId, new GeoPt(latitude, longitude));
            updatableLocation.save();
            driver.updatableLocationId = updatableLocation.id;
            driver.saveProfile();
            DriverStats driverStats = new DriverStats(driver.id);
            driver.driverStatsId = driverStats.id;
            driver.saveProfile();
            //saving profile is implied in addRegToken inside the constructor
        } else {
            driver = (Driver) drivers.get(0);
        }
        return driver;
    }
}
