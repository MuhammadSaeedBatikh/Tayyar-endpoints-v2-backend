/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package backend.apis;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.firebase.auth.FirebaseAuth;
import com.googlecode.objectify.cmd.Query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import backend.TestEntity;
import backend.cityArea.City;
import backend.deliveryRequests.DeliveryRequest;
import backend.general.ConstantParams;
import backend.general.UserPrivileges;
import backend.helpers.Constants;
import backend.helpers.CursorHelper;
import backend.helpers.returnWrappers.BooleanWrapper;
import backend.helpers.returnWrappers.StringWrapper;
import backend.merchants.Choice;
import backend.merchants.Item;
import backend.merchants.Merchant;
import backend.merchants.MerchantCategory;
import backend.merchants.Option;
import backend.merchants.dessertsMerchant.DessertsMerchant;
import backend.merchants.dessertsMerchant.DessertsMerchantItem;
import backend.merchants.inventory.Inventory;
import backend.merchants.pharmacy.Pharmacy;
import backend.merchants.pharmacy.PharmacyItem;
import backend.merchants.restaurant.Restaurant;
import backend.merchants.restaurant.RestaurantItem;
import backend.merchants.specialMerchant.SpecialMerchant;
import backend.merchants.specialMerchant.SpecialMerchantItem;
import backend.merchants.superMarket.SuperMarket;
import backend.merchants.superMarket.SuperMarketItem;
import backend.monitor.DeliveryRequestCheckView;
import backend.offers.Offer;
import backend.offers.OfferMessages;
import backend.profiles.driver.Driver;
import backend.stats.DriverStats;
import backend.stats.MerchantStats;

import static backend.merchants.Merchant.getMerchantByID;
import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "merchantApi",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID, Constants.API_EXPLORER},
        audiences = {Constants.ANDROID_AUDIENCE}
)
public class MerchantApi {
    private static MerchantApi merchantApiInstance;

    public MerchantApi() {
    }

    public static MerchantApi getApiSingleton() {
        if (merchantApiInstance == null) {
            merchantApiInstance = new MerchantApi();
            return merchantApiInstance;
        }
        return merchantApiInstance;
    }

    /**
     * todo change return types to wrappers after testing
     */

