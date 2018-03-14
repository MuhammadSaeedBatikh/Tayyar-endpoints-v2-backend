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
import backend.general.ConstantParams;
import backend.offers.Offer;
import backend.profiles.driver.Driver;

import static backend.stats.MerchantStats.getByMerchantId;
import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 05/02/2018.
 */

@Entity
@Cache
public class DriverStats {
    @Id
    public Long id;

    public String driverName;

    @Index
    public Long driverId;

    @Index
    public Long cityId; // denormalization, to get Stats Of Al Merchants In a City, instead of getting

    public double ourCredit;
    public double overallMoneyCollected; //deliveryFees + tips
    public double overallDeliveryFees;
    public double overAllTips;
    public double overAllTayyarFee;

    public int overallCompletedDeliveryRequestsCount;
    public Map<String, Double> deliveryFeeMap = new HashMap<>(); //Map<Date of Delivery completion, charge amount>
    public Map<String, Integer> deliveryRequestsStatesMap = new HashMap<>();//Map<state integer, count>


    // payment
    public Date lastPaymentDate;
    public Map<String, Double> paymentMap = new HashMap<>(); // Map<Date of payment, our charge amount //charge * percentage >
    public Map<String, List<Long>> paidDeliveryRequests = new HashMap<>(); // Map<Date of payment, list<delivery requests ids>>


    // unpaid for
    public Map<String, Double> unpaidChargeMap = new HashMap<>(); //Map<Date of Delivery completion, charge amount* percentage>
    public List<Long> unpaidDeliveryIds = new ArrayList<>();

    @Index
    public int unpaidDeliveryRequestsCount;

    public double ourPercentagePerDelivery = .2;
    @Index
    public double overallUnpaidCharge;


    // computed metrics
    public double averageChargePerDeliverRequest;
    public double deliveryRatePerDay;
    public int deliveryCountLast24Hours;


    //default constructor for Entity initialization
    public DriverStats() {
    }
    //============


    public DriverStats(Long driverId) {
        this.driverId = driverId;
        Driver driver = Driver.getDriverByID(driverId);
        this.cityId = driver.cityId;
        this.ourPercentagePerDelivery = ConstantParams.getParamsByCityId(this.cityId).ourPercentagePerDeliveryFromDelivery;
        this.driverName = driver.name; // for viewing
        save();
    }

    public static DriverStats deliveryCompleted(Long deliveryRequestId) {
        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestId);
        DriverStats driverStats = getByDriverId(deliveryRequest.driverId);

        if (driverStats.unpaidDeliveryIds.contains(deliveryRequestId)) {
            return null;
        }
        driverStats.overallCompletedDeliveryRequestsCount++;
        double addedCharge = 0;
        double deliveryFee = 0;
        Offer offer = Offer.getByCoupon(deliveryRequest.coupon);
        if (offer == null) {
            addedCharge = deliveryRequest.deliveryFee + deliveryRequest.tip + deliveryRequest.tayyarFee;
            deliveryFee = deliveryRequest.deliveryFee;
        } else {
            int discountType = Offer.getByCoupon(deliveryRequest.coupon).discountType;
            if (discountType == 2) {// discount on deliveryFee
                addedCharge = deliveryRequest.tip + deliveryRequest.tayyarFee;
                driverStats.ourCredit -= deliveryRequest.deliveryFee;
            } else {
                addedCharge = deliveryRequest.deliveryFee + deliveryRequest.tip + deliveryRequest.tayyarFee;
                deliveryFee = deliveryRequest.deliveryFee;
            }
        }

        String deliveryCompletionDate = new Date().toString();
        driverStats.overallMoneyCollected += addedCharge;
        driverStats.deliveryFeeMap.put(deliveryCompletionDate, deliveryFee);

