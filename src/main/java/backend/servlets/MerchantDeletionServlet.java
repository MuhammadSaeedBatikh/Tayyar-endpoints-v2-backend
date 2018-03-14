package backend.servlets;

import com.googlecode.objectify.cmd.QueryKeys;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.merchants.Choice;
import backend.merchants.Item;
import backend.merchants.Merchant;
import backend.merchants.MerchantCategory;
import backend.merchants.Option;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 07/12/2017.
 */

public class MerchantDeletionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String merchantIdStr = req.getParameter("merchantId");
        Long merchantId = Long.parseLong(merchantIdStr);
        System.out.println("merchantId = " + merchantId);
        Merchant merchant = Merchant.getMerchantByID(merchantId);
        QueryKeys<MerchantCategory> categoryQueryKeys =
                ofy().load().type(MerchantCategory.class).filter("parentMerchantId =", merchantId).keys();
        QueryKeys<Item> itemQueryKeys =
                ofy().load().type(Item.class).filter("parentMerchantId =", merchantId).keys();
        QueryKeys<Option> optionQueryKeys =
                ofy().load().type(Option.class).filter("parentMerchantId =", merchantId).keys();
        QueryKeys<Choice> choiceQueryKeys =
                ofy().load().type(Choice.class).filter("parentMerchantId =", merchantId).keys();

        ofy().delete().keys(categoryQueryKeys).now();
        ofy().delete().keys(itemQueryKeys).now();
        ofy().delete().keys(optionQueryKeys).now();
        ofy().delete().keys(choiceQueryKeys).now();
        ofy().delete().entity(merchant).now();
        resp.getWriter().write("deleting ....");
    }
}
