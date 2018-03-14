package backend.cityArea;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad on 12/01/2018.
 */

public class JCity {
    public City city;
    public List<Area> areas = new ArrayList<>();

    public City uploadCity() {
        this.city.save();
        for (Area area : areas) {
            city.addArea(area);
        }
        return city;
    }

    @Override
    public String toString() {
        return "JCity{" +
                "City=" + city +
                ", areas=" + areas +
                '}';
    }
}
