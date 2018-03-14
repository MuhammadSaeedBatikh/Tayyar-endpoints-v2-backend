package backend.offers;

import java.util.ArrayList;
import java.util.List;

import backend.merchants.MerchantView;

/**
 * Created by Muhammad on 08/02/2018.
 */

public class OffersView {
    public List<MerchantView> merchantViews = new ArrayList<>();
    public List<CouponView> coupons = new ArrayList<>();

}

class CouponView {
    public String coupon;
    public String description;

}
