package backend.deliveryRequests.clientWrappers;

import com.google.appengine.repackaged.com.google.gson.Gson;

import java.util.Date;

import backend.deliveryRequests.DeliveryRequest;

/**
 * Created by Muhammad on 28/01/2018.
 */

public class DeliveryTimeline {
    public String parsingType = "DeliveryTimeline";
    public Long id;

    public int state;

    public Date submitted;

    public Date confirmed;

    public Date outForDelivery;

    public Date nearCustomer;

    public Date finished;


    public DeliveryTimeline(DeliveryRequest deliveryRequest) {
        this.id = deliveryRequest.id;
        this.state = deliveryRequest.state;

        this.submitted = deliveryRequest.creationDate;
        this.confirmed = deliveryRequest.driverAcceptsOrderDate;
        this.outForDelivery = deliveryRequest.driverConfirmedPickUpOrderDate;
        this.nearCustomer = deliveryRequest.driverArrivedAtCustomerDate;
        this.finished = deliveryRequest.completionDate;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
