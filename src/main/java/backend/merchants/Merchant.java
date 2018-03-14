package backend.merchants;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.*;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import backend.cityArea.Area;
import backend.cityArea.City;
import backend.reviews.MerchantReviewsViews;
import backend.reviews.Review;
import backend.general.Viewable;
import backend.helpers.CustomQueries;
import backend.merchants.inventory.ActualCategory;
import backend.merchants.inventory.Inventory;
import backend.merchants.jsonWrappers.JMerchant;
import backend.stats.ReviewStats;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 24/07/2017.
 */
@Entity
@Cache
public abstract class Merchant implements Viewable {

    @Id
    public Long id;
    @Index
    public String nameAr;
    @Index
    public String nameEn;

    @Index
    public List<String> phones = new ArrayList<>();

    @Index
    public String addressInstructionsAr;

    @Index
    public String addressInstructionsEn;


    @Index
    public GeoPt currentLocationGeoPt;

    @Index
    public Long cityId;
    public String cityEn;
    public String cityAr;

    @Index
    public String opensAt;
    @Index
    public String closesAt;


    @Index
    public Long areaId;
    @Index
    public String merchantArea;

    @Index
    public List<String> regTokenList = new ArrayList<>();

    public HashMap<String, Double> supportedAreasMapIds = new HashMap<>();
    public HashMap<String, Double> supportedAreasMapEn = new HashMap<>();
    public HashMap<String, Double> supportedAreasMapAr = new HashMap<>();

    @Index
    public List<Long> supportedAreasListIds = new ArrayList<>();


    @Index
    public boolean browsable = false;

    public List<Long> reviewsIds = new ArrayList<>();
    public ReviewStats reviewStats = new ReviewStats();
    @Index
    public Long merchantStatsId;

    @Index
    public int favouriteCount;
    public SortedSet<Long> favouriteCustomerIds = new TreeSet<>();


    @Index
    public List<Long> menuCategoriesIds = new ArrayList<>();


    public HashMap<String, Long> actualCategoriesMapAr = new HashMap<>();
    public HashMap<String, Long> actualCategoriesMapEn = new HashMap<>();
    @Index
    public List<Long> actualCategoriesIds = new ArrayList<>();


    public String imageURL;
    @Index
    public int pricing;

    @Index
    public double rating;

    @Index
    public boolean active;

    @Index
    public double baseDeliveryFee;
    @Index
    public double minimumOrder;
    @Index
    public int estimatedDeliveryTime;

    public String tax;

    @Index
    public int defaultOrder;

    @Index
    public int deliveryOption; /* 0 => has no delivery service, send driver
     1 => send driver, we get a share from both driver and merchant
     2 => switch to special driver, doesn't want to use our delivery service

     */

    @Index
    public double ourPercentagePerDelivery;

    @Index
    public boolean featured;
    public String featuringMessage; // ad, discount, free delivery
    @Index
    public String merchantCoupon;

    //default constructor for Entity initialization
    public Merchant() {
    }

    public static Merchant getMerchantByID(Long id) {
        return ofy().load().type(Merchant.class).id(id).now();
    }

    public Merchant(String nameAr, String nameEn, List<String> phones, String imageURL) {
        this.nameAr = nameAr;
        this.nameEn = nameEn;
        this.imageURL = imageURL;
        this.phones = phones;
    }

