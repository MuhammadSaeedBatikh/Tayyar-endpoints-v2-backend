package backend.merchants.pharmacy;

import com.googlecode.objectify.annotation.Subclass;

import backend.merchants.Item;
import backend.merchants.jsonWrappers.JItem;

/**
 * Created by Muhammad on 25/07/2017.
 */
//enum flag{pills,drops,etc}
@Subclass(index = true)

public class PharmacyItem extends Item {
    boolean requiresPrescription =false;

    public PharmacyItem() {
    }
    public PharmacyItem (String nameAr, String nameEn, double basePrice) {
        super(nameAr, nameEn, basePrice);
    }

    public PharmacyItem(boolean newMerchant, JItem jItem) {
        super(newMerchant, jItem);
    }
}
