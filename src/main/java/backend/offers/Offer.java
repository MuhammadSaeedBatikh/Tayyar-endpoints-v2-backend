package backend.offers;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import backend.deliveryRequests.JDeliveryRequest;
import backend.profiles.customer.Customer;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 31/01/2018.
 */

@Entity
@Cache
public class Offer {
    @Id
    public Long id;

    @Index
    public String coupon;

    public int hotness; // order in list

    public OfferMessages messages;


    public int discountType;
    public double discountValue; // value or percentage
    /*
    *    0 ==> percentage
    *    1 ==> value
    *    2 ==> free delivery
    * */

    public String defaultMessage = "";
    public String description;
    public boolean isWorking = true; // gets set forcefully

    @Index
    public Date creationDate;
    public Date startDate;
    public Date endDate;

    public int overallMaxNumberAllowed;
    public int overallUsedCount;
    public int maxNumberAllowedPerCustomer;

    public List<String> merchantTypes = new ArrayList<>(); // "all", "r", "ph", ""
    /*flag 0 = all, 1 particular, 2 condition, 3 tag
        "all"           applies to all with no condition, associated list is empty
        "particular"    applies to subset of entities of which ids are provided in the associated list
        "condition"     applies to entities that satisfy this particular condition
        "tag"
    */

    public int cityFlag; //0 or 1, all or particular
    public List<Long> cityIds = new ArrayList<>(); // applied in these cities
    public int areaFlag;
    public List<Long> areaIds = new ArrayList<>(); // applied in these areas


    public double minimumCharge;
    public double maximumCharge;

    public int merchantFlag;
    public List<Long> merchantIds = new ArrayList<>();
    public List<Long> excludedMerchantIds = new ArrayList<>();

    // check items also

    public int customerFlag;
    public List<Long> customersIds = new ArrayList<>();
    public List<Long> excludedCustomersId = new ArrayList<>();


    public Offer() {
    }


    public Offer(String coupon, int discountType, double discountValue,
                 String defaultMessage, String description,
                 Date startDate, Date endDate, int overallMaxNumberAllowed,
                 int maxNumberAllowedPerCustomer, List<String> merchantTypes, int cityFlag, List<Long> cityIds,
                 int areaFlag, List<Long> areaIds, double minimumCharge, double maximumCharge, int merchantFlag,
                 List<Long> merchantIds, List<Long> excludedMerchantIds, int customerFlag,
                 List<Long> customersIds, List<Long> excludedCustomersId) {

        this.coupon = coupon;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.defaultMessage = defaultMessage;
        this.description = description;
        this.creationDate = new Date();
        this.startDate = startDate;
        this.endDate = endDate;
        this.overallMaxNumberAllowed = overallMaxNumberAllowed;
        this.maxNumberAllowedPerCustomer = maxNumberAllowedPerCustomer;
        for (String merchantType : merchantTypes) {
            this.merchantTypes.add(merchantType.toLowerCase().trim());
        }
        this.cityFlag = cityFlag;
        this.cityIds = cityIds;
        this.areaFlag = areaFlag;
        this.areaIds = areaIds;
        this.minimumCharge = minimumCharge;
        this.maximumCharge = maximumCharge;

        //merchant
        this.merchantFlag = merchantFlag;
        this.merchantIds = merchantIds == null ? new ArrayList<Long>() : merchantIds;
        this.excludedMerchantIds = excludedMerchantIds == null ? new ArrayList<Long>() : excludedMerchantIds;

        // TODO: 01/02/2018  add conditional checking schema
        this.customerFlag = customerFlag;
        this.customersIds = customersIds == null ? new ArrayList<Long>() : customersIds;
        this.excludedCustomersId = excludedCustomersId == null ? new ArrayList<Long>() : excludedCustomersId;
        this.messages = OfferMessages.getMessages();
    }


