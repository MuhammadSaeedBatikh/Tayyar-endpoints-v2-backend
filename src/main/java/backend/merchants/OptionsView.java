package backend.merchants;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad on 21/10/2017.
 */

public class OptionsView {
    public Long id;
    public String name;//size , additions etc
    public boolean required;
    public List<ChoiceView> choices = new ArrayList<>();
    public String description; //ingredients, etc

    public OptionsView(String lang, Option option) {
        this.id = option.id;
        this.required = option.required;
        for (Choice choice : option.getChoices()) {
            choices.add(new ChoiceView(lang, choice));
        }

            if (lang.trim().equalsIgnoreCase("ar")){
                this.name = option.nameAr;
                this.description = option.descriptionAr;

            }
            else {
                this.name = option.nameEn;
                this.description = option.descriptionEn;
            }
    }

    public static List<OptionsView> toOptionsViews(String lang, List<Option> options){
        List<OptionsView> optionsViews = new ArrayList<>();
        for (Option option : options) {
            optionsViews.add(new OptionsView(lang, option));
        }
        return optionsViews;

    }

    @Override
    public String toString() {
        return "OptionsView{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", required=" + required +
                ", choices=" + choices +
                ", description='" + description + '\'' +
                '}';
    }
}
