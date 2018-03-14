package backend.merchants.specialMerchant;

import com.googlecode.objectify.annotation.Subclass;

import backend.merchants.Item;
import backend.merchants.jsonWrappers.JItem;

/**
 * Created by Muhammad on 19/08/2017.
 */

@Subclass(index = true)
public class SpecialMerchantItem extends Item {
    //default constructor for Entity initialization
    public SpecialMerchantItem() {
    }
    //============

    public SpecialMerchantItem (String nameAr, String nameEn, double basePrice) {
        super(nameAr, nameEn, basePrice);
    }

    public SpecialMerchantItem(boolean newMerchant, JItem jItem) {
        super(newMerchant, jItem);
    }
}
