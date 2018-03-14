package backend.deliveryRequests;

import com.google.appengine.repackaged.com.google.gson.Gson;


import java.util.Date;

/**
 * Created by Muhammad on 17/01/2018.
 */

public class DeliveryRequestState {
    public String parsingType = "DeliveryRequestState";
    public Long deliveryRequestId;
    public int state;
    public Date date;
    public int deliveryOption;

    public DeliveryRequestState(Long deliveryRequestId, int state) {
        this.deliveryRequestId = deliveryRequestId;
        this.state = state;
    }


    public DeliveryRequestState(DeliveryRequest deliveryRequest) {
        this.deliveryRequestId = deliveryRequest.id;
        this.state = deliveryRequest.getState();
        this.deliveryOption = deliveryRequest.deliveryOption;
        setStateDate(deliveryRequest);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public void setStateDate(DeliveryRequest deliveryRequest) {
        switch (this.state) {
            case 0:
                this.date = new Date();
                //deliveryRequest.creationDate;
                break;
            case 1:
                this.date = deliveryRequest.driverAcceptsOrderDate;
                break;
            case 2:
                this.date = deliveryRequest.driverConfirmedPickUpOrderDate;
                break;
            case 3:
                this.date = deliveryRequest.driverArrivedAtCustomerDate;
                break;
            case 4:
                this.date = deliveryRequest.completionDate;
                break;
        }
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "DeliveryRequestState{" +
                "deliveryRequestId=" + deliveryRequestId +
                ", state=" + state +
                ", date=" + date +
                ", deliveryOption=" + deliveryOption +
                '}';
    }
}