        driverStats.overallDeliveryFees += deliveryFee;
        driverStats.overAllTips += deliveryRequest.tip;
        driverStats.overAllTayyarFee += deliveryRequest.tayyarFee;
        driverStats.ourCredit += deliveryRequest.charge;
        Integer count = driverStats.deliveryRequestsStatesMap.get("4");
        if (count == null) {
            count = 0;
        }
        driverStats.deliveryRequestsStatesMap.put("4", ++count);
        driverStats.unpaidDeliveryIds.add(deliveryRequestId);
        driverStats.unpaidDeliveryRequestsCount++;
        double ourShare = deliveryFee * driverStats.ourPercentagePerDelivery + deliveryRequest.tayyarFee;
        driverStats.overallUnpaidCharge += ourShare;
        driverStats.unpaidChargeMap.put(deliveryCompletionDate, ourShare);
        driverStats.save();

        return driverStats;

    }

    public static void arrivesAtCustomers(Long deliveryRequestId) {
        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestId);
        DriverStats driverStats = getByDriverId(deliveryRequest.driverId);
        Integer count = driverStats.deliveryRequestsStatesMap.get("3");
        if (count == null) {
            count = 0;
        }
        driverStats.deliveryRequestsStatesMap.put("3", ++count);
        driverStats.save();
    }

    public static void confirmedPickUp(Long deliveryRequestId) {
        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestId);
        DriverStats driverStats = getByDriverId(deliveryRequest.driverId);
        Integer count = driverStats.deliveryRequestsStatesMap.get("2");
        if (count == null) {
            count = 0;
        }
        driverStats.deliveryRequestsStatesMap.put("2", ++count);
        double chargeDriverPaidToMerchant = 0;
        Offer offer = Offer.getByCoupon(deliveryRequest.coupon);
        if (offer == null) {
            chargeDriverPaidToMerchant = deliveryRequest.charge;
        } else {
            int discountType = Offer.getByCoupon(deliveryRequest.coupon).discountType;
            if (discountType == 2) {// discount on deliveryFee
                chargeDriverPaidToMerchant = deliveryRequest.charge;
                driverStats.ourCredit -= deliveryRequest.deliveryFee;
            } else {
                chargeDriverPaidToMerchant = deliveryRequest.charge;
            }
        }
        driverStats.ourCredit -= chargeDriverPaidToMerchant;
        driverStats.save();
    }

    public static void deliveryAccepted(Long deliveryRequestId) {
        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestId);
        DriverStats driverStats = getByDriverId(deliveryRequest.driverId);
        Integer count = driverStats.deliveryRequestsStatesMap.get("1");
        if (count == null) {
            count = 0;
        }
        driverStats.deliveryRequestsStatesMap.put("1", ++count);
        driverStats.save();
    }


    public static DriverStats getByDriverId(Long driverId) {
        List<DriverStats> driverStatsList = ofy().load().type(DriverStats.class)
                .filter("driverId =", driverId)
                .list();
        return driverStatsList.get(0);
    }

    public void save() {
        ofy().save().entity(this).now();
    }

    public static DriverStats getCompleteStats(Long driverId) {
        DriverStats driverStats = getByDriverId(driverId);
        driverStats.calculateAdditionalStats();
        return driverStats;
    }

    public static List<DriverStats> getDriversStatisticsInCity(Long cityId) {
        List<DriverStats> driverStatsList = ofy().load().type(DriverStats.class)
                .filter("cityId =", cityId)
                .list();

        for (DriverStats driverStats : driverStatsList) {
            driverStats.calculateAdditionalStats();
        }

        return driverStatsList;
    }

    public static DriverStats driverPays(Long driverId) {
        DriverStats driverStats = getByDriverId(driverId);

        driverStats.lastPaymentDate = new Date();
        driverStats.paymentMap.put(driverStats.lastPaymentDate.toString(), driverStats.overallUnpaidCharge);
        driverStats.paidDeliveryRequests.put(driverStats.lastPaymentDate.toString(), driverStats.unpaidDeliveryIds);

        // clear
        driverStats.unpaidDeliveryRequestsCount = 0;
        driverStats.overallUnpaidCharge = 0.0;
        driverStats.unpaidChargeMap = new HashMap<>();
        driverStats.unpaidDeliveryIds = new ArrayList<>();
        driverStats.save();
        return driverStats;
    }


    public void calculateAdditionalStats() {
        this.averageChargePerDeliverRequest = this.calculateAverageChargePerDelivery();
    }

    public double calculateAverageChargePerDelivery() {
        Collection<Double> chargeList = this.deliveryFeeMap.values();
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
