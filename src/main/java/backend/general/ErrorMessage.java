package backend.general;

import com.google.appengine.repackaged.com.google.gson.Gson;

/**
 * Created by Muhammad on 07/02/2018.
 */

public class ErrorMessage {
    public String parsingType = "ErrorMessage";
    public int flag;
    public String title;
    public String content;
    public Long deliveryRequestId;

    public ErrorMessage(int flag, String title, String content) {
        this.flag = flag;
        this.title = title;
        this.content = content;
    }

    public ErrorMessage(int flag, String title, String content, Long deliveryRequestId) {
        this.flag = flag;
        this.title = title;
        this.content = content;
        this.deliveryRequestId = deliveryRequestId;
    }

    public String toJson(){
       return new Gson().toJson(this);
    }
}
