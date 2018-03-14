package backend.merchants.dessertsMerchant;

import com.googlecode.objectify.annotation.Subclass;

import backend.merchants.Item;
import backend.merchants.jsonWrappers.JItem;

/**
 * Created by Muhammad on 19/08/2017.
 */

@Subclass(index = true)
public class DessertsMerchantItem extends Item {
    //default constructor for Entity initialization
    public DessertsMerchantItem() {
    }
    //============

    public DessertsMerchantItem(String nameAr, String nameEn, double basePrice) {
        super(nameAr, nameEn, basePrice);
    }

    public DessertsMerchantItem(boolean newMerchant, JItem jItem) {
        super(newMerchant, jItem);
    }
}
