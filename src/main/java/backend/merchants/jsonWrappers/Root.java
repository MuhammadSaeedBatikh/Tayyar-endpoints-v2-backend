package backend.merchants.jsonWrappers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad on 02/11/2017.
 */
public class Root {
    List<JMerchant> merchants = new ArrayList<>();
    //default constructor for Entity initialization
    public Root (){}
    //============
    public void addMerchant(JMerchant...merchants){
        for (JMerchant merchant : merchants) {
            this.merchants.add(merchant);
        }
    }

    @Override
    public String toString() {
        return "Root{" +
                "merchants=" + merchants.toString() +
                '}';
    }
}
