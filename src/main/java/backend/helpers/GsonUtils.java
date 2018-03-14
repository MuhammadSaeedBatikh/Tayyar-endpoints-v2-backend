package backend.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by Muhammad on 16/01/2018.
 */

public class GsonUtils {

    private static final GsonBuilder gsonBuilder = new GsonBuilder()
            .setPrettyPrinting();



    public static Gson getGson() {
        return gsonBuilder.create();
    }
}