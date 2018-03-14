package backend.apis;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiCacheControl;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethodCacheControl;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import backend.cityArea.Area;
import backend.cityArea.City;
import backend.cityArea.CityView;
import backend.deliveryRequests.DeliveryRequest;
import backend.deliveryRequests.JDeliveryRequest;
import backend.deliveryRequests.clientWrappers.CustomerDeliveryRequestView;
import backend.deliveryRequests.clientWrappers.DeliveryTimeline;
import backend.general.BlackListedProfile;
import backend.helpers.Constants;
import backend.helpers.CursorHelper;
import backend.helpers.FireBaseHelper;
import backend.helpers.returnWrappers.BooleanWrapper;
import backend.helpers.returnWrappers.LongWrapper;
import backend.helpers.returnWrappers.StringWrapper;
import backend.merchants.Item;
import backend.merchants.ItemView;
import backend.merchants.Merchant;
import backend.merchants.MerchantCategory;
import backend.merchants.MerchantView;
import backend.merchants.Option;
import backend.merchants.OptionsView;
import backend.merchants.inventory.Inventory;
import backend.merchants.inventory.InventoryView;
import backend.merchants.pharmacy.Pharmacy;
import backend.merchants.restaurant.Restaurant;
import backend.merchants.superMarket.SuperMarket;
import backend.monitor.DeliveryRequestChecker;
import backend.offers.Offer;
import backend.offers.OfferApplied;
import backend.profiles.Profile;
import backend.profiles.customer.Customer;
import backend.reviews.MerchantReviewsViews;
import backend.reviews.Review;
import backend.reviews.ReviewsTypes;
import backend.views.MenuView;
import backend.views.MerchantViewWithMenu;

import static backend.helpers.UtilityHelper.toKeys;
import static backend.profiles.Profile.getProfileByID;
import static backend.reviews.ReviewsTypes.CustomerReviewedMerchant;
import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 19/08/2017.
 */
@Api(
        name = "customerApi",
        version = "v1",

        scopes = {Constants.EMAIL_SCOPE},
        cacheControl = @ApiCacheControl(
                type = ApiCacheControl.Type.PUBLIC,
                maxAge = 60
        ),
        clientIds = {Constants.WEB_CLIENT_ID,
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE}
)

public class CustomerApi {
    private static CustomerApi customerApiInstance;

    public CustomerApi() {
    }

    public static CustomerApi getApiSingleton() {
        if (customerApiInstance == null) {
            customerApiInstance = new CustomerApi();
            return customerApiInstance;
        }
        return customerApiInstance;
    }

    @ApiMethod(name = "createCustomer")
    public Customer createCustomer(@Named("name") String name,
                                   @Named("firebaseUid") String firebaseUid,
                                   @Named("regToken") String regToken,
                                   @Named("phone") String phone) {

        // // TODO: 08/12/2017  driver signs up as a customer
        Customer customer = null;
        List<Profile> customers = Customer.getByPhone(phone);
        if (customers.size() == 0) {
            boolean isInBlackList = BlackListedProfile.isInBlackList(phone, firebaseUid, regToken);
            System.out.println("isInBlackList = " + isInBlackList);
            if (isInBlackList) {
                return null;
            } else {
                customer = new Customer(name, firebaseUid, regToken, phone);
                //saving profile is implied in addRegToken inside the constructor
            }

        } else {
            //registered with the same phone number
            customer = (Customer) customers.get(0);
            if (customer.isInBlackList()) {
                return null;
            }
            customer.addRegToken(regToken); //saves from within
        }
        return customer;
    }

    @ApiMethod(name = "getCustomerIdByFirebaseUid")
    public LongWrapper getCustomerIdByFirebaseUid(@Named("firebaseUid") String firebaseUid) {
        return new LongWrapper(Customer.getProfileIdByFirebaseUid(firebaseUid));
    }

    @ApiMethod(name = "getCustomerByFirebaseUid")
    public Customer getCustomerByFirebaseUid(@Named("firebaseUid") String firebaseUid) {
        return (Customer) Customer.getProfileByFirebaseUid(firebaseUid);
    }

