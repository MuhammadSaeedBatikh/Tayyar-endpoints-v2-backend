package backend.merchants;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import backend.general.Viewable;
import backend.merchants.jsonWrappers.JCategory;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad Saeed on 2/11/2017.
 */
@Entity
@Cache
public class MerchantCategory implements Viewable {
    @Id
    public Long id;

    @Index
    public Long parentMerchantId;

    @Index
    public String nameAr;
    @Index
    public String nameEn;

    public String descriptionEn;
    public String descriptionAr;
    public int itemsCount;
    public int defaultOrder;
    public String imageURL;

    @Index
    public List<Long> itemsIds = new ArrayList<>();

    //default constructor for Entity initalization
    public MerchantCategory() {
    }


    public MerchantCategory(String nameAr, String nameEn, String descriptionAr,
                            String descriptionEn, String imageURL) {
        this.nameAr = nameAr;
        this.nameEn = nameEn;
        this.descriptionAr = descriptionAr;
        this.descriptionEn = descriptionEn;

        this.imageURL = imageURL;
    }

    public MerchantCategory(boolean newMerchant, JCategory jCategory) {
        if (!newMerchant) {
            this.id = jCategory.id;
            this.parentMerchantId = jCategory.parentMerchantId;
            this.itemsIds = jCategory.itemsIds;
        }
        this.nameAr = jCategory.nameAr;
        this.nameEn = jCategory.nameEn;

        this.descriptionAr = jCategory.descriptionAr;
        this.descriptionEn = jCategory.descriptionEn;
        this.itemsCount = jCategory.itemsCount;
        this.imageURL = jCategory.imageURL;
        this.defaultOrder = jCategory.defaultOrder;
    }

    public void saveCategory() {
        ofy().save().entity(this).now();
    }

    public static MerchantCategory getCategoryByID(Long id) {
        return ofy().load().type(MerchantCategory.class).id(id).now();
    }

    public void addItem(Long itemID) {
        this.itemsIds.add(itemID);//add item key to this category
        saveCategory();
        // save changes in this category
    }

    public List<Item> getItems() {
        Collection<Item> items = ofy().load().type(Item.class).ids(this.itemsIds).values();
        return new ArrayList<Item>(items);
    }

    public void setParentMerchantId(Long parentMerchantId) {
        this.parentMerchantId = parentMerchantId;
    }


}
