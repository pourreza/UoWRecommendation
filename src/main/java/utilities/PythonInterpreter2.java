package utilities;

import org.apache.commons.collections.map.HashedMap;
import org.python.core.PyInstance;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PythonInterpreter2
{

    PythonInterpreter interpreter = null;


    public PythonInterpreter2()
    {
        PythonInterpreter.initialize(System.getProperties(),
                System.getProperties(), new String[0]);

        this.interpreter = new PythonInterpreter();
    }

    void execfile( final String fileName )
    {
        this.interpreter.execfile(fileName);
    }

    PyInstance createClass( final String className, final String opts )
    {
        return (PyInstance) this.interpreter.eval(className + "(" + opts + ")");
    }

    public static void main( String gargs[] )
    {
        PythonInterpreter2 ie = new PythonInterpreter2();

        ie.execfile("hello.py");

        PyInstance hello = ie.createClass("Hello", "None");

        hello.invoke("run");
    }

    public static double getCosine2(String first, String second){

        Pattern p = Pattern.compile("\\w+");   // the pattern to search for
        Matcher m = p.matcher(first);
        ArrayList<String> questionList = new ArrayList<String>();
        while (m.find()) {
            questionList.add(m.group(0));
        }

        ArrayList<String> abList = new ArrayList<String>();
        Matcher m2 = p.matcher(second);
        while (m2.find()) {
            abList.add(m2.group(0));
        }

        Map<String, Integer> questionMap = new HashMap<String, Integer>();
        for(String str: questionList){
            int value = 0;
            for(String str2: questionList){
                if(str.equals(str2))
                    value++;
            }
            questionMap.put(str, value);
        }

        Map<String, Integer> abMap = new HashMap<String, Integer>();
        for(String str: abList){
            int value = 0;
            for(String str2: abList){
                if(str.equals(str2))
                    value++;
            }
            abMap.put(str, value);
        }

        Double cosine = cosineSimilarity(questionMap, abMap);
        if(cosine !=null)
            return cosine.doubleValue();

        return 0;

    }

    public static Double cosineSimilarity(final Map<String, Integer> leftVector,
                                          final Map<String, Integer> rightVector) {
        if (leftVector == null || rightVector == null) {
            throw new IllegalArgumentException("Vectors must not be null");
        }

        final Set<String> intersection = getIntersection(leftVector, rightVector);

        final double dotProduct = dot(leftVector, rightVector, intersection);
        double d1 = 0.0d;
        for (final Integer value : leftVector.values()) {
            d1 += Math.pow(value, 2);
        }
        double d2 = 0.0d;
        for (final Integer value : rightVector.values()) {
            d2 += Math.pow(value, 2);
        }
        double cosineSimilarity;
        if (d1 <= 0.0 || d2 <= 0.0) {
            cosineSimilarity = 0.0;
        } else {
            cosineSimilarity = dotProduct / (Math.sqrt(d1) * Math.sqrt(d2));
        }
        return cosineSimilarity;
    }

    /**
     * Returns a set with strings common to the two given maps.
     *
     * @param leftVector left vector map
     * @param rightVector right vector map
     * @return common strings
     */
    private static Set<String> getIntersection(final Map<String, Integer> leftVector,
                                               final Map<String, Integer> rightVector) {
        final Set<String> intersection = new HashSet<String>(leftVector.keySet());
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
    private static double dot(final Map<String, Integer> leftVector, final Map<String, Integer> rightVector,
                              final Set<String> intersection) {
        long dotProduct = 0;
        for (final String key : intersection) {
            dotProduct += leftVector.get(key) * rightVector.get(key);
        }
        return dotProduct;
    }


    public static double getCosine(String first, String second){
        try {
            String prg = "import api; result = api.related(\"" + first + "\", \"" + second + "\")";
            BufferedWriter out = new BufferedWriter(new FileWriter("api.py"));
            out.write(prg);
            out.close();
            int number1 = 10;
            int number2 = 32;
            Process p = Runtime.getRuntime().exec("python -c \'"+prg+"\'");
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            System.out.println(in.readLine());
            double ret = new Double(in.readLine()).doubleValue();
            return ret;
        }catch(Exception e){
            System.out.println(e.getStackTrace());
        }

        return 0;
    }
}