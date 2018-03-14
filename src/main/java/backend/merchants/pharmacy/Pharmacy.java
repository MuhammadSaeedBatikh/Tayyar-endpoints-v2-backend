package backend.merchants.pharmacy;

import backend.merchants.Merchant;
import backend.merchants.jsonWrappers.JMerchant;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Subclass;

import java.util.List;

/**
 * Created by Muhammad on 24/07/2017.
 */
@Subclass(index = true)
@Cache
public class Pharmacy extends Merchant {

    //default constructor for Entity initalization
    public Pharmacy(){}

    public Pharmacy(String nameAr, String nameEn, List<String> phones, String imageURL) {
        super(nameAr, nameEn, phones, imageURL);
        this.browsable = true;
    }

    public Pharmacy(boolean newMerchant, JMerchant jMerchant) throws Exception {
        super(newMerchant, jMerchant);
        this.browsable = true;
    }
}
