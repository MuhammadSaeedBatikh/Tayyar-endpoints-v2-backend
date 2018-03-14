package backend.merchants.jsonWrappers;

import com.google.api.client.json.Json;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;

import javax.xml.bind.annotation.XmlIDREF;

import backend.helpers.OfyHelper;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 06/01/2018.
 */
//created to bypass TaskQueue parameter size limit, and also to store json files
@Entity
public class JsonRawMerchant {
    @Id public Long id;
    @Index public Long merchantId;
    public String merchantJson;
    @Index
    public Date creationDate;
    //default constructor for Entity initialization
    public JsonRawMerchant (){}
    //============


    public JsonRawMerchant(Long merchantId, String merchantJson) {
        this.merchantId = merchantId;
        this.merchantJson = merchantJson;
        creationDate = new Date();
        save();
    }

    public void save(){
        ofy().save().entity(this).now();
    }
    public static JsonRawMerchant getById(Long id){
        return ofy().load().type(JsonRawMerchant.class).id(id).now();
    }
}
