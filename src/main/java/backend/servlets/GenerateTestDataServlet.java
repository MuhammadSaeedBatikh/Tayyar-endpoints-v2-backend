package backend.servlets;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.org.apache.commons.codec.language.ColognePhonetic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.TestEntity;
import backend.apis.CustomerApi;
import backend.apis.DriverApi;
import backend.apis.MerchantApi;
import backend.apis.UnexposedApiMethods;
import backend.apis.UnexposedApiMethods;
import backend.cityArea.CityView;
import backend.deliveryRequests.CustomerLocationView;
import backend.deliveryRequests.DeliveryItem;
import backend.deliveryRequests.DeliveryItemView;
import backend.deliveryRequests.JDeliveryRequest;
import backend.merchants.Choice;
import backend.merchants.Merchant;
import backend.merchants.MerchantView;
import backend.merchants.Option;
import backend.offers.OfferMessages;
import backend.profiles.customer.Customer;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 20/01/2018.
 */

public class GenerateTestDataServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        System.out.println("here");
        DriverApi driverApi = DriverApi.getApiSingleton();
        CustomerApi customerApi = CustomerApi.getApiSingleton();

        UnexposedApiMethods.uploadCityData(getCityJson(), true);
        UnexposedApiMethods.createInventory(getInventory());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<String> phones = new ArrayList<String>() {{
            add("customer support phone");
        }};

        List<CityView> supportedCities = customerApi.getSupportedCities();
        Long cityId = supportedCities.get(0).id;
        Long areaId = supportedCities.get(0).areas.get(0).id;
        UnexposedApiMethods.setConstantParams(6000, 3000, 3, 4 * 1000 * 60, 3, cityId, .2, phones, null);


        try {
            UnexposedApiMethods.uploadJsonMerchant(true, getMerchantData());
            Thread.sleep(7000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            UnexposedApiMethods.createDriver("driver 1", "pass", cityId, "p1", (float) 10.5, (float) 10.5);
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        driverApi.signIn("p1", "pass", "reg");
        try {
            UnexposedApiMethods.createDriver("driver 2", "pass", cityId, "p2", (float) 15.3, (float) 13.5);
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        driverApi.signIn("p2", "pass", "reg");
        try {
            UnexposedApiMethods.createDriver("driver 3", "pass", cityId, "p3", (float) 11.2, (float) 11.2);
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        driverApi.signIn("p3", "pass", "reg");
        Long specialId = null;
        try {
            specialId = UnexposedApiMethods.createDriver("special", "pass", cityId, "sp", (float) 11.5, (float) 11.5).id;
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }
        final Long eFinalSPDriverID= specialId;
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        driverApi.signIn("sp", "pass", "reg");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        UnexposedApiMethods.setConstantParams(6000, 3000, 3, 4 * 1000 * 60, 3, cityId, .2, phones, new ArrayList<Long>() {{
            add(eFinalSPDriverID);
        }});
        Long customerId = customerApi.createCustomer("muhammad", "fire", "reg", "phone").id;
        List<MerchantView> merchantViews = (List<MerchantView>) customerApi
                .getListOfMerchantsByCategoryNameOrderedBy("en", customerId, areaId, 1000L, "null", "pricing", 5).getItems();
        Long merchantID = merchantViews.get(0).merchantID;
        Option option = ofy().load().type(Option.class).list().get(0);
        Long itemId = option.parentItemId;
        Long optionId = option.id;
        Long choiceId = ofy().load().type(Choice.class).filter("parentOption =", optionId).list().get(0).id;
        System.out.println("\n\n\n\n");
        String deliverJson = createDeliveryRequest(merchantID, customerId, cityId, areaId, itemId, optionId, choiceId);
        try {
            setOfferMessags();
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }
        System.out.println(deliverJson);
        resp.getWriter().write(deliverJson);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String jDeliveryStr = generateDeliveryRequest();
        resp.getWriter().write(jDeliveryStr);
    }

    private String createDeliveryRequest(Long merchantID, Long customerId, Long cityId, Long areaId,
                                         Long itemId, Long optionId, Long choiceId) {

        return "{\n" +
                "\"merchantType\":\"r\"," +
                "  \"merchantId\":" + merchantID + " ," +
                "  \"customerId\": " + customerId + "," +
                "  \"customerLocationView\": {" +
                "    \"cityId\": " + cityId + "," +
                "    \"areaId\": " + areaId + "," +
                "    \"longitude\": 10.0," +
                "    \"latitude\": 10.0," +
                "    \"address\": \"bla/bla\"," +
                "    \"deliveryInstructions\": \"nothing\"," +
                "    \"buildingNumber\": \"10\"," +
                "    \"floorNumber\": \"2\"," +
                "    \"apartmentNumber\": \"3\"" +
                "  },\n" +
                "  \"deliveryItems\": [\n" +
                "    {\n" +
                "      \"itemId\": " + itemId + ",\n" +
                "      \"options\": {\n" +
                "        \"" + optionId + "\": [\n" +
                "          " + choiceId + "\n" +
                "        ]\n" +
                "      },\n" +
                "      \"quantity\": 2,\n" +
                "      \"price\": 20.0,\n" +
                "      \"itemInstructions\": \"ee eee eeeee ee eeee\"\n" +
                "    }\n" +
                "  ],\n" +
                "\"coupon\": \"tayyar-50\"," +
                "  \"charge\": 50.0," +
                "  \"tip\": 2.0," +
                "  \"deliveryFee\": 5.0," +
                "  \"tayyarFee\": 2.0," +
                "  \"totalCharge\": 60.0," +
                "  \"generalInstructions\": \"do something\"" +
                "}";
    }


    public String generateDeliveryRequest() {
        System.out.println("generateDeliveryRequest called ");
        Customer customer = ofy().load().type(Customer.class).list().get(0);
        Long customerId = customer.id;
        Merchant merchant = ofy().load().type(Merchant.class).list().get(0);
        Long cityId = merchant.cityId;
        Long areaId = merchant.supportedAreasListIds.get(0);
        Long merchantId = merchant.id;
        List<Option> optionList = ofy().load().type(Option.class).filter("parentMerchantId =", merchantId).list();
        Option option = optionList.get(0);
        final Option option1 = option;
        Long itemId = option.parentItemId;
        List<Long> choices = new ArrayList<Long>() {{
            add(ofy().load().type(Choice.class).id(option1.choicesIds.get(0)).now().id);
        }};
        HashMap<String, List<Long>> options = new HashMap<>();
        options.put(option.id.toString(), choices);

        List<DeliveryItem> deliveryItems = new ArrayList<DeliveryItem>();
        deliveryItems.add(new DeliveryItem(itemId, options, 5, "do something"));
        String merchantType = "r";
        CustomerLocationView customerLocationView = new CustomerLocationView();
        customerLocationView.setValues(cityId, areaId, 50, 50, "el moqaweloon", "hang it there", "13", "2", "1");
        JDeliveryRequest jDeliveryRequest = new JDeliveryRequest();
        jDeliveryRequest.setValues(merchantType, customerId, null, null,
                customerLocationView, merchantId, deliveryItems, 50, 2, 6, 2, 60, "", "nothing");
        return new Gson().toJson(jDeliveryRequest);
    }

    public String getCityJson() {
        return "{\"city\":{\"nameAr\":\"eeee\",\"nameEn\":\"Tanta\",\"areasIds\":[],\"supported\":true},\"areas\":[{\"nameAr\":\"eeee eeee\",\"nameEn\":\"Botros Street\",\"supported\":true},{\"nameAr\":\"eeee eee eeeeeeee eeeeeee eeeeee eeeeee\",\"nameEn\":\"Cairo Alex highway Agricultural Rd\",\"supported\":true},{\"nameAr\":\"eeee\",\"nameEn\":\"Defra\",\"supported\":true},{\"nameAr\":\"eeeeeee\",\"nameEn\":\"El Ajizy\",\"supported\":true},{\"nameAr\":\"eeee eeeee\",\"nameEn\":\"El Bahr Street\",\"supported\":true},{\"nameAr\":\"eeee eeeeee\",\"nameEn\":\"El Fateh Street\",\"supported\":true},{\"nameAr\":\"eeee eeeeee\",\"nameEn\":\"El Galaa Street\",\"supported\":true},{\"nameAr\":\"eeeeee\",\"nameEn\":\"El Hekma\",\"supported\":true},{\"nameAr\":\"eeee eeeee\",\"nameEn\":\"El Helw Street\",\"supported\":true},{\"nameAr\":\"eeeeee\",\"nameEn\":\"El Mahatta\",\"supported\":true},{\"nameAr\":\"eeee eeeeeee\",\"nameEn\":\"El Moatasem Street\",\"supported\":true},{\"nameAr\":\"eeee eeeeeee\",\"nameEn\":\"El Motawakel Street\",\"supported\":true},{\"nameAr\":\"eeee eeeeee\",\"nameEn\":\"El Nady Street\",\"supported\":true},{\"nameAr\":\"eeee eeeeee\",\"nameEn\":\"El Nahas Street\",\"supported\":true},{\"nameAr\":\"eeee eeeeeee\",\"nameEn\":\"El Qantra Street\",\"supported\":true},{\"nameAr\":\"eeeee eeeeee\",\"nameEn\":\"El Saa Square\",\"supported\":true},{\"nameAr\":\"eeee eeeee eeeeeeeee\",\"nameEn\":\"El Sayed AbdelLatif Street\",\"supported\":true},{\"nameAr\":\"eeeee eeeee eeeeee\",\"nameEn\":\"Elsayed Elbadawy Square\",\"supported\":true},{\"nameAr\":\"eeee eee eeee\",\"nameEn\":\"Hassan Hasseb Street\",\"supported\":true},{\"nameAr\":\"eeee eeee ee eeee\",\"nameEn\":\"Hassan Ibn Thabet Street\",\"supported\":true},{\"nameAr\":\"eeee eee eeeee\",\"nameEn\":\"Hassan Radwan Street\",\"supported\":true},{\"nameAr\":\"eee eeee\",\"nameEn\":\"Kafr Essam\",\"supported\":true},{\"nameAr\":\"eeee eeeee\",\"nameEn\":\"Mahalet Marhoum\",\"supported\":true},{\"nameAr\":\"eeee eeee eeee\",\"nameEn\":\"Mohammed Farid Street\",\"supported\":true},{\"nameAr\":\"eeee eee\",\"nameEn\":\"Moheb Street\",\"supported\":true},{\"nameAr\":\"eeee eeee eeeeeeee\",\"nameEn\":\"Nadi El Moalemeen Street\",\"supported\":true},{\"nameAr\":\"eeee eee ee eee eeeeee\",\"nameEn\":\"Omar Ebn Abd El Aziz Street\",\"supported\":true},{\"nameAr\":\"eeeeeee eee\",\"nameEn\":\"Orouba Mall\",\"supported\":true},{\"nameAr\":\"eeee eeee\",\"nameEn\":\"Saeed Street\",\"supported\":true},{\"nameAr\":\"eeeeee\",\"nameEn\":\"Sebrbay\",\"supported\":true},{\"nameAr\":\"eeee eeee\",\"nameEn\":\"Sedki Street\",\"supported\":true},{\"nameAr\":\"eeee\",\"nameEn\":\"Segar\",\"supported\":true},{\"nameAr\":\"eeee eeeeee\",\"nameEn\":\"Shobra Elnamla\",\"supported\":true},{\"nameAr\":\"eeee\",\"nameEn\":\"Shoubar\",\"supported\":true},{\"nameAr\":\"eeeee eeeeeee\",\"nameEn\":\"Stadium Area\",\"supported\":true},{\"nameAr\":\"eeee ee eeeeee\",\"nameEn\":\"Taha El Hakeem Street\",\"supported\":true}]}";
    }

    public String getInventory() {
        return "[\n" +
                "  {\n" +
                "    \"id\": 1000,\n" +
                "    \"nameAr\": \"eeee\",\n" +
                "    \"nameEn\": \"chicken\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 1001,\n" +
                "    \"nameAr\":\"eeeeeee\",\n" +
                "    \"nameEn\": \"sandwich\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 1002,\n" +
                "    \"nameAr\": \"eeeeeee eeeee\",\n" +
                "    \"nameEn\": \"seafood\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 1003,\n" +
                "    \"nameAr\":\"eeeee\",\n" +
                "    \"nameEn\": \"pizza\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 1004,\n" +
                "    \"nameAr\": \"eeeeee\",\n" +
                "    \"nameEn\": \"grilled\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 1005,\n" +
                "    \"nameAr\": \"eeeeee\",\n" +
                "    \"nameEn\": \"asian\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 1006,\n" +
                "    \"nameAr\": \"eeee\",\n" +
                "    \"nameEn\": \"burger\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 1007,\n" +
                "    \"nameAr\": \"eeeeee\",\n" +
                "    \"nameEn\": \"desserts\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 1008,\n" +
                "    \"nameAr\": \"eeeeeee\",\n" +
                "    \"nameEn\": \"pasta\"\n" +
                "  }\n" +
                "]";
    }

    public String getMerchantData() {
        return "{\n" +
                "    \"flag\": \"r\",\n" +
                "    \"id\": \"3222\",\n" +
                "    \"branchId\": \"4409\",\n" +
                "    \"nameEn\": \"El Mamlaka\",\n" +
                "    \"nameAr\": \"eeeeeee\",\n" +
                "    \"phones\": [\"$\"],\n" +
                "    \"addressEn\": \"In front of Tanta Scan\",\n" +
                "    \"addressAr\": \"eeee eeee eeee\",\n" +
                "    \"cityEn\": \"Tanta\",\n" +
                "    \"cityAr\": \"eeee\",\n" +
                "    \"estimatedDeliveryTime\": 45,\n" +
                "    \"merchantArea\": \"El Nady Street\",\n" +
                "    \"opensAt\": \"10:00:00\",\n" +
                "    \"closesAt\": \"06:00:00\",\n" +
                "    \"latitude\": 11.5,\n" +
                "    \"longitude\": 11.5,\n" +
                "    \"imageURL\": \"https://www.akelni.com/images/res/e1de_sqp.jpg\",\n" +
                "    \"pricing\": 0,\n" +
                "    \"rating\": 0,\n" +
                "    \"minimumOrder\": 0.0,\n" +
                "    \"tax\": \"0\",\n" +
                "\"deliveryOption\": 1,\n" +
                "    \"actualCategoriesEn\": [\n" +
                "      \"pasta\",\n" +
                "      \"pizza\",\n" +
                "      \"sandwich\",\n" +
                "      \"italian\",\n" +
                "      \"fasting\"\n" +
                "    ],\n" +
                "    \"actualCategoriesAr\": [\n" +
                "      \"eeeeeee\",\n" +
                "      \"eeeee\",\n" +
                "      \"eeeeeee\",\n" +
                "      \"eeeeee\",\n" +
                "      \"eeeee\"\n" +
                "    ],\n" +
                "    \"baseDeliveryFee\": 5.0,\n" +
                "    \"supportedAreasMapEn\": {\n" +
                "      \"El Bahr Street\": 0.0,\n" +
                "      \"El Sayed AbdelLatif Street\": 0.0,\n" +
                "      \"Sebrbay\": 0.0,\n" +
                "      \"Orouba Mall\": 2.0,\n" +
                "      \"El Saa Square\": 0.0,\n" +
                "      \"Moheb Street\": 0.0,\n" +
                "      \"Omar Ebn Abd El Aziz Street\": 0.0,\n" +
                "      \"El Mahatta\": 0.0,\n" +
                "      \"Nadi El Moalemeen Street\": 0.0,\n" +
                "      \"Sedki Street\": 0.0,\n" +
                "      \"Cairo Alex highway Agricultural Rd\": 2.0,\n" +
                "      \"Hassan Ibn Thabet Street\": 0.0,\n" +
                "      \"El Ajizy\": 0.0,\n" +
                "      \"El Galaa Street\": 0.0,\n" +
                "      \"El Helw Street\": 0.0,\n" +
                "      \"El Qantra Street\": 0.0,\n" +
                "      \"Saeed Street\": 0.0,\n" +
                "      \"Mahalet Marhoum\": 2.0,\n" +
                "      \"Segar\": 0.0,\n" +
                "      \"Shoubar\": 0.0,\n" +
                "      \"El Motawakel Street\": 0.0,\n" +
                "      \"Botros Street\": 0.0,\n" +
                "      \"Shobra Elnamla\": 2.0,\n" +
                "      \"Stadium Area\": 0.0,\n" +
                "      \"Elsayed Elbadawy Square\": 0.0,\n" +
                "      \"Mohammed Farid Street\": 0.0,\n" +
                "      \"El Nady Street\": 0.0,\n" +
                "      \"El Fateh Street\": 0.0,\n" +
                "      \"El Moatasem Street\": 0.0,\n" +
                "      \"Hassan Radwan Street\": 0.0,\n" +
                "      \"Defra\": 2.0,\n" +
                "      \"El Hekma\": 0.0,\n" +
                "      \"Taha El Hakeem Street\": 0.0,\n" +
                "      \"Hassan Hasseb Street\": 0.0,\n" +
                "      \"Kafr Essam\": 0.0,\n" +
                "      \"El Nahas Street\": 0.0\n" +
                "    },\n" +
                "    \"supportedAreasMapAr\": {\n" +
                "      \"eeee eeee ee eeee\": 0.0,\n" +
                "      \"eeee eee ee eee eeeeee\": 0.0,\n" +
                "      \"eeeeee\": 0.0,\n" +
                "      \"eeee eee eeeee\": 0.0,\n" +
                "      \"eeeee eeeeee\": 0.0,\n" +
                "      \"eeee eeeeeee\": 0.0,\n" +
                "      \"eeee eeeee\": 0.0,\n" +
                "      \"eeee eeeeee\": 2.0,\n" +
                "      \"eeee eee\": 0.0,\n" +
                "      \"eeee eeee\": 0.0,\n" +
                "      \"eeee\": 0.0,\n" +
                "      \"eeee eeee eeee\": 0.0,\n" +
                "      \"eeee eeee eeeeeeee\": 0.0,\n" +
                "      \"eeee eeee\": 0.0,\n" +
                "      \"eeeee eeeeeee\": 0.0,\n" +
                "      \"eeee eeeeeee\": 0.0,\n" +
                "      \"eeee eeeee eeeeeeeee\": 0.0,\n" +
                "      \"eeee eeee\": 0.0,\n" +
                "      \"eeeeee\": 0.0,\n" +
                "      \"eeee eee eeee\": 0.0,\n" +
                "      \"eeeeeee eee\": 2.0,\n" +
                "      \"eeee eeeeee\": 0.0,\n" +
                "      \"eeee eeeee\": 0.0,\n" +
                "      \"eeee eeeeeee\": 0.0,\n" +
                "      \"eeeeee\": 0.0,\n" +
                "      \"eeee\": 2.0,\n" +
                "      \"eeee eeeeee\": 0.0,\n" +
                "      \"eeee ee eeeeee\": 0.0,\n" +
                "      \"eeee eeeeee\": 0.0,\n" +
                "      \"eee eeee\": 0.0,\n" +
                "      \"eeee\": 0.0,\n" +
                "      \"eeee eee eeeeeeee eeeeeee eeeeee eeeeee\": 2.0,\n" +
                "      \"eeeeeee\": 0.0,\n" +
                "      \"eeee eeeeee\": 0.0,\n" +
                "      \"eeeee eeeee eeeeee\": 0.0,\n" +
                "      \"eeee eeeee\": 2.0\n" +
                "    },\n" +
                "    \"categories\": [\n" +
                "      {\n" +
                "        \"id\": \"43\",\n" +
                "        \"nameEn\": \"Drinks\",\n" +
                "        \"nameAr\": \"eeeeeeeee\",\n" +
                "        \"itemsCount\": 4,\n" +
                "        \"imageURL\": \"$\",\n" +
                "        \"items\": [\n" +
                "          {\n" +
                "            \"id\": \"102663\",\n" +
                "            \"nameEn\": \"Pepsi\",\n" +
                "            \"nameAr\": \"eeeee\",\n" +
                "            \"descriptionEn\": \"\",\n" +
                "            \"descriptionAr\": \"\",\n" +
                "            \"basePrice\": 6.0,\n" +
                "            \"imageURL\": \"pepsi.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139392\",\n" +
                "                    \"nameEn\": \"Cans\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139393\",\n" +
                "                    \"nameEn\": \"Liter\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 1.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102665\",\n" +
                "            \"nameEn\": \"7up\",\n" +
                "            \"nameAr\": \"eee ee\",\n" +
                "            \"descriptionEn\": \"\",\n" +
                "            \"descriptionAr\": \"\",\n" +
                "            \"basePrice\": 6.0,\n" +
                "            \"imageURL\": \"7up.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139395\",\n" +
                "                    \"nameEn\": \"Cans\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139396\",\n" +
                "                    \"nameEn\": \"Liter\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 1.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102668\",\n" +
                "            \"nameEn\": \"Apple Mirnida\",\n" +
                "            \"nameAr\": \"eeeeee eeee\",\n" +
                "            \"descriptionEn\": \"\",\n" +
                "            \"descriptionAr\": \"\",\n" +
                "            \"basePrice\": 6.0,\n" +
                "            \"imageURL\": \"Mirindaapple.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139399\",\n" +
                "                    \"nameEn\": \"Cans\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139400\",\n" +
                "                    \"nameEn\": \"Liter\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 1.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102671\",\n" +
                "            \"nameEn\": \"Orange Mirinda\",\n" +
                "            \"nameAr\": \"eeeeee eeeeee\",\n" +
                "            \"descriptionEn\": \"\",\n" +
                "            \"descriptionAr\": \"\",\n" +
                "            \"basePrice\": 6.0,\n" +
                "            \"imageURL\": \"Mirindaorange.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139403\",\n" +
                "                    \"nameEn\": \"Cans\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139404\",\n" +
                "                    \"nameEn\": \"Liter\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 1.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": \"321\",\n" +
                "        \"nameEn\": \"Calzone\",\n" +
                "        \"nameAr\": \"eeeeeee\",\n" +
                "        \"itemsCount\": 4,\n" +
                "        \"imageURL\": \"$\",\n" +
                "        \"items\": [\n" +
                "          {\n" +
                "            \"id\": \"102643\",\n" +
                "            \"nameEn\": \"Cheese Calzone\",\n" +
                "            \"nameAr\": \"eeeeeee eee\",\n" +
                "            \"descriptionEn\": \"Mozzarella Cheese , Roumy Cheese , Kiri Cheese and Cheddar Cheese\",\n" +
                "            \"descriptionAr\": \"eeee eeeeeeeee e eeee eeee e eeee eeee e eeee eeee\",\n" +
                "            \"basePrice\": 30.0,\n" +
                "            \"imageURL\": \"\",\n" +
                "            \"options\": []\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102644\",\n" +
                "            \"nameEn\": \"Beef Calzone\",\n" +
                "            \"nameAr\": \"eeeeeee eeee\",\n" +
                "            \"descriptionEn\": \"Sausage , Pastrami , Sosis and Minced Beef\",\n" +
                "            \"descriptionAr\": \"eee e eeeeee e eeeee e eee eeeee\",\n" +
                "            \"basePrice\": 30.0,\n" +
                "            \"imageURL\": \"\",\n" +
                "            \"options\": []\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102646\",\n" +
                "            \"nameEn\": \"Chicken Calzone\",\n" +
                "            \"nameAr\": \"eeeeeee eeee\",\n" +
                "            \"descriptionEn\": \"\",\n" +
                "            \"descriptionAr\": \"\",\n" +
                "            \"basePrice\": 30.0,\n" +
                "            \"imageURL\": \"\",\n" +
                "            \"options\": []\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102647\",\n" +
                "            \"nameEn\": \"Seafood Calzone\",\n" +
                "            \"nameAr\": \"eeeeeee ee eee\",\n" +
                "            \"descriptionEn\": \"Shrimp , Crabs and Squid\",\n" +
                "            \"descriptionAr\": \"eeeee e eeeeeee e eeee\",\n" +
                "            \"basePrice\": 40.0,\n" +
                "            \"imageURL\": \"\",\n" +
                "            \"options\": []\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": \"490\",\n" +
                "        \"nameEn\": \"Italian Pizza Corner\",\n" +
                "        \"nameAr\": \"eeeeeee eeeeeeee\",\n" +
                "        \"itemsCount\": 17,\n" +
                "        \"imageURL\": \"$\",\n" +
                "        \"items\": [\n" +
                "          {\n" +
                "            \"id\": \"102567\",\n" +
                "            \"nameEn\": \"Margarita Pizza\",\n" +
                "            \"nameAr\": \"eeeee eeeeeeee\",\n" +
                "            \"descriptionEn\": \"\",\n" +
                "            \"descriptionAr\": \"\",\n" +
                "            \"basePrice\": 20.0,\n" +
                "            \"imageURL\": \"margritapizza.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139236\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139237\",\n" +
                "                    \"nameEn\": \"Large\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 15.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139238\",\n" +
                "                    \"nameEn\": \"Family\",\n" +
                "                    \"nameAr\": \"eeeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 40.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102583\",\n" +
                "            \"nameEn\": \"Mix Cheese Pizza\",\n" +
                "            \"nameAr\": \"eeeee eeee eee\",\n" +
                "            \"descriptionEn\": \"Mozzarella Cheese , Roumy Cheese , Kiri Cheese and Cheddar Cheese\",\n" +
                "            \"descriptionAr\": \"eeee eeeeeeeee e eeee eeee e eeee eeee e eeee eeee\",\n" +
                "            \"basePrice\": 30.0,\n" +
                "            \"imageURL\": \"cheeseloverspizza.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139280\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139281\",\n" +
                "                    \"nameEn\": \"Large\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 25.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139282\",\n" +
                "                    \"nameEn\": \"Family\",\n" +
                "                    \"nameAr\": \"eeeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 45.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102586\",\n" +
                "            \"nameEn\": \"Mushroom Pizza\",\n" +
                "            \"nameAr\": \"eeeee eeeee\",\n" +
                "            \"descriptionEn\": \"\",\n" +
                "            \"descriptionAr\": \"\",\n" +
                "            \"basePrice\": 30.0,\n" +
                "            \"imageURL\": \"pizzamashroom.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139287\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139288\",\n" +
                "                    \"nameEn\": \"Large\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 20.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139289\",\n" +
                "                    \"nameEn\": \"Family\",\n" +
                "                    \"nameAr\": \"eeeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 45.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102570\",\n" +
                "            \"nameEn\": \"Alexandrian Sausage Pizza\",\n" +
                "            \"nameAr\": \"eeeee eee eeeeeeeee\",\n" +
                "            \"descriptionEn\": \"\",\n" +
                "            \"descriptionAr\": \"\",\n" +
                "            \"basePrice\": 25.0,\n" +
                "            \"imageURL\": \"susagesmoke.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139243\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139244\",\n" +
                "                    \"nameEn\": \"Large\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 25.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139245\",\n" +
                "                    \"nameEn\": \"Family\",\n" +
                "                    \"nameAr\": \"eeeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 40.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102572\",\n" +
                "            \"nameEn\": \"Minced Beef Pizza\",\n" +
                "            \"nameAr\": \"eeeee eee eeeee\",\n" +
                "            \"descriptionEn\": \"\",\n" +
                "            \"descriptionAr\": \"\",\n" +
                "            \"basePrice\": 25.0,\n" +
                "            \"imageURL\": \"beefpizza.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139248\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139249\",\n" +
                "                    \"nameEn\": \"Large\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 25.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139250\",\n" +
                "                    \"nameEn\": \"Family\",\n" +
                "                    \"nameAr\": \"eeeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 40.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102573\",\n" +
                "            \"nameEn\": \"Sosis Pizza\",\n" +
                "            \"nameAr\": \"eeeee eeeee\",\n" +
                "            \"descriptionEn\": \"\",\n" +
                "            \"descriptionAr\": \"\",\n" +
                "            \"basePrice\": 25.0,\n" +
                "            \"imageURL\": \"sosisepizza.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139251\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139252\",\n" +
                "                    \"nameEn\": \"Large\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 25.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139253\",\n" +
                "                    \"nameEn\": \"Family\",\n" +
                "                    \"nameAr\": \"eeeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 40.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102574\",\n" +
                "            \"nameEn\": \"Pastrami Pizza\",\n" +
                "            \"nameAr\": \"eeeee eeeeee\",\n" +
                "            \"descriptionEn\": \"\",\n" +
                "            \"descriptionAr\": \"\",\n" +
                "            \"basePrice\": 30.0,\n" +
                "            \"imageURL\": \"pastrmapizza.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139254\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139255\",\n" +
                "                    \"nameEn\": \"Large\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 25.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139256\",\n" +
                "                    \"nameEn\": \"Family\",\n" +
                "                    \"nameAr\": \"eeeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 45.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102578\",\n" +
                "            \"nameEn\": \"Pepperoni Pizza\",\n" +
                "            \"nameAr\": \"eeeee eeeeee\",\n" +
                "            \"descriptionEn\": \"\",\n" +
                "            \"descriptionAr\": \"\",\n" +
                "            \"basePrice\": 30.0,\n" +
                "            \"imageURL\": \"pipronipizza.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139266\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139267\",\n" +
                "                    \"nameEn\": \"Large\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 25.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139268\",\n" +
                "                    \"nameEn\": \"Family\",\n" +
                "                    \"nameAr\": \"eeeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 45.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102579\",\n" +
                "            \"nameEn\": \"Salami Pizza\",\n" +
                "            \"nameAr\": \"eeeee eeeee\",\n" +
                "            \"descriptionEn\": \"\",\n" +
                "            \"descriptionAr\": \"\",\n" +
                "            \"basePrice\": 30.0,\n" +
                "            \"imageURL\": \"pizzasalami.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139269\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139270\",\n" +
                "                    \"nameEn\": \"Large\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 25.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139271\",\n" +
                "                    \"nameEn\": \"Family\",\n" +
                "                    \"nameAr\": \"eeeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 45.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102581\",\n" +
                "            \"nameEn\": \"Mix Beef Pizza\",\n" +
                "            \"nameAr\": \"eeeee eeee eeee\",\n" +
                "            \"descriptionEn\": \"Sausage , Sosis , Pastrami and Minced Beef\",\n" +
                "            \"descriptionAr\": \"eee e eeeee e eeeeee e eee eeeee\",\n" +
                "            \"basePrice\": 30.0,\n" +
                "            \"imageURL\": \"mixbeefpizza.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139275\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139276\",\n" +
                "                    \"nameEn\": \"Large\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 25.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139277\",\n" +
                "                    \"nameEn\": \"Family\",\n" +
                "                    \"nameAr\": \"eeeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 45.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102575\",\n" +
                "            \"nameEn\": \"Chicken Pizza\",\n" +
                "            \"nameAr\": \"eeeee eeee\",\n" +
                "            \"descriptionEn\": \"\",\n" +
                "            \"descriptionAr\": \"\",\n" +
                "            \"basePrice\": 30.0,\n" +
                "            \"imageURL\": \"ChickenPizza.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139257\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139258\",\n" +
                "                    \"nameEn\": \"Large\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 25.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139259\",\n" +
                "                    \"nameEn\": \"Family\",\n" +
                "                    \"nameAr\": \"eeeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 45.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102576\",\n" +
                "            \"nameEn\": \"Chicken Shawerma Pizza\",\n" +
                "            \"nameAr\": \"eeeee eeeeee eeee\",\n" +
                "            \"descriptionEn\": \"\",\n" +
                "            \"descriptionAr\": \"\",\n" +
                "            \"basePrice\": 30.0,\n" +
                "            \"imageURL\": \"chickensupreme.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139260\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139261\",\n" +
                "                    \"nameEn\": \"Large\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 25.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139262\",\n" +
                "                    \"nameEn\": \"Family\",\n" +
                "                    \"nameAr\": \"eeeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 45.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102577\",\n" +
                "            \"nameEn\": \"Mix Chicken Pizza\",\n" +
                "            \"nameAr\": \"eeeee eeee eeee\",\n" +
                "            \"descriptionEn\": \"Chicken Pane and Chicken Strips\",\n" +
                "            \"descriptionAr\": \"eeee eeeee e eeee eeeee\",\n" +
                "            \"basePrice\": 30.0,\n" +
                "            \"imageURL\": \"ChickenspicyPizza.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139263\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139264\",\n" +
                "                    \"nameEn\": \"Large\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 25.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139265\",\n" +
                "                    \"nameEn\": \"Family\",\n" +
                "                    \"nameAr\": \"eeeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 45.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102580\",\n" +
                "            \"nameEn\": \"Super Supreme Pizza\",\n" +
                "            \"nameAr\": \"eeeee eeee eeeeee\",\n" +
                "            \"descriptionEn\": \"Pastrami , Salami and Mushroom\",\n" +
                "            \"descriptionAr\": \"eeeeee e eeeee e eeeee\",\n" +
                "            \"basePrice\": 30.0,\n" +
                "            \"imageURL\": \"suprempizza.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139272\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139273\",\n" +
                "                    \"nameEn\": \"Large\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 20.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139274\",\n" +
                "                    \"nameEn\": \"Family\",\n" +
                "                    \"nameAr\": \"eeeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 45.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102589\",\n" +
                "            \"nameEn\": \"Tuna Pizza\",\n" +
                "            \"nameAr\": \"eeeee eeee\",\n" +
                "            \"descriptionEn\": \"\",\n" +
                "            \"descriptionAr\": \"\",\n" +
                "            \"basePrice\": 30.0,\n" +
                "            \"imageURL\": \"tunapizza.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139294\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139295\",\n" +
                "                    \"nameEn\": \"Large\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 20.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139296\",\n" +
                "                    \"nameEn\": \"Family\",\n" +
                "                    \"nameAr\": \"eeeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 45.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102590\",\n" +
                "            \"nameEn\": \"Seafood Pizza\",\n" +
                "            \"nameAr\": \"eeeee ee eee\",\n" +
                "            \"descriptionEn\": \"Shrimp , Crabs and Squid\",\n" +
                "            \"descriptionAr\": \"eeeee e eeeeeee e eeee\",\n" +
                "            \"basePrice\": 40.0,\n" +
                "            \"imageURL\": \"seafoodpizza.jpg\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139297\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139298\",\n" +
                "                    \"nameEn\": \"Large\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 20.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139299\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eeeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 50.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": \"102593\",\n" +
                "            \"nameEn\": \"El Mamlaka Pizza\",\n" +
                "            \"nameAr\": \"eeeee eeeeeee\",\n" +
                "            \"descriptionEn\": \"1/4 Seafood , 1/4 Beef , 1/4 Cheese and1/4 Mix Chicken\",\n" +
                "            \"descriptionAr\": \"1/4 ee eee e 1/4 eeee e 1/4 eee e 1/4 eeee eeee\",\n" +
                "            \"basePrice\": 40.0,\n" +
                "            \"imageURL\": \"\",\n" +
                "            \"options\": [\n" +
                "              {\n" +
                "                \"nameEn\": \"size\",\n" +
                "                \"nameAr\": \"eeeee\",\n" +
                "                \"required\": true,\n" +
                "                \"onlyOneChoice\": true,\n" +
                "                \"choices\": [\n" +
                "                  {\n" +
                "                    \"id\": \"139304\",\n" +
                "                    \"nameEn\": \"Medium\",\n" +
                "                    \"nameAr\": \"eee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139305\",\n" +
                "                    \"nameEn\": \"Large\",\n" +
                "                    \"nameAr\": \"eeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 20.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"139306\",\n" +
                "                    \"nameEn\": \"Family\",\n" +
                "                    \"nameAr\": \"eeeee\",\n" +
                "                    \"descriptionEn\": \"\",\n" +
                "                    \"descriptionAr\": \"\",\n" +
                "                    \"addedPrice\": 50.0\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "      \n" +
                "    ]\n" +
                "  }";
    }

    public void setOfferMessags() throws UnauthorizedException {
        MerchantApi.getApiSingleton().createOfferMessages("success", "success", "q", "doesn't exist", "expired", "expired",
                "max limit", "max limit", "q", "merchantType", "q", "doesn't start until", "q",
                "city", "q", "area", "q", "desn't apply to this merchant", "q", "minimum charge", "q",
                "above maximum charge", "q", "doesn't apply to you", "q", "max per customer");
    }

}