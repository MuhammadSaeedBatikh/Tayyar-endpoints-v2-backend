package backend.deliveryRequests;

import java.util.ArrayList;
import java.util.List;

import backend.merchants.ChoiceView;

/**
 * Created by Muhammad on 15/01/2018.
 */

public class DeliveryItemOption {
    public String name;
    public List<String> choicesNames = new ArrayList<>();



    public void addChoice(String choiceName) {
        choicesNames.add(choiceName);
    }

    @Override
    public String toString() {
        return "DeliveryItemOption{" +
                "name='" + name + '\'' +
                ", choicesNames=" + choicesNames +
                '}';
    }
}