    @ApiMethod(name = "signIn")
    public Customer signIn(@Named("customerID") Long customerID, @Named("regToken") String regToken) {
        getProfileByID(customerID).addRegToken(regToken);
        return null;
    }


    @ApiMethod(name = "signOut")
    public Customer signOut(@Named("customerID") Long customerID, @Named("regToken") String regToken) {
        getProfileByID(customerID).removeRegToken(regToken);
        return null;
    }


    @ApiMethod(name = "changeCurrentArea")
    public Customer changeCurrentArea(@Named("customerId") Long cusomerId, @Named("Area") Long areaId) {
        Customer customer = (Customer) Customer.getProfileByID(cusomerId);
        customer.setAreaId(areaId);
        return customer;
    }


    @ApiMethod(name = "putCustomerInBlackList")
    public StringWrapper putCustomerInBlackList(@Named("customerId") Long customerId, @Named("whyBlackListed") String whyBlackListed) {
        Profile customer = Customer.getProfileByID(customerId);
        BlackListedProfile.putInBlackList(customer, whyBlackListed);
        return new StringWrapper("got blacklisted");
    }


    @ApiMethod(name = "addMerchantToFavourite", path = "addMerchantToFavourite")
    public BooleanWrapper addMerchantToFavourite(@Named("customerId") Long customerId,
                                                 @Named("merchantId") Long merchantId) {
        if (Profile.doesProfileExist(customerId)) {
            boolean incrementMerchantFavourite = Customer.addMerchantToFavourite(customerId, merchantId); //update customer
            if (incrementMerchantFavourite)
                Merchant.addedToFavourite(customerId, merchantId); //update merchant

            return new BooleanWrapper(incrementMerchantFavourite);
        } else {
            return new BooleanWrapper(false, 1, "not signed in");
        }
    }


    @ApiMethod(name = "removeMerchantFromFavourite", path = "removeMerchantFromFavourite")
    public BooleanWrapper removeMerchantFromFavourite(@Named("customerId") Long customerId,
                                                      @Named("merchantId") Long merchantId) {
        if (Profile.doesProfileExist(customerId)) {
            boolean removed = Customer.removeMerchantFromFavourite(customerId, merchantId); //update customer
            if (removed)
                Merchant.removedFromFavourite(customerId, merchantId);

            return new BooleanWrapper(removed);
        } else {
            return new BooleanWrapper(false, 1, "not signed in");
        }
    }


    @ApiMethod(name = "getMyFavouriteMerchants", path = "getMyFavouriteMerchants")
    public List<MerchantView> getMyFavouriteMerchants(@Named("lang") String lang,
                                                      @Named("customerID") Long customerID,
                                                      @Named("areaId") Long areaId,
                                                      @Named("customerId") Long customerId) {
        Customer customer = (Customer) Customer.getProfileByID(customerId);
        List<Long> favouriteMerchantsIds = customer.favouriteMerchants;
        System.out.println(favouriteMerchantsIds);
        Map<Key<Merchant>, Merchant> merchantsMap = ofy().load().keys(toKeys(favouriteMerchantsIds, Merchant.class));
        List<Merchant> merchants = new ArrayList<>(merchantsMap.values());
        return MerchantView.getListOfMerchantsViews(lang, areaId, merchants, customerID);
    }

    @ApiMethod(name = "getListOfMerchantViewsByName")
    public List<MerchantView> getListOfMerchantViewsByName(@Named("lang") String lang,
                                                           @Named("name") String name,
                                                           @Named("customerId") Long customerId,
                                                           @Named("areaId") Long areaId,
                                                           @Named("limit") int limit) {

        List<Merchant> merchantList = Merchant.getMerchantByName(lang, name)
                .filter("supportedAreasListIds", areaId)
                .limit(limit)
                .list();
        return MerchantView.
                getListOfMerchantsViews(lang, areaId, merchantList, customerId);
    }


    @ApiMethod(name = "getMerchantByID")
    public Merchant getMerchantByID(@Named("merchantID") Long merchantID) {
        return Merchant.getMerchantByID(merchantID);
    }

    @ApiMethod(name = "getMerchantViewByID")
    public MerchantView getMerchantViewByID(@Named("lang") String lang,
                                            @Named("customerID") Long customerID,
                                            @Named("merchantID") Long merchantID,
                                            @Named("areaId") Long areaId) {
        return new MerchantView(lang, areaId, Merchant.getMerchantByID(merchantID), customerID);
    }


