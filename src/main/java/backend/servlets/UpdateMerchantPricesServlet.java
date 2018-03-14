package backend.servlets;

import com.googlecode.objectify.Key;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.merchants.Item;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 05/01/2018.
 */

public class UpdateMerchantPricesServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Long merchantId = Long.parseLong(req.getParameter("merchantId"));
        String updateType = req.getParameter("updateType");
        double addedPrice = Double.parseDouble(req.getParameter("addedPrice"));
        if (!updateType.trim().equalsIgnoreCase("percentage") & !updateType.trim().equalsIgnoreCase("price")) {
            throw new IllegalArgumentException("percentage or price only allowed as types");
        }

        List<Item> items = Item.getByParentMerchantId(merchantId);
        List<Item> changedItems = new ArrayList<>();
        for (Item item : items) {
            double oldPrice = item.basePrice;
            item.basePrice = calcNewPrice(oldPrice,addedPrice,updateType);
            if (oldPrice != item.basePrice){
                changedItems.add(item);
            }
        }
        ofy().save().entities(changedItems).now();
        System.out.println("all prices got updated");
    }

    public double calcNewPrice(double oldPrice, double addedPrice, String updateType) {
        double newPrice = oldPrice;
        if (updateType.trim().equalsIgnoreCase("percentage")) {
            newPrice = oldPrice > 10 ? roundUp(oldPrice + oldPrice * addedPrice) : oldPrice;
        }
        else if(updateType.trim().equalsIgnoreCase("price")){
            if (oldPrice > 10) {
                newPrice = roundUp(oldPrice + addedPrice * (int)oldPrice/10);
            }
        }
        return  newPrice;
    }

    public double roundUp(double price) {
        double fraction = price - ((int) price);
        if (fraction < 0.001) return price;

        fraction = fraction > 0.5 ? 1 : .5;
        return ((int) price) + fraction;
    }
}
