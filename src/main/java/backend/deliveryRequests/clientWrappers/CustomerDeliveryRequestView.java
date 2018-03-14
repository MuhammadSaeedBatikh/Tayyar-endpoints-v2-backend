package backend.deliveryRequests.clientWrappers;


import java.util.ArrayList;
import java.util.List;

import backend.deliveryRequests.DeliveryItemView;
import backend.deliveryRequests.DeliveryRequest;
import backend.merchants.Merchant;

/**
 * Created by Muhammad on 28/01/2018.
 */

public class CustomerDeliveryRequestView {

    public long id;

    public long merchantId;
    public String merchantName;
    public String imageURL;
    public double charge;
    public double totalCharge;
    public double tip;
    public double deliveryFee;
    public double tayyarFee;
    public String generalInstructions;
    public List<DeliveryItemView> deliveryItemsViews;
    public DeliveryTimeline timeline;


    public CustomerDeliveryRequestView setValues(String lang, DeliveryRequest deliveryRequest) {

        this.id = deliveryRequest.id;
        this.merchantId = deliveryRequest.merchantId;
        this.charge = deliveryRequest.charge;
        this.tip = deliveryRequest.tip;
        this.deliveryFee = deliveryRequest.deliveryFee;
        this.tayyarFee = deliveryRequest.tayyarFee;
        this.totalCharge = deliveryRequest.totalCharge;
        this.generalInstructions = deliveryRequest.generalInstructions;
        Merchant merchant = Merchant.getMerchantByID(merchantId);
        if (merchant == null) { // merchant deleted and stuff
            return null;
        }
        if (lang.trim().equalsIgnoreCase("ar")) {
            this.merchantName = merchant.nameAr;
        } else {
            this.merchantName = merchant.nameEn;
        }
        this.imageURL = merchant.imageURL;
        this.deliveryItemsViews = DeliveryItemView
                .createListOfDeliveryItemsViews(lang, deliveryRequest.deliveryItems);
        this.timeline = new DeliveryTimeline(deliveryRequest);
        return this;
    }

    public static List<CustomerDeliveryRequestView> toViewList(String lang, List<DeliveryRequest> deliveryRequests) {
        List<CustomerDeliveryRequestView> deliveryRequestViews = new ArrayList<>();
        for (DeliveryRequest deliveryRequest : deliveryRequests) {
            CustomerDeliveryRequestView customerDeliveryRequestView = new CustomerDeliveryRequestView()
                    .setValues(lang, deliveryRequest);

            if (customerDeliveryRequestView != null) {
                deliveryRequestViews.add(customerDeliveryRequestView);
            }
        }
        return deliveryRequestViews;
    }

}
