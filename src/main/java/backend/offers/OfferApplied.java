package backend.offers;

import backend.deliveryRequests.DeliveryRequest;
import backend.deliveryRequests.JDeliveryRequest;

/**
 * Created by Muhammad on 31/01/2018.
 */

public class OfferApplied {
    public boolean success;
    public String message;

    public String coupon;
    public double chargeBeforeDiscount;
    public double chargeAfterDiscount;
    public String description;

    public OfferApplied(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public OfferApplied(boolean success, String message, String coupon, double chargeBeforeDiscount,
                        double chargeAfterDiscount, String description) {

        this.success = success;
        this.message = message;
        this.coupon = coupon;
        this.chargeBeforeDiscount = chargeBeforeDiscount;
        this.chargeAfterDiscount = chargeAfterDiscount;
        this.description = description;
    }

    @Override
    public String toString() {
        return "OfferApplied{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", coupon='" + coupon + '\'' +
                ", chargeBeforeDiscount=" + chargeBeforeDiscount +
                ", chargeAfterDiscount=" + chargeAfterDiscount +
                ", description='" + description + '\'' +
                '}';
    }
}
