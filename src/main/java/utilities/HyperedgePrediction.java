package utilities;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;

import static utilities.Printer.print;
import static utilities.Printer.saveToExcel;

/**
 *
 */
public class HyperedgePrediction
{
    private static ArrayList<HyperedgeLog> hyperedgeLogs;
    private static ArrayList<HyperedgeLog>[] splittedExecutionLogs;
    private static int numberOfSplits;
    private static int trainingSize;
    private static double smoothingFactorForPopularity;

    private static int numberOfTrainingNodes;
    private static int numberOfTrainingHyperEdges;
    private static ArrayList<Set<String>> uniqueTrainingHyperedges;
    private static ArrayList<String> uniqueTrainingNodes;

    private static double[] precisions;
    private static double[] recalls;
    private static double[] fscores;
    private static ArrayList<Set<String>>[] oldTestHyperEdges;
    private static ArrayList<Set<String>>[] predictedTestHyperEdges;


    public static void main(String[] args){

        init();

//        for (int trainingIndex=0; trainingIndex<(numberOfSplits/trainingSize)*trainingSize; trainingIndex+=trainingSize){
//
//            createHypergraph(trainingIndex);
//            int[][][] trainingTensor = createTensor(trainingIndex);
//
//            TensorPrediction tensorPrediction = new TensorPrediction(numberOfTrainingNodes, numberOfTrainingHyperEdges, trainingSize);
//            tensorPrediction.decomposeTensor(trainingTensor, trainingIndex/trainingSize);
//        }

        Random random = new Random();
        for (int trainingIndex=0; trainingIndex<(numberOfSplits/trainingSize)*trainingSize; trainingIndex+=trainingSize){
            createHypergraph(trainingIndex);
            if(trainingIndex==9) {
                break;
            }
            print("trainingIndex: "+trainingIndex);
            double avg = 0;
            int maxSize = 0;
            int numInRange = 0;

            ArrayList<Set<String>> selectedHyperedges = new ArrayList<Set<String>>();
            for(int i=0; i<uniqueTrainingHyperedges.size(); i++){
                int hyperEdgeSize = uniqueTrainingHyperedges.get(i).size();
                avg += hyperEdgeSize;
                if(maxSize<hyperEdgeSize) {
                    maxSize = hyperEdgeSize;
                }
                if(hyperEdgeSize>=1) {
                    selectedHyperedges.add(uniqueTrainingHyperedges.get(i));
                    numInRange++;
                }
            }
            avg /= uniqueTrainingHyperedges.size();
//            print("Average Hyperedge size in this training: "+avg);
//            print("Max Hyperedge size in this training: "+maxSize);
//            print("Number of Hyperedges in this training: "+uniqueTrainingHyperedges.size());
//            print("Numbe of Hyperedges with size more than 5: "+ numInRange);
            TensorPrediction tensorPrediction = new TensorPrediction(numberOfTrainingNodes, numberOfTrainingHyperEdges, trainingSize);
            int testIndex = trainingIndex / trainingSize;
            double[][] similarityMatrix = tensorPrediction.createSimilarityMatrix(testIndex);

            //            double[] predictions = predictHyperedges(similarityMatrix, trainingIndex);

            print("Similarity Matrix created.");
            /////////////////////////////// new lines added for the case of subset

            Set<Set<String>> selectedSubsetsSet = new HashSet<Set<String>>();
            for(int i=0; i<selectedHyperedges.size(); i++){
                selectHyperEdgeSubset(selectedSubsetsSet, selectedHyperedges.get(i), 2);
                selectHyperEdgeSubset(selectedSubsetsSet, selectedHyperedges.get(i), 3);
            }

            ArrayList<Set<String>> selectedSubsetsForTest = new ArrayList<Set<String>>(selectedSubsetsSet);

            print("sizes more than one: "+selectedHyperedges.size());
            print("all subsets size: "+selectedSubsetsForTest.size());

            double[] predictions = predictHyperedges(similarityMatrix, trainingIndex, selectedSubsetsForTest);
            precisions[testIndex] = calculatePresisionForSubsets(predictions, trainingIndex, selectedSubsetsForTest);
            recalls[testIndex] = calculateRecallForSubsets(predictions, trainingIndex, selectedSubsetsForTest);
            fscores[testIndex] = calculateFScore(precisions[testIndex], recalls[testIndex]);


//            precisions[testIndex] = calculatePresision(predictions, trainingIndex);
//            recalls[testIndex] = calculateRecall(predictions, trainingIndex);
//            fscores[testIndex] = calculateFScore(precisions[testIndex], recalls[testIndex]);
            print(trainingIndex+" Finished");
        }

        saveToExcel(precisions,recalls,fscores, oldTestHyperEdges, predictedTestHyperEdges, "MashupSubsetPredictionForAllSubsetsSize2and3.xlsx");
    }