    //applyOffer if false ==> just verifying offer
    public static OfferApplied applyOffer(String lang, boolean applyOffer, JDeliveryRequest jDeliveryRequest) {
        boolean isArabic = lang.trim().equalsIgnoreCase("ar");
        Offer offer = getByCoupon(jDeliveryRequest.coupon);
        String message;

        if (offer == null) {
            OfferMessages messages = OfferMessages.getMessages();
            message = isArabic ? messages.doesNotExistAr : messages.doesNotExistEn;
            return new OfferApplied(false, message);
        }

        if (!offer.isWorking) {
            message = isArabic ? offer.messages.couponExpiredAr : offer.messages.couponExpiredEn;
            return new OfferApplied(false, message);
        }

        if (offer.overallUsedCount >= offer.overallMaxNumberAllowed) {
            message = isArabic ? offer.messages.reachedMaxNumberLimitAr : offer.messages.reachedMaxNumberLimitEn;
            offer.isWorking = false;
            return new OfferApplied(false, message);
        }

        String merchantType = jDeliveryRequest.merchantType;
        boolean containsAll = offer.merchantTypes.contains("all");
        if (!containsAll) {

            if (!offer.merchantTypes.contains(merchantType.toLowerCase().trim())) {
                message = isArabic ? offer.messages.doseNotApplyToMerchantTypeAr :
                        offer.messages.doseNotApplyToMerchantTypeEn;
                return new OfferApplied(false, message);
            }
        }
        Date currentDate = new Date();
        if (currentDate.before(offer.startDate)) {
            message = isArabic ? offer.messages.doseNotStartYetAr + offer.startDate :
                    offer.messages.doseNotStartYetEn + offer.startDate;

            return new OfferApplied(false, message);
        }

        if (currentDate.after(offer.endDate)) {
            message = isArabic ? offer.messages.couponExpiredAr : offer.messages.couponExpiredEn;
            return new OfferApplied(false, message);
        }


        if (offer.cityFlag != 0) {
            Long cityId = jDeliveryRequest.customerLocationView.cityId;
            if (!offer.cityIds.contains(cityId)) {
                message = isArabic ? offer.messages.doseNotApplyToYourCityAr : offer.messages.doseNotApplyToYourCityEn;
                return new OfferApplied(false, message);
            }
        }
        if (offer.areaFlag != 0) {
            Long areaId = jDeliveryRequest.customerLocationView.areaId;
            if (!offer.areaIds.contains(areaId)) {
                message = isArabic ? offer.messages.doseNotApplyToYourAreaAr : offer.messages.doseNotApplyToYourAreaEn;
                return new OfferApplied(false, message);
            }
        }

        message = isArabic ? offer.messages.doseNotApplyToThisMerchantAr : offer.messages.doseNotApplyToThisMerchantEn;
        if (offer.merchantFlag == 0) {
            if (offer.excludedMerchantIds.contains(jDeliveryRequest.merchantId)) {
                return new OfferApplied(false, message);
            }
        } else {
            if (!offer.merchantIds.contains(jDeliveryRequest.merchantId)) {
                return new OfferApplied(false, message);
            }
        }
        if (jDeliveryRequest.charge < offer.minimumCharge) {
            message = isArabic ? offer.messages.belowMinimumChargeAr + offer.minimumCharge :
                    offer.messages.belowMinimumChargeEn + offer.minimumCharge;
            return new OfferApplied(false, message);

        } else if (jDeliveryRequest.charge > offer.maximumCharge) {
            message = isArabic ? offer.messages.aboveMaximumChargeAr + offer.maximumCharge :
                    offer.messages.aboveMaximumChargeEn + offer.maximumCharge;
            return new OfferApplied(false, message);
        }


        message = isArabic ? offer.messages.doseNotApplyToCustomerAr : offer.messages.doseNotApplyToCustomerEn;

        if (offer.customerFlag == 0) {
            if (offer.excludedCustomersId.contains(jDeliveryRequest.customerId)) {
                return new OfferApplied(false, message);
            }
        } else if (offer.customerFlag == 1) {
            if (!offer.customersIds.contains(jDeliveryRequest.customerId)) {
                return new OfferApplied(false, message);
            }
        }

        Customer customer = (Customer) Customer.getProfileByID(jDeliveryRequest.customerId);
        int couponUsageCount = customer.getCouponUsageCount(offer.id);

        if (couponUsageCount >= offer.maxNumberAllowedPerCustomer) {
            message = isArabic ? offer.messages.exceededMaxNumberToCustomerAr :
                    offer.messages.exceededMaxNumberToCustomerEn;
            return new OfferApplied(false, message);
        }

        if (applyOffer) {
            customer.usesCoupon(offer.id);
        }
        message = isArabic ? offer.messages.successMessageAr : offer.messages.successMessageEn;
        return offer.performDiscount(applyOffer, jDeliveryRequest, message);
    }


    public OfferApplied performDiscount(boolean applyOffer, JDeliveryRequest jDeliveryRequest, String message) {
        OfferApplied offerApplied = null;
        double chargeAfterDiscount = 0;
        switch (this.discountType) {
            case 0: // discount percentage
                chargeAfterDiscount = jDeliveryRequest.charge * this.discountValue
                        + jDeliveryRequest.tip + jDeliveryRequest.deliveryFee + jDeliveryRequest.tayyarFee;
                break;
            case 1: // discount value
                chargeAfterDiscount = jDeliveryRequest.charge - this.discountValue
                        + jDeliveryRequest.tip + jDeliveryRequest.deliveryFee + jDeliveryRequest.tayyarFee;
                break;
            case 2: // free delivery
                chargeAfterDiscount = jDeliveryRequest.charge * this.discountValue
                        + jDeliveryRequest.tip + jDeliveryRequest.tayyarFee;
                break;
        }
        if (applyOffer) {
            this.overallUsedCount++;
            this.save();
        }
        return new OfferApplied(true, message, this.coupon, jDeliveryRequest.totalCharge,
                chargeAfterDiscount, this.description);
    }

    public void save() {
        ofy().save().entity(this).now();
    }


    public static Offer getByCoupon(String coupon) {
        List<Offer> offers = ofy().load().type(Offer.class)
                .filter("coupon =", coupon)
                .order("-creationDate")
                .list();
        if (offers.size() == 0) {
            return null;
        }
        return offers.get(0);
    }


    @Override
    public String toString() {
        return "Offer{" +
                "coupon='" + coupon + '\'' +
                ", discountType=" + discountType +
                ", discountValue=" + discountValue +
                ", defaultMessage='" + defaultMessage + '\'' +
                ", description='" + description + '\'' +
                ", isWorking=" + isWorking +
                ", creationDate=" + creationDate +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", overallMaxNumberAllowed=" + overallMaxNumberAllowed +
                ", overallUsedCount=" + overallUsedCount +
                ", maxNumberAllowedPerCustomer=" + maxNumberAllowedPerCustomer +
                ", merchantTypes=" + merchantTypes +
                ", cityFlag=" + cityFlag +
                ", cityIds=" + cityIds +
                ", areaFlag=" + areaFlag +
                ", areaIds=" + areaIds +
                ", minimumCharge=" + minimumCharge +
                ", maximumCharge=" + maximumCharge +
                ", merchantFlag=" + merchantFlag +
                ", merchantIds=" + merchantIds +
                ", excludedMerchantIds=" + excludedMerchantIds +
                ", customerFlag=" + customerFlag +
                ", customersIds=" + customersIds +
                ", excludedCustomersId=" + excludedCustomersId +
                '}';
    }
}
