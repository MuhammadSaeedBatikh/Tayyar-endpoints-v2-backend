package backend.deliveryRequests;

import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import backend.helpers.OfyHelper;
import backend.profiles.customer.Customer;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 16/01/2018.
 */

@Entity
@Cache
public class CustomerLocation {
    @Id
    public Long id;
    @Index
    public Long deliveryRequestId;
    @Index
    public Long customerId;
    @Index
    public Long cityId;
    @Index
    public Long areaId;
    @Index
    public GeoPt geoPt;

    public String address;
    public String deliveryInstructions;
    public String buildingNumber;
    public String floorNumber;
    public String apartmentNumber;

    //default constructor for Entity initialization
    public CustomerLocation() {
    }
    //============

    public CustomerLocation(Long deliveryRequestId, Long customerId, CustomerLocationView customerLocationView) {
        this.deliveryRequestId = deliveryRequestId;
        this.customerId = customerId;
        this.cityId = customerLocationView.cityId;
        this.areaId = customerLocationView.areaId;
        float latitude = customerLocationView.latitude;
        float longitude = customerLocationView.longitude;
        this.geoPt = new GeoPt(latitude, longitude);
        this.address = customerLocationView.address;
        this.deliveryInstructions = customerLocationView.deliveryInstructions;
        this.buildingNumber = customerLocationView.buildingNumber;
        this.floorNumber = customerLocationView.floorNumber;
        this.apartmentNumber = customerLocationView.apartmentNumber;
        save();
    }

    public void save() {
        ofy().save().entity(this).now();
    }

    public CustomerLocation getById(Long id) {
        return ofy().load().type(CustomerLocation.class).id(id).now();
    }
}
