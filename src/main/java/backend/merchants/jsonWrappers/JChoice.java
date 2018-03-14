package backend.merchants.jsonWrappers;


import backend.merchants.Choice;

/**
 * Created by Muhammad on 02/11/2017.
 */
public class JChoice {
    public String nameEn;
    public String nameAr;
    public String descriptionEn;
    public String descriptionAr;
    public double addedPrice;


    //===============
    public Long id;
    public Long parentMerchantId;
    public Long parentOption;


    public static JChoice fromChoice(Choice choice) {
        JChoice jChoice = new JChoice();
        jChoice.nameEn = choice.nameEn;
        jChoice.nameAr = choice.nameAr;
        jChoice.descriptionEn = choice.descriptionEn;
        jChoice.descriptionAr = choice.descriptionAr;
        jChoice.addedPrice = choice.addedPrice;
        jChoice.id = choice.id;
        jChoice.parentMerchantId = choice.parentMerchantId;
        jChoice.parentOption = choice.parentOption;
        return jChoice;
    }
}
