package backend.merchants.jsonWrappers;


import com.google.appengine.api.datastore.Category;
import com.googlecode.objectify.Key;

import java.util.ArrayList;
import java.util.List;

import backend.merchants.Item;
import backend.merchants.MerchantCategory;

/**
 * Created by Muhammad on 02/11/2017.
 */
public class JCategory {
    public String nameEn;
    public String nameAr;
    public String descriptionEn;
    public String descriptionAr;
    public int itemsCount;
    public String imageURL;
    public int defaultOrder;
    public List<JItem> items = new ArrayList<>();
    // generated
    public Long id;
    public Long parentMerchantId;
    public List<Long> itemsIds = new ArrayList<>();



    public static JCategory fromMerchantCategory(MerchantCategory category) {
        JCategory jCategory = new JCategory();
        jCategory.nameEn = category.nameEn;
        jCategory.nameAr = category.nameAr;
        jCategory.descriptionEn = category.descriptionEn;
        jCategory.descriptionAr = category.descriptionAr;
        jCategory.itemsCount = category.itemsCount;
        jCategory.imageURL = category.imageURL;
        jCategory.defaultOrder = category.defaultOrder;
        jCategory.id = category.id;
        jCategory.parentMerchantId = category.parentMerchantId;
        jCategory.itemsIds = category.itemsIds;
        for (Long itemId : jCategory.itemsIds) {
            Item item = Item.getItemByID(itemId);
            JItem jItem = JItem.fromItem(item);
            jCategory.items.add(jItem);
        }
        return jCategory;
    }

}