    public Merchant(boolean newMerchant, JMerchant jMerchant) throws Exception {

        if (!newMerchant) {
            this.id = jMerchant.id;
            this.reviewsIds = jMerchant.reviewsIds;
            this.reviewStats = jMerchant.reviewStats;
            this.merchantStatsId = jMerchant.merchantStatsId;
            this.favouriteCount = jMerchant.favouriteCount;
            this.favouriteCustomerIds = jMerchant.favouriteCustomerIds;
            this.actualCategoriesIds = jMerchant.actualCategoriesIds;
            this.menuCategoriesIds = jMerchant.menuCategoriesIds;
        }

        this.nameAr = jMerchant.nameAr;
        this.nameEn = jMerchant.nameEn;
        this.phones = jMerchant.phones;
        this.imageURL = jMerchant.imageURL;
        this.pricing = jMerchant.pricing;
        this.rating = jMerchant.rating;
        this.opensAt = jMerchant.opensAt;
        this.closesAt = jMerchant.closesAt;
        this.defaultOrder = jMerchant.defaultOrder;
        this.tax = jMerchant.tax;
        this.deliveryOption = jMerchant.deliveryOption;
        this.featured = jMerchant.featured;
        //location info
        this.addressInstructionsAr = jMerchant.addressAr;
        this.addressInstructionsEn = jMerchant.addressEn;
        this.currentLocationGeoPt = new GeoPt((float) jMerchant.latitude, (float) jMerchant.longitude);

        this.cityAr = jMerchant.cityAr;
        this.cityEn = jMerchant.cityEn;
        this.cityId = City.getCityIdByName("en", cityEn);

        this.merchantArea = jMerchant.merchantArea;
        this.areaId = Area.getId("en", this.cityId, this.merchantArea);
        this.baseDeliveryFee = jMerchant.baseDeliveryFee;
        this.minimumOrder = jMerchant.minimumOrder;
        this.estimatedDeliveryTime = jMerchant.estimatedDeliveryTime;
        this.ourPercentagePerDelivery = jMerchant.ourPercentagePerDelivery;
        // actual categories

        int aCatNumber = jMerchant.actualCategoriesEn.size();
        for (int i = 0; i < aCatNumber; i++) {
            String catEn = jMerchant.actualCategoriesEn.get(i);
            String catAr = jMerchant.actualCategoriesAr.get(i);
            Long categoryId = Inventory.getACategoryIdByName(catAr, catEn);
            this.actualCategoriesMapAr.put(catAr, categoryId);
            this.actualCategoriesMapEn.put(catEn, categoryId);
            this.addActualCategoryWithoutSaving(categoryId);
        }


        for (Map.Entry<String, Double> entry : jMerchant.supportedAreasMapEn.entrySet()) {
            String area = entry.getKey();
            Double value = entry.getValue();
            System.out.println(cityId + ", " + area);
            Long areaId = Area.getId("en", cityId, area);
            String nameAr = Area.getById(areaId).nameAr;
            this.supportedAreasMapEn.put(area, value);
            this.supportedAreasMapAr.put(nameAr, value);
            this.supportedAreasMapIds.put(areaId.toString(), value);
            if (!this.supportedAreasListIds.contains(areaId)) {
                this.supportedAreasListIds.add(areaId);
            }
        }


    }

    public void saveMerchant() {
        ofy().save().entity(this).now();
    }

    public void addMenuCategory(Long categoryID) {
        this.menuCategoriesIds.add(categoryID);
        saveMerchant();// save changes in this Merchant
    }


    public List<MerchantCategory> getMenuCategories() {
        Collection<MerchantCategory> categories = ofy().load().type(MerchantCategory.class)
                .ids(this.menuCategoriesIds).values();
        return new ArrayList<MerchantCategory>(categories);
    }

    public static Query<Merchant> getMerchantByName(String lang, String name) {
        String fieldName = lang.trim().equalsIgnoreCase("ar") ? "nameAr" : "nameEn";

        Query<Merchant> query = ObjectifyService.ofy().load().type(Merchant.class);
        query = CustomQueries.searchStringByPrefix(query, fieldName, name);
        return query;
    }

    public static void addedToFavourite(Long customerId, Long merchantId) {
        Merchant merchant = getMerchantByID(merchantId);
        merchant.favouriteCount++;
        merchant.favouriteCustomerIds.add(customerId);
        merchant.saveMerchant();
    }

    public static void removedFromFavourite(Long customerId, Long merchantId) {
        Merchant merchant = getMerchantByID(merchantId);
        merchant.favouriteCount--;
        merchant.favouriteCustomerIds.remove(customerId);
        merchant.saveMerchant();
    }