    private static void selectHyperEdgeSubset(Set<Set<String>> selectedSubsetsForTest, Set<String> hyperedge, int subsetSize) {

        String data[] = new String[subsetSize];

        // Print all combination using temprary
        // array 'data[]'
        String[] hyperEdgeArray = new String[hyperedge.size()];
        List<String> hyperedgeList = new ArrayList<String>(hyperedge);
        for(int i=0; i<hyperedgeList.size(); i++)
            hyperEdgeArray[i] = hyperedgeList.get(i);
        combinationUtil(selectedSubsetsForTest, hyperEdgeArray, hyperedge.size(), subsetSize, 0, data, 0);
    }
    static void combinationUtil(Set<Set<String>> selectedSubstesForTest, String[] arr, int n, int r,
                                int index, String[] data, int i)
    {
        // Current combination is ready to be printed,
        // print it
        if (index == r) {
            Set<String> subset = new HashSet<String>();
            for (int j = 0; j < r; j++)
                subset.add(data[j]);
            selectedSubstesForTest.add(subset);
            return;
        }

        // When no more elements are there to put in data[]
        if (i >= n)
            return;

        // current is included, put next at next
        // location
        data[index] = arr[i];
        combinationUtil(selectedSubstesForTest, arr, n, r, index + 1,
                data, i + 1);

        // current is excluded, replace it with
        // next (Note that i+1 is passed, but
        // index is not changed)
        combinationUtil(selectedSubstesForTest, arr, n, r, index, data, i + 1);
    }

//    // The main function that prints all combinations
//    // of size r in arr[] of size n. This function
//    // mainly uses combinationUtil()
//    static void printCombination(String arr[], int arrayLength, int subsetLength)
//    {
//        // A temporary array to store all combination
//        // one by one
//        String data[] = new String[subsetLength];
//
//        // Print all combination using temprary
//        // array 'data[]'
//        combinationUtil(arr, arrayLength, subsetLength, 0, data, 0);
//    }

    private static void init(){
        fillHyperedgeLogs1();
        print(findMinDate()+"");
        print(findMaxDate()+"");
//        fillHyperedgeLogs2();

        numberOfSplits =145;
        trainingSize = 20;
        smoothingFactorForPopularity = 0.8;

        splitExecutionLogs();

        int numberOfTests = numberOfSplits / trainingSize;
        precisions = new double[numberOfTests];
        recalls = new double[numberOfTests];
        fscores = new double[numberOfTests];
        oldTestHyperEdges = new ArrayList[numberOfTests];
        predictedTestHyperEdges = new ArrayList[numberOfTests];
    }

    private static void createHypergraph(int trainingIndex) {
        findeUniqueTrainingNodes(trainingIndex);
        findUniqueTrainingHyperEdges(trainingIndex);
        numberOfTrainingNodes = uniqueTrainingNodes.size();
        numberOfTrainingHyperEdges = uniqueTrainingHyperedges.size();
    }

