package backend.monitor;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import backend.deliveryRequests.DeliveryRequest;
import backend.general.ConstantParams;
import backend.general.ErrorMessage;
import backend.helpers.FireBaseHelper;
import backend.profiles.driver.Driver;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 08/02/2018.
 */

@Entity
@Cache
public class DeliveryRequestChecker {

    @Id
    public Long id;
    @Index
    public Long cityId;
    public List<Long> deliveryRequestsIds = new ArrayList<>();

    //default constructor for Entity initialization
    public DeliveryRequestChecker() {
    }
    //============

    public DeliveryRequestChecker(Long cityId, Long deliveryRequestId) {
        this.cityId = cityId;
        this.addDeliveryRequestId(deliveryRequestId);
    }

    public void addDeliveryRequestId(Long deliveryRequestId) {
        if (!this.deliveryRequestsIds.contains(deliveryRequestId)) {
            this.deliveryRequestsIds.add(deliveryRequestId);
            save();
        }
    }

    public void save() {
        ofy().save().entity(this).now();
    }

    public static DeliveryRequestChecker getByCityId(Long cityId) {
        List<DeliveryRequestChecker> deliveryRequestCheckers = ofy().load().type(DeliveryRequestChecker.class)
                .filter("cityId =", cityId)
                .list();
        if (deliveryRequestCheckers != null) {
            if (deliveryRequestCheckers.size() != 0) {
                return deliveryRequestCheckers.get(0);
            }
        }
        return null;
    }

    public static DeliveryRequestChecker getById(Long cityId) {
        return ofy().load().type(DeliveryRequestChecker.class).id(cityId).now();
    }

    public static DeliveryRequestChecker addDeliveryToBeChecked(Long deliveryRequestId) {
        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestId);
        Long cityId = deliveryRequest.cityId;
        DeliveryRequestChecker deliveryRequestChecker = getByCityId(cityId);
        if (deliveryRequestChecker == null) {
            deliveryRequestChecker = new DeliveryRequestChecker(cityId, deliveryRequestId);
        } else {
            deliveryRequestChecker.addDeliveryRequestId(deliveryRequestId);
        }
        return deliveryRequestChecker;
    }

    public static boolean doneCheckingADelivery(Long deliveryRequestId) {
        Long cityId = DeliveryRequest.getById(deliveryRequestId).cityId;
        DeliveryRequestChecker deliveryRequestChecker = getByCityId(cityId);
        boolean isCleared = deliveryRequestChecker.clearDeliveryRequest(deliveryRequestId);
        if (isCleared) {
            deliveryRequestChecker.save();
        }
        return isCleared;
    }

    public boolean clearDeliveryRequest(Long deliveryRequestId) {
        return this.deliveryRequestsIds.remove(deliveryRequestId);
    }

    public static List<DeliveryRequestCheckView> getDeliveryRequestsThatNeedToBeChecked(Long cityId) {
        DeliveryRequestChecker deliveryRequestChecker = getByCityId(cityId);
        if (deliveryRequestChecker == null) {
            return null;
        }
        List<DeliveryRequestCheckView> checkViews = new ArrayList<>(deliveryRequestChecker.deliveryRequestsIds.size());
        for (Long deliveryRequestsId : deliveryRequestChecker.deliveryRequestsIds) {
            checkViews.add(new DeliveryRequestCheckView(deliveryRequestsId));
        }
        return checkViews;
    }

    public static void notifySpecialDriversInCityToCheck(Long deliveryRequestId) {
        Long cityId = DeliveryRequest.getById(deliveryRequestId).cityId;
        List<Long> specialDriversIds = ConstantParams.getParamsByCityId(cityId).specialDriversIds;
        Map<Long, Driver> driverMap = ofy().load().type(Driver.class).ids(specialDriversIds);
        List<Driver> drivers = new ArrayList<>(driverMap.values());
        String errorMessageJson = new ErrorMessage(1, "Check Delivery", "delivery has not arrived!", deliveryRequestId).toJson();
        for (Driver driver : drivers) {
            try {
                FireBaseHelper.sendNotification(driver.getRegTokenList(), errorMessageJson);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
