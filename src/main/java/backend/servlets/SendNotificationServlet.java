package backend.servlets;


import com.google.appengine.repackaged.com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.apis.CustomerApi;
import backend.helpers.FireBaseHelper;
import backend.helpers.returnWrappers.StringWrapper;
import backend.profiles.customer.Customer;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 08/11/2017.
 */

public class SendNotificationServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long notifiedCityId = Long.valueOf(req.getParameter("notifiedCityId"));
        String promotionMessageJson = req.getParameter("promotionMessageJson");
        List<Customer> customers = ofy().load().type(Customer.class)
                .filter("cityId =", notifiedCityId)
                .list();
        ArrayList<String> regTokensInCity = new ArrayList<>(customers.size());
        for (Customer customer : customers) {
            for (String regToken : customer.regTokenList) {
                regTokensInCity.add(regToken);
            }
        }
        FireBaseHelper.sendNotification(regTokensInCity, promotionMessageJson);
        resp.getWriter().write(new Gson().toJson(regTokensInCity));
    }
}