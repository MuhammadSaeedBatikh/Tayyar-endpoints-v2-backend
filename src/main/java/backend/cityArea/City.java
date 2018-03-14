package backend.cityArea;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.List;


import javax.print.attribute.standard.MediaSize;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 12/01/2018.
 */

@Entity
@Cache
public class City {
    @Id
    public Long id;
    @Index
    public String nameAr;
    @Index
    public String nameEn;
    @Index
    public List<Long> areasIds = new ArrayList<>();
    @Index
    public boolean supported = false;

    //default constructor for Entity initialization
    public City() {
    }

    public City(String nameAr, String nameEn, List<Long> areasIds, boolean supported) {
        this.nameAr = nameAr;
        this.nameEn = nameEn;
        this.areasIds = areasIds;
        this.supported = supported;
    }

    public City(String nameAr, String nameEn, boolean supported) {
        this.nameAr = nameAr;
        this.nameEn = nameEn;
        this.supported = supported;
    }

    public void save() {
        ofy().save().entity(this).now();
    }

    public void addArea(Long areaId) {
        this.areasIds.add(areaId);
        save();
    }

    public static City getById(Long id) {
        return ofy().load().type(City.class).id(id).now();
    }

    public static City supportNewArea(Long id, String nameAr, String nameEn, Long cityId){
        Area area = Area.createArea(id, nameAr, nameEn, cityId, true);
        City city = getById(cityId);
        city.addArea(area.id);
        return city;
    }

    public static City supportById(Long id){
        City city = getById(id);
        city.supported = true;
        city.save();
        return city;
    }

    public static List<City> getSupportedCities(){
        return ofy().load().type(City.class)
                .filter("supported =",true).list();
    }

    public void addArea(Area area) {
        area.cityId = this.id;
        area.save();
        this.areasIds.add(area.id);
        save();
    }


    public static Long getCityIdByName (String lang, String cityName) throws Exception {
        boolean isArabic = lang.trim().equalsIgnoreCase("ar");
        String cityLang = isArabic ? "nameAr" : "nameEn";

        List<City> cityList = ofy().load().type(City.class)
                .filter(cityLang + " =", cityName)
                .filter("supported =", true)
                .list();

        if (cityList == null) {
        throw new Exception("no city found");
        }
        else return cityList.get(0).id;
    }

    @Override
    public String toString() {
        return "City{" +
                "id=" + id +
                ", nameAr='" + nameAr + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", areasIds=" + areasIds +
                ", supported=" + supported +
                '}';
    }


    //============

}
