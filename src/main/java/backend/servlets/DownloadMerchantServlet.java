package backend.servlets;

import com.google.appengine.repackaged.com.google.gson.Gson;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.merchants.Merchant;
import backend.merchants.jsonWrappers.JMerchant;
import backend.merchants.jsonWrappers.JsonRawMerchant;

/**
 * Created by Muhammad on 01/03/2018.
 */

public class DownloadMerchantServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long merchantId = Long.valueOf(req.getParameter("merchantId"));
        Merchant merchant = Merchant.getMerchantByID(merchantId);
        JMerchant jMerchant = JMerchant.fromMerchant(merchant);
        String merchantJson = new Gson().toJson(jMerchant);
        JsonRawMerchant jsonRawMerchant = new JsonRawMerchant(jMerchant.id, merchantJson);
        resp.getWriter().write(merchantJson);
    }
}
