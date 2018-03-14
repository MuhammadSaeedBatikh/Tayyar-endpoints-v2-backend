package backend.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.apis.CustomerApi;
import backend.apis.MerchantApi;
import backend.apis.UnexposedApiMethods;
import backend.helpers.returnWrappers.StringWrapper;
import backend.merchants.Choice;
import backend.merchants.Item;
import backend.merchants.Merchant;
import backend.merchants.MerchantCategory;
import backend.merchants.Option;
import backend.merchants.dessertsMerchant.DessertsMerchant;
import backend.merchants.inventory.Inventory;
import backend.merchants.pharmacy.PharmacyItem;
import backend.merchants.restaurant.Restaurant;
import backend.merchants.restaurant.RestaurantItem;
import backend.merchants.superMarket.SuperMarket;
import backend.merchants.superMarket.SuperMarketItem;

/**
 * Created by Muhammad on 19/08/2017.
 */

public class UploadMerchantServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("UploadMerchantServlet");
        String jsonMerchant = req.getParameter("jsonMerchant");
        String newMerchantStr = req.getParameter("newMerchant");
        boolean newMerchant = newMerchantStr.equals("true");
        try {
            UnexposedApiMethods.uploadJsonMerchant(newMerchant, jsonMerchant);
        } catch (Exception e) {
            e.printStackTrace();
        }
        resp.getWriter().write("uploading ..... ");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("UploadMerchantServlet");
        Long merchantId = Long.parseLong(req.getParameter("merchantId"));
       UnexposedApiMethods.deleteMerchant(merchantId);
        resp.getWriter().write("deleting ..... ");
    }
}
