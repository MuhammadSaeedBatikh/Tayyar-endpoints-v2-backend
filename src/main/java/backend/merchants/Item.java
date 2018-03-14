package backend.merchants;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Pack200;

import backend.general.Viewable;
import backend.merchants.jsonWrappers.JItem;
import backend.stats.ReviewStats;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 24/07/2017.
 */
@Entity
@Cache

public abstract class Item implements Viewable {
    @Id
    public Long id;

    @Index
    public Long parentMerchantId;

    @Index
    public Long parentCategoryId;

    public String nameAr;
    public String nameEn;

    public String descriptionEn;
    public String descriptionAr;

    @Index
    public double basePrice;//without extras, minimum of all required Options like size etc
    @Index
    public List<Long> optionsIds = new ArrayList<>();
    public String imageURL;

    @Index
    public boolean available = true;

    public ReviewStats reviewStats = new ReviewStats();
    @Index
    public double rating;

    @Index
    public int defaultOrder;

    @Index
    public int orderCount; // how many times the item was delivered;

    @Index
    public List<Long> actualCategoriesIds = new ArrayList<>();

    //default constructor for Entity initialization
    public Item() {
    }

    public Item(String nameAr, String nameEn, double basePrice) {
        this.nameAr = nameAr;
        this.nameEn = nameEn;
        this.basePrice = basePrice;
    }

    public Item(boolean newMerchant, JItem jItem) {
        if (!newMerchant) {
            this.id = jItem.id;
            this.parentMerchantId = jItem.parentMerchantId;
            this.parentCategoryId = jItem.parentCategoryId;
            this.orderCount = jItem.orderCount;
            this.reviewStats = jItem.reviewStats;
            this.rating = jItem.rating;
            this.actualCategoriesIds = jItem.actualCategoriesIds;
            this.optionsIds = jItem.optionsIds;
        }
        this.nameAr = jItem.nameAr;
        this.nameEn = jItem.nameEn;
        this.imageURL = jItem.imageURL;
        this.descriptionAr = jItem.descriptionAr;
        this.descriptionEn = jItem.descriptionEn;
        this.basePrice = jItem.basePrice;
        this.defaultOrder = jItem.defaultOrder;
    }

    public static Item getItemByID(Long id) {
        return ofy().load().type(Item.class).id(id).now();
    }

    public void saveItem() {
        ofy().save().entity(this).now();
    }

    public void addOption(Long optionID) {
        this.optionsIds.add(optionID);
        ofy().save().entity(this).now();// save changes in this Item
    }

    public List<Option> getOptions() {
        Collection<Option> options = ofy().load().type(Option.class).ids(this.optionsIds).values();
        return new ArrayList<Option>(options);

    }


    public void setParentMerchantId(Long parentMerchantId) {
        this.parentMerchantId = parentMerchantId;
    }

    public void setParentCategoryId(Long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public static List<Item> getByParentMerchantId(Long parentMerchantId) {
        return ofy().load().type(Item.class)
                .filter("parentMerchantId =", parentMerchantId).list();
    }

    public static void itemsDelivered(int rating, List<Long> itemsIds) {
        Map<Long, Item> itemMap = ofy().load().type(Item.class).ids(itemsIds);
        List<Item> items = new ArrayList<>(itemMap.values());
        for (Item item : items) {
            item.orderCount++;
        }
        gotRated(rating, itemsIds);
        ofy().save().entities(items).now();
    }

    public void gotRated(int rating, boolean save) {
        this.reviewStats.revieweeId = this.id;
        this.reviewStats.updateWithRating(rating);
        this.rating = this.reviewStats.rating;
        if (save) {
            saveItem();
        }
    }

    public static void gotRated(int rating, List<Long> itemsIds) {
        Map<Long, Item> itemsMap = ofy().load().type(Item.class).ids(itemsIds);
        List<Item> items = new ArrayList<>(itemsMap.values());
        for (Item item : items) {
            item.gotRated(rating, false);
        }
        ofy().save().entities(items);
    }


}
