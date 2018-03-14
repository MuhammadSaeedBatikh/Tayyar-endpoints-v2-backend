package backend.cityArea;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad on 12/01/2018.
 */

public class CityView {

    public Long id;
    public String nameAr;
    public String nameEn;

    public List<Area> areas = new ArrayList<>();

    public CityView(City city, List<Area> areas) {
        this.id = city.id;
        this.nameAr = city.nameAr;
        this.nameEn = city.nameEn;
        this.areas = areas;
    }
}
