package utilities;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;

import static utilities.Printer.print;

public class JSONReader {

    public static ArrayList<JSONObject> readJsons(String pathName) {
        ArrayList<JSONObject> jsons = new ArrayList<JSONObject>();
        JSONParser parser = new JSONParser();

        try {
            print("Reading: "+pathName);
            BufferedReader br = new BufferedReader(new FileReader(new File(pathName)));
            String line;
            while ((line = br.readLine()) != null) {
                Object obj = null;
                try {
                    obj = parser.parse(line.trim());
                    JSONObject jsonObject = (JSONObject) obj;
                    jsons.add(jsonObject);
                } catch (ParseException e) {
//                        e.printStackTrace();
                    print("Issue occurred when reading one row of your file!");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsons;
    }
}
