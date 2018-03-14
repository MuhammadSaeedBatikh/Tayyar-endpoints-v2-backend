package backend.merchants.inventory;

import java.util.HashMap;

/**
 * Created by Muhammad on 14/01/2018.
 */

public class InventoryView {
    Long id;
    public HashMap<String, Long> actualCategoriesMap = new HashMap<>();

    public InventoryView(String lang, Inventory inventory) {
        this.id = inventory.id;
        boolean isArabic = lang.trim().equalsIgnoreCase("ar");
        if (isArabic){
            this.actualCategoriesMap = inventory.actualCategoriesMapAr;
        }
        else {
            this.actualCategoriesMap = inventory.actualCategoriesMapEn;
        }
    }

    @Override
    public String toString() {
        return "InventoryView{" +
                "id=" + id +
                ", actualCategoriesMap=" + actualCategoriesMap +
                '}';
    }
}

