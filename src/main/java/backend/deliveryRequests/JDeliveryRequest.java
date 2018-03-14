package backend.deliveryRequests;

import java.util.ArrayList;
import java.util.List;

import backend.merchants.Choice;
import backend.merchants.Item;
import backend.merchants.Merchant;


/**
 * Created by Muhammad on 16/01/2018.
 */
public class JDeliveryRequest {

    public String merchantType;
    public Long customerId;
    public String prescriptionImageUrl; //  pharmacy
    public String prescription;         //  pharmacy

    public CustomerLocationView customerLocationView;

    public Long merchantId;
    public List<DeliveryItem> deliveryItems = new ArrayList<>();
    public double charge;

    public double tip;
    public double deliveryFee;
    public double tayyarFee;
    public double totalCharge;
    public double chargeAfterDiscount;
    public String coupon;
    public String generalInstructions;


    public void setValues(String merchantType, Long customerId, String prescriptionImageUrl, String prescription,
                          CustomerLocationView customerLocationView, Long merchantId, List<DeliveryItem> deliveryItems,
                          double charge, double tip, double deliveryFee, double tayyarFee,
                          double totalCharge, String coupon, String generalInstructions) {

        this.merchantType = merchantType;
        this.customerId = customerId;
        this.prescriptionImageUrl = prescriptionImageUrl;
        this.prescription = prescription;
        this.customerLocationView = customerLocationView;
        this.merchantId = merchantId;
        this.deliveryItems = deliveryItems;
        this.charge = calculateCharge(deliveryItems);
        if (tip < 0 | tayyarFee < 0) {
            throw new IllegalArgumentException("tip cann't be less than 0");
        }
        this.tip = tip;
        Merchant merchant = Merchant.getMerchantByID(merchantId);
        double baseDeliveryFee = merchant.baseDeliveryFee;
        this.deliveryFee = merchant.supportedAreasMapIds.get(String.valueOf(customerLocationView.areaId)) + baseDeliveryFee;
        this.tayyarFee = tayyarFee;
        this.totalCharge = this.charge + this.tip + this.deliveryFee + this.tayyarFee;
        this.generalInstructions = generalInstructions;
        this.coupon = coupon;
    }


    public boolean correctPrices() {
        double oldCharge = this.totalCharge;
        if (tip < 0 | tayyarFee < 0) {
            throw new IllegalArgumentException("tip cannot be less than 0");
        }
        Merchant merchant = Merchant.getMerchantByID(merchantId);
        this.charge = calculateCharge(this.deliveryItems);
        double baseDeliveryFee = merchant.baseDeliveryFee;
        this.deliveryFee = merchant.supportedAreasMapIds.get(String.valueOf(customerLocationView.areaId)) + baseDeliveryFee;
        this.totalCharge = this.charge + this.tip + this.deliveryFee + this.tayyarFee;
        if (oldCharge != this.totalCharge) {
//            throw new IllegalArgumentException("inconsistent calculations");
        }
        return true;
    }

    public static double calculateCharge(List<DeliveryItem> deliveryItems) {
        double charge = 0;
        for (DeliveryItem deliveryItem : deliveryItems) {
            double basePrice = Item.getItemByID(deliveryItem.itemId).basePrice;
            int quantity = deliveryItem.quantity;
            if (deliveryItem.options != null) {
                for (List<Long> choicesIds : deliveryItem.options.values()) {
                    List<Choice> choices = Choice.getListOfChoices(choicesIds);
                    for (Choice choice : choices) {
                        double addedPrice = choice.addedPrice;
                        basePrice += addedPrice;
                    }
                }
            }
            double itemPrice = basePrice * quantity;
            charge += itemPrice;
        }
        return charge;
    }
}
