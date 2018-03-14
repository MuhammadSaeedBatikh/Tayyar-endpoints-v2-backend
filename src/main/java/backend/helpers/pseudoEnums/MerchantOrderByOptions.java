package backend.helpers.pseudoEnums;

/**
 * Created by Muhammad on 26/08/2017.
 */

public class MerchantOrderByOptions {
    private static String orderByOption;

    public static String getOption(String option) {
        option = option.toLowerCase().trim();
        if (option.equals("pricing") | option.equals("rating") |
                option.equals("active") | option.equals("location")) {
            orderByOption = option;
        } else {
            throw new IllegalArgumentException("Merchant Order By Options should be either pricing, rating, active or location");
        }
        return orderByOption;
    }
}

