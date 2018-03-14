package backend.deliveryRequests;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 19/08/2017.
 */

@Entity
public class DeliveryItem {
    @Id
    public Long id;
    public Long deliveryRequestId;

    public Long itemId;
    // string instead of long, because key has to be string otherwise it throws an ExceptionInInitializerError
    public HashMap<String, List<Long>> options = new HashMap<>();
    public int quantity;
    public double price;
    public String itemInstructions;

    //default constructor for Entity initialization
    public DeliveryItem() {
    }



    //============
    public DeliveryItem(Long itemId, HashMap<String, List<Long>> options,
                        int quantity, String itemInstructions) {
        this.quantity = quantity;
        this.options = options;
        this.itemId = itemId;
        this.itemInstructions = itemInstructions;
    }


    public void save() {
        ofy().save().entity(this).now();
    }



    @Override
    public String toString() {
        return "DeliveryItem{" +
                "id=" + id +
                ", itemId=" + itemId +
                ", options=" + options +
                ", quantity=" + quantity +
                ", price=" + price +
                ", itemInstructions='" + itemInstructions + '\'' +
                '}';
    }
}
