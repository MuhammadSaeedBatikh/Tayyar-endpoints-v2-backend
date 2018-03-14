package backend.cityArea;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.List;

import javax.print.attribute.standard.Chromaticity;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 12/01/2018.
 */

@Entity
@Cache
public class Area {

    @Id
    public Long id;
    @Index
    public String nameAr;
    @Index
    public String nameEn;
    @Index
    public Long cityId;
    @Index
    boolean supported = true;

    //default constructor for Entity initialization
    public Area() {
    }
    //============

    public void save() {
        ofy().save().entity(this).now();
    }

    public static List<Area> getAreasIncity(Long cityId) {
        return ofy().load().type(Area.class)
                .filter("cityId =", cityId)
                .filter("supported =", true)
                .list();
    }

    public Area(Long id, String nameAr, String nameEn, Long cityId, boolean supported) {
        this.id = id;
        this.nameAr = nameAr;
        this.nameEn = nameEn;
        this.cityId = cityId;
        this.supported = supported;
    }

    public static Area createArea(Long id, String nameAr, String nameEn, Long cityId, boolean supported){
        Area area = new Area(id,nameAr,nameEn,cityId,supported);
        area.save();
        return area;
    }

    public static Area getById(Long id) {
        return ofy().load().type(Area.class).id(id).now();
    }


    public static Long getId(String lang, Long cityId, String areaName) {
        boolean isArabic = lang.trim().equalsIgnoreCase("ar");
        String areaLang = isArabic ? "nameAr" : "nameEn";
        List<Area> areaList = ofy().load().type(Area.class)
                .filter("cityId =", cityId)
                .filter(areaLang + " =", areaName).list();
        if (areaList == null) {
            return -1L;
        }
        else return areaList.get(0).id;
    }


}
