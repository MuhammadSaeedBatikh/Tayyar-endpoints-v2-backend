package backend.merchants.jsonWrappers;


import com.google.appengine.api.datastore.Category;
import com.googlecode.objectify.Key;

import java.util.*;

import backend.merchants.Merchant;
import backend.merchants.MerchantCategory;
import backend.stats.ReviewStats;


/**
 * Created by Muhammad on 02/11/2017.
 */
public class JMerchant {
    public String type = "r";
    public String nameEn;
    public String nameAr;

    public List<String> phones = new ArrayList<>();

    public String addressEn;
    public String addressAr;
    public String cityEn;
    public String cityAr;

    public int estimatedDeliveryTime;
    public String merchantArea;

    public String opensAt;
    public String closesAt;

    public double latitude;
    public double longitude;

    public String imageURL;

    public int pricing;
    public double rating;
    public double minimumOrder;
    public String tax;

    public List<String> actualCategoriesEn = new ArrayList<>();
    public List<String> actualCategoriesAr = new ArrayList<>();

    public double baseDeliveryFee;
    public HashMap<String, Double> supportedAreasMapEn = new HashMap<>();
    public List<JCategory> categories = new ArrayList<>();
    public int deliveryOption;
    public int defaultOrder;
    public boolean featured;
    public double ourPercentagePerDelivery = 0.1;

    //===================== generated
    public Long id;
    public List<Long> reviewsIds = new ArrayList<>();
    public ReviewStats reviewStats = new ReviewStats();
    public Long merchantStatsId;
    public int favouriteCount;
    public SortedSet<Long> favouriteCustomerIds = new TreeSet<>();
    public List<Long> actualCategoriesIds = new ArrayList<>();
    public List<Long> menuCategoriesIds = new ArrayList<>();


    public static JMerchant fromMerchant(Merchant merchant) {
        JMerchant jMerchant = new JMerchant();
        jMerchant.nameEn = merchant.nameEn;
        jMerchant.nameAr = merchant.nameAr;
        jMerchant.phones = merchant.phones;
        jMerchant.addressEn = merchant.addressInstructionsEn;
        jMerchant.addressAr = merchant.addressInstructionsAr;
        jMerchant.cityEn = merchant.cityEn;
        jMerchant.cityAr = merchant.cityAr;
        jMerchant.estimatedDeliveryTime = merchant.estimatedDeliveryTime;
        jMerchant.merchantArea = merchant.merchantArea;
        jMerchant.opensAt = merchant.opensAt;
        jMerchant.closesAt = merchant.closesAt;
        jMerchant.latitude = merchant.currentLocationGeoPt.getLatitude();
        jMerchant.longitude = merchant.currentLocationGeoPt.getLongitude();
        jMerchant.imageURL = merchant.imageURL;
        jMerchant.pricing = merchant.pricing;
        jMerchant.rating = merchant.rating;
        jMerchant.minimumOrder = merchant.minimumOrder;
        jMerchant.tax = merchant.tax;
        jMerchant.actualCategoriesEn = new ArrayList<String>(merchant.actualCategoriesMapEn.keySet());
        jMerchant.actualCategoriesAr = new ArrayList<String>(merchant.actualCategoriesMapAr.keySet());
        jMerchant.baseDeliveryFee = merchant.baseDeliveryFee;
        jMerchant.supportedAreasMapEn = merchant.supportedAreasMapEn;
        jMerchant.deliveryOption = merchant.deliveryOption;
        jMerchant.defaultOrder = merchant.defaultOrder;
        jMerchant.featured = merchant.featured;
        jMerchant.ourPercentagePerDelivery = merchant.ourPercentagePerDelivery;
        jMerchant.id = merchant.id;
        jMerchant.reviewsIds = merchant.reviewsIds;
        jMerchant.reviewStats = merchant.reviewStats;
        jMerchant.merchantStatsId = merchant.merchantStatsId;
        jMerchant.favouriteCount = merchant.favouriteCount;
        jMerchant.favouriteCustomerIds = merchant.favouriteCustomerIds;
        jMerchant.actualCategoriesIds = merchant.actualCategoriesIds;
        jMerchant.menuCategoriesIds = merchant.menuCategoriesIds;
        for (Long menuCategoryId : jMerchant.menuCategoriesIds) {
            MerchantCategory merchantCategory = MerchantCategory.getCategoryByID(menuCategoryId);
            JCategory jCategory = JCategory.fromMerchantCategory(merchantCategory);
            jMerchant.categories.add(jCategory);
        }
        return jMerchant;
    }
}
