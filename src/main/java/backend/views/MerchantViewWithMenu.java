package backend.views;

import backend.merchants.Merchant;
import backend.merchants.MerchantView;
import backend.views.MenuView;

/**
 * Created by Muhammad on 01/02/2018.
 */

public class MerchantViewWithMenu {
    public MerchantView merchantView;
    public MenuView menuView;

    public MerchantViewWithMenu(String lang, Long areaId, Merchant merchant, Long customerId) {
        this.merchantView = new MerchantView(lang, areaId, merchant, customerId);
        this.menuView = new MenuView(lang, merchant);
    }
}