    @ApiMethod(name = "getMerchantById")
    public Merchant getMerchantById(User user, @Named("merchantId") Long merchantId) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        return getMerchantByID(merchantId);
    }

    @ApiMethod(name = "checkAccessUser")
    public StringWrapper checkAccess(User user) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        TestEntity log = new TestEntity();
        log.log(user);
        UserPrivileges.createFirstOwner(user);
        return new StringWrapper(user.toString());
    }


    @ApiMethod(name = "checkAccess2")
    public StringWrapper checkAccess2(User user) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        UserService userService = UserServiceFactory.getUserService();
        TestEntity log = new TestEntity();
        com.google.appengine.api.users.User currentUser = userService.getCurrentUser();
        log.log(currentUser);
        String email = currentUser.getEmail();
        log.log(email);
        String userId = currentUser.getUserId();
        log.log(userId);

        String authDomain = currentUser.getAuthDomain();

        String info = "isUserAdmin= " + " emailS " + email + "\n userIdS= " + userId +
                "authDomain= " + authDomain;
        return new StringWrapper(info);
    }

    @ApiMethod(name = "createRestaurant")
    public Restaurant createRestaurant(User user, @Named("nameAr") String nameAr, @Named("nameEn") String nameEn,
                                       @Named("phone") final String phone,
                                       @Named("imageURL") String imageURL
    ) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        List<String> phones = new ArrayList<String>() {{
            add(phone);
        }};
        Restaurant restaurant = new Restaurant(nameAr, nameEn, phones, imageURL);
        restaurant.saveMerchant();
        return restaurant;
    }

    @ApiMethod(name = "createPharmacy")
    public Pharmacy createPharmacy(User user, @Named("nameAr") String nameAr, @Named("nameEn") String nameEn,
                                   @Named("phone") final String phone,
                                   @Named("imageURL") String imageURL
    ) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        List<String> phones = new ArrayList<String>() {{
            add(phone);
        }};
        Pharmacy pharmacy = new Pharmacy(nameAr, nameEn, phones, imageURL);
        pharmacy.saveMerchant();
        return pharmacy;
    }

    @ApiMethod(name = "createSuperMarket")
    public SuperMarket createSuperMarket(User user, @Named("nameAr") String nameAr, @Named("nameEn") String nameEn,
                                         @Named("phone") List<String> phone,
                                         @Named("imageURL") String imageURL
    ) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        SuperMarket superMarket = new SuperMarket(nameAr, nameEn, phone, imageURL);
        superMarket.saveMerchant();
        return superMarket;
    }

    @ApiMethod(name = "createDessertMerchant")
    public DessertsMerchant createDessertsMerchant(User user, @Named("nameAr") String nameAr, @Named("nameEn") String nameEn,
                                                   @Named("phone") List<String> phone,
                                                   @Named("imageURL") String imageURL
    ) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        DessertsMerchant dessertsMerchant = new DessertsMerchant(nameAr, nameEn, phone, imageURL);
        dessertsMerchant.saveMerchant();
        return dessertsMerchant;
    }

    @ApiMethod(name = "createSpecialMerchant")
    public SpecialMerchant createSpecialMerchant(User user, @Named("nameAr") String nameAr, @Named("nameEn") String nameEn,
                                                 @Named("phone") final String phone,
                                                 @Named("imageURL") String imageURL
    ) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        List<String> phones = new ArrayList<String>() {{
            add(phone);
        }};
        SpecialMerchant specialMerchant = new SpecialMerchant(nameAr, nameEn, phones, imageURL);
        specialMerchant.saveMerchant();
        return specialMerchant;
    }

    //==================================================

    @ApiMethod(name = "createCategory")
    public MerchantCategory createCategory(User user, @Named("nameAr") String nameAr, @Named("nameEn") String nameEn,
                                           @Named("descriptionAr") String descriptionAr,
                                           @Named("descriptionEn") String descriptionEn,
                                           @Named("imageURL") String imageURL
    ) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        MerchantCategory merchantCategory = new MerchantCategory(nameAr, nameEn, descriptionAr, descriptionEn, imageURL);
        merchantCategory.saveCategory();
        return merchantCategory;
    }

    //===========================================
    @ApiMethod(name = "createInventory")
    public Inventory createInventory(User user, @Named("categoriesJson") String categoriesJson) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        return UnexposedApiMethods.createInventory(categoriesJson);
    }

    @ApiMethod(name = "createRestaurantItem")
    public RestaurantItem createRestaurantItem(User user, @Named("nameAr") String nameAr, @Named("nameEn") String nameEn,
                                               @Named("basePrice") double basePrice) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        RestaurantItem item = new RestaurantItem(nameAr, nameEn, basePrice);
        item.saveItem();
        return item;
    }

    @ApiMethod(name = "createPharmacyItem")
    public PharmacyItem createPharmacyItem(User user, @Named("nameAr") String nameAr, @Named("nameEn") String nameEn,
                                           @Named("basePrice") double basePrice) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        PharmacyItem item = new PharmacyItem(nameAr, nameEn, basePrice);
        item.saveItem();
        return item;
    }

    @ApiMethod(name = "createSuperMarketItem")
    public SuperMarketItem createSuperMarketItem(User user, @Named("nameAr") String nameAr, @Named("nameEn") String nameEn,
                                                 @Named("basePrice") double basePrice) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        SuperMarketItem item = new SuperMarketItem(nameAr, nameEn, basePrice);
        item.saveItem();
        return item;
    }

    @ApiMethod(name = "createDessertsMerchantItem")
    public DessertsMerchantItem createDessertsMerchantItem(User user, @Named("nameAr") String nameAr, @Named("nameEn") String nameEn,
                                                           @Named("basePrice") double basePrice) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        DessertsMerchantItem item = new DessertsMerchantItem(nameAr, nameEn, basePrice);
        item.saveItem();
        return item;
    }

    @ApiMethod(name = "createSpecialMerchantItem")
    public SpecialMerchantItem createSpecialMerchantItem(User user, @Named("nameAr") String nameAr, @Named("nameEn") String nameEn,
                                                         @Named("basePrice") double basePrice) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        SpecialMerchantItem item = new SpecialMerchantItem(nameAr, nameEn, basePrice);
        item.saveItem();
        return item;
    }

    //=============================================

    @ApiMethod(name = "createOption")
    public Option createOption(User user, @Named("nameAr") String nameAr, @Named("nameEn") String nameEn,
                               @Named("descriptionAr") String descriptionAr,
                               @Named("descriptionEn") String descriptionEn,
                               @Named("required") boolean required) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        Option option = new Option(nameAr, nameEn, required, descriptionAr, descriptionEn);
        option.saveOption();
        return option;
    }

    @ApiMethod(name = "createChoice")
    public Choice createChoice(User user, @Named("nameAr") String nameAr, @Named("nameEn") String nameEn,
                               @Named("descriptionAr") String descriptionAr,
                               @Named("descriptionEn") String descriptionEn,
                               @Named("addedPrice") double addedPrice) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        Choice choice = new Choice(nameAr, nameEn, addedPrice, descriptionAr, descriptionEn);
        choice.saveChoice();
        return choice;
    }


    @ApiMethod(name = "addMenuCategoryToMerchant")
    public Merchant addMenuCategoryToMerchant(User user, @Named("merchantID") Long merchantID,
                                              @Named("categoryID") Long categoryID) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        Merchant merchant = getMerchantByID(merchantID);
        MerchantCategory category = MerchantCategory.getCategoryByID(categoryID);
        category.setParentMerchantId(merchantID);
        merchant.addMenuCategory(categoryID);
        return merchant;
    }


    @ApiMethod(name = "addCategoryToInventoryCategories")
    public Inventory addCategoryToInventoryCategories(User user, @Named("categoryName") String categoryName,
                                                      @Named("flag") String type) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        Inventory inventory = Inventory.getInventory();
        return inventory;
    }

    @ApiMethod(name = "addItemToCategory")
    public MerchantCategory addItemToCategory(User user, @Named("categoryID") Long categoryID, @Named("itemID") Long itemID) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        MerchantCategory merchantCategory = MerchantCategory.getCategoryByID(categoryID);
        Item item = Item.getItemByID(itemID);
        item.setParentCategoryId(categoryID);
        item.setParentMerchantId(merchantCategory.parentMerchantId);
        merchantCategory.addItem(itemID);
        return merchantCategory;
    }


    @ApiMethod(name = "addOptionToItem")
    public Item addOptionTotItem(User user, @Named("itemID") Long itemID,
                                 @Named("optionID") Long optionID) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        Item item = Item.getItemByID(itemID);
        Option option = Option.getOptionByID(optionID);
        option.setParentItemId(itemID);
        option.setParentMerchantId(item.parentMerchantId);
        item.addOption(optionID);
        return item;
    }


    @ApiMethod(name = "addChoiceToOption")
    public Option addChoiceToOption(User user, @Named("optionID") Long optionID,
                                    @Named("choiceID") Long choiceID) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        Option option = Option.getOptionByID(optionID);
        Choice choice = Choice.getChoiceByID(choiceID);
        choice.setParentMerchantId(option.parentMerchantId);
        choice.setParentOption(optionID);
        option.addChoice(choiceID);
        return option;
    }


    @ApiMethod(name = "getItemsOfCategoryByID")
    public List<Item> getItemsOfCategoryByID(User user, @Named("categoryID") Long categoryID) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        return MerchantCategory.getCategoryByID(categoryID).getItems();
    }


    @ApiMethod(name = "getById")
    public DeliveryRequest getDeliveryRequestByID(User user, @Named("deliveryRequestID") Long deliveryRequestID) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        return DeliveryRequest.getById(deliveryRequestID);
    }


    @ApiMethod(name = "merchantAcceptsDeliveryRequest")
    public DeliveryRequest merchantAcceptsDeliveryRequest(User user, @Named("deliveryRequestID") Long deliveryRequestID) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestID);
        final Queue queue = QueueFactory.getQueue("driverQueue");
        queue.add(TaskOptions.Builder.withUrl("/admin/GetTheNearestDriverServlet").
                param("deliveryRequestId", deliveryRequest.toString()));
        return deliveryRequest;
    }

    public void signUpWithFirebase() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
    }

    @ApiMethod(name = "uploadJsonMerchant")
    public StringWrapper uploadJsonMerchant(User user, @Named("newMerchant") boolean newMerchant,
                                            @Named("JMerchantJSON") String jMerchantJSON) throws Exception {
        UserPrivileges.isAdmin(user);
        return UnexposedApiMethods.uploadJsonMerchant(newMerchant, jMerchantJSON);
    }


    @ApiMethod(name = "updateMerchantPrices", path = "updateMerchantPrices")
    public StringWrapper updateMerchantPrices(User user, @Named("merchantId") Long merchantId
            , @Named("updateType") String updateType
            , @Named("addedPrice") double addedPrice) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        final Queue queue = QueueFactory.getQueue("UpdateMerchantPricesQueue");
        queue.add(TaskOptions.Builder.withUrl("/admin/UpdateMerchantPricesServlet")
                .param("updateType", updateType)
                .param("addedPrice", String.valueOf(addedPrice))
                .param("merchantId", String.valueOf(merchantId)));

        return new StringWrapper("updating ...");
    }


    @ApiMethod(name = "deleteMerchant")
    public StringWrapper deleteMerchant(User user, @Named("merchantId") Long merchantId) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        return UnexposedApiMethods.deleteMerchant(merchantId);
    }

    @ApiMethod(name = "uploadCityData", path = "uploadCityData")
    public StringWrapper uploadCityData(User user, @Named("dataJson") String dataJson,
                                        @Named("support") boolean support) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        return UnexposedApiMethods.uploadCityData(dataJson, support);
    }

    @ApiMethod(name = "setConstantParams", path = "setConstantParams")
    public ConstantParams setConstantParams(User user, @Named("waitingTimeForDriverToAccept") int waitingTimeForDriverToAccept,
                                            @Named("checkTime") int checkTime,
                                            @Named("checkCount") int checkCount,
                                            @Named("timeSliceForCheckingDriverState") int timeSliceForCheckingDriverState,
                                            @Named("maxDeliveryRequestsPerDriver") int maxDeliveryRequestsPerDriver,
                                            @Named("cityId") Long cityId,
                                            @Named("ourPercentagePerDeliveryFromDelivery") double ourPercentagePerDeliveryFromDelivery,
                                            @Named("customerSupportPhones") List<String> customerSupportPhones,
                                            @Nullable @Named("specialDriversIds") List<Long> specialDriversIds) throws UnauthorizedException {

        UserPrivileges.isAdmin(user);
        return UnexposedApiMethods.setConstantParams(waitingTimeForDriverToAccept, checkTime,
                checkCount, timeSliceForCheckingDriverState, maxDeliveryRequestsPerDriver, cityId, ourPercentagePerDeliveryFromDelivery,
                customerSupportPhones, specialDriversIds);
    }


    @ApiMethod(name = "addSpecialDriver", path = "addSpecialDriver")
    public ConstantParams addSpecialDriver(User user, @Named("cityId") Long cityId,
                                           @Named("specialDriverId") Long specialDriverId) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        ConstantParams constantParams = ConstantParams.getParamsByCityId(cityId);
        constantParams.addSpecialDriver(specialDriverId);
        return constantParams;
    }


    @ApiMethod(name = "supportCity", path = "supportCity")
    public StringWrapper supportCity(User user, @Named("areaId") Long cityId) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        City city = City.supportById(cityId);
        return new StringWrapper(city.toString());
    }


    @ApiMethod(name = "createOfferMessages", path = "createOfferMessages")
    public OfferMessages createOfferMessages(@Named("successMessageAr") String successMessageAr,
                                             @Named("successMessageEn") String successMessageEn,
                                             @Named("doesNotExistAr") String doesNotExistAr,
                                             @Named("doesNotExistEn") String doesNotExistEn,
                                             @Named("couponExpiredAr") String couponExpiredAr,
                                             @Named("couponExpiredEn") String couponExpiredEn,
                                             @Named("reachedMaxNumberLimitAr") String reachedMaxNumberLimitAr,
                                             @Named("reachedMaxNumberLimitEn") String reachedMaxNumberLimitEn,
                                             @Named("doseNotApplyToMerchantTypeAr") String doseNotApplyToMerchantTypeAr,
                                             @Named("doseNotApplyToMerchantTypeEn") String doseNotApplyToMerchantTypeEn,
                                             @Named("doseNotStartYetAr") String doseNotStartYetAr,
                                             @Named("doseNotStartYetEn") String doseNotStartYetEn,
                                             @Named("doseNotApplyToYourCityAr") String doseNotApplyToYourCityAr,
                                             @Named("doseNotApplyToYourCityEn") String doseNotApplyToYourCityEn,
                                             @Named("doseNotApplyToYourAreaAr") String doseNotApplyToYourAreaAr,
                                             @Named("doseNotApplyToYourAreaEn") String doseNotApplyToYourAreaEn,
                                             @Named("doseNotApplyToThisMerchantAr") String doseNotApplyToThisMerchantAr,
                                             @Named("doseNotApplyToThisMerchantEn") String doseNotApplyToThisMerchantEn,
                                             @Named("belowMinimumChargeAr") String belowMinimumChargeAr,
                                             @Named("belowMinimumChargeEn") String belowMinimumChargeEn,
                                             @Named("aboveMaximumChargeAr") String aboveMaximumChargeAr,
                                             @Named("aboveMaximumChargeEn") String aboveMaximumChargeEn,
                                             @Named("doseNotApplyToCustomerAr") String doseNotApplyToCustomerAr,
                                             @Named("doseNotApplyToCustomerEn") String doseNotApplyToCustomerEn,
                                             @Named("exceededMaxNumberToCustomerAr") String exceededMaxNumberToCustomerAr,
                                             @Named("exceededMaxNumberToCustomerEn") String exceededMaxNumberToCustomerEn) throws UnauthorizedException {
        // UserPrivileges.isAdmin(user);
        OfferMessages offerMessages = new OfferMessages(successMessageAr, successMessageEn,
                doesNotExistAr, doesNotExistEn,
                couponExpiredAr, couponExpiredEn,
                reachedMaxNumberLimitAr, reachedMaxNumberLimitEn,
                doseNotApplyToMerchantTypeAr, doseNotApplyToMerchantTypeEn,
                doseNotStartYetAr, doseNotStartYetEn,
                doseNotApplyToYourCityAr, doseNotApplyToYourCityEn,
                doseNotApplyToYourAreaAr, doseNotApplyToYourAreaEn,
                doseNotApplyToThisMerchantAr, doseNotApplyToThisMerchantEn,
                belowMinimumChargeAr, belowMinimumChargeEn,
                aboveMaximumChargeAr, aboveMaximumChargeEn,
                doseNotApplyToCustomerAr, doseNotApplyToCustomerEn,
                exceededMaxNumberToCustomerAr, exceededMaxNumberToCustomerEn);

        OfferMessages offerMessagesInDataStore = OfferMessages.getMessages();
        if (offerMessagesInDataStore != null) {
            offerMessages.id = offerMessagesInDataStore.id;
        }
        offerMessages.save();
        return offerMessages;
    }


    @ApiMethod(name = "createOffer", path = "createOffer")
    public Offer createOffer(User user, @Named("couponAppearsToCustomer") boolean couponAppearsToCustomer, // coupon doesn't appear to customer
                             @Named("coupon") String coupon,
                             @Named("discountType") int discountType,
                             @Named("discountValue") double discountValue,
                             @Named("defaultMessage") String defaultMessage,
                             @Named("description") String description,
                             @Named("startDate") Date startDate, @Named("endDate") Date endDate,
                             @Named("overallMaxNumberAllowed") int overallMaxNumberAllowed,
                             @Named("maxNumberAllowedPerCustomer") int maxNumberAllowedPerCustomer,
                             @Named("merchantTypes") List<String> merchantTypes, @Named("cityFlag") int cityFlag,
                             @Nullable @Named("cityIds") List<Long> cityIds, @Named("areaFlag") int areaFlag,
                             @Nullable @Named("areaIds") List<Long> areaIds, @Named("minimumCharge") double minimumCharge,
                             @Named("maximumCharge") double maximumCharge, @Named("merchantFlag") int merchantFlag,
                             @Nullable @Named("merchantIds") List<Long> merchantIds,
                             @Nullable @Named("excludedMerchantIds") List<Long> excludedMerchantIds,
                             @Named("customerFlag") int customerFlag,
                             @Nullable @Named("customersIds") List<Long> customersIds,
                             @Nullable @Named("excludedCustomersId") List<Long> excludedCustomersId) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        Offer offer = new Offer(coupon, discountType, discountValue, defaultMessage, description, startDate,
                endDate, overallMaxNumberAllowed, maxNumberAllowedPerCustomer, merchantTypes, cityFlag,
                cityIds, areaFlag, areaIds, minimumCharge, maximumCharge, merchantFlag, merchantIds, excludedMerchantIds,
                customerFlag, customersIds, excludedCustomersId);
        offer.save();
        if (!couponAppearsToCustomer) {
            // feature all merchant
            String message = "";
            if (discountType == 0) message = discountValue * 100 + "% discount";
            else if (discountType == 1) message = "-" + discountValue + " LE";
            else if (discountType == 2) message = "Free Delivery";
            for (Long merchantId : merchantIds) {
                featureMerchant(user, merchantId, message, coupon);
            }
        }
        System.out.println(offer);
        return offer;
    }


    @ApiMethod(name = "featureMerchant", path = "featureMerchant")
    public Merchant featureMerchant(User user, @Named("merchantId") Long merchantId,
                                    @Named("featuringMessage") String featuringMessage,
                                    @Named("coupon") String coupon) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        Merchant merchant = Merchant.getMerchantByID(merchantId);
        merchant.feature(featuringMessage, coupon);
        return merchant;
    }

    @ApiMethod(name = "unFeatureMerchant", path = "unFeatureMerchant")
    public Merchant unFeatureMerchant(User user, @Named("merchantId") Long merchantId) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        Merchant merchant = Merchant.getMerchantByID(merchantId);
        merchant.unFeature();
        return merchant;
    }


    @ApiMethod(name = "merchantPays", path = "merchantPays")
    public MerchantStats merchantPays(User user, @Named("merchantId") Long merchantId) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        return MerchantStats.merchantPays(merchantId);
    }

    @ApiMethod(name = "getMerchantStatistics", path = "getMerchantStatistics")
    public MerchantStats getMerchantStatistics(User user, @Named("merchantId") Long merchantId) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        return MerchantStats.getCompleteStats(merchantId);
    }

    @ApiMethod(name = "getMerchantsStatisticsInCity", path = "getMerchantsStatisticsInCity")
    public List<MerchantStats> getMerchantsStatisticsInCity(User user, @Named("cityId") Long cityId) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        return MerchantStats.getMerchantsStatisticsInCity(cityId);
    }

    @ApiMethod(name = "driverPays", path = "driverPays")
    public DriverStats driverPays(User user, @Named("driverId") Long driverId) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        return DriverStats.driverPays(driverId);
    }


    @ApiMethod(name = "getDriverStatistics", path = "getDriverStatistics")
    public DriverStats getDriverStatistics(User user, @Named("driverId") Long driverId) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        return DriverStats.getCompleteStats(driverId);
    }


    @ApiMethod(name = "getDriverStatisticsInCity", path = "getDriverStatisticsInCity")
    public List<DriverStats> getDriverStatisticsInCity(User user, @Named("cityId") Long cityId) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        return DriverStats.getDriversStatisticsInCity(cityId);
    }

    @ApiMethod(name = "cancelDeliveryRequest")
    public DeliveryRequest cancelDeliveryRequest(User user, @Named("deliveryRequestId") Long deliveryRequestId) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        DeliveryRequest deliveryRequest = DeliveryRequest.getById(deliveryRequestId);
        deliveryRequest.cancelDeliveryRequest();
        return deliveryRequest;
    }


    @ApiMethod(name = "setDriverCredit")
    public DriverStats addToDriverCredit(User user, @Named("driverId") Long driverId,
                                         @Named("amount") double amount,
                                         @Named("addOrSet") boolean addOrSet) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        DriverStats driverStats = DriverStats.getByDriverId(driverId);
        if (addOrSet) {
            //add
            driverStats.ourCredit += amount;
        } else {
            //set
            driverStats.ourCredit = amount;
        }
        driverStats.save();
        return driverStats;
    }

    @ApiMethod(name = "getDeliveryRequestsThatNeedToBeChecked")
    public List<DeliveryRequestCheckView> getDeliveryRequestsThatNeedToBeChecked(User user, @Named("cityId") Long cityId) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        return UnexposedApiMethods.getDeliveryRequestsThatNeedToBeChecked(cityId);
    }

    @ApiMethod(name = "doneCheckingADelivery")
    public BooleanWrapper doneCheckingADelivery(User user, @Named("deliveryRequestId") Long deliveryRequestId) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        return UnexposedApiMethods.doneCheckingADelivery(deliveryRequestId);
    }

    @ApiMethod(name = "sendNotificationToAllCustomers")
    public BooleanWrapper sendNotificationToAllCustomers(User user, @Named("cityId") Long cityId,
                                                         @Named("flag") int flag, @Named("title") String title,
                                                         @Named("content") String content,
                                                         @Named("merchantId") Long merchantId) throws UnauthorizedException {
        UserPrivileges.isAdmin(user);
        return UnexposedApiMethods.sendNotificationToAllCustomers(cityId, title, content, flag, merchantId);
    }

    @ApiMethod(name = "associateDriverWithMerchant")
    public BooleanWrapper associateDriverWithMerchant(@Named("merchantId") Long merchantId,
                                                      @Named("driverId") Long driverId,
                                                      @Named("generalDriver") boolean generalDriver) {
        Driver driver = Driver.getDriverByID(driverId);
        driver.associateWithMerchant(merchantId, generalDriver);
        return new BooleanWrapper(true);
    }
    // testing methods
    //===========================================================================


    @ApiMethod(name = "countCategories")
    public StringWrapper countCategories(User user, @Named("merchantId") Long merchantId) {
        String info = "merchant categories " + String.valueOf(Merchant.getMerchantByID(merchantId).menuCategoriesIds.size());
        info += "\n parent merchant " + ofy().load().type(MerchantCategory.class).filter("parentMerchantId =", merchantId).count();
        info += "\n  categories in system" + ofy().load().type(MerchantCategory.class).count();

        return new StringWrapper(info);
    }


    @ApiMethod(name = "getTestEntityByJavaDate")
    public List<TestEntity> getTestEntityByJavaDate() throws ParseException {
        List<TestEntity> testEntities = ofy().load().type(TestEntity.class).order("javaDate").list();
        System.out.println(testEntities);
        return testEntities;
    }

    @ApiMethod(name = "createTestEntityByGeoPt", path = "createTestEntityByGeoPt")
    public StringWrapper createTestEntityByGeoPt(final User user) {
        System.out.println(user);
        return new StringWrapper("great");
    }

    @ApiMethod(name = "createTestEntityByStringGeoPt", path = "createTestEntityByStringGeoPt")
    public TestEntity createTestEntityByStringGeoPt(User user, @Named("geoPt") String geoPt) {
        Gson gson = new Gson();
        GeoPt geoPt1 = gson.fromJson(geoPt, GeoPt.class);
        TestEntity testEntitie = new TestEntity(geoPt1);
        System.out.println(testEntitie);
        testEntitie.save();
        return testEntitie;
    }

    @ApiMethod(name = "createTestEntity", path = "createTestEntity")
    public TestEntity createTestEntity(User user, @Named("category") String category, @Named("Area") String area,
                                       @Named("anotherFilter") String anotherFilter) {

        TestEntity testEntity = new TestEntity(category, area, anotherFilter);
        testEntity.save();
        return testEntity;
    }

    @ApiMethod(name = "createTestEntities", path = "createTestEntities")
    public List<TestEntity> createTestEntities() {
        List<TestEntity> testEntities = new ArrayList<>();
        List<String> categories = new ArrayList<String>() {{
            add("seafood");
            add("fastfood");
            add("salads");
            add("pizza");
        }};
        TestEntity testEntity = new TestEntity(categories.get(0), "nasr", "cairo");
        testEntity.categories.add("pizza");
        testEntities.add(testEntity);
        TestEntity testEntity01 = new TestEntity(categories.get(0), "nasr", "cairo");
        testEntity01.categories.add("pizza");
        testEntity01.categories.add("salads");
        testEntities.add(testEntity01);

        TestEntity testEntity1 = new TestEntity(categories.get(0), "elska", "cairo");
        TestEntity testEntity11 = new TestEntity(categories.get(0), "elska", "cairo");
        testEntity11.categories.add("fastfood");
        testEntities.add(testEntity1);
        testEntities.add(testEntity11);

        TestEntity testEntity2 = new TestEntity(categories.get(2), "marhoom", "tanta");
        TestEntity testEntity22 = new TestEntity(categories.get(2), "marhoom", "tanta");
        testEntity22.categories.add("fastfood");

        testEntities.add(testEntity2);
        testEntities.add(testEntity22);

        TestEntity testEntity3 = new TestEntity(categories.get(3), "bahr", "tanta");
        testEntity3.categories.add("fastfood");

        testEntities.add(testEntity3);

        TestEntity testEntity4 = new TestEntity(categories.get(0), "mahta", "kfs");
        TestEntity testEntity44 = new TestEntity(categories.get(0), "mahta", "kfs");

        testEntity4.categories.add("salads");
        testEntity44.categories.add("pizza");

        testEntities.add(testEntity4);
        testEntities.add(testEntity44);

        TestEntity testEntity5 = new TestEntity(categories.get(0), "khalifa", "kfs");
        testEntities.add(testEntity5);

        for (TestEntity entity : testEntities) {
            entity.save();
        }
        return testEntities;
    }


    @ApiMethod(name = "getTestEntityByGeoPt", path = "getTestEntityByGeoPt")
    public List<TestEntity> getTestEntityByGeoPt(User user, @Named("geoPt") String geoPt) throws ParseException {
        Gson gson = new Gson();
        GeoPt geoPt1 = gson.fromJson(geoPt, GeoPt.class);
        List<TestEntity> testEntities = ofy().load().type(TestEntity.class).filter("geoPt =", geoPt1).list();
        System.out.println(testEntities);
        return testEntities;
    }

    @ApiMethod(name = "generateGeoPtHson", path = "generateGeoPtHson")
    public StringWrapper generateGeoPtHson(User user, @Named("longitiude") double longitiude, @Named("latitude") double latitude) {
        GeoPt geoPt = new GeoPt((float) latitude, (float) longitiude);
        return new StringWrapper(new Gson().toJson(geoPt));
    }


    @ApiMethod(name = "multipleFilters", path = "multipleFilters")
    public CollectionResponse<TestEntity> multipleFilters(User user, @Named("category") String category, @Named("Area") String area,
                                                          @Named("anotherFilter") String anotherFilter, @Named("numb") int numb,
                                                          @Named("cursorStr") String cursorStr, @Named("limit") int limit) {


        cursorStr = cursorStr.toLowerCase().trim().equals("null") ? null : cursorStr;

        Query<TestEntity> query = null;

        switch (numb) {
            case 1:
                query = ofy().load().type(TestEntity.class)
                        .filter("categories", category).limit(limit);
                break;
            case 2:
                query = ofy().load().type(TestEntity.class)
                        .filter("categories", category)
                        .filter("areas", area).limit(limit);
                break;
            case 3:
                query = ofy().load().type(TestEntity.class)
                        .filter("categories", category)
                        .filter("areas", area)
                        .filter("anotherFilterList", anotherFilter).limit(limit);
                break;
            case 4:
                query = ofy().load().type(TestEntity.class)
                        .filter("categories", category).order("javaDate").limit(limit);
                break;
            case 5:
                query = ofy().load().type(TestEntity.class)
                        .filter("categories", category)
                        .filter("areas", area).order("javaDate").limit(limit);
                break;
            case 6:
                query = ofy().load().type(TestEntity.class)
                        .filter("categories", category)
                        .filter("areas", area)
                        .filter("anotherFilterList", anotherFilter).order("javaDate").limit(limit);
                break;

        }
        CursorHelper<TestEntity> cursorHelper = new CursorHelper<>(TestEntity.class);
        CollectionResponse<TestEntity> merchantResponse =
                cursorHelper.queryAtCursor(query, cursorStr);
        return merchantResponse;
    }

}