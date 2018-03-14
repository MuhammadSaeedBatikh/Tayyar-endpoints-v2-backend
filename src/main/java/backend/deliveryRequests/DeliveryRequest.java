package backend.deliveryRequests;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import backend.merchants.Choice;
import backend.merchants.Item;
import backend.merchants.Merchant;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 16/08/2017.
 */
@Entity
public class DeliveryRequest {


    @Id
    public Long id;

    @Index
    public String merchantType;

    @Index
    public Long customerId;
    @Index
    public Long merchantId;

    @Index
    public Long driverId;

    @Index
    public Date creationDate;
    //time info;
    //location info

    @Index
    public Long cityId;

    @Index
    public Long areaId;

    @Index
    public boolean driverAcceptsOrder = false;
    @Index
    public Date driverAcceptsOrderDate;

    @Index
    public boolean driverConfirmedPickUP = false;
    @Index
    public Date driverConfirmedPickUpOrderDate;

    @Index
    public boolean driverArrivesAtCustomer = false;
    @Index
    public Date driverArrivedAtCustomerDate;

    @Index
    public Date completionDate;

    @Index
    public boolean orderDelivered = false;

    @Index
    public double charge; //subtotal
    @Index
    public double tip;
    @Index
    public double deliveryFee;
    @Index
    public double tayyarFee;
    @Index
    public double totalCharge;
    public double chargeAfterDiscount;

    @Index
    public String coupon;

    @Index
    public double distanceDriverDrove;

    @Index
    public double timeDriverSpent;

    public String prescriptionImageUrl; //  pharmacy
    public String prescription;         //  pharmacy

    @Index
    public int state;

    @Index
    public boolean canceled = false;

    @Index
    public List<Long> deliveryItemsIds = new ArrayList<>();
    public List<DeliveryItem> deliveryItems = new ArrayList<>();

    public String generalInstructions;
    @Index
    public Long customerLocationId;

    public CustomerLocationView customerLocationView;

    @Index
    public List<Long> driversWhoRefusedIDs = new ArrayList<>();

    @Index
    public boolean customerAcknowledgesCompletion;

    @Index
    public int deliveryOption;

    //default constructor for Entity initialization
    public DeliveryRequest() {
    }
    //============


    public DeliveryRequest(JDeliveryRequest jDeliveryRequest) {
        this.customerId = jDeliveryRequest.customerId;
        this.merchantId = jDeliveryRequest.merchantId;
        this.generalInstructions = jDeliveryRequest.generalInstructions;

        // items
        this.deliveryOption = Merchant.getMerchantByID(merchantId).deliveryOption;
        this.deliveryItems = jDeliveryRequest.deliveryItems;
        this.save();
        for (DeliveryItem deliveryItem : this.deliveryItems) {
            deliveryItem.deliveryRequestId = this.id;
            deliveryItem.save();
            this.deliveryItemsIds.add(deliveryItem.id);
        }


        //charge
        this.charge = jDeliveryRequest.charge;
        this.tip = jDeliveryRequest.tip;
        this.deliveryFee = jDeliveryRequest.deliveryFee;
        this.tayyarFee = jDeliveryRequest.tayyarFee;
        this.totalCharge = jDeliveryRequest.totalCharge;
        this.chargeAfterDiscount = jDeliveryRequest.chargeAfterDiscount;

        // customer location info
        this.customerLocationView = jDeliveryRequest.customerLocationView;
        this.customerLocationId = new CustomerLocation(this.id, this.customerId,
                this.customerLocationView).id;
        this.cityId = this.customerLocationView.cityId;
        this.areaId = this.customerLocationView.areaId;

        // merchant flag processing
        this.merchantType = jDeliveryRequest.merchantType;
        this.prescription = jDeliveryRequest.prescription;
        this.prescriptionImageUrl = jDeliveryRequest.prescriptionImageUrl;
        this.coupon = jDeliveryRequest.coupon;
        this.creationDate = new Date();
        this.save();
    }


    public void save() {
        ofy().save().entity(this).now();
    }

    public static DeliveryRequest getById(Long deliveryRequestID) {
        ofy().clear();
        return ofy().load().type(DeliveryRequest.class).id(deliveryRequestID).now();
    }

    public void addDriverWhoRefused(Long id) {
        this.driversWhoRefusedIDs.add(id);
        save();
    }


    public void driverAcceptsOrder(Long driverID) {
        this.driverAcceptsOrder = true;
        this.driverId = driverID;
        this.driverAcceptsOrderDate = new Date();
        this.state = 1;
        save();
    }


    public void driverConfirmsPickUP() {
        this.driverConfirmedPickUP = true;
        this.driverConfirmedPickUpOrderDate = new Date();
        this.state = 2;
        save();
    }

    public void driverArrivesAtCustomer() {
        this.driverArrivesAtCustomer = true;
        this.driverArrivedAtCustomerDate = new Date();
        this.state = 3;
        save();
    }

    public void driverCompletedDelivery() {
        this.orderDelivered = true;
        this.completionDate = new Date();
        this.state = 4;
        save();
    }

    public void cancelDeliveryRequest() {
        this.canceled = true;
        this.save();
    }

    public static boolean didCustomerEverOrderFromMerchant(Long customerId, Long merchantId) {
        List<DeliveryRequest> list = ofy().load().type(DeliveryRequest.class)
                .filter("customerId =", customerId)
                .filter("merchantId =", merchantId)
                .filter("state =", 4)
                .list();
        return list.size() > 0 ? true : false;
    }

    public static DeliveryRequest customerAcknowledgeCompletion(Long deliveryRequestId, boolean isCompleted){
        DeliveryRequest deliveryRequest = getById(deliveryRequestId);
        deliveryRequest.customerAcknowledgesCompletion = isCompleted;
        deliveryRequest.save();
        return deliveryRequest;
    }

    public static boolean isCustomerAllowedToReviewDriver(Long customerId, Long driverId) {
        List<DeliveryRequest> list = ofy().load().type(DeliveryRequest.class)
                .filter("customerId =", customerId)
                .filter("driverId =", driverId)
                .filter("state =", 4)
                .list();
        return list.size() > 0 ? true : false;
    }

    public List<Long> getItemsIds() {
        List<Long> itemsIds = new ArrayList<>(this.deliveryItems.size());
        for (DeliveryItem deliveryItem : this.deliveryItems) {
            itemsIds.add(deliveryItem.itemId);
        }
        return itemsIds;
    }

    public int getState() {
        return this.state;
    }


}
