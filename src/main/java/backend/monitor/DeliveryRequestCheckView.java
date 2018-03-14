package backend.monitor;

import java.awt.font.TextMeasurer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import backend.deliveryRequests.DeliveryRequest;
import backend.deliveryRequests.DeliveryRequestView;
import backend.merchants.Merchant;
import backend.profiles.driver.Driver;

/**
 * Created by Muhammad on 08/02/2018.
 */

public class DeliveryRequestCheckView {
    public Long deliveryRequestId;
    public MerchantDriverQuickInfo merchantDriverQuickInfo;
    public DeliveryRequestView deliveryRequestDetails;

    public DeliveryRequestCheckView(Long deliveryRequestId) {
        this.deliveryRequestId = deliveryRequestId;
        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestId);
        this.merchantDriverQuickInfo = new MerchantDriverQuickInfo(deliveryRequest);
        this.deliveryRequestDetails = new DeliveryRequestView(deliveryRequest);
    }

    @Override
    public String toString() {
        return "DeliveryRequestCheckView{" +
                "deliveryRequestId=" + deliveryRequestId +
                ", merchantDriverQuickInfo=" + merchantDriverQuickInfo +
                ", deliveryRequestDetails=" + deliveryRequestDetails +
                '}';
    }
}

class MerchantDriverQuickInfo {
    public String merchantName;
    public List<String> merchantPhones = new ArrayList<>();
    public String driverName;
    public String driverPhone;

    public MerchantDriverQuickInfo(DeliveryRequest deliveryRequest) {
        Merchant merchant = Merchant.getMerchantByID(deliveryRequest.merchantId);
        Driver driver = Driver.getDriverByID(deliveryRequest.driverId);
        this.merchantName = merchant.nameAr;
        this.merchantPhones = merchant.phones;
        this.driverName = driver.name;
        this.driverPhone = driver.phone;
    }

    @Override
    public String toString() {
        return "MerchantDriverQuickInfo{" +
                "merchantName='" + merchantName + '\'' +
                ", merchantPhones=" + merchantPhones +
                ", driverName='" + driverName + '\'' +
                ", driverPhone='" + driverPhone + '\'' +
                '}';
    }
}


