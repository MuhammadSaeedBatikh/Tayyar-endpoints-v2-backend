package backend.offers;

/**
 * Created by Muhammad on 23/02/2018.
 */

public class PromotionMessage {
    public String parsingType = "PromotionMessage";
    public int flag;
    public String title;
    public String content;
    public String merchantImgUrl;
    public Long merchantId;

    public PromotionMessage(int flag, String title, String content) {
        this.flag = flag;
        this.title = title;
        this.content = content;
    }

    public PromotionMessage(int flag, String title, String content, String merchantImgUrl, Long merchantId) {
        this.flag = flag;
        this.title = title;
        this.content = content;
        this.merchantImgUrl = merchantImgUrl;
        this.merchantId = merchantId;
    }
}
