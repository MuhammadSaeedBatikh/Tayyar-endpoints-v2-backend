package backend.merchants.dessertsMerchant;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Subclass;

import java.util.List;

import backend.merchants.Merchant;
import backend.merchants.jsonWrappers.JMerchant;

/**
 * Created by Muhammad on 19/08/2017.
 */
@Subclass(index = true)
@Cache
public class DessertsMerchant extends Merchant {
    //default constructor for Entity initialization
    public DessertsMerchant() {
        this.browsable = true;

    }
    //============
    public DessertsMerchant(String nameAr, String nameEn, List<String> phones, String imageURL) {
        super(nameAr, nameEn, phones, imageURL);
        this.browsable = true;
    }

    public DessertsMerchant(boolean newMerchant, JMerchant jMerchant) throws Exception {
        super(newMerchant, jMerchant);
    }
}