    @ApiMethod(name = "getMenuByMerchantID")
    public MenuView getMenuByMerchantID(@Named("lang") String lang, @Named("merchantID") Long merchantID) {
        //todo test this vs making menu view an Entity and adding its ID to merchant
        MenuView menuView = new MenuView(lang, Merchant.getMerchantByID(merchantID));
        return menuView;
    }

    @ApiMethod(name = "getPharmacy", path = "getPharmacy")
    public MerchantViewWithMenu getPharmacy(@Named("lang") String lang,
                                            @Named("customerId") Long customerId,
                                            @Named("areaId") Long areaId) {
        Pharmacy pharmacy = null;
        List<Pharmacy> pharmaciesInArea = ofy().load().type(Pharmacy.class)
                .filter("supportedAreasListIds", areaId)
                .list();

        if (pharmaciesInArea.size() != 0) {
            pharmacy = pharmaciesInArea.get(0);
        } else {
            return null;
        }

        // TODO: 04/02/2018  figure another way of getting pharmacy in city, without triggering null pointer exception when getting area-specific info
        /*else {
            List<Pharmacy> pharmaciesInCity = ofy().load().type(Pharmacy.class)
                    .filter("cityId =", cityId)
                    .list();
            if (pharmaciesInCity.size() == 0) {
                return null;
            }
            pharmacy = pharmaciesInCity.get(0);
        }
*/
        //todo test this vs making menu view an Entity and adding its ID to merchant
        MerchantViewWithMenu merchantViewWithMenu = new MerchantViewWithMenu(lang, areaId, pharmacy, customerId);
        return merchantViewWithMenu;
    }


    @ApiMethod(name = "getSuperMarket", path = "getSuperMarket")
    public MerchantViewWithMenu getSuperMarket(@Named("lang") String lang,
                                               @Named("customerId") Long customerId,
                                               @Named("areaId") Long areaId) {
        SuperMarket superMarket = null;
        List<SuperMarket> superMarketsInArea = ofy().load().type(SuperMarket.class)
                .filter("supportedAreasListIds", areaId)
                .list();

        if (superMarketsInArea.size() != 0) {
            superMarket = superMarketsInArea.get(0);
        } else {
            return null;
        }
        //todo test this vs making menu view an Entity and adding its ID to merchant
        MerchantViewWithMenu merchantViewWithMenu = new MerchantViewWithMenu(lang, areaId, superMarket, customerId);
        return merchantViewWithMenu;
    }


    @ApiMethod(name = "getItemsOfCategoryByID")
    public List<ItemView> getItemsOfCategoryByID(@Named("lang") String lang, @Named("categoryID") Long categoryID) {
        MerchantCategory category = MerchantCategory.getCategoryByID(categoryID);
        if (category != null) {
            List<Item> items = category.getItems();
            return ItemView.toItemView(lang, items);
        } else return null;
    }


    @ApiMethod(name = "getOptionsOfItemByID")
    public List<OptionsView> getOptionsOfItemByID(@Named("lang") String lang, @Named("itemID") Long itemID) {
        List<Option> options = Item.getItemByID(itemID).getOptions();
        return OptionsView.toOptionsViews(lang, options);
    }


    @ApiMethod(name = "verifyOffer")
    public OfferApplied verifyOffer(@Named("lang") String lang,
                                    @Named("jDeliveryRequestJson") String jDeliveryRequestJson) {
        JDeliveryRequest jDeliveryRequest = new Gson().fromJson(jDeliveryRequestJson, JDeliveryRequest.class);
        jDeliveryRequest.correctPrices();
        if (jDeliveryRequest.coupon != null) {
            return Offer.applyOffer(lang, false, jDeliveryRequest);
        }
        boolean isArabic = lang.trim().equalsIgnoreCase("ar");
        String message = "no coupon was entered!";
        return new OfferApplied(false, message);
    }

