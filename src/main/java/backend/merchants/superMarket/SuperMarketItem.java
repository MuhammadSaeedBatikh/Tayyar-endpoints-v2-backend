package backend.merchants.superMarket;

import com.googlecode.objectify.annotation.Subclass;

import backend.merchants.Item;
import backend.merchants.jsonWrappers.JItem;

/**
 * Created by Muhammad on 19/08/2017.
 */

@Subclass(index = true)
public class SuperMarketItem extends Item {
    //default constructor for Entity initialization
    public SuperMarketItem() {
    }
    //============

    public SuperMarketItem(String nameAr, String nameEn, double basePrice) {
        super(nameAr, nameEn, basePrice);
    }
    public SuperMarketItem(boolean newMerchant, JItem jItem) {
        super(newMerchant, jItem);
    }
}
