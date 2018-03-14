package backend.general;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import backend.profiles.driver.Driver;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 17/01/2018.
 */

@Entity
@Cache
public class ConstantParams {
    @Id
    public Long id;

    @Index
    public Long cityId;

    @Index
    public int waitingTimeForDriverToAccept; //in milli seconds

    public int checkTime;

    public int checkCount;

    public int timeSliceForCheckingDriverState;

    @Index
    public int maxDeliveryRequestsPerDriver;

    @Index
    public List<Long> specialDriversIds = new ArrayList<>();

    @Index
    public List<String> customerSupportPhones = new ArrayList<>();

    @Index
    public double ourPercentagePerDeliveryFromDelivery = .2;

    //default constructor for Entity initialization
    public ConstantParams() {
    }

    //============
    public void save() {
        ofy().save().entity(this).now();
    }


    public static void setAll(int waitingTimeForDriverToAccept, int checkTime, int checkCount,
                              int maxDeliveryRequestsPerDriver,
                              int timeSliceForCheckingDriverState,
                              Long cityId,
                              double ourPercentagePerDeliveryFromDelivery,
                              List<String> customerSupportPhones,
                              List<Long> specialDriversIds) {
        ConstantParams constantParams = ConstantParams.getParamsByCityId(cityId);
        if (constantParams == null) {
            constantParams = new ConstantParams();
        }
        constantParams.cityId = cityId;
        constantParams.timeSliceForCheckingDriverState = timeSliceForCheckingDriverState;
        constantParams.waitingTimeForDriverToAccept = waitingTimeForDriverToAccept;
        constantParams.checkTime = checkTime;
        constantParams.checkCount = checkCount;
        constantParams.maxDeliveryRequestsPerDriver = maxDeliveryRequestsPerDriver;
        constantParams.ourPercentagePerDeliveryFromDelivery = ourPercentagePerDeliveryFromDelivery;
        if (specialDriversIds != null) {
            System.out.println("here");
            if (specialDriversIds.size() != 0) {
                for (Long specialDriversId : specialDriversIds) {
                    constantParams.addSpecialDriver(specialDriversId);
                }
            }

        }
        for (String phone : customerSupportPhones) {
            constantParams.addCustomerSupportPhone(phone);
        }

        constantParams.save();
    }


    public static ConstantParams getParamsByCityId(Long cityId) {
        List<ConstantParams> constantParams = ofy().load().type(ConstantParams.class)
                .filter("cityId =", cityId)
                .list();
        if (constantParams.size() == 0)
            return null;
        else return constantParams.get(0);
    }

    public void addSpecialDriver(Long specialDriverId) {

        if (!this.specialDriversIds.contains(specialDriverId)) {
            this.specialDriversIds.add(specialDriverId);
            this.save();
        }
    }


    public void addCustomerSupportPhone(String phone) {

        if (!this.customerSupportPhones.contains(phone)) {
            this.customerSupportPhones.add(phone);
            this.save();
        }
    }

    public Long getBestSpecialDriver() {
        List<Key<Driver>> keys = new ArrayList<>();
        for (Long specialDriversId : this.specialDriversIds) {
            keys.add(Key.create(Driver.class, specialDriversId));
        }
        Map<Key<Driver>, Driver> driverMap = ofy().load().keys(keys);

        List<Driver> drivers = new ArrayList<Driver>(driverMap.values());
        Long specialDriverId = drivers.get(0).id;
        int minDeliveryRequests = drivers.get(0).currentlyAcceptedDelReq;
        for (Driver driver : drivers) {
            if (driver.currentlyAcceptedDelReq < minDeliveryRequests) {
                specialDriverId = driver.id;
                minDeliveryRequests = driver.currentlyAcceptedDelReq;
            }
        }
        return specialDriverId;

//        return ofy().load().flag(Driver.class)
//                .filter("id in",this.specialDriversIds)
//                .order("currentlyAcceptedDelReq")
//                .list().get(0).id;
    }

}
