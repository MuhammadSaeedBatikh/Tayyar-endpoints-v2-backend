package backend.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.apis.MerchantApi;
import backend.apis.UnexposedApiMethods;
import backend.helpers.returnWrappers.BooleanWrapper;
import backend.monitor.DeliveryRequestCheckView;

/**
 * Created by Muhammad on 08/02/2018.
 */

public class DeliveriesNeedToBeCheckedServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long cityId = Long.valueOf(req.getParameter("cityId"));
        List<DeliveryRequestCheckView> deliveryRequestsThatNeedToBeChecked = UnexposedApiMethods.getDeliveryRequestsThatNeedToBeChecked(cityId);
        if (deliveryRequestsThatNeedToBeChecked == null){
            resp.getWriter().write("no delivery ever required any checking ");
        }
        StringBuffer stringBuffer = new StringBuffer("");

        for (DeliveryRequestCheckView deliveryRequestCheckView : deliveryRequestsThatNeedToBeChecked) {
            stringBuffer.append(deliveryRequestCheckView.toString() + "\n\n\n\n\n\n");
        }
        resp.getWriter().write(stringBuffer.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long deliveryRequestId = Long.valueOf(req.getParameter("deliveryRequestId"));
        BooleanWrapper booleanWrapper = UnexposedApiMethods.doneCheckingADelivery(deliveryRequestId);
        resp.getWriter().write(booleanWrapper.toString());
    }
}
