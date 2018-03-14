package backend.deliveryRequests;


import com.google.appengine.repackaged.com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import backend.merchants.Merchant;
import backend.profiles.Profile;
import backend.profiles.customer.Customer;

/**
 * Created by Muhammad on 15/01/2018.
 */

public class DeliveryRequestView {
    public Long merchantId;
    public Long customerId;
    public String merchantType;
    public CustomerLocationView customerLocationView;
    public List<DeliveryItemView> deliveryItemsViews = new ArrayList<>();
    public double charge;
    public double tip;
    public double deliveryFee;
    public double tayyarFee;
    public double totalCharge;
    public String generalInstructions;
    public Long id; //deliveryRequestId
    public String merchantName;
    public String merchantAddress;
    public List<String> merchantPhones = new ArrayList<>();
    public String imageURL;
    public String prescriptionImageUrl; //  pharmacy
    public String prescription;         //  pharmacy

    public String customerPhone;
    public int state;
    public int estimatedDeliveryTime;
    public int deliveryOption;
    public boolean notCorrupted = false;

    public DeliveryRequestView(DeliveryRequest deliveryRequest) {
        this.merchantId = deliveryRequest.merchantId;
        this.customerId = deliveryRequest.customerId;
        this.customerLocationView = deliveryRequest.customerLocationView;
        this.deliveryItemsViews = DeliveryItemView.createListOfDeliveryItemsViews("ar", deliveryRequest.deliveryItems);
        if (deliveryItemsViews == null) {
            return;
        }
        this.charge = deliveryRequest.charge;
        this.tip = deliveryRequest.tip;
        this.deliveryFee = deliveryRequest.deliveryFee;
        this.tayyarFee = deliveryRequest.tayyarFee;
        this.totalCharge = deliveryRequest.totalCharge;
        this.generalInstructions = deliveryRequest.generalInstructions;
        this.id = deliveryRequest.id;
        Merchant merchant = Merchant.getMerchantByID(merchantId);
        if (merchant == null) {
            return;
        }
        notCorrupted = true;
        this.merchantName = merchant.nameAr;
        this.merchantPhones = merchant.phones;
        this.merchantType = deliveryRequest.merchantType;
        this.prescription = deliveryRequest.prescription;
        this.prescriptionImageUrl = deliveryRequest.prescriptionImageUrl;
        this.imageURL = merchant.imageURL;
        this.merchantAddress = merchant.addressInstructionsAr;
        this.estimatedDeliveryTime = merchant.estimatedDeliveryTime;
        Profile profile = Customer.getProfileByID(customerId);
        if (profile != null) {
            this.customerPhone = profile.phone;
        }
        this.deliveryOption = merchant.deliveryOption;
        this.state = deliveryRequest.getState();
    }

    public static List<DeliveryRequestView> toViewList(List<DeliveryRequest> deliveryRequests) {
        List<DeliveryRequestView> deliveryRequestViews = new ArrayList<>();
        for (DeliveryRequest deliveryRequest : deliveryRequests) {
            DeliveryRequestView deliveryRequestView = new DeliveryRequestView(deliveryRequest);
            if (deliveryRequestView.notCorrupted) {
                deliveryRequestViews.add(deliveryRequestView);
            }
        }
        return deliveryRequestViews;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return "DeliveryRequestView{" +
                "merchantId=" + merchantId +
                ", customerId=" + customerId +
                ", merchantType='" + merchantType + '\'' +
                ", customerLocationView=" + customerLocationView +
                ", deliveryItemsViews=" + deliveryItemsViews +
                ", charge=" + charge +
                ", tip=" + tip +
                ", deliveryFee=" + deliveryFee +
                ", tayyarFee=" + tayyarFee +
                ", totalCharge=" + totalCharge +
                ", generalInstructions='" + generalInstructions + '\'' +
                ", id=" + id +
                ", merchantName='" + merchantName + '\'' +
                ", merchantAddress='" + merchantAddress + '\'' +
                ", merchantPhones=" + merchantPhones +
                ", imageURL='" + imageURL + '\'' +
                ", prescriptionImageUrl='" + prescriptionImageUrl + '\'' +
                ", prescription='" + prescription + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", state=" + state +
                ", estimatedDeliveryTime=" + estimatedDeliveryTime +
                ", deliveryOption=" + deliveryOption +
                '}';
    }
}