    @ApiMethod(name = "sendDeliveryRequest")
    public DeliveryTimeline sendDeliveryRequest(@Named("jDeliveryRequestJson") String jDeliveryRequestJson) throws IOException {

        JDeliveryRequest jDeliveryRequest = new Gson().fromJson(jDeliveryRequestJson, JDeliveryRequest.class);
        jDeliveryRequest.correctPrices();
        if (jDeliveryRequest.coupon != null) {
            OfferApplied offerApplied = Offer.applyOffer("en", true, jDeliveryRequest);
            if (offerApplied.success) {
                jDeliveryRequest.chargeAfterDiscount = offerApplied.chargeAfterDiscount;
            }
        }
        DeliveryRequest deliveryRequest = new DeliveryRequest(jDeliveryRequest);
        System.out.println(deliveryRequest);

        DeliveryTimeline deliveryTimeline = new DeliveryTimeline(deliveryRequest);
        final Queue queue = QueueFactory.getQueue("driverQueue");
        queue.add(TaskOptions.Builder.withUrl("/admin/GetTheNearestDriverServlet")
                .param("deliveryRequestId", String.valueOf(deliveryRequest.id)));

        //the merchant client App parses the delivery request id and calls getById
        return deliveryTimeline;
    }


    @ApiMethod(name = "getListOfRestaurantsByCategoryNameOrderedBy",
            path = "getListOfRestaurantsByCategoryNameOrderedBy")
    public CollectionResponse<MerchantView> getListOfMerchantsByCategoryNameOrderedBy
            (@Named("lang") String lang,
             @Named("customerID") Long customerID,
             @Named("areaId") Long areaId,
             @Named("categoryId") Long categoryId,
             @Named("cursorStr") String cursorStr,
             @Named("orderByOption") String orderByOption,
             @Named("limitNumber") int limitNumber) {

        if (orderByOption.trim().equalsIgnoreCase("default")) {
            orderByOption = "-defaultOrder";
        }
        if (orderByOption.trim().equalsIgnoreCase("rating")) {
            orderByOption = "-rating";
        }

        Query<Restaurant> query = ofy().load().type(Restaurant.class);
        query = query
                .filter("supportedAreasListIds", areaId)
                .order(orderByOption).limit(limitNumber)
                .limit(limitNumber);
        if (categoryId != 0) {
            query = query.filter("actualCategoriesIds", categoryId);
        }

        cursorStr = cursorStr.toLowerCase().trim().equals("null") ? null : cursorStr;
        CursorHelper<Restaurant> cursorHelper = new CursorHelper<>(Restaurant.class);
        CollectionResponse<Restaurant> merchantResponse =
                cursorHelper.queryAtCursor(query, cursorStr);

        List<MerchantView> result = MerchantView
                .getListOfMerchantsViews(lang, areaId, (List<Restaurant>) merchantResponse.getItems(), customerID);
        Collections.sort(result);
        CollectionResponse<MerchantView> response = cursorHelper.buildCollectionResponse(result);
        return response;
    }


    @ApiMethod(name = "getFeaturedRestaurants", path = "getAdvertisedRestaurants")
    public List<MerchantView> getAdvertisedRestaurants
            (@Named("lang") String lang,
             @Named("customerID") Long customerID,
             @Named("areaId") Long areaId) {
        List<Restaurant> restaurants = ofy().load().type(Restaurant.class)
                .filter("supportedAreasListIds", areaId)
                .filter("featured", true)
                .order("-defaultOrder")
                .list();

        List<MerchantView> result = MerchantView
                .getListOfMerchantsViews(lang, areaId, restaurants, customerID);
        return result;
    }

    @ApiMethod(name = "getListOfItemsByCategoryNameOrderedBy", path = "getListOfItemsByCategoryNameOrderedBy")
    public CollectionResponse<Item> getListOfItemsByCategoryNameOrderedBy
            (@Named("categoryId") Long categoryId,
             @Named("areaId") String area,
             @Named("cursorStr") String cursorStr,
             @Named("orderByOption") String orderByOption,
             @Named("limitNumber") int limitNumber) {

        Query<Item> query = ofy().load().type(Item.class)
                .filter("actualCategoriesIds", categoryId)
                .filter("supportedAreasList", area.toLowerCase().trim())
                .order(orderByOption).limit(limitNumber);


        cursorStr = cursorStr.toLowerCase().equals("null") ? null : cursorStr;

        CursorHelper<Item> cursorHelper = new CursorHelper<>(Item.class);
        CollectionResponse<Item> response =
                cursorHelper.queryAtCursor(query, cursorStr);

        return response;
    }

