package utilities;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.json.simple.parser.JSONParser;


public class RestCall {
    public static final String COSINE_URL = "http://hawking.sv.cmu.edu:9054/related";
    public static final String USER_AGENT = "Mozilla/5.0";

    public static Double getCosineValue(String firstStr, String secondStr) throws IOException {
        try {
            URL url = new URL(COSINE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Content-Type", "application/json");

            String input = "[\""+firstStr+"\", \""+secondStr+"\"]";
            input = input.replaceAll("\n", " ");
            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                JSONObject jsonObj = new JSONObject(output);
                return Double.parseDouble(jsonObj.get("cosine value").toString());
            }

            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
