package backend.stats;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import backend.deliveryRequests.DeliveryRequest;
import backend.merchants.Merchant;
import backend.offers.Offer;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 05/02/2018.
 */
@Entity
@Cache
public class MerchantStats {
    @Id
    public Long id;

    public String merchantName;

    @Index
    public Long merchantId;

    @Index
    public Long cityId; // denormalization, to get Stats Of Al Merchants In a City, instead of getting

    public int overallCompletedDeliveryRequestsCount;
    public double overallCharge;
    public Map<String, Double> chargeMap = new HashMap<>(); //Map<Date of Delivery completion, charge amount>
    public Map<String, Integer> deliveryRequestsStatesMap = new HashMap<>();//Map<state integer, count>

    public Date lastPaymentDate;
    public Map<String, Double> paymentMap = new HashMap<>(); // Map<Date of payment, our charge amount //charge * percentage >
    public Map<String, List<Long>> paidDeliveryRequests = new HashMap<>(); // Map<Date of payment, list<delivery requests ids>>


    public Map<String, Double> unpaidChargeMap = new HashMap<>(); //Map<Date of Delivery completion, charge amount* percentage>
    public List<Long> unpaidDeliveryIds = new ArrayList<>();

    @Index
    public int unpaidDeliveryRequestsCount;

    public double ourPercentagePerDelivery;

    @Index
    public double overallUnpaidCharge;


    // computed metrics

    public double averageChargePerDeliverRequest;
    public double deliveryRatePerDay;
    public int deliveryCountLast24Hours;

    //default constructor for Entity initialization
    public MerchantStats() {

    }
    //============

    public static MerchantStats deliveryCompleted(Long deliveryRequestId) {
        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestId);
        MerchantStats merchantStats = getByMerchantId(deliveryRequest.merchantId);
        if (merchantStats.unpaidDeliveryIds.contains(deliveryRequestId)) {
            return null;
        }
        merchantStats.overallCompletedDeliveryRequestsCount++;
        double addedCharge = 0;
        Offer offer = Offer.getByCoupon(deliveryRequest.coupon);
        if (offer == null) {
            addedCharge = deliveryRequest.charge;
        } else {
            int discountType = Offer.getByCoupon(deliveryRequest.coupon).discountType;
            if (discountType == 2) {// discount on deliveryFee
                addedCharge = deliveryRequest.charge;
            } else {
                addedCharge = deliveryRequest.chargeAfterDiscount
                        - deliveryRequest.deliveryFee - deliveryRequest.tip - deliveryRequest.tayyarFee;
            }
        }

        merchantStats.overallCharge += addedCharge;
        String deliveryCompletionDate = new Date().toString();
        merchantStats.chargeMap.put(deliveryCompletionDate, addedCharge);
        Integer count = merchantStats.deliveryRequestsStatesMap.get("4");
        if (count == null) {
            count = 0;
        }
        merchantStats.deliveryRequestsStatesMap.put("4", ++count);
        merchantStats.unpaidDeliveryIds.add(deliveryRequestId);
        merchantStats.unpaidDeliveryRequestsCount++;
        double ourShare = addedCharge * merchantStats.ourPercentagePerDelivery;
        merchantStats.overallUnpaidCharge += ourShare;
        merchantStats.unpaidChargeMap.put(deliveryCompletionDate, ourShare);
        merchantStats.save();
        return merchantStats;
    }
    public static void confirmedPickUp(Long deliveryRequestId){
        // reason about whether calculation in deliveryCompleted method should be done here
    }
    public static void deliveryAccepted(Long deliveryRequestId) {
        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestId);
        MerchantStats merchantStats = getByMerchantId(deliveryRequest.merchantId);
        Integer count = merchantStats.deliveryRequestsStatesMap.get("1");
        if (count == null) {
            count = 0;
        }
        merchantStats.deliveryRequestsStatesMap.put("1", ++count);
        merchantStats.save();
    }


    public static MerchantStats merchantPays(Long merchantId) {
        MerchantStats merchantStats = getByMerchantId(merchantId);
        merchantStats.lastPaymentDate = new Date();
        merchantStats.paymentMap.put(merchantStats.lastPaymentDate.toString(), merchantStats.overallUnpaidCharge);
        merchantStats.paidDeliveryRequests.put(merchantStats.lastPaymentDate.toString(), merchantStats.unpaidDeliveryIds);

        // clear
        merchantStats.unpaidDeliveryRequestsCount = 0;
        merchantStats.overallUnpaidCharge = 0.0;
        merchantStats.unpaidChargeMap = new HashMap<>();
        merchantStats.unpaidDeliveryIds = new ArrayList<>();
        merchantStats.save();
        return merchantStats;
    }

    public MerchantStats(Long merchantId) {
        this.merchantId = merchantId;
        Merchant merchant = Merchant.getMerchantByID(merchantId);
        this.ourPercentagePerDelivery = merchant.ourPercentagePerDelivery;
        this.cityId = merchant.cityId;
        this.merchantName = merchant.nameAr; // for viewing
        save();
    }

    public void save() {
        ofy().save().entity(this).now();
    }

    public static MerchantStats getByMerchantId(Long merchantId) {
        List<MerchantStats> merchantStatsList = ofy().load().type(MerchantStats.class)
                .filter("merchantId =", merchantId)
                .list();
        return merchantStatsList.get(0);
    }

    public static MerchantStats getCompleteStats(Long merchantId) {
        MerchantStats merchantStats = getByMerchantId(merchantId);
        merchantStats.calculateAdditionalStats();
        return merchantStats;
    }

    public static List<MerchantStats> getMerchantsStatisticsInCity(Long cityId) {
        List<MerchantStats> merchantStatsList = ofy().load().type(MerchantStats.class)
                .filter("cityId =", cityId)
                .list();
        for (MerchantStats merchantStats : merchantStatsList) {
            merchantStats.calculateAdditionalStats();
        }
        return merchantStatsList;
    }


    public void calculateAdditionalStats() {
        this.averageChargePerDeliverRequest = this.calculateAverageChargePerDelivery();
    }

    public double calculateAverageChargePerDelivery() {
        Collection<Double> chargeList = this.chargeMap.values();
        double overallCharge = 0.0;
        double count = chargeList.size();
        for (Double charge : chargeList) {
            overallCharge += charge;
        }
        return overallCharge / count;
    }

    public double calculateDeliveryRatePerDay() {
        // TODO: 05/02/2018 
        return 0.0;
    }

}
