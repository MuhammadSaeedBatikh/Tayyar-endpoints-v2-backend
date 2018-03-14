package backend.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.deliveryRequests.DeliveryRequest;
import backend.merchants.Item;
import backend.merchants.Merchant;
import backend.profiles.customer.Customer;
import backend.reviews.Review;

import static backend.profiles.Profile.getProfileByID;

/**
 * Created by Muhammad on 04/02/2018.
 */

public class ReviewServlet extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String deliveryRequestIdStr = req.getParameter("deliveryRequestId");
        Long deliveryRequestId = Long.parseLong(deliveryRequestIdStr);

        String reviewIdStr= req.getParameter("reviewId");
        Long reviewId= Long.parseLong(reviewIdStr);

        String customerIdStr= req.getParameter("customerId");
        Long  customerId= Long.parseLong(customerIdStr);


        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestId);
        Review review = Review.getByID(reviewId);
        Customer customer = (Customer) getProfileByID(customerId);
        Long merchantId = deliveryRequest.merchantId;



        // submit review
        // update merchant stats
        // update items stats
        // update customer stats



        Merchant merchant = Merchant.getMerchantByID(merchantId);
        merchant.gotReviewed(review.id);
        Item.itemsDelivered(review.rating, deliveryRequest.getItemsIds());
        customer.reviewedSomething(review.id);
    }
}