    @ApiMethod(name = "getInventory")
    public InventoryView getInventory(@Named("lang") String lang) {

        return new InventoryView(lang, Inventory.getInventory());
    }

    @ApiMethod(name = "getFeedOfTopMerchants")
    public CollectionResponse<MerchantView> getFeedOfTopMerchants(
            @Named("lang") String lang,
            @Named("customerID") Long customerID,
            @Named("areaId") Long areaId,
            @Named("cursorStr") String cursorStr,
            @Named("orderByOption") String orderByOption,
            @Named("limitNumber") int limitNumber) {

        if (orderByOption.trim().equalsIgnoreCase("default")) {
            orderByOption = "defaultOrder";
        }
        Query<Merchant> query = ofy().load().type(Merchant.class)
                .filter("browsable", true)
                .filter("supportedAreasListIds", areaId)
                .order(orderByOption)
                .limit(limitNumber);
        cursorStr = cursorStr.toLowerCase().equals("null") ? null : cursorStr;
        CursorHelper<Merchant> cursorHelper = new CursorHelper<>(Merchant.class);
        CollectionResponse<Merchant> merchantResponse =
                cursorHelper.queryAtCursor(query, cursorStr);

        List<MerchantView> result = MerchantView
                .getListOfMerchantsViews(lang, areaId, (List<Merchant>) merchantResponse.getItems(), customerID);
        CollectionResponse<MerchantView> response = cursorHelper.buildCollectionResponse(result);
        return response;
    }

    @ApiMethod(name = "customerAcknowledgedDeliveryCompleted")
    public BooleanWrapper customerAcknowledgedDeliveryCompleted(@Named("deliveryRequestId") Long deliveryRequestId,
                                                                @Named("isCompleted") boolean isCompleted) throws IOException {
        if (isCompleted == false) {
            DeliveryRequestChecker.addDeliveryToBeChecked(deliveryRequestId);
            DeliveryRequestChecker.notifySpecialDriversInCityToCheck(deliveryRequestId);
            return new BooleanWrapper(false, "uncheckedDeliveryRequest");
        } else {
            UnexposedApiMethods.deliveryCompleted(deliveryRequestId, false);
            return new BooleanWrapper(true, "completed");
        }
    }

    // customer reviews api

    @ApiMethod(name = "getMerchantReviews")
    public MerchantReviewsViews getMerchantReviews(@Named("merchantId") Long merchantId) {
        return Merchant.getReviews(merchantId);
    }

    @ApiMethod(name = "getMerchantsThatCustomerReviewed")
    public List<MerchantView> getMerchantsThatCustomerReviewed(@Named("customerId") Long customerId) {
        //// TODO: 04/02/2018
        return null;
    }


