package backend.helpers;

import backend.TestEntity;
import backend.cityArea.Area;
import backend.cityArea.City;
import backend.deliveryRequests.CustomerLocation;
import backend.deliveryRequests.DeliveryItem;
import backend.deliveryRequests.DeliveryRequest;
import backend.general.BlackListedProfile;
import backend.general.ConstantParams;
import backend.general.UserPrivileges;
import backend.merchants.specialMerchant.SpecialMerchantItem;
import backend.monitor.DeliveryRequestChecker;
import backend.stats.MerchantStats;
import backend.stats.DriverStats;
import backend.reviews.Review;
import backend.merchants.MerchantCategory;
import backend.merchants.Choice;
import backend.merchants.Item;
import backend.merchants.Merchant;
import backend.merchants.dessertsMerchant.DessertsMerchant;
import backend.merchants.inventory.ActualCategory;
import backend.merchants.inventory.Inventory;
import backend.merchants.jsonWrappers.JsonRawMerchant;
import backend.merchants.pharmacy.Pharmacy;
import backend.merchants.pharmacy.PharmacyItem;
import backend.merchants.Option;
import backend.merchants.restaurant.Restaurant;
import backend.merchants.restaurant.RestaurantItem;
import backend.merchants.specialMerchant.SpecialMerchant;
import backend.merchants.superMarket.SuperMarket;
import backend.merchants.superMarket.SuperMarketItem;
import backend.offers.Offer;
import backend.offers.OfferMessages;
import backend.profiles.Profile;
import backend.profiles.customer.Customer;
import backend.profiles.driver.Driver;
import backend.profiles.driver.UpdatableLocation;
import backend.stats.ReviewStats;

import com.googlecode.objectify.ObjectifyService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by Muhammad on 24/07/2017.
 */

public class OfyHelper implements ServletContextListener {
    static {
        System.out.println("aaaaaaaaaaaaaaaaaaaaawala walaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        System.out.println("aaaaaaaaaaaaaaaaaaaaawala walaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        System.out.println("aaaaaaaaaaaaaaaaaaaaawala walaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");


        ObjectifyService.register(Merchant.class);
        ObjectifyService.register(Restaurant.class);
        ObjectifyService.register(Pharmacy.class);
        ObjectifyService.register(SuperMarket.class);
        ObjectifyService.register(SpecialMerchant.class);
        ObjectifyService.register(DessertsMerchant.class);
        ObjectifyService.register(MerchantCategory.class);
        ObjectifyService.register(Item.class);
        ObjectifyService.register(RestaurantItem.class);
        ObjectifyService.register(PharmacyItem.class);
        ObjectifyService.register(SpecialMerchantItem.class);
        ObjectifyService.register(SuperMarketItem.class);
        ObjectifyService.register(DessertsMerchant.class);
        ObjectifyService.register(Option.class);
        ObjectifyService.register(Choice.class);
        ObjectifyService.register(Review.class);
        ObjectifyService.register(Profile.class);
        ObjectifyService.register(Customer.class);
        ObjectifyService.register(Driver.class);
        ObjectifyService.register(DeliveryRequest.class);
        ObjectifyService.register(DeliveryItem.class);
        ObjectifyService.register(BlackListedProfile.class);
        ObjectifyService.register(JsonRawMerchant.class);
        ObjectifyService.register(Area.class);
        ObjectifyService.register(City.class);
        ObjectifyService.register(Inventory.class);
        ObjectifyService.register(ActualCategory.class);
        ObjectifyService.register(CustomerLocation.class);
        ObjectifyService.register(TestEntity.class);
        ObjectifyService.register(ConstantParams.class);
        ObjectifyService.register(UpdatableLocation.class);
        ObjectifyService.register(Offer.class);
        ObjectifyService.register(OfferMessages.class);
        ObjectifyService.register(ReviewStats.class);
        ObjectifyService.register(MerchantStats.class);
        ObjectifyService.register(DriverStats.class);
        ObjectifyService.register(DeliveryRequestChecker.class);
        ObjectifyService.register(UserPrivileges.class);

        System.out.println("registered");

    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
