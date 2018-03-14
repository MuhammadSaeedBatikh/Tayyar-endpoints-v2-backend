package backend.helpers;

import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;

import backend.merchants.Merchant;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 06/01/2018.
 */

public class CustomQueries {
    public static <T> Query<T> searchStringByPrefix(Query<T> query, String fieldName, String searchStr) {
        query = query.filter(fieldName + " >=", searchStr)
                .filter(fieldName + " <=", searchStr + "\ufffd");
        //ufffd maximum string length
        return query;
    }

    public static List<Long> getMerchantIdsInCity(Long cityId){
        List<Merchant> merchants = ofy().load().type(Merchant.class)
                .filter("cityId =", cityId)
                .list();
        List<Long> merchantsIds = new ArrayList<>(merchants.size());
        for (Merchant merchant : merchants) {
            merchantsIds.add(merchant.id);
        }
        return merchantsIds;
    }

}
