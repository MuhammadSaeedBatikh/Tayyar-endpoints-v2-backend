package backend.merchants.jsonWrappers;


import com.googlecode.objectify.Key;

import java.util.ArrayList;
import java.util.List;

import backend.merchants.Item;
import backend.merchants.Option;
import backend.stats.ReviewStats;

/**
 * Created by Muhammad on 02/11/2017.
 */
public class JItem {
    public String nameEn;
    public String nameAr;
    public String descriptionEn;
    public String descriptionAr;
    public double basePrice;
    public String imageURL;
    public int defaultOrder;
    public List<JOption> options = new ArrayList<>();

    //============
    public Long id;
    public Long parentMerchantId;
    public Long parentCategoryId;
    public int orderCount; // how many times the item was delivered;
    public ReviewStats reviewStats = new ReviewStats();
    public double rating;
    public List<Long> actualCategoriesIds = new ArrayList<>();
    public List<Long> optionsIds = new ArrayList<>();



    public static JItem fromItem(Item item){
        JItem jItem = new JItem();
        jItem.nameEn = item.nameEn;
        jItem.nameAr = item.nameAr;
        jItem.descriptionEn = item.descriptionEn;
        jItem.descriptionAr =item. descriptionAr;
        jItem.basePrice = item.basePrice;
        jItem.imageURL = item.imageURL;
        jItem.defaultOrder = item.defaultOrder;
        jItem.id = item.id;
        jItem.parentMerchantId = item.parentMerchantId;
        jItem.parentCategoryId = item.parentCategoryId;
        jItem.orderCount = item.orderCount;
        jItem.reviewStats = item.reviewStats;
        jItem.rating = item.rating;
        jItem.actualCategoriesIds = item.actualCategoriesIds;
        jItem.optionsIds = item.optionsIds;
        for (Long optionId: jItem.optionsIds) {
            Option option = Option.getOptionByID(optionId);
            JOption jOption = JOption.fromOption(option);
            jItem.options.add(jOption);
        }
        return jItem;
    }
}
