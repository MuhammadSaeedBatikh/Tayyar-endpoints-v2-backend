package backend.profiles.driver;

import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 18/01/2018.
 */


@Entity
public class UpdatableLocation {
    @Id
    public Long id;

    @Index
    public Long driverId;

    @Index
    public Long cityId;

    @Index
    public GeoPt geoPt;

    @Index
    public Date lastUpdateDate;

    //default constructor for Entity initialization
    public UpdatableLocation() {
    }

    //============
    public UpdatableLocation(Long driverId,Long cityId, GeoPt geoPt) {
        this.driverId = driverId;
        this.cityId = cityId;
        this.geoPt = geoPt;
    }

    public static Long updateLocation(Long driverId, GeoPt newGeoPt) {
        ofy().clear();
        UpdatableLocation updatableLocation = getById(Driver.getDriverByID(driverId).updatableLocationId);
        updatableLocation.geoPt = newGeoPt;
        updatableLocation.lastUpdateDate = new Date();
        updatableLocation.save();
        return updatableLocation.id;
    }

    public static UpdatableLocation getById(Long id) {
        return ofy().load().type(UpdatableLocation.class).id(id).now();
    }

    public static GeoPt getDriverLocation(Long driverId) {
        return ofy().load().type(UpdatableLocation.class)
                .filter("driverId =", driverId)
                .list().get(0).geoPt;
    }

    public void save() {
        ofy().save().entity(this).now();
    }

    @Override
    public String toString() {
        return "UpdatableLocation<br>{" +
                "id=" + id +
                ", driverId=" + driverId +
                ", cityId=" + cityId +
                ", geoPt=" + geoPt +
                ", lastUpdateDate=" + lastUpdateDate +
                '}'+"<br>";
    }
}
