package backend.profiles.customer;

import backend.deliveryRequests.DeliveryRequest;
import backend.merchants.Merchant;
import backend.profiles.Profile;

import com.google.appengine.repackaged.org.apache.commons.codec.language.ColognePhonetic;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Subclass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad Saeed on 2/9/2017.
 */
@Subclass(index = true)
@Cache
public class Customer extends Profile {

    @Index
    public int deliveryRequestCount = 0;
    @Index
    public List<Long> deliveryRequestsIds = new ArrayList<>();

    @Index
    public double totalAmountOfSpentMoney = 0;


    @Index
    public List<Long> favouriteMerchants = new ArrayList<>();

    public HashMap<String, Integer> offersMap = new HashMap<>(); //Map<code, usedCount>

    //default constructor for Entity initialization
    public Customer() {
    }

    public Customer(String name, String firebaseUid, String regToken, String phone) {
        super(name, firebaseUid, phone);
        this.addRegToken(regToken);
    }

    public static boolean addMerchantToFavourite(Long customerId, Long merchantId) {
        Customer customer = (Customer) getProfileByID(customerId);

        if (!customer.favouriteMerchants.contains(merchantId)) {
            customer.favouriteMerchants.add(merchantId);
            customer.saveProfile();
            return true;
        }
        return false;
    }

    public static boolean removeMerchantFromFavourite(Long customerId, Long merchantId) {
        Customer customer = (Customer) getProfileByID(customerId);
        boolean removed = customer.favouriteMerchants.remove(merchantId);
        if (removed) {
            customer.saveProfile();
        }
        return removed;
    }

    public static void deliveryCompleted(Long customerId, DeliveryRequest deliveryRequest) {
        Customer customer = (Customer) getProfileByID(customerId);
        customer.deliveryRequestCount++;
        customer.totalAmountOfSpentMoney += deliveryRequest.totalCharge;
        customer.deliveryRequestsIds.add(deliveryRequest.id);
        customer.saveProfile();
    }

    public int getCouponUsageCount(Long offerId) {
        String offerIdStr = offerId.toString();
        if (this.offersMap.get(offerIdStr) == null) {
            return 0;
        }
        return this.offersMap.get(offerIdStr);
    }

    public void usesCoupon(Long offerId) {
        int count = getCouponUsageCount(offerId);
        String offerIdStr = offerId.toString();
        this.offersMap.put(offerIdStr, ++count);
        this.saveProfile();

    }
    //============

}

