package backend.merchants.jsonWrappers;


import com.googlecode.objectify.Key;

import java.util.ArrayList;
import java.util.List;

import backend.merchants.Choice;
import backend.merchants.Option;

/**
 * Created by Muhammad on 02/11/2017.
 */
public class JOption {
    public String nameEn;
    public String nameAr;
    public String descriptionEn;
    public String descriptionAr;
    public boolean required;
    public boolean onlyOneChoice;
    public List<JChoice> choices = new ArrayList<>();
    public List<Long> choicesIds = new ArrayList<>();

    //============
    public Long id;
    public Long parentMerchantId;
    public Long parentItemId;

    public static JOption fromOption(Option option) {
        JOption jOption = new JOption();
        jOption.nameEn = option.nameEn;
        jOption.nameAr = option.nameAr;
        jOption.descriptionEn = option.descriptionEn;
        jOption.descriptionAr = option.descriptionAr;
        jOption.required = option.required;
        jOption.onlyOneChoice = option.onlyOneChoice;
        jOption.id = option.id;
        jOption.parentMerchantId = option.parentMerchantId;
        jOption.parentItemId = option.parentItemId;
        jOption.choicesIds = option.choicesIds;
        for (Long choiceId : jOption.choicesIds) {
            Choice choice = Choice.getChoiceByID(choiceId);
            JChoice jChoice = JChoice.fromChoice(choice);
            jOption.choices.add(jChoice);
        }
        return jOption;
    }
}
