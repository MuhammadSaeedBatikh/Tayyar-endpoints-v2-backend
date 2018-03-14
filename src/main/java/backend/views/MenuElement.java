package backend.views;

import java.util.ArrayList;
import java.util.List;

import backend.merchants.Item;
import backend.merchants.ItemView;
import backend.merchants.MerchantCategory;

/**
 * Created by Muhammad Saeed on 4/5/2017.
 */

public class MenuElement {
    public String name;
    public int itemCount;
    public Long categoryID;
    public String imageURL;


    public MenuElement(String lang, MerchantCategory merchantCategory) {
        this.categoryID = merchantCategory.id;
        this.itemCount = merchantCategory.itemsCount;
        this.imageURL = merchantCategory.imageURL;

        this.name = lang.trim().equalsIgnoreCase("ar") ? merchantCategory.nameAr : merchantCategory.nameEn;

    }

}