    private static int[][][] createTensor(int trainingIndex) {

        int[][][] tensor = new int[numberOfTrainingNodes][numberOfTrainingHyperEdges][trainingSize];

        int timeIndex = 0;
        for (int snapshotIndex=trainingIndex; snapshotIndex<trainingIndex+trainingSize; snapshotIndex++) {
            if (splittedExecutionLogs[snapshotIndex]!=null) {
                for (HyperedgeLog hyperedgeLog : splittedExecutionLogs[snapshotIndex]) {
                    int hyperEdgeIndex = uniqueTrainingHyperedges.indexOf(hyperedgeLog.nodes);
                    for (String hyperNode : hyperedgeLog.nodes) {
                        int nodeIndex = uniqueTrainingNodes.indexOf(hyperNode);
                        tensor[nodeIndex][hyperEdgeIndex][timeIndex]++;
                    }
                }
            }
            timeIndex++;
        }

        return tensor;
    }

    private static double[] predictHyperedges(double[][] similarityMatrix, int trainingIndex) {
        double[] probabilities = new double[numberOfTrainingHyperEdges];

        for(int i=0; i<probabilities.length; i++)
            probabilities[i] = 1;

        for (Set<String> nodes: uniqueTrainingHyperedges){
            int hyperEdgeIndex = uniqueTrainingHyperedges.indexOf(nodes);
            for(String node: nodes){
                int nodeIndex = uniqueTrainingNodes.indexOf(node);
                probabilities[hyperEdgeIndex] *= similarityMatrix[nodeIndex][hyperEdgeIndex]*popularity(uniqueTrainingHyperedges.get(hyperEdgeIndex), trainingIndex);
            }
        }
        return probabilities;
    }

    private static double[] predictHyperedges(double[][] similarityMatrix, int trainingIndex, ArrayList<Set<String>> selectedSubsetsForTest) {
        double[] predictions = new double[selectedSubsetsForTest.size()];

        for(int i=0; i<predictions.length; i++)
            predictions[i] = 0;

        for(Set<String> nodes: selectedSubsetsForTest){
            int probabilityIndex = selectedSubsetsForTest.indexOf(nodes);
            ArrayList<Integer> hyperEdgeIndexesInSubsets = getHyperedgeIndexes(nodes);
            for(int i=0; i<hyperEdgeIndexesInSubsets.size(); i++){
                double probabilty = 1;
                for(String node: nodes){
                    int nodeIndex = uniqueTrainingNodes.indexOf(node);
                    double similarity = similarityMatrix[nodeIndex][hyperEdgeIndexesInSubsets.get(i)];
                    probabilty *= similarity;
                }
                predictions[probabilityIndex] +=probabilty;
            }

        }
        return predictions;
    }

    private static ArrayList<Integer> getHyperedgeIndexes(Set<String> nodes) {
        ArrayList<Integer> hyperEdgeIndexes = new ArrayList<Integer>();
        for(int i=0; i<uniqueTrainingHyperedges.size(); i++) {
            Set<String> hyperEdge = uniqueTrainingHyperedges.get(i);
            boolean found = containsSubset(hyperEdge, nodes);
            if (found)
                hyperEdgeIndexes.add(i);
        }
        return hyperEdgeIndexes;
    }

    private static boolean containsSubset(Set<String> targetSet, Set<String> subset) {
        boolean found = true;
        for (String node : subset) {
            if (!targetSet.contains(node)) {
                found = false;
                break;
            }
        }
        return found;
    }

    private static double popularity(Set<String> hyperEdge, int trainingIndex) {
        SingleExponentialSmoothing popularityCalculation = new SingleExponentialSmoothing(smoothingFactorForPopularity);
        double[] hyperEdgeSeries = new double[trainingSize];
        for(int time = trainingIndex; time< trainingIndex+trainingSize; time++){
            if(splittedExecutionLogs[time]!=null) {
                for (HyperedgeLog log : splittedExecutionLogs[time]) {
                    if (log.nodes.equals(hyperEdge)) {
                        hyperEdgeSeries[time-trainingIndex]++;
                    }
                }
            }
        }
        return popularityCalculation.predictValue(hyperEdgeSeries);
    }