    public boolean isCustomerInFavourite(Long customerId) {
        return this.favouriteCustomerIds.contains(customerId);
    }

    public void addRegToken(String regToken) {
        this.regTokenList.add(regToken);
        saveMerchant();
    }

    public void removeRegToken(String regToken) {
        this.regTokenList.remove(regToken);
        saveMerchant();
    }

    public List<String> getRegTokenList() {
        return this.regTokenList;
    }

    public void addActualCategory(Long categoryId) {
        addActualCategoryWithoutSaving(categoryId);
        saveMerchant();
    }

    public void addActualCategoryWithoutSaving(Long categoryId) {
        if (!actualCategoriesIds.contains(categoryId)) {
            this.actualCategoriesIds.add(categoryId);
            ActualCategory actualCategory = ActualCategory.getById(categoryId);
            this.actualCategoriesMapAr.put(actualCategory.nameAr, categoryId);
            this.actualCategoriesMapEn.put(actualCategory.nameEn, categoryId);
        }
    }

    public void gotReviewed(Long reviewId) {
        Review review = Review.getByID(reviewId);
        this.reviewsIds.add(reviewId);
        this.reviewStats.revieweeId = this.id;
        this.reviewStats.updateWithRating(review.rating);
        this.rating = this.reviewStats.rating;
        saveMerchant();
    }

    public static MerchantReviewsViews getReviews(Long merchantId) {
        Merchant merchant = getMerchantByID(merchantId);
        ReviewStats reviewStats = merchant.reviewStats;
        Map<Long, Review> reviewMap = ofy().load().type(Review.class).ids(merchant.reviewsIds);
        List<Review> reviews = new ArrayList<Review>(reviewMap.values());
        MerchantReviewsViews merchantReviewsViews = new MerchantReviewsViews(reviewStats, reviews);
        return merchantReviewsViews;
    }

    public void feature(String featuringMessage, String coupon) {
        this.featured = true;
        this.featuringMessage = featuringMessage;
        this.merchantCoupon = coupon;
        saveMerchant();
    }


    public void unFeature() {
        this.featured = false;
        this.featuringMessage = null;
        this.merchantCoupon = null;
        saveMerchant();
    }

    @Override
    public String toString() {
        return "Merchant{" +
                "id=" + id +
                ", nameAr='" + nameAr + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", phones=" + phones +
                ", addressInstructionsAr='" + addressInstructionsAr + '\'' +
                ", addressInstructionsEn='" + addressInstructionsEn + '\'' +
                ", currentLocationGeoPt=" + currentLocationGeoPt +
                ", areaId=" + cityId +
                ", cityEn='" + cityEn + '\'' +
                ", cityAr='" + cityAr + '\'' +
                ", opensAt='" + opensAt + '\'' +
                ", closesAt='" + closesAt + '\'' +
                ", merchantArea='" + merchantArea + '\'' +
                ", regTokenList=" + regTokenList +
                ", supportedAreasMapIds=" + supportedAreasMapIds +
                ", supportedAreasMapEn=" + supportedAreasMapEn +
                ", supportedAreasMapAr=" + supportedAreasMapAr +
                ", supportedAreasListIds=" + supportedAreasListIds +
                ", browsable=" + browsable +
                ", reviews=" + reviewsIds +
                ", menuCategories=" + menuCategoriesIds +
                ", actualCategoriesMapAr=" + actualCategoriesMapAr +
                ", actualCategoriesMapEn=" + actualCategoriesMapEn +
                ", actualCategoriesIds=" + actualCategoriesIds +
                ", imageURL='" + imageURL + '\'' +
                ", pricing=" + pricing +
                ", rating=" + rating +
                ", active=" + active +
                ", baseDeliveryFee=" + baseDeliveryFee +
                ", minimumOrder=" + minimumOrder +
                ", estimatedDeliveryTime=" + estimatedDeliveryTime +
                ", tax='" + tax + '\'' +
                '}';
    }
}
