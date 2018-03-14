package backend.deliveryRequests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import backend.TestEntity;
import backend.merchants.Choice;
import backend.merchants.Item;
import backend.merchants.Option;

/**
 * Created by Muhammad on 15/01/2018.
 */

public class DeliveryItemView {

    public Long itemId;
    public String name;
    public int quantity;
    public String description;
    public double price;
    public String itemInstructions;
    public List<DeliveryItemOption> optionsViews = new ArrayList<>();


    public static DeliveryItemView toView(String lang, DeliveryItem deliveryItem) {
        DeliveryItemView itemView = new DeliveryItemView();
        itemView.itemId = deliveryItem.itemId;
        Item item = Item.getItemByID(itemView.itemId);

        itemView.quantity = deliveryItem.quantity;
        itemView.price = deliveryItem.price;
        itemView.itemInstructions = deliveryItem.itemInstructions;
        HashMap<String, List<Long>> options = deliveryItem.options;

        if (lang.trim().equalsIgnoreCase("ar")) {
            if (item == null) {
                new TestEntity().log(itemView.toString());
                return null;
            }
            itemView.name = item.nameAr;
            itemView.description = item.descriptionAr;

        } else {
            itemView.name = item.nameEn;
            itemView.description = item.descriptionEn;

        }
        for (String optionIdStr : options.keySet()) {
            List<Long> choicesIds = options.get(optionIdStr);
            Long optionId = Long.valueOf(optionIdStr);
            Option option = Option.getOptionByID(optionId);

            String optionName = lang.trim().equalsIgnoreCase("ar") ? option.nameAr : option.nameEn;

            DeliveryItemOption deliveryItemOption = new DeliveryItemOption();
            deliveryItemOption.name = optionName;
            for (Long choiceId : choicesIds) {
                Choice choice = Choice.getChoiceByID(choiceId);
                String name = lang.trim().equalsIgnoreCase("ar") ? choice.nameAr : choice.nameEn;
                deliveryItemOption.addChoice(name);
            }
            itemView.optionsViews.add(deliveryItemOption);
        }
        return itemView;

    }

    public static List<DeliveryItemView> createListOfDeliveryItemsViews(String lang, List<DeliveryItem> deliveryItems) {
        List<DeliveryItemView> deliveryItemViews = new ArrayList<>();
        for (DeliveryItem deliveryItem : deliveryItems) {
            DeliveryItemView itemView = DeliveryItemView.toView(lang, deliveryItem);
            deliveryItemViews.add(itemView);
        }
        if (deliveryItemViews.size() == 0){
            return null;
        }
        return deliveryItemViews;
    }

    @Override
    public String toString() {
        return "DeliveryItemView{" +
                "itemId=" + itemId +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", itemInstructions='" + itemInstructions + '\'' +
                ", optionsViews=" + optionsViews +
                '}';
    }
}