    private static double calculatePresision(double[] hyperEdgeProbabilities, int trainingIndex) {
        double allPredicted = 0;
        for(double probability: hyperEdgeProbabilities){
            if (probability>0.0001){
                allPredicted++;
            }
        }

        double correctlyPredicted = numberOfCorrectlyPredictedHyperEdges(hyperEdgeProbabilities, splittedExecutionLogs[trainingIndex + trainingSize], trainingIndex);
        return correctlyPredicted/allPredicted;
    }

    private static double calculatePresisionForSubsets(double[] predictions, int trainingIndex, ArrayList<Set<String>> selectedSubsetsForTest) {
        double allPredicted = 0;
        for(double probability: predictions){
            if (probability>0){
                allPredicted++;
            }
        }

//        double correctlyPredicted = numberOfCorrectlyPredictedHyperEdgesForSubsets(predictions, splittedExecutionLogs[trainingIndex + trainingSize], trainingIndex, selectedSubsetsForTest);
        double correctlyPredicted = 0;
        for(int i=0; i<selectedSubsetsForTest.size(); i++){
            if(predictions[i]>0) {
                Set<String> subset = selectedSubsetsForTest.get(i);
                ArrayList<HyperedgeLog> splittedExecutionLog = splittedExecutionLogs[trainingIndex + trainingSize];
                if (splittedExecutionLog != null) {
                    for (HyperedgeLog hyperedgeLog : splittedExecutionLog) {
                        if (containsSubset(hyperedgeLog.nodes, subset)) {
                            correctlyPredicted++;
                            break;
                        }
                    }
                }
            }
        }

        return correctlyPredicted/allPredicted;
    }

    private static double calculateRecallForSubsets(double[] predictions, int trainingIndex, ArrayList<Set<String>> selectedSubsetsForTest) {
        Set<Set<String>> uniqueTestExecutionLogs = new HashSet<Set<String>>();
        double allOldHyperEdges = 0;
        if(splittedExecutionLogs[trainingIndex+trainingSize] !=null){
            for(HyperedgeLog hyperedgeLog : splittedExecutionLogs[trainingIndex+trainingSize]){
                for(int i=0; i<selectedSubsetsForTest.size(); i++){
                    if(uniqueTestExecutionLogs.contains(hyperedgeLog.nodes))
                        break;
                    if(containsSubset(hyperedgeLog.nodes , selectedSubsetsForTest.get(i))){
                        allOldHyperEdges++;
                    }
                }
                if(!uniqueTestExecutionLogs.contains(hyperedgeLog.nodes))
                    uniqueTestExecutionLogs.add(hyperedgeLog.nodes);
            }
        }

        oldTestHyperEdges[trainingIndex/trainingSize] = new ArrayList<Set<String>>(uniqueTestExecutionLogs);
        double correctlyPredicted = numberOfCorrectlyPredictedHyperEdgesForSubsets(predictions, splittedExecutionLogs[trainingIndex + trainingSize], trainingIndex, selectedSubsetsForTest);
        double recall = correctlyPredicted / allOldHyperEdges;
        print(recall+" recall");
        return recall;
    }

