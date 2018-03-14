package backend.merchants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad on 13/01/2018.
 */

public class ItemView {
    public Long id;
    public String name;
    public String description;
    public double basePrice;
    public String imageURL;
    public boolean available = true;
    public int optionsCount;

    public ItemView(String lang, Item item) {
        this.id = item.id;
        this.available = item.available;
        this.imageURL = item.imageURL;
        this.basePrice = item.basePrice;
        if (lang.trim().equalsIgnoreCase("ar")){
            this.name = item.nameAr;
            this.description = item.descriptionAr;

        }
        else {
            this.name = item.nameEn;
            this.description = item.descriptionEn;
        }

        this.optionsCount = item.optionsIds.size();
    }


    public static List<ItemView> toItemView(String lang, List<Item> items){
        List<ItemView> itemViews = new ArrayList<>();
        for (Item item : items) {
            itemViews.add(new ItemView(lang, item));
        }
        return itemViews;
    }
}
