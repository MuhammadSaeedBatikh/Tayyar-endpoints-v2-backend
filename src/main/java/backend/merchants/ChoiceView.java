package backend.merchants;

import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by Muhammad on 13/01/2018.
 */

public class ChoiceView {

    public Long id;
    public String name;
    public String description;
    public double addedPrice;
    public boolean available = true;

    public ChoiceView(String lang, Choice choice){
        this.id = choice.id;
        this.addedPrice = choice.addedPrice;
        this.available = choice.available;

        if (lang.trim().equalsIgnoreCase("ar")){
            this.name = choice.nameAr;
            this.description = choice.descriptionAr;

        }
        else {
            this.name = choice.nameEn;
            this.description = choice.descriptionEn;
        }
    }

    @Override
    public String toString() {
        return "ChoiceView{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", addedPrice=" + addedPrice +
                ", available=" + available +
                '}';
    }
}