    private static double calculateRecall(double[] hyperEdgeProbabilities, int trainingIndex) {
        Set<Set<String>> uniqueTestExecutionLogs = new HashSet<Set<String>>();
        double allOldHyperEdges = 0;
        if(splittedExecutionLogs[trainingIndex+trainingSize] !=null){
            for(HyperedgeLog hyperedgeLog : splittedExecutionLogs[trainingIndex+trainingSize]){
                if (uniqueTrainingHyperedges.contains(hyperedgeLog.nodes) && !uniqueTestExecutionLogs.contains(hyperedgeLog.nodes)) {
                    allOldHyperEdges++;
                    uniqueTestExecutionLogs.add(hyperedgeLog.nodes);
                }
            }
        }

        oldTestHyperEdges[trainingIndex/trainingSize] = new ArrayList<Set<String>>(uniqueTestExecutionLogs);
        double correctlyPredicted = numberOfCorrectlyPredictedHyperEdges(hyperEdgeProbabilities, splittedExecutionLogs[trainingIndex + trainingSize], trainingIndex);
        return correctlyPredicted/allOldHyperEdges;
    }

    private static double calculateFScore(double precision, double recall) {
        return 2* (precision*recall)/(precision+recall);
    }

    private static double numberOfCorrectlyPredictedHyperEdgesForSubsets(double[] predictions, ArrayList<HyperedgeLog> splittedHyperedgeLog, int trainingIndex, ArrayList<Set<String>> selectedSubsets) {
        Set<Set<String>> uniqueTestExecutionLogs = new HashSet<Set<String>>();
        double correctlyPredicted = 0;
        if(splittedHyperedgeLog !=null){
            for(HyperedgeLog hyperedgeLog : splittedHyperedgeLog){
                for(int i=0; i<selectedSubsets.size(); i++){
                    if(uniqueTestExecutionLogs.contains(hyperedgeLog.nodes))
                        break;
                    Set<String> subset = selectedSubsets.get(i);
                    if(containsSubset(hyperedgeLog.nodes , subset)){
                        if(predictions[i]>0){
                            correctlyPredicted++;
                        }
                    }
                }
                if(!uniqueTestExecutionLogs.contains(hyperedgeLog.nodes))
                    uniqueTestExecutionLogs.add(hyperedgeLog.nodes);
            }
        }

        predictedTestHyperEdges[trainingIndex/trainingSize] = new ArrayList<Set<String>>(uniqueTestExecutionLogs);
        return correctlyPredicted;
    }

    private static double numberOfCorrectlyPredictedHyperEdges(double[] hyperEdgeProbabilities, ArrayList<HyperedgeLog> splittedHyperedgeLog, int trainingIndex) {
        Set<Set<String>> uniqueTestExecutionLogs = new HashSet<Set<String>>();
        double correctlyPredicted = 0;
        if(splittedHyperedgeLog !=null){
            for(HyperedgeLog hyperedgeLog : splittedHyperedgeLog){
                if(uniqueTrainingHyperedges.contains(hyperedgeLog.nodes) && !uniqueTestExecutionLogs.contains(hyperedgeLog.nodes)){
                    if(hyperEdgeProbabilities[uniqueTrainingHyperedges.indexOf(hyperedgeLog.nodes)]>0.0001){
                        correctlyPredicted++;
                        uniqueTestExecutionLogs.add(hyperedgeLog.nodes);
                    }
                }
            }
        }

        predictedTestHyperEdges[trainingIndex/trainingSize] = new ArrayList<Set<String>>(uniqueTestExecutionLogs);
        return correctlyPredicted;
    }

    private static void splitExecutionLogs() {
        splittedExecutionLogs = new ArrayList[numberOfSplits];

        for (HyperedgeLog hyperedgeLog : hyperedgeLogs) {
            int snapshotIndex = (int)(hyperedgeLog.date-2005);
            for (int copyIndex=0; copyIndex<12; copyIndex++){
                if(splittedExecutionLogs[snapshotIndex*12+copyIndex]==null){
                    splittedExecutionLogs[snapshotIndex*12+copyIndex] = new ArrayList<HyperedgeLog>();
                }
                splittedExecutionLogs[snapshotIndex*12+copyIndex].add(hyperedgeLog);
            }
        }
    }

