package backend.servlets;


import com.googlecode.objectify.Key;


import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.cityArea.City;
import backend.general.ConstantParams;
import backend.helpers.OfyHelper;
import backend.profiles.driver.Driver;
import backend.profiles.driver.UpdatableLocation;

import static backend.helpers.UtilityHelper.toKeys;
import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 02/02/2018.
 */

public class DriverLocationCheckServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("DriverLocationCheckServlet called");
        String info = "<body topmargin=\"5\" leftmargin=\"50\">";
        // TODO: 02/02/2018  java8 map stream to ids
        List<City> cities = ofy().load().type(City.class).list();
        HashMap<Long, Integer> timeMap = new HashMap<>();
        for (City city : cities) {
            Long cityId = city.id;
            int time = ConstantParams.getParamsByCityId(cityId).timeSliceForCheckingDriverState;
            timeMap.put(cityId, time);
        }

        info += "timeMap<CityId, time> =" + timeMap + "<br><br>";

        for (Long cityId : timeMap.keySet()) {
            int time = timeMap.get(cityId);
            Date currentDate = new Date();
            Date lowBoundaryDate = DateUtils.addMilliseconds(currentDate, -time);
            info += "currentDate&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp=" + currentDate +
                    "<br>lowBoundaryDate =" + lowBoundaryDate + "<br><br>";
            List<UpdatableLocation> locations = ofy().load().type(UpdatableLocation.class)
                    .filter("cityId =", cityId)
                    .filter("lastUpdateDate <", lowBoundaryDate)
                    .list();

            info += "query result =<br>" + locations + "<br>";

            List<Long> driversIds = new ArrayList<>(locations.size());
            for (UpdatableLocation location : locations) {
                driversIds.add(location.driverId);
            }

            List<Key<Driver>> keys = toKeys(driversIds, Driver.class);
            Map<Key<Driver>, Driver> driverMap = ofy().load().keys(keys);
            ArrayList<Driver> drivers = new ArrayList<Driver>(driverMap.values());
            for (Driver driver : drivers) {
                info += "<br>" + driver + "<br>";
                if (driver.idle == true) {
                    info += "changing driver state<br><br>";
                    driver.changeDriverState(false);
                }
            }
        }
        info += "</body>";
        resp.getWriter().write(info);

        //check for their location date with current date
        // save if passed certain amount of time
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("from doGet");
        doPost(req, resp);
    }
}
