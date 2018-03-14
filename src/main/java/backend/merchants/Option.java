package backend.merchants;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import backend.merchants.jsonWrappers.JOption;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad Saeed on 2/11/2017.
 */
@Entity
@Cache

public class Option {

    @Id
    public Long id;

    @Index
    public Long parentMerchantId;
    @Index
    public Long parentItemId;

    public String nameEn; //size , additions etc
    public boolean required;
    public String nameAr;
    public String descriptionEn;  //ingredients, etc
    public String descriptionAr;

    public boolean onlyOneChoice;
    @Index
    public List<Long> choicesIds = new ArrayList<>();


    //default constructor for Entity initialization
    public Option() {
    }

    public Option(String nameAr, String nameEn, boolean required, String descriptionAr, String descriptionEn) {
        this.nameAr = nameAr;
        this.nameEn = nameEn;
        this.required = required;
        this.descriptionEn = descriptionEn;
        this.descriptionAr = descriptionAr;
    }

    public Option(boolean newMerchant, JOption jOption) {
        if (!newMerchant) {
            this.id = jOption.id;
            this.parentMerchantId = jOption.parentMerchantId;
            this.parentItemId = jOption.parentItemId;
            this.choicesIds = jOption.choicesIds;
        }
        this.nameAr = jOption.nameAr;
        this.nameEn = jOption.nameEn;
        this.descriptionAr = jOption.descriptionAr;
        this.descriptionEn = jOption.descriptionEn;

        this.required = jOption.required;
        this.onlyOneChoice = jOption.onlyOneChoice;
    }

    public static Option getOptionByID(Long id) {
        return ofy().load().type(Option.class).id(id).now();
    }

    public void saveOption() {
        ofy().save().entity(this).now();
    }

    public void addChoice(Long choiceID) {
        this.choicesIds.add(choiceID);
        saveOption();// save changes in this Merchant
    }

    public List<Choice> getChoices() {
        Collection<Choice> choices = ofy().load().type(Choice.class).ids(this.choicesIds).values();
        return new ArrayList<>(choices);
    }

    public void setParentMerchantId(Long parentMerchantId) {
        this.parentMerchantId = parentMerchantId;
    }

    public void setParentItemId(Long parentItemId) {
        this.parentItemId = parentItemId;
    }

    //============

}
