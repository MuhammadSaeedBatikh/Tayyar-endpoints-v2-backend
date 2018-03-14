package backend.merchants;

import com.google.api.server.spi.types.SimpleDate;
import com.googlecode.objectify.annotation.Index;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import backend.TestEntity;
import backend.general.Viewable;
import backend.profiles.customer.Customer;

/**
 * Created by Muhammad Saeed on 3/10/2017.
 */
public class MerchantView implements Viewable, Comparable<MerchantView> {
    public String name;
    public Long merchantID;
    public String imageURL;
    public int pricing;
    public double rating;
    public boolean active;
    public List<String> actualCategories;
    public double deliveryFee;
    public double minimumOrder;
    public int estimatedDeliveryTime;
    public int favourite;
    public boolean inMyFavourites = false;
    public boolean featured;
    public String featuringMessage; // ad, discount, free delivery
    public String merchantCoupon;
    public String tax;

    public int categoriesNumber;

    public MerchantView(String lang, Long areaId, Merchant merchant, Long customerId) {
        this.merchantID = merchant.id;
        if (lang.equalsIgnoreCase("ar")) {
            this.name = merchant.nameAr;
            this.actualCategories = new ArrayList<String>(merchant.actualCategoriesMapAr.keySet());
        } else {
            this.name = merchant.nameEn;
            this.actualCategories = new ArrayList<String>(merchant.actualCategoriesMapEn.keySet());
            ;
        }

        this.imageURL = merchant.imageURL;
        this.pricing = merchant.pricing;
        this.rating = merchant.rating;

        this.active = isActive(merchant.opensAt, merchant.closesAt);
        double baseDeliveryFee = merchant.baseDeliveryFee;
        this.deliveryFee = merchant.supportedAreasMapIds.get(String.valueOf(areaId)) + baseDeliveryFee;
        this.estimatedDeliveryTime = merchant.estimatedDeliveryTime;
        this.minimumOrder = merchant.minimumOrder;
        this.favourite = merchant.favouriteCount;
        this.tax = merchant.tax;
        this.featured = merchant.featured;
        this.inMyFavourites = merchant.isCustomerInFavourite(customerId);
        this.categoriesNumber = merchant.menuCategoriesIds.size();
        this.featuringMessage = merchant.featuringMessage;
        this.merchantCoupon = merchant.merchantCoupon;
        //delivery estimate
    }

    public static List<MerchantView> getListOfMerchantsViews(String lang, Long areaId, List<? extends Merchant> list, Long customerId) {
        List<MerchantView> merchantViews = new ArrayList<>();
        for (Merchant merchant : list) {
            merchantViews.add(new MerchantView(lang, areaId, merchant, customerId));
        }

        return merchantViews;
    }

    public static boolean isActive(String opensAt, String closesAt) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        try {
            Date opensAtDate = format.parse(opensAt);
            Date closesAtDate = format.parse(closesAt);
            Calendar rightNow = Calendar.getInstance();
            int hour = rightNow.get(Calendar.HOUR_OF_DAY);
            int min = rightNow.get(Calendar.MINUTE);
            Date now = new SimpleDateFormat("yyyy:MM:dd:HH:mm")
                    .parse("1970:01:01:" + hour + ":" + min);
            return now.before(closesAtDate) && now.after(opensAtDate);

        } catch (Exception e) {
            new TestEntity().log(e);
        }
        return false;
    }

    @Override
    public int compareTo(MerchantView merchantView) {
        return Boolean.compare(merchantView.active, this.active);
    }
}
