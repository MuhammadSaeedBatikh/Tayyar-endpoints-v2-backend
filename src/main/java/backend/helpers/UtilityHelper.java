package backend.helpers;

import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.Key;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import backend.merchants.Item;
import backend.merchants.Merchant;
import backend.merchants.dessertsMerchant.DessertsMerchant;
import backend.merchants.dessertsMerchant.DessertsMerchantItem;
import backend.merchants.jsonWrappers.JItem;
import backend.merchants.pharmacy.Pharmacy;
import backend.merchants.pharmacy.PharmacyItem;
import backend.merchants.restaurant.Restaurant;
import backend.merchants.restaurant.RestaurantItem;
import backend.merchants.specialMerchant.SpecialMerchant;
import backend.merchants.specialMerchant.SpecialMerchantItem;
import backend.merchants.superMarket.SuperMarket;
import backend.merchants.superMarket.SuperMarketItem;

/**
 * Created by Muhammad on 18/01/2018.
 */

public class UtilityHelper {

    public static double distance(GeoPt p1, GeoPt p2) {
        double latitude = Math.toRadians((double) p1.getLatitude());
        double longitude = Math.toRadians((double) p1.getLongitude());
        double otherLatitude = Math.toRadians((double) p2.getLatitude());
        double otherLongitude = Math.toRadians((double) p2.getLongitude());
        double deltaLat = latitude - otherLatitude;
        double deltaLong = longitude - otherLongitude;
        double a1 = haversin(deltaLat);
        double a2 = Math.cos(latitude) * Math.cos(otherLatitude) * haversin(deltaLong);
        return 1.274202E7D * Math.asin(Math.sqrt(a1 + a2));
    }

    public static double haversin(double delta) {
        double x = Math.sin(delta / 2.0D);
        return x * x;
    }

    public static boolean isDateWithinRange(Date startDate, Date endDate, Date testDate) {
        return !(testDate.before(startDate) || testDate.after(endDate));
    }


    public static <T> List<Key<T>> toKeys(List<Long> idsList, Class<T> entityClass) {
        List<Key<T>> keys = new ArrayList<>(idsList.size());
        for (Long id : idsList) {
            Key key = Key.create(entityClass, id);
            keys.add(key);
        }
        return keys;
    }

    public static Item createTypedItem(boolean newMerchant, JItem jItem, Merchant merchant) {
        Item item = null;
        if (merchant instanceof Restaurant) {
            item = new RestaurantItem(newMerchant, jItem);
        } else if (merchant instanceof SpecialMerchant) {
            item = new SpecialMerchantItem(newMerchant, jItem);
        } else if (merchant instanceof DessertsMerchant) {
            item = new DessertsMerchantItem(newMerchant, jItem);
        } else if (merchant instanceof Pharmacy) {
            item = new PharmacyItem(newMerchant, jItem);
        } else if (merchant instanceof SuperMarket) {
            item = new SuperMarketItem(newMerchant, jItem);
        }
        return item;
    }

}
