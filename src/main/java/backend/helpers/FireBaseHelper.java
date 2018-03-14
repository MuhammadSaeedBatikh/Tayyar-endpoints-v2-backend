package backend.helpers;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Muhammad Saeed on 3/19/2017.
 */

public class FireBaseHelper {
    public static String type = "application/json";
    static URL url;

    static {
        try {
            url = new URL("https://fcm.googleapis.com/fcm/send");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    static String webApiKey = "key=AIzaSyAFxKRMmo6iCdsmUBW5jBruhGI6Iv1Qf2I";

    public FireBaseHelper() throws MalformedURLException {
    }

    public static void sendNotification(List<String> registerationTokens,
                                        String message) throws IOException {
        for (String regToken : registerationTokens) {
            sendNotification(regToken,message);
        }
    }

    public static String sendNotification(String registerationToken,
                                          String message){

        try {
            String regToken = registerationToken;
            String body = message;
            JSONObject jsonObject = createJSONNotification(regToken, body);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("Content-Type", type);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Authorization", webApiKey);

            httpURLConnection.setDoOutput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
            outputStreamWriter.write(jsonObject.toString());
            outputStreamWriter.close();
            return httpURLConnection.getResponseCode() + "  "
                    + httpURLConnection.getResponseMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public static JSONObject createJSONNotification(String regToken, String body) {
        JSONObject jsonObject = new JSONObject();
        JSONObject dataJson = new JSONObject();
        dataJson.put("request", body);
        jsonObject.put("data", dataJson);
        jsonObject.put("to", regToken);
        return jsonObject;
    }
}