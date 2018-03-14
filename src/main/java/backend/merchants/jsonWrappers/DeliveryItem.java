package backend.merchants.jsonWrappers;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Muhammad on 19/08/2017.
 */

public class DeliveryItem {
    Long itemId;
    HashMap<Long, List<Long>> options = new HashMap<>();
    int quantity;
    String itemInstructions;

    public DeliveryItem(Long itemId, HashMap<Long, List<Long>> options,
                        int quantity, String itemInstructions) {
        this.quantity = quantity;
        this.options = options;
        this.itemId = itemId;
        this.itemInstructions = itemInstructions;
    }


    @Override
    public String toString() {
        return "DeliveryItem{" +
                "itemId=" + itemId +
                ", options=" + options +
                ", quantity=" + quantity +
                ", itemInstructions='" + itemInstructions + '\'' +
                '}';
    }
}
