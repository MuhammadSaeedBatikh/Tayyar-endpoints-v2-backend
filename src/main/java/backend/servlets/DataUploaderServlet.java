package backend.servlets;

import com.google.appengine.api.datastore.Category;
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
import backend.merchants.superMarket.SuperMarket;
import backend.merchants.superMarket.SuperMarketItem;
import backend.profiles.driver.UpdatableLocation;
import backend.stats.MerchantStats;
import backend.merchants.Option;
import backend.merchants.dessertsMerchant.DessertsMerchant;
import backend.merchants.dessertsMerchant.DessertsMerchantItem;
import backend.merchants.jsonWrappers.JCategory;
import backend.merchants.jsonWrappers.JChoice;
import backend.merchants.jsonWrappers.JItem;
import backend.merchants.jsonWrappers.JMerchant;
import backend.merchants.jsonWrappers.JOption;
import backend.merchants.jsonWrappers.JsonRawMerchant;
import backend.merchants.pharmacy.Pharmacy;
import backend.merchants.pharmacy.PharmacyItem;
import backend.merchants.restaurant.Restaurant;
import backend.merchants.restaurant.RestaurantItem;
import backend.merchants.specialMerchant.SpecialMerchant;
import backend.merchants.specialMerchant.SpecialMerchantItem;

import static backend.helpers.UtilityHelper.createTypedItem;

/**
 * Created by Muhammad on 19/08/2017.
 */

public class DataUploaderServlet extends HttpServlet {
    static int counter = 0;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Long merchantId = Long.valueOf(req.getParameter("merchantId"));
        Long jsonRawMerchantId = Long.valueOf(req.getParameter("jsonRawMerchantId"));
        String newMerchantStr = req.getParameter("newMerchant");
        boolean newMerchant = newMerchantStr.equals("true");

        String jMerchantJson = JsonRawMerchant.getById(jsonRawMerchantId).merchantJson;
        Gson gson = new Gson();
        JMerchant jMerchant = gson.fromJson(jMerchantJson, JMerchant.class);
        List<JCategory> jCategories = jMerchant.categories;
        System.out.println("counter = " + counter);
        counter++;

        if (newMerchant) {
            addMerchantData(merchantId, jCategories);
        } else {
            updateMerchant(jMerchant);
        }
        resp.getWriter().write("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    }

    void updateMerchant(JMerchant jMerchant) {
        Merchant merchant = Merchant.getMerchantByID(jMerchant.id);
        List<JCategory> categories = jMerchant.categories;
        for (JCategory jCategory : categories) {
            MerchantCategory merchantCategory = new MerchantCategory(false, jCategory);
            merchantCategory.saveCategory();
            for (JItem jItem : jCategory.items) {
                Item item = createTypedItem(false, jItem, merchant);
                item.saveItem();
                for (JOption jOption : jItem.options) {
                    Option option = new Option(false, jOption);
                    option.saveOption();
                    for (JChoice jChoice : jOption.choices) {
                        Choice choice = new Choice(false, jChoice);
                        choice.saveChoice();
                    }
                }
            }
        }

    }

    void addMerchantData(Long merchantId, List<JCategory> jCategories) {
        boolean newMerchant = true;
        int sign = (int) (Math.random() * 10E05);
        Merchant merchant = Merchant.getMerchantByID(merchantId);

        MerchantStats merchantStats = new MerchantStats(merchantId);
        merchant.merchantStatsId = merchantStats.id;

        int countS = 0;
        boolean sellsChicken = false;
        for (JCategory jCategory : jCategories) {
            MerchantCategory merchantCategory = new MerchantCategory(newMerchant, jCategory);
            merchantCategory.saveCategory();
            merchant.addMenuCategory(merchantCategory.id); //merchant saved with the new Category
            merchantCategory.setParentMerchantId(merchant.id); // getting merchant id after being saved in datastore
            countS++;
            System.out.println(sign + " parent merchant " + countS);
            List<JItem> jItems = jCategory.items;
            for (JItem jItem : jItems) {
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
                    Option option = new Option(newMerchant, jOption);
                    option.setParentMerchantId(merchant.id);
                    option.setParentItemId(item.id);
                    option.saveOption();
                    item.addOption(option.id);

                    List<JChoice> choices = jOption.choices;
                    for (JChoice jChoice : choices) {
                        Choice choice = new Choice(newMerchant, jChoice);
                        choice.setParentOption(option.id);
                        choice.setParentMerchantId(merchant.id);
                        choice.saveChoice();
                        option.addChoice(choice.id);
                    }
                }
            }
        }
    }


   }
