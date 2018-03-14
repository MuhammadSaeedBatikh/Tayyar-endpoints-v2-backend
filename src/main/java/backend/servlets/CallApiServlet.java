package backend.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.apis.CustomerApi;
import backend.apis.MerchantApi;
import backend.apis.UnexposedApiMethods;

/**
 * Created by Muhammad on 12/01/2018.
 */

public class CallApiServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String inventory = req.getParameter("inventory");
        System.out.println(inventory);
        if (inventory.equalsIgnoreCase("false")) {
            String reqPram = req.getParameter("reqPram");
            String check = req.getParameter("check");
            boolean support = check.trim().equalsIgnoreCase("true");
            UnexposedApiMethods.uploadCityData(reqPram, support);
        } else {
            UnexposedApiMethods.createInventory(inventory);
        }
    }
}
