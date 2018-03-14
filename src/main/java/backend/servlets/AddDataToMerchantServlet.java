package backend.servlets;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.merchants.Choice;
import backend.merchants.Item;
import backend.merchants.Merchant;
import backend.merchants.MerchantCategory;
import backend.merchants.Option;
import backend.merchants.jsonWrappers.JChoice;
import backend.merchants.jsonWrappers.JItem;
import backend.merchants.jsonWrappers.JOption;
import backend.merchants.restaurant.Restaurant;

import static backend.helpers.UtilityHelper.createTypedItem;

/**
 * Created by Muhammad on 03/03/2018.
 */

public class AddDataToMerchantServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String newMerchantStr = req.getParameter("newMerchant");
        boolean newMerchant = newMerchantStr.equals("true");
        Long categoryId = Long.valueOf(req.getParameter("categoryId"));
        String  itemsJson = req.getParameter("itemsJson");
        addItemsToCategory(newMerchant, categoryId, itemsJson);
        resp.getWriter().write("done uploading");
    }

    void addCategoryToMerchant(Long merchantId, String categoryJson) {

    }

    void addItemsToCategory(boolean newMerchant, Long categoryId, String itemsJson) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<JItem>>() {
        }.getType();
        List<JItem> jItems = gson.fromJson(itemsJson, listType);
        MerchantCategory merchantCategory = MerchantCategory.getCategoryByID(categoryId);
        Merchant merchant = Merchant.getMerchantByID(merchantCategory.parentMerchantId);
        boolean sellsChicken = false;
        for (JItem jItem : jItems) {
            sellsChicken = addItem(newMerchant, sellsChicken, jItem, merchantCategory, merchant);
        }
    }

    void addChoice(boolean newMerchant, JChoice jChoice, Option option, Merchant merchant) {
        Choice choice = new Choice(newMerchant, jChoice);
        choice.setParentOption(option.id);
        choice.setParentMerchantId(merchant.id);
        choice.saveChoice();
        option.addChoice(choice.id);
    }

    void addOption(boolean newMerchant, JOption jOption, Item item, Merchant merchant) {
        Option option = new Option(newMerchant, jOption);
        option.setParentMerchantId(merchant.id);
        option.setParentItemId(item.id);
        option.saveOption();
        item.addOption(option.id);
        List<JChoice> choices = jOption.choices;
        for (JChoice jChoice : choices) {
            addChoice(newMerchant, jChoice, option, merchant);
        }
    }

    boolean addItem(boolean newMerchant, boolean sellsChicken, JItem jItem, MerchantCategory merchantCategory, Merchant merchant) {
        Item item = createTypedItem(newMerchant, jItem, merchant);
        item.setParentMerchantId(merchant.id);
        item.setParentCategoryId(merchantCategory.id);
        String nameEn = item.nameEn.trim().toLowerCase();
        if ((nameEn.contains("chicken") || nameEn.contains("fajita")) && !sellsChicken && merchant instanceof Restaurant) {//does it sell chicken
            sellsChicken = true;
            merchant.addActualCategory(1000L);
            merchant.saveMerchant();
        }
        item.saveItem();
        //save item, generates ID
        merchantCategory.addItem(item.id);
        //add item to category
        List<JOption> jOptions = jItem.options;
        for (JOption jOption : jOptions) {
            addOption(newMerchant, jOption, item, merchant);
        }
        return sellsChicken;
    }

}
