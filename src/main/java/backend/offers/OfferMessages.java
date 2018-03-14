package backend.offers;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 03/02/2018.
 */

@Entity
@Cache
public class OfferMessages {
    @Id
    public Long id;

    public String successMessageAr;
    public String successMessageEn;

    public String doesNotExistAr;
    public String doesNotExistEn;

    public String couponExpiredAr;
    public String couponExpiredEn;

    public String reachedMaxNumberLimitAr;
    public String reachedMaxNumberLimitEn;

    public String doseNotApplyToMerchantTypeAr;
    public String doseNotApplyToMerchantTypeEn;

    public String doseNotStartYetAr;
    public String doseNotStartYetEn;

    public String doseNotApplyToYourCityAr;
    public String doseNotApplyToYourCityEn;

    public String doseNotApplyToYourAreaAr;
    public String doseNotApplyToYourAreaEn;

    public String doseNotApplyToThisMerchantAr;
    public String doseNotApplyToThisMerchantEn;

    public String belowMinimumChargeAr;
    public String belowMinimumChargeEn;

    public String aboveMaximumChargeAr;
    public String aboveMaximumChargeEn;

    public String doseNotApplyToCustomerAr;
    public String doseNotApplyToCustomerEn;

    public String exceededMaxNumberToCustomerAr;
    public String exceededMaxNumberToCustomerEn;

    //default constructor for Entity initialization
    public OfferMessages (){}
    //============
    
    public OfferMessages(String successMessageAr, String successMessageEn, String doesNotExistAr, String doesNotExistEn,
                         String couponExpiredAr, String couponExpiredEn,
                         String reachedMaxNumberLimitAr, String reachedMaxNumberLimitEn,
                         String doseNotApplyToMerchantTypeAr, String doseNotApplyToMerchantTypeEn,
                         String doseNotStartYetAr, String doseNotStartYetEn,
                         String doseNotApplyToYourCityAr, String doseNotApplyToYourCityEn,
                         String doseNotApplyToYourAreaAr, String doseNotApplyToYourAreaEn,
                         String doseNotApplyToThisMerchantAr, String doseNotApplyToThisMerchantEn,
                         String belowMinimumChargeAr, String belowMinimumChargeEn,
                         String aboveMaximumChargeAr, String aboveMaximumChargeEn,
                         String doseNotApplyToCustomerAr, String doseNotApplyToCustomerEn,
                         String exceededMaxNumberToCustomerAr, String exceededMaxNumberToCustomerEn) {


        this.successMessageAr = successMessageAr;
        this.successMessageEn = successMessageEn;
        this.doesNotExistAr = doesNotExistAr;
        this.doesNotExistEn = doesNotExistEn;

        this.couponExpiredAr = couponExpiredAr;
        this.couponExpiredEn = couponExpiredEn;
        this.reachedMaxNumberLimitAr = reachedMaxNumberLimitAr;
        this.reachedMaxNumberLimitEn = reachedMaxNumberLimitEn;

        this.doseNotApplyToMerchantTypeAr = doseNotApplyToMerchantTypeAr;
        this.doseNotApplyToMerchantTypeEn = doseNotApplyToMerchantTypeEn;
        this.doseNotStartYetAr = doseNotStartYetAr;
        this.doseNotStartYetEn = doseNotStartYetEn;

        this.doseNotApplyToYourCityAr = doseNotApplyToYourCityAr;
        this.doseNotApplyToYourCityEn = doseNotApplyToYourCityEn;
        this.doseNotApplyToYourAreaAr = doseNotApplyToYourAreaAr;
        this.doseNotApplyToYourAreaEn = doseNotApplyToYourAreaEn;

        this.doseNotApplyToThisMerchantAr = doseNotApplyToThisMerchantAr;
        this.doseNotApplyToThisMerchantEn = doseNotApplyToThisMerchantEn;
        this.belowMinimumChargeAr = belowMinimumChargeAr;
        this.belowMinimumChargeEn = belowMinimumChargeEn;

        this.aboveMaximumChargeAr = aboveMaximumChargeAr;
        this.aboveMaximumChargeEn = aboveMaximumChargeEn;
        this.doseNotApplyToCustomerAr = doseNotApplyToCustomerAr;
        this.doseNotApplyToCustomerEn = doseNotApplyToCustomerEn;
        this.exceededMaxNumberToCustomerAr = exceededMaxNumberToCustomerAr;
        this.exceededMaxNumberToCustomerEn = exceededMaxNumberToCustomerEn;
    }


    public static OfferMessages getMessages() {
        List<OfferMessages> list = ofy().load().type(OfferMessages.class).list();
        if (list.size() == 0)
            return null;
        return list.get(0);
    }

    public void save() {
        ofy().save().entity(this).now();
    }
}
