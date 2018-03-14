package backend.merchants.inventory;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.impl.translate.SaveContext;

import java.util.ArrayList;
import java.util.List;

import backend.helpers.OfyHelper;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 14/01/2018.
 */

@Entity
@Cache
public class ActualCategory {
    @Id
    public Long id;
    public String nameAr;
    public String nameEn;

    //default constructor for Entity initialization
    public ActualCategory() {
    }
    //============

    public void save() {
        ofy().save().entity(this).now();
    }

    public ActualCategory(Long id, String nameAr, String nameEn) {
        this.id = id;
        this.nameAr = nameAr;
        this.nameEn = nameEn;
    }

    public ActualCategory(String nameAr, String nameEn) {
        this.nameAr = nameAr;
        this.nameEn = nameEn;
    }


    public static ActualCategory getById(Long id) {
        return ofy().load().type(ActualCategory.class).id(id).now();
    }

    public static ActualCategory getByName(String lang, String name) {
        boolean isArabic = lang.trim().equalsIgnoreCase("ar");
        String nameLang = isArabic ? "nameAr" : "nameEn";
        return ofy().load().type(ActualCategory.class)
                .filter(nameLang + " =", name).list().get(0);
    }

    public static ActualCategory create(ActualCategory actualCategory) {
        if (actualCategory.id == null | actualCategory.id == 0) {
            actualCategory = new ActualCategory(actualCategory.nameAr, actualCategory.nameEn);
        }
        actualCategory.save();
        return actualCategory;
    }


    @Override
    public String toString() {
        return "ActualCategory{" +
                "id=" + id +
                ", nameAr='" + nameAr + '\'' +
                ", nameEn='" + nameEn + '\'' +
                '}';
    }
}
