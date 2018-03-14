package backend.helpers.pseudoEnums;

import backend.merchants.Merchant;
import backend.merchants.dessertsMerchant.DessertsMerchant;
import backend.merchants.pharmacy.Pharmacy;
import backend.merchants.restaurant.Restaurant;
import backend.merchants.superMarket.SuperMarket;

/**
 * Created by Muhammad on 26/08/2017.
 */

public class MerchantTypes {
    private static Class<? extends Merchant> merchantTypeClass;


    public static Class<? extends Merchant> getMerchantTypeClass(String merchantType) {
        switch (merchantType.toLowerCase()) {
            case "restaurant":
                merchantTypeClass = Restaurant.class;
                break;
            case "pharmacy":
                merchantTypeClass = Pharmacy.class;
                break;
            case "supermarket":
                merchantTypeClass = SuperMarket.class;
                break;
            case "dessertsmerchant":
                merchantTypeClass = DessertsMerchant.class;
                break;

            default:
                throw new IllegalArgumentException("merchant flag should be a restaurant, pharmacy, supermarket ");
        }
        return merchantTypeClass;
    }
}