    @ApiMethod(name = "reviewDriver")
    public Review reviewDriver(@Named("customerId") Long customerId,
                               @Named("deliveryRequestId") Long deliveryRequestId,
                               @Named("anonymous") boolean anonymous,
                               @Named("rating") int rating, @Named("comment") String comment) {

        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestId);
        Long driverId = deliveryRequest.driverId;
        return Review.submitReview(ReviewsTypes.CustomerReviewedDriver, customerId, driverId, anonymous, comment, rating);
    }


    @ApiMethod(name = "reviewMerchantAfterDeliveryRequest")
    public Review reviewMerchantAfterDeliveryRequest(@Named("customerId") Long customerId,
                                                     @Named("deliveryRequestId") Long deliveryRequestId,
                                                     @Named("anonymous") boolean anonymous,
                                                     @Named("rating") int rating, @Named("comment") String comment) {

        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestId);
        Long merchantId = deliveryRequest.merchantId;
        if (!DeliveryRequest.didCustomerEverOrderFromMerchant(customerId, merchantId)) {
            return null;
        }
        Review review = Review.submitReview(CustomerReviewedMerchant, customerId,
                merchantId, anonymous, comment, rating);

        final Queue queue = QueueFactory.getQueue("reviewQueue");
        queue.add(TaskOptions.Builder.withUrl("/admin/ReviewServlet")
                .param("reviewId", String.valueOf(review.id))
                .param("deliveryRequestId", String.valueOf(deliveryRequest.id))
                .param("customerId", String.valueOf(customerId))
        );

        // submit review
        // update merchant stats
        // update items stats
        // update customer stats


        return review;
    }


    @ApiMethod(name = "customerLikesOrDislikesMerchantReview")
    public Review customerLikesOrDislikesMerchantReview(@Named("customerId") Long customerId,
                                                        @Named("reviewId") Long reviewId, @Named("like") int likeDislikeHelpful) {
        Review review = Review.getByID(reviewId);
        switch (likeDislikeHelpful) {
            case 0:
                review.gotLiked(customerId);
                break;
            case 1:
                review.gotDisliked(customerId);
                break;
            case 2:
                review.markedAsHelpful(customerId);
                break;
        }

        return review;
    }

    @ApiMethod(name = "sendNoti")
    public StringWrapper sendNoti(@Named("regToken") String regToken, @Named("message") String message) {
        return new StringWrapper(FireBaseHelper.sendNotification(regToken, message));
    }


    @ApiMethod(name = "getRecentDeliveryRequests", path = "getRecentDeliveryRequests")
    public List<CustomerDeliveryRequestView> getRecentDeliveryRequests(@Named("lang") String lang
            , @Named("customerID") Long customerID) {

        List<DeliveryRequest> deliveryRequests = ofy().load().type(DeliveryRequest.class)
                .filter("customerId =", customerID)
                .order("-creationDate")
                .list();

        return CustomerDeliveryRequestView.toViewList(lang, deliveryRequests);
    }

    @ApiMethod(name = "getDeliveryRequestById")
    public CustomerDeliveryRequestView getDeliveryRequestViewById(@Named("lang") String lang,
                                                                  @Named("customerId") Long customerId,
                                                                  @Named("deliveryRequestId") Long deliveryRequestId) {

        return new CustomerDeliveryRequestView().setValues(lang, DeliveryRequest.getById(deliveryRequestId));
    }


    @ApiMethod(name = "getActiveDeliveryRequests", path = "getActiveDeliveryRequests")
    public List<CustomerDeliveryRequestView> getActiveDeliveryRequests(@Named("lang") String lang
            , @Named("customerID") Long customerID) {

        List<DeliveryRequest> deliveryRequests = ofy().load().type(DeliveryRequest.class)
                .filter("customerId =", customerID)
                .filter("orderDelivered =", false)
                .filter("canceled =", false)
                .order("-creationDate")
                .list();

        return CustomerDeliveryRequestView.toViewList(lang, deliveryRequests);
    }


    @ApiMethod(name = "getSupportedCities", path = "getSupportedCities")
    public List<CityView> getSupportedCities() {
        List<CityView> cityViews = new ArrayList<>();
        List<City> supportedCitys = City.getSupportedCities();
        for (City city : supportedCitys) {
            List<Area> areasInCity = Area.getAreasIncity(city.id);
            cityViews.add(new CityView(city, areasInCity));
        }
        return cityViews;
    }

    //dhKFesUHNtw:APA91bH3IxX5sT-EpcBsNY3GQlZDYF9n7nXAM1ZhthT3et0edxZnRr5-hsH2nRMvaQS01HL7ZYMmDmosRkjdlP0z4A791WWSVdb5akx5mbemg1rJlUtf8qAR-j4jgxwehxUghGEILGm7
    //testing methods
 /*@ApiMethod(name = "getListOfRestaurantsViewsOrderedBy",path = "getListOfRestaurantsViewsOrderedBy")
 public CollectionResponse<MerchantView> getListOfRestaurantsViewsOrderedBy(
         @Named("cursorStr") @Nullable String cursorStr,
         @Named("merchantsOrderBy") MerchantsOrderBy merchantsOrderBy,
         @Named("limitNumber") int limitNumber){
     return getListOfMerchantsViewsOrderedBy(cursorStr,merchantsOrderBy,MerchantTypes.RESTAURANT,limitNumber);
 }*/


    @ApiMethod(name = "testClientLib")
    public JDeliveryRequest testClientLib() {
        return null;
    }

}
