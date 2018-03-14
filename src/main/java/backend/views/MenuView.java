package backend.views;

import java.util.ArrayList;
import java.util.List;

import backend.general.Viewable;
import backend.merchants.MerchantCategory;
import backend.merchants.Merchant;

/**
 * Created by Muhammad Saeed on 3/24/2017.
 */

public class MenuView implements Viewable {
    public Long id;
    public int categoriesCount;
    public List<MenuElement> menuElements = new ArrayList<>();

    //internal uses
    public MenuView(String lang, Merchant merchant) {
        this.id = merchant.id;
        List<MerchantCategory> merchantCategoryList = merchant.getMenuCategories();
        for (MerchantCategory merchantCategory : merchantCategoryList) {
            MenuElement menuElement = new MenuElement(lang, merchantCategory);
            this.menuElements.add(menuElement);
        }
        this.categoriesCount = menuElements.size();
    }

    //for the endpoint
    public MenuView(String lang, Long merchantID) {
        Merchant merchant = Merchant.getMerchantByID(merchantID);
        new MenuView(lang, merchant);
    }


}
