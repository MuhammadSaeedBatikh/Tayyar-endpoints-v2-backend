package backend.merchants.inventory;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 30/09/2017.
 */

@Entity
public class Inventory {
    @Id
    public Long id;

    @Index
    public List<Long> actualCategoriesIds = new ArrayList<>();

    public HashMap<String, Long> actualCategoriesMapAr = new HashMap<>();
    public HashMap<String, Long> actualCategoriesMapEn = new HashMap<>();

    @Index
    public List<String> itemsCategoriesNames = new ArrayList<>();

    //default constructor for Entity initialization
    public Inventory() {
    }
    //============


    public static void addCategory(ActualCategory actualCategory) {
        actualCategory = ActualCategory.create(actualCategory);
        Long actualCategoryId = actualCategory.id;
        Inventory inventory = getInventory();
        if (!inventory.actualCategoriesIds.contains(actualCategoryId)) {
            inventory.actualCategoriesIds.add(actualCategoryId);
            inventory.actualCategoriesMapAr.put(actualCategory.nameAr, actualCategoryId);
            inventory.actualCategoriesMapEn.put(actualCategory.nameEn, actualCategoryId);
            inventory.save();
        }

    }

    public static Long getACategoryIdByName(String nameAr, String nameEn) {
        Inventory inventory = getInventory();
        boolean exists = inventory.actualCategoriesMapEn.containsKey(nameEn);
        if (exists) {
            return inventory.actualCategoriesMapEn.get(nameEn);
        } else {
            ActualCategory actualCategory = new ActualCategory(nameAr, nameEn);
            actualCategory.save();
            addCategory(actualCategory);
            return actualCategory.id;
        }
    }

    public static Inventory init(List<ActualCategory> actualCategories) {
        System.out.println("actualCategories = " + actualCategories);
        Inventory inventory = new Inventory();
        inventory.save();
        System.out.println(inventory);
        for (ActualCategory actualCategory : actualCategories) {
            addCategory(actualCategory);
        }
        return getInventory();
    }

    public void save() {
        ofy().save().entity(this).now();
    }

    public static Inventory getInventory() {
        return ofy().load().type(Inventory.class).list().get(0);
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "id=" + id +
                ", actualCategoriesIds=" + actualCategoriesIds +
                ", actualCategoriesMapAr=" + actualCategoriesMapAr +
                ", actualCategoriesMapEn=" + actualCategoriesMapEn +
                ", itemsCategoriesNames=" + itemsCategoriesNames +
                '}';
    }
}
