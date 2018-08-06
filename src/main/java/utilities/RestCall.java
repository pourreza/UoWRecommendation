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
import java.util.*;

import org.apache.http.client.methods.HttpPost;
import org.json.simple.parser.JSONParser;


public class RestCall {
    public static final String COSINE_URL = "http://hawking.sv.cmu.edu:9054/related";
    public static final String USER_AGENT = "Mozilla/5.0";

    public static Double getCosineValue(String firstStr, String secondStr) throws IOException {
        int numberOfTrys = 0;

        while(numberOfTrys<6) {
            try {
                URL url = new URL(COSINE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Content-Type", "application/json");

                String input = "[\"" + firstStr + "\", \"" + secondStr + "\"]";
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
//            System.out.println("Output from Server .... \n");
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
            numberOfTrys++;

            System.out.println("Tried again");
        }

        return null;
    }

    public static Double getCosineValue2(String firstStr, String secondStr) throws IOException {
//        final Set<CharSequence> intersection = getIntersection(leftVector, rightVector);
//
//        final double dotProduct = dot(leftVector, rightVector, intersection);
//        double d1 = 0.0d;
//        for (final Integer value : leftVector.values()) {
//            d1 += Math.pow(value, 2);
//        }
//        double d2 = 0.0d;
//        for (final Integer value : rightVector.values()) {
//            d2 += Math.pow(value, 2);
//        }
//        double cosineSimilarity;
//        if (d1 <= 0.0 || d2 <= 0.0) {
//            cosineSimilarity = 0.0;
//        } else {
//            cosineSimilarity = dotProduct / (Math.sqrt(d1) * Math.sqrt(d2));
//        }
//        return cosineSimilarityosineSimilarity;
//        String question = secondStr;
//        vector = []
//        cosine = []
//
//    # converting texts as vectors
//        def text_to_vector(text):
//        words = WORD.findall(text)
//        return Counter(words)
//
//        vector1 = text_to_vector(question)
//        c=0
//    # looping through all the questions to calculate cosine values
//        for i in ab:
//        vector.append(text_to_vector(ab[c]))
//        cosine.append(get_cosine(vector1, vector[c]))
//        c+=1
//    #     finding maximum cosine value
//        d = max(cosine)
//    # gettign correspoinding index of that cosine value
//                index = cosine.index(d)
//        data = {"question ": ab[index], "cosine value": str(cosine[index])}
//    # returning as json data
//        json_data = json.dumps(data)
//
//        return '{}'.format(json_data)
        return 0.;
    }

    public static double get_cosine(){
//        intersection = set(vec1.keys()) & set(vec2.keys())
//
//        numerator = sum([vec1[x] * vec2[x] for x in intersection])
//        # print ([vec1[x] * vec2[x] for x in intersection])
//
//        sum1 = sum([vec1[x] ** 2 for x in vec1.keys()])
//        sum2 = sum([vec2[x] ** 2 for x in vec2.keys()])
//        denominator = math.sqrt(sum1) * math.sqrt(sum2)
//
//        if not denominator:
//        return 0.0
//        else:
//        return float(numerator) / denominator
        return 0.;
    }

    /**
     * Returns a set with strings common to the two given maps.
     *
     * @param leftVector left vector map
     * @param rightVector right vector map
     * @return common strings
     */
    private Set<CharSequence> getIntersection(final Map<CharSequence, Integer> leftVector,
                                              final Map<CharSequence, Integer> rightVector) {
        final Set<CharSequence> intersection = new HashSet<CharSequence>(leftVector.keySet());
        intersection.retainAll(rightVector.keySet());
        return intersection;
    }

    /**
     * Computes the dot product of two vectors. It ignores remaining elements. It means
     * that if a vector is longer than other, then a smaller part of it will be used to compute
     * the dot product.
     *
     * @param leftVector left vector
     * @param rightVector right vector
     * @param intersection common elements
     * @return the dot product
     */
    private double dot(final Map<CharSequence, Integer> leftVector, final Map<CharSequence, Integer> rightVector,
                       final Set<CharSequence> intersection) {
        long dotProduct = 0;
        for (final CharSequence key : intersection) {
            dotProduct += leftVector.get(key) * rightVector.get(key);
        }
        return dotProduct;
    }
}