    private static void findeUniqueTrainingNodes(int trainingIndex) {
        Set<String> setOfUniqueNodes = new HashSet<String>();
        for (int snapshotIndex= trainingIndex; snapshotIndex<trainingIndex+trainingSize; snapshotIndex++){
            if(splittedExecutionLogs[snapshotIndex]!= null){
                for(HyperedgeLog hyperedgeLog : splittedExecutionLogs[snapshotIndex]){
                    for(String nodeId: hyperedgeLog.nodes){
                        setOfUniqueNodes.add(nodeId);
                    }
                }
            }
        }
        uniqueTrainingNodes = new ArrayList<String>(setOfUniqueNodes);
    }

    private static void findUniqueTrainingHyperEdges(int trainingIndex) {
        Set<Set<String>> setOfUniqueHyperedges = new HashSet<Set<String>>();
        for (int snapshotIndex= trainingIndex; snapshotIndex<trainingIndex+trainingSize; snapshotIndex++) {
            if(splittedExecutionLogs[snapshotIndex]!= null){
                for (HyperedgeLog hyperedgeLog : splittedExecutionLogs[snapshotIndex]) {
                    setOfUniqueHyperedges.add(hyperedgeLog.nodes);
                }
            }
        }
        uniqueTrainingHyperedges = new ArrayList<Set<String>>(setOfUniqueHyperedges);
    }

    private static void fillHyperedgeLogs1() {
        hyperedgeLogs = new ArrayList<HyperedgeLog>();
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader("metadataMashup.txt"));
            JSONObject jsonObject = (JSONObject) obj;

            PrintWriter writer = new PrintWriter("Mashups_with_only_two_related_apis.txt", "UTF-8");

            for (Object jsonKey: jsonObject.keySet()) {
                JSONObject mashup = (JSONObject) jsonObject.get(jsonKey);
                JSONArray relatedApis = (JSONArray) mashup.get("Related APIs");
                if(relatedApis!=null){
                    List<String> apiList = new ArrayList<String>(relatedApis);
//                    if (apiList.size()==2 && !apiList.get(0).contains("Google") && !apiList.get(1).contains("Google"))
//                        writer.println(jsonKey.toString()+" : "+apiList.toString());
                    String dateValues[] = ((String) mashup.get("date")).split("\\.");
                    HyperedgeLog hyperedgeLog = new HyperedgeLog(Integer.parseInt(dateValues[2]), apiList);
                    hyperedgeLogs.add(hyperedgeLog);
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void fillHyperedgeLogs2() {
        hyperedgeLogs = new ArrayList<HyperedgeLog>();

        JSONParser parser = new JSONParser();
        print("I am reading");
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("C://Users/Maryam/mag_papers_1.txt")));
            String line;
            while ((line = br.readLine()) != null) {
                Object obj = parser.parse(line);
                JSONObject jsonObject = (JSONObject) obj;
                JSONArray authors = (JSONArray) jsonObject.get("authors");
                if(authors!=null){
                    List<String> authorNames = new ArrayList<String>();
                    for (Object authorObject: authors) {
                        JSONObject author = (JSONObject) authorObject;
                        authorNames.add((String) author.get("name"));
                    }
                    long year = (Long) jsonObject.get("year");
                    HyperedgeLog hyperedgeLog = new HyperedgeLog(year, authorNames);
                    hyperedgeLogs.add(hyperedgeLog);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static long findMaxDate() {
        long maxDate = hyperedgeLogs.get(0).date;
        for(HyperedgeLog hyperedgeLog : hyperedgeLogs){
            if (maxDate < hyperedgeLog.date){
                maxDate = hyperedgeLog.date;
            }
        }
        return maxDate;
    }

    private static long findMinDate() {
        long minDate = hyperedgeLogs.get(0).date;
        for(HyperedgeLog hyperedgeLog : hyperedgeLogs){
            if (minDate > hyperedgeLog.date){
                minDate = hyperedgeLog.date;
            }
        }
        return minDate;
    }
}
