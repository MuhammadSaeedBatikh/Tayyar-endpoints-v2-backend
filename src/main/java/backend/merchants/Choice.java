package backend.merchants;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import backend.merchants.jsonWrappers.JChoice;

import static backend.helpers.UtilityHelper.toKeys;
import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 19/08/2017.
 */
@Entity
@Cache

public class Choice  {
    @Id
    public Long id;
    @Index
    public Long parentMerchantId;

    @Index
    public Long parentOption;

    public String nameEn;
    public String nameAr;
    public String descriptionEn;
    public String descriptionAr;
    public double addedPrice;
    public boolean available = true;

    /* final price = basePrice+addedPrice

       ex: Kushari

        size options: "required"                     large 20LE, medium 15LE and small 10LE
        additions options: "not required"            meet balls 5LE

            basePrice = 10 LE i.e the minimum of all required Options

                                    and small Option addedPrice  = 0
                                    medium Option addedPrice = 5LE
                                    large  Option addedPrice = 10LE

                                    meet balls option addedPrice =5LE
    */

    //default constructor for Entity initialization
    public Choice() {
    }
    //============


    public Choice(String nameAr,String nameEn, double addedPrice, String descriptionAr, String descriptionEn) {
        this.nameAr = nameAr;
        this.nameEn = nameEn;
        this.addedPrice = addedPrice;
        this.descriptionAr = descriptionAr;
        this.descriptionEn = descriptionEn;
    }

    public Choice(boolean newMerchant, JChoice jChoice) {
        if (!newMerchant){
            this.id = jChoice.id;
            this.parentMerchantId = jChoice.parentMerchantId;
            this.parentOption = jChoice.parentOption;
        }
        
        this.nameAr = jChoice.nameAr;
        this.nameEn = jChoice.nameEn;
        this.descriptionAr = jChoice.descriptionAr;
        this.descriptionEn = jChoice.descriptionEn;
        this.addedPrice = jChoice.addedPrice;
    }

    public static Choice getChoiceByID(Long id) {
        return ofy().load().type(Choice.class).id(id).now();
    }
    
    public static List<Choice> getListOfChoices(List<Long> choicesIds){
        List<Key<Choice>> keys = toKeys(choicesIds, Choice.class);
        Map<Key<Choice>, Choice> choiceMap = ofy().load().keys(keys);
        List<Choice> values = new ArrayList<Choice>(choiceMap.values());
        return values;
    }

    public void setParentOption(Long parentOption) {
        this.parentOption = parentOption;
    }

    public void setParentMerchantId(Long parentMerchantId) {
        this.parentMerchantId = parentMerchantId;
    }

    public void saveChoice() {
        ofy().save().entity(this).now();
    }

    @Override
    public String toString() {
        return "Choice{" +
                "id=" + id +
                ", parentMerchantId=" + parentMerchantId +
                ", parentOption=" + parentOption +
                ", nameEn='" + nameEn + '\'' +
                ", nameAr='" + nameAr + '\'' +
                ", descriptionEn='" + descriptionEn + '\'' +
                ", descriptionAr='" + descriptionAr + '\'' +
                ", addedPrice=" + addedPrice +
                ", available=" + available +
                '}';
    }
}
