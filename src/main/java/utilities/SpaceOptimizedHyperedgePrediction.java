package utilities;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static utilities.Printer.print;
import static utilities.Printer.printInFile;

public class SpaceOptimizedHyperedgePrediction {
    private static ArrayList<HyperedgeLog> hyperedgeLogs;
    private static ArrayList<HyperedgeLog>[] splittedExecutionLogs;
    private static int numberOfSplits;
    private static int trainingSize;
    private static double smoothingFactorForPopularity;

    private static int minYear = 2018;
    private static int maxYear = 1800;

    private static int numberOfTrainingNodes;
    private static int numberOfTrainingHyperEdges;
    private static ArrayList<Set<String>> uniqueTrainingHyperedges;
    private static ArrayList<String> uniqueTrainingNodes;

    private static double[] precisions;
    private static double[] recalls;
    private static double[] fscores;
    private static ArrayList<Set<String>>[] oldTestHyperEdges;
    private static ArrayList<Set<String>>[] predictedTestHyperEdges;

    private static ArrayList<HyperedgeLog>[] allHyperLogs;
    private static ArrayList<HyperedgeLog>[] testHyperLogs;

    public static void main(String[] args){

        init();

        allHyperLogs = new ArrayList[13];
        testHyperLogs = new ArrayList[13];
        fillSOCHyperedgeLogs();
//        fillHyperedgeLog3();
        print("all papers: ");
        for (int i=0; i<13; i++){
            print("Number of papers from "+(i)+" is: "+allHyperLogs[i].size());
        }

        print("Runtime heap: "+ Runtime.getRuntime().totalMemory());
        print("Free Runtime heap: "+ Runtime.getRuntime().freeMemory());

        for (int trainingIndex=0; trainingIndex<13; trainingIndex++){
            print("trainingIndex "+trainingIndex);
//            fillHyperedgeLogs(trainingIndex);
//            splitExecutionLogs(trainingIndex);

            createHypergraph(trainingIndex);
            print(numberOfTrainingNodes+" : nodes");
            print(numberOfTrainingHyperEdges+" : edges");

            print(testHyperLogs[trainingIndex].size()+ ": number of tests");
            int numOld = findOldTestHyperedges(trainingIndex);
            print("new: "+(testHyperLogs[trainingIndex].size()-numOld)+" old: "+ numOld);
//            int[][][] trainingTensor = createTensor(trainingIndex);
//            TensorPrediction tensorPrediction = new TensorPrediction(numberOfTrainingNodes, numberOfTrainingHyperEdges, trainingSize);
//            tensorPrediction.decomposeTensor(trainingTensor, trainingIndex);
//            print("training index "+trainingIndex+" finished!");
        }

//        for (int trainingIndex=0; trainingIndex<(numberOfSplits/trainingSize)*trainingSize; trainingIndex+=trainingSize){
//            createHypergraph(trainingIndex);
//            if(trainingIndex==9)
//                break;
//            TensorPrediction tensorPrediction = new TensorPrediction(numberOfTrainingNodes, numberOfTrainingHyperEdges, trainingSize);
//            int testIndex = trainingIndex / trainingSize;
//            double[][] similarityMatrix = tensorPrediction.createSimilarityMatrix(testIndex);
//            double[] predictions = predictHyperedges(similarityMatrix, trainingIndex);
//            precisions[testIndex] = calculatePresision(predictions, trainingIndex);
//            recalls[testIndex] = calculateRecall(predictions, trainingIndex);
//            fscores[testIndex] = calculateFScore(precisions[testIndex], recalls[testIndex]);
//            print(trainingIndex+" Finished");
//        }
//
//        saveToExcel(precisions,recalls,fscores, oldTestHyperEdges, predictedTestHyperEdges);
    }

    private static int findOldTestHyperedges(int trainingIndex) {
        int numberOfOldHyperedges = 0;
        print("In testhyperedges with size: "+testHyperLogs[trainingIndex].size());
        for(int i=0; i<testHyperLogs[trainingIndex].size(); i++){
            if(uniqueTrainingHyperedges.contains(testHyperLogs[trainingIndex].get(i).nodes)){
                numberOfOldHyperedges++;
            }
        }
        return numberOfOldHyperedges;
    }

    private static void init(){
        numberOfSplits = 13;
        trainingSize = 5;
        smoothingFactorForPopularity = 0.8;

        int numberOfTests = numberOfSplits / trainingSize;
        precisions = new double[numberOfTests];
        recalls = new double[numberOfTests];
        fscores = new double[numberOfTests];
        oldTestHyperEdges = new ArrayList[numberOfTests];
        predictedTestHyperEdges = new ArrayList[numberOfTests];
    }

    private static void createHypergraph() {
        findeUniqueTrainingNodes2();
        findUniqueTrainingHyperEdges2();
        numberOfTrainingNodes = uniqueTrainingNodes.size();
        numberOfTrainingHyperEdges = uniqueTrainingHyperedges.size();
    }

    private static void createHypergraph(int trainingIndex) {
        findeUniqueTrainingNodes2(trainingIndex);
        findUniqueTrainingHyperEdges2(trainingIndex);
        numberOfTrainingNodes = uniqueTrainingNodes.size();
        numberOfTrainingHyperEdges = uniqueTrainingHyperedges.size();
    }

    private static int[][][] createTensor(int trainingIndex) {

        int[][][] tensor = new int[numberOfTrainingNodes][numberOfTrainingHyperEdges][trainingSize];

        for(HyperedgeLog log: allHyperLogs[trainingIndex]){
            int timeIndex = (int) log.date-2001-trainingIndex;
            int hyperEdgeIndex = uniqueTrainingHyperedges.indexOf(log.nodes);
            for (String hyperNode : log.nodes) {
                int nodeIndex = uniqueTrainingNodes.indexOf(hyperNode);
                tensor[nodeIndex][hyperEdgeIndex][timeIndex]++;
            }
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

    private static void splitExecutionLogs(int trainingIndex) {
        splittedExecutionLogs = new ArrayList[trainingSize];

        for (HyperedgeLog hyperedgeLog : hyperedgeLogs) {
            int snapshotIndex = (int)(hyperedgeLog.date-trainingIndex-1800);
            if(splittedExecutionLogs[snapshotIndex]==null){
               splittedExecutionLogs[snapshotIndex] = new ArrayList<HyperedgeLog>();
            }
            splittedExecutionLogs[snapshotIndex].add(hyperedgeLog);
        }
    }

    private static void findeUniqueTrainingNodes() {
        Set<String> setOfUniqueNodes = new HashSet<String>();
        for (int snapshotIndex= 0; snapshotIndex<trainingSize; snapshotIndex++){
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
    private static void findeUniqueTrainingNodes2() {
        Set<String> setOfUniqueNodes = new HashSet<String>();
        for(HyperedgeLog hyperedgeLog : hyperedgeLogs){
            for(String nodeId: hyperedgeLog.nodes){
                setOfUniqueNodes.add(nodeId);
            }
        }
        uniqueTrainingNodes = new ArrayList<String>(setOfUniqueNodes);
    }

    private static void findeUniqueTrainingNodes2(int trainingIndex) {
        Set<String> setOfUniqueNodes = new HashSet<String>();
        for(HyperedgeLog hyperedgeLog : allHyperLogs[trainingIndex]){
            for(String nodeId: hyperedgeLog.nodes){
                setOfUniqueNodes.add(nodeId);
            }
        }
        uniqueTrainingNodes = new ArrayList<String>(setOfUniqueNodes);
    }

    private static void findUniqueTrainingHyperEdges() {
        Set<Set<String>> setOfUniqueHyperedges = new HashSet<Set<String>>();
        for (int snapshotIndex= 0; snapshotIndex<trainingSize; snapshotIndex++) {
            if(splittedExecutionLogs[snapshotIndex]!= null){
                for (HyperedgeLog hyperedgeLog : splittedExecutionLogs[snapshotIndex]) {
                    setOfUniqueHyperedges.add(hyperedgeLog.nodes);
                }
            }
        }
        uniqueTrainingHyperedges = new ArrayList<Set<String>>(setOfUniqueHyperedges);
    }

    private static void findUniqueTrainingHyperEdges2() {
        Set<Set<String>> setOfUniqueHyperedges = new HashSet<Set<String>>();
        for (HyperedgeLog hyperedgeLog : hyperedgeLogs) {
            setOfUniqueHyperedges.add(hyperedgeLog.nodes);
        }
        uniqueTrainingHyperedges = new ArrayList<Set<String>>(setOfUniqueHyperedges);
    }

    private static void fillSOCHyperedgeLogs() {
        hyperedgeLogs = new ArrayList<HyperedgeLog>();

        JSONParser parser = new JSONParser();

        try {
            String pathname = "SOCpapersFinal2.txt";
            print("Reading: "+pathname);
            BufferedReader br = new BufferedReader(new FileReader(new File(pathname)));
            String line;
            int size =0;
            while ((line = br.readLine()) != null) {
                Object obj = null;
                try {
                    obj = parser.parse(line.trim());
                    JSONObject jsonObject = (JSONObject) obj;
                    JSONArray authors = (JSONArray) jsonObject.get("authors");
                    String venue = (String) jsonObject.get("venue");
                    long year = (Long) jsonObject.get("year");
                    if (authors != null) {
                        if(year<=2000)
                            continue;
                        List<String> authorNames = new ArrayList<String>();
                        for (Object authorObject : authors) {
                            JSONObject author = (JSONObject) authorObject;
                            authorNames.add((String) author.get("name"));
                        }
                        HyperedgeLog hyperedgeLog = new HyperedgeLog(year, authorNames);
                        int firstLogIndex =(int) year-2001-4;
                        if(firstLogIndex<0)
                            firstLogIndex=0;

                        int lastLogIndex = firstLogIndex + 4;
                        if(year<2005){
                            lastLogIndex = (int) year - 2000;
                        }
                        if(lastLogIndex>13){
                            lastLogIndex  = 13;
                        }
                        for(int firstIndex=firstLogIndex; firstIndex< lastLogIndex; firstIndex++){
                            if(allHyperLogs[firstIndex]==null){
                                allHyperLogs[firstIndex] = new ArrayList<HyperedgeLog>();
                            }
                            allHyperLogs[firstIndex].add(hyperedgeLog);
                        }

                        if(year==2018)
                            print("yeaaaaaay");

                        int testIndex = (int) year-2006;
                        if(testIndex>=0) {
                            if (testHyperLogs[testIndex] == null) {
                                testHyperLogs[testIndex] = new ArrayList<HyperedgeLog>();
                            }
                            testHyperLogs[testIndex].add(hyperedgeLog);
                        }
                    }
                } catch (ParseException e) {
//                        e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void findUniqueTrainingHyperEdges2(int trainingIndex) {
        Set<Set<String>> setOfUniqueHyperedges = new HashSet<Set<String>>();
        for (HyperedgeLog hyperedgeLog : allHyperLogs[trainingIndex]) {
            setOfUniqueHyperedges.add(hyperedgeLog.nodes);
        }
        uniqueTrainingHyperedges = new ArrayList<Set<String>>(setOfUniqueHyperedges);
    }

    private static void fillHyperedgeLogs(int trainingIndex) {
        hyperedgeLogs = new ArrayList<HyperedgeLog>();

        JSONParser parser = new JSONParser();
        try {
            int fileIndex=0;
            for(int folderIndex = 0; folderIndex<9; folderIndex++){
                int fIndex;
                for(fIndex = fileIndex; fIndex<fileIndex+20; fIndex++){
                    String pathname = "C://Users/Maryam/Microsoftdataset/mag_papers_" + folderIndex + "/mag_papers_"+fIndex+".txt";
                    print("Reading: "+pathname);
                    BufferedReader br = new BufferedReader(new FileReader(new File(pathname)));
                    String line;
                    while ((line = br.readLine()) != null) {
                        Object obj = parser.parse(line);
                        JSONObject jsonObject = (JSONObject) obj;
                        JSONArray authors = (JSONArray) jsonObject.get("authors");
                        if (authors != null) {
                            long year = (Long) jsonObject.get("year");
                            if (year < minYear)
                                minYear = (int) year;
                            if (year > maxYear)
                                maxYear = (int) year;

                            if (year >= (trainingIndex + 1800) && year < (trainingIndex + trainingSize + 1800)) {
                                List<String> authorNames = new ArrayList<String>();
                                for (Object authorObject : authors) {
                                    JSONObject author = (JSONObject) authorObject;
                                    authorNames.add((String) author.get("name"));
                                }
                                HyperedgeLog hyperedgeLog = new HyperedgeLog(year, authorNames);
                                hyperedgeLogs.add(hyperedgeLog);
                            }
                        }
                    }
                }
                fileIndex = fIndex;
                break;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void fillHyperedgeLog2() {
        hyperedgeLogs = new ArrayList<HyperedgeLog>();

        JSONParser parser = new JSONParser();


        try {
            int fileIndex=166;
            int everythingWithVenue = 0;
            for(int folderIndex = 8; folderIndex<9; folderIndex++){
                int fIndex;
                for(fIndex = fileIndex; fIndex<167; fIndex++){
                    String pathname = "C://Users/Maryam/Microsoftdataset/mag_papers_" + folderIndex + "/mag_papers_"+fIndex+".txt";
                    String outputPathName = "C://Users/Maryam/Microsoftdataset/venue_papers_" + folderIndex + "/mag_venue_papers_"+fIndex+".txt";
                    print("Reading: "+pathname);
                    BufferedReader br = new BufferedReader(new FileReader(new File(pathname)));
                    PrintWriter writer = new PrintWriter(outputPathName);
                    String line;
                    while ((line = br.readLine()) != null) {
                        Object obj = parser.parse(line);
                        JSONObject jsonObject = (JSONObject) obj;
                        JSONArray authors = (JSONArray) jsonObject.get("authors");
                        String venue = (String) jsonObject.get("venue");
                        long year = (Long) jsonObject.get("year");
                        if(venue!=null){
                            everythingWithVenue++;
                            writer.println(jsonObject.toString());
//                            outputStream.writeObject(jsonObject.toString()+"\n");
                        }
//                        if (authors != null) {
//                            long year = (Long) jsonObject.get("year");
//                            if(year<2000)
//                                continue;
//                            List<String> authorNames = new ArrayList<String>();
//                            for (Object authorObject : authors) {
//                                JSONObject author = (JSONObject) authorObject;
//                                authorNames.add((String) author.get("name"));
//                            }
//                            HyperedgeLog hyperedgeLog = new HyperedgeLog(year, authorNames);
//                            int firstLogIndex =(int) year-2000-5;
//                            if(firstLogIndex<0)
//                                firstLogIndex=0;
//
//                            int lastLogIndex = firstLogIndex + 6;
//                            if(lastLogIndex>12){
//                                lastLogIndex  = 12;
//                            }
//                            for(int firstIndex=firstLogIndex; firstIndex< lastLogIndex; firstIndex++){
//                                if(allHyperLogs[firstIndex]==null){
//                                    allHyperLogs[firstIndex] = new ArrayList<HyperedgeLog>();
//                                }
//                                allHyperLogs[firstIndex].add(hyperedgeLog);
//                            }
//
//                            int testIndex = (int) year-2006;
//                            if(testHyperLogs[testIndex]==null)
//                        }
                    }
                    writer.close();
                }
                fileIndex = fIndex;
                break;
            }
            print("number of venues: "+ everythingWithVenue);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void fillHyperedgeLog3() {
        hyperedgeLogs = new ArrayList<HyperedgeLog>();

        JSONParser parser = new JSONParser();
        Set<String> conferencesJournals = readFromFile();//socList();

        try {
            int fileIndex=0;
            int everythingWithVenue = 1;
            PrintWriter writer = new PrintWriter("SOCpapersFinal2.txt");
            for(int folderIndex = 0; folderIndex<9; folderIndex++){
                int fIndex;
                int lastFIndex = fileIndex+20;
                if(lastFIndex>167){
                    lastFIndex = 167;
                }
                for(fIndex = fileIndex; fIndex<lastFIndex; fIndex++){
                    String pathName = "C://Users/Maryam/Microsoftdataset/venue_papers_" + folderIndex + "/mag_venue_papers_"+fIndex+".txt";
                    print(pathName);
                    BufferedReader br = new BufferedReader(new FileReader(new File(pathName)));
                    String line;
                    while ((line = br.readLine()) != null) {
                        Object obj = parser.parse(line);
                        JSONObject jsonObject = (JSONObject) obj;
                        JSONArray authors = (JSONArray) jsonObject.get("authors");
                        String venue = (String) jsonObject.get("venue");
                        long year = (Long) jsonObject.get("year");
                        String publisher = (String) jsonObject.get("publisher");
                        if (isIEEE(venue.trim().toLowerCase(), conferencesJournals, publisher)){// || (venue.toLowerCase().contains("it professional")&&publisher!=null&&publisher.contains("IEEE"))) {
                            everythingWithVenue++;
                            writer.println(jsonObject.toString());
                        }
//                        if (isIEEE(venue.trim().toLowerCase(), conferencesJournals) && (publisher != null && !publisher.contains("IEEE"))) {
//                            print(venue + " : " + publisher);
//                        }
                    }
                }
                fileIndex = fIndex;
            }
            writer.close();
            print("number of venues: "+ everythingWithVenue);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static Set<String> socList() {
        Set<String> socConferencesJournals = new HashSet<String>();
        socConferencesJournals.add("IEEE Transactions on Services Computing");
        socConferencesJournals.add("IEEE Transactions on Software Engineering");
        socConferencesJournals.add("IEEE Transactions on Cloud Computing");
        socConferencesJournals.add("IEEE Transactions on Big Data");
        socConferencesJournals.add("IEEE Computer");
        socConferencesJournals.add("IEEE Software");
        socConferencesJournals.add("IEEE Internet Computing");
        socConferencesJournals.add("IEEE International Conference on Web Services");
        socConferencesJournals.add("IEEE International Conference on Services Computing");
        socConferencesJournals.add("IEEE International Conference on Cloud Computing");
        socConferencesJournals.add("IEEE International Conference on Cognitive Computing");
        socConferencesJournals.add("IEEE International Conference on Edge Computing");
        socConferencesJournals.add("IEEE International Conference on Big Data");
        socConferencesJournals.add("IEEE International Conference on Internet of Things");
        socConferencesJournals.add("IEEE International Congress on Big Data");
        socConferencesJournals.add("IEEE International Congress on Internet of Things");
        return socConferencesJournals;
    }

    private static Set<String> readFromFile() {
        Set<String> cj = new HashSet<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("cleaned IEEE-SOC-V7.txt")));
            String line;
            while ((line = br.readLine()) != null) {
            cj.add(line.trim());
            }

//            ArrayList<String> cjCopy = new ArrayList<String>(cj);
//            Set<String> cjFinal = new HashSet<String>();
//            for(String tempLine: cjCopy){
//                if(!tempLine.contains("workshop"))
//                    cjFinal.add(tempLine.trim());
//            }
//
//            printInFile(cjFinal, "cleaned IEEE-SOC-V6.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cj;
    }

    private static boolean isIEEE(String venue, Set<String> conferencesJournals, String publisher) {
        venue = venue.toLowerCase();
        for(String conference: conferencesJournals){
//            if(venue.toLowerCase().contains(conference.toLowerCase()) && !venue.toLowerCase().equals(conference.toLowerCase())){
//                print(venue);
//                print(conference);
//                print(publisher);
//            }
            if(venue.toLowerCase().contains(conference.toLowerCase()) && !venue.contains("ieee computer graphics") && !venue.contains("ieee computer security") && !venue.contains("ieee computer architecture") && !venue.contains("ieee computer applications in power"))
                return true;
        }
        return false;
    }

    private static Set<String> datasetPruningRules() {
        String path="ListOfIEEEConferencesJournalssss.txt";
        Set<String> conferencesJournals = new HashSet<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(path)));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.toLowerCase();
                line = removeParanthesis(line, "(", ")");
                line = removeParanthesis(line, "[", "]");
                line = removeYear(line);
                line = removeSubstring(line, "proceedings of the ");
                line = removeSubstring(line, "proceedings");
                line = removeSubstring(line, "proceeding");
                line = removeSubstring(line, "annual");
                line = removeSubstring(line, "memocode '04");
                line = removeSubstring(line, "icse'08");
                line = removeSubstring(line, "micro 29");
                line = removeSubstring(line, "micro-33");
                line = removeSubstring(line, "micro-34");
                line = removeSubstring(line, "micro-36");
                line = removeSubstring(line, "estimedia");
                line = removeSubstring(line, "asap");
                line = removeSubstring(line, "bsn");
                line = removeSubstring(line, "spring 88");
                line = removeSubstring(line, "dagstuhl");
                line = removeSubstring(line, "discex'01");
                line = removeSubstring(line, "dexa '97");
                line = removeSubstring(line, "date'08");
                line = removeSubstring(line, "dfma 05");
                line = removeSubstring(line, "sccc");
                line = removeSubstring(line, "sc14:");
                line = removeSubstring(line, "sc16:");
                line = removeSubstring(line, "rtas");
                line = removeSubstring(line, "rtss");
                line = removeSubstring(line, "scw");
                line = removeSubstring(line, "sibgrapi");
                line = removeSubstring(line, ".");
                line = removeSubstring(line, ",");
                line = removeSubstring(line, "?");
                if(line.trim().indexOf("ase")==0){
                    line = line.substring(3).trim();
                }
                if (line.trim().indexOf("the ")==0){
                    line = line.trim().substring(4);
                }
                line = removeExtraSpace(line);
                line = removeIndex(line, false);
                if (line.trim().indexOf("- ")==0){
                    line = line.trim().substring(2);
                }

                conferencesJournals.add(line.trim());

                Set<String> otherFormats = new HashSet<String>();
                otherFormats.add(line.trim());

                line = line.trim();
                if(line.contains("ieee international conference on") && !line.contains("ieee international conference on ")){
                    String tempLine = "ieee international conference on" + line.substring(0, line.indexOf("ieee international conference on"));
                    tempLine = removeExtraSpace(tempLine);
                    conferencesJournals.add(tempLine.trim());
                    otherFormats.add(tempLine.trim());
                }

                while (line.contains("&")){
                    String tempLine2 = line.substring(0,line.indexOf("&"))+"and"+line.substring(line.indexOf("&")+1);
                    line = removeExtraSpace(tempLine2);
                    conferencesJournals.add(line.trim());
                    otherFormats.add(line.trim());
                }

                while (line.contains("'n'")){
                    String tempLine2 = line.substring(0,line.indexOf("'n'"))+"and"+line.substring(line.indexOf("'n'")+3);
                    tempLine2 = removeExtraSpace(tempLine2).trim();
                    conferencesJournals.add(tempLine2);
                    otherFormats.add(tempLine2);
                    tempLine2 = line.substring(0,line.indexOf("'n'"))+"&"+line.substring(line.indexOf("'n'")+3);
                    line = removeExtraSpace(tempLine2);
                    conferencesJournals.add(line.trim());
                    otherFormats.add(line.trim());
                }

                swapEnding(conferencesJournals, otherFormats);
                ArrayList<String> copyOtherFormats = new ArrayList<String>(otherFormats);
                for(String lineCopy : copyOtherFormats){
                    while (lineCopy.contains("-")){
                        String l = lineCopy.substring(0,lineCopy.indexOf('-')) + " " + lineCopy.substring(lineCopy.indexOf('-')+1);
                        l = removeExtraSpace(l);
                        conferencesJournals.add(l.trim());
                        otherFormats.add(l.trim());
//                    String l2 = line.substring(0,line.indexOf('-')) + line.substring(line.indexOf('-')+1);
                        lineCopy = l;
//                    print(l2);
//                    conferencesJournals.add(l2.trim());
                    }
                }

                copyOtherFormats = new ArrayList<String>(otherFormats);
                for(String lineCopy : copyOtherFormats){
                    while (lineCopy.contains("'")&&!lineCopy.contains("'n'")){
                        String l = lineCopy.substring(0,lineCopy.indexOf('\'')) + " " + lineCopy.substring(lineCopy.indexOf('\'')+1);
                        l = removeExtraSpace(l);
                        conferencesJournals.add(l.trim());
                        otherFormats.add(l.trim());
                        l = lineCopy.substring(0,lineCopy.indexOf('\'')) + lineCopy.substring(lineCopy.indexOf('\'')+1);
                        l = removeExtraSpace(l);
                        conferencesJournals.add(l.trim());
                        otherFormats.add(l.trim());
//                    String l2 = line.substring(0,line.indexOf('-')) + line.substring(line.indexOf('-')+1);
                        lineCopy = l;
//                    print(l2);
//                    conferencesJournals.add(l2.trim());
                    }
                }
                copyOtherFormats = new ArrayList<String>(otherFormats);
                addVariousFormats(conferencesJournals, otherFormats, copyOtherFormats);
                copyOtherFormats = new ArrayList<String>(otherFormats);
                for(String copyLine : copyOtherFormats){
                    copyLine = removeExtraSpace(removeIndex(copyLine, true).trim()).trim();
                    conferencesJournals.add(copyLine);
                    otherFormats.add(copyLine);
                }

//                if(line.contains("&")){
//                    String l = line.substring(0,line.indexOf('&')) + "and" + line.substring(line.indexOf('&')+1);
//                    print(l);
//                    conferencesJournals.add(l.trim());
//                }
//                if(line.contains(",")){
//                    String l = line.substring(0,line.indexOf(',')) + " " + line.substring(line.indexOf(',')+1);
//                    conferencesJournals.add(l.trim());
//                }
//                if(line.contains("/")){
//                    String l = line.substring(0,line.indexOf('/')) + " " + line.substring(line.indexOf('/')+1);
//                    conferencesJournals.add(l.trim());
//                    String l2 = line.substring(0,line.indexOf('/')) + line.substring(line.indexOf('/')+1);
//                    conferencesJournals.add(l2.trim());
//                }
//                if(line.contains(":")){
//                    String l = line.substring(0,line.indexOf(':')) + line.substring(line.indexOf(':')+1);
//                    conferencesJournals.add(l.trim());
//                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        printInFile(conferencesJournals,"IEEE2.txt");
        return conferencesJournals;
    }

    private static void swapEnding(Set<String> conferencesJournals, Set<String> otherFormats) {
        ArrayList<String> copyOtherFormats = new ArrayList<String>(otherFormats);
        String[] endings = {
                "international association of",
                "international forum of",
                "ieee/acm international workshop on",
                "ieee/acm international conference on",
                "acis/jnu international conference on",
                "ieee/wic/acm international conference on",
                "international joint conference on",
                "world congress on",
                "ieee workshop on",
                "ieee",
                "international symposium on",
                "international multi-symposiums on",
                "international conference on",
                "ieee symposium on",
                "american symposium on",
                "international workshop on",
                "ieee international",
                "indian conference on",
                "national conference on",
                "symposium on",
                "conference on",
                "workshops on",
                "workshop on",
                "international"
        };
        for (String copyLine: copyOtherFormats){
            for(int i=0; i<endings.length; i++){
                if(copyLine.trim().contains(endings[i])&&!copyLine.trim().contains(endings[i]+" ")){
                    String tempLine = endings[i]+" "+copyLine.substring(0, copyLine.indexOf(endings[i]));
                    tempLine = removeExtraSpace(tempLine);
                    conferencesJournals.add(tempLine);
                    otherFormats.add(tempLine);
                    break;
                }
            }
        }
    }

    private static void addVariousFormats(Set<String> conferencesJournals, Set<String> otherFormats, ArrayList<String> copyOtherFormats) {
        String[] acmIeees = {"acm/ieee", "ieee/acm", "acm & ieee", "acm and ieee", "acm-ieee", "ieee-acm"};
        boolean checked = false;
        for(String lineCopy : copyOtherFormats) {
            for (int i = 0; i < acmIeees.length; i++) {
                if (lineCopy.indexOf(acmIeees[i])==0 || lineCopy.indexOf(acmIeees[i])==1) {
                    checked = true;
                    String tempLine = lineCopy.substring(acmIeees[i].length()).trim();
                    boolean ifip = false;
                    if(tempLine.indexOf("/ifip")==0){
                        tempLine = tempLine.substring(5).trim();
                    }
                    boolean scs = false;
                    if(tempLine.indexOf("/scs")==0){
                        tempLine = tempLine.substring(4).trim();
                    }
                    tempLine = removeExtraSpace(removeIndex(tempLine, false).trim());
                    conferencesJournals.add("acm " + tempLine);
                    otherFormats.add("acm " + tempLine);
                    conferencesJournals.add("ieee " + tempLine);
                    otherFormats.add("ieee " + tempLine);
                    conferencesJournals.add("acm ieee " + tempLine);
                    otherFormats.add("acm ieee " + tempLine);
                    conferencesJournals.add("ieee/acm " + tempLine);
                    otherFormats.add("ieee/acm " + tempLine);
                    conferencesJournals.add("acm/ieee " + tempLine);
                    otherFormats.add("acm/ieee " + tempLine);
                    conferencesJournals.add("ieee acm " + tempLine);
                    otherFormats.add("ieee acm " + tempLine);
                    if(ifip){
                        addStr(conferencesJournals, otherFormats, tempLine, "ifip");
                    }
                    if(scs){
                        addStr(conferencesJournals, otherFormats, tempLine, "scs");
                    }
                }
            }
        }
        if(!checked){
            for(String lineCopy : copyOtherFormats) {
                if (lineCopy.indexOf("ieee/ifip") == 0 || lineCopy.indexOf("ieee/ifip") == 1) {
                    checked = true;
                    String tempLine = lineCopy.substring(9).trim();
                    tempLine = removeIndex(tempLine, false).trim();
                    conferencesJournals.add("ifip " + tempLine);
                    otherFormats.add("ifip " + tempLine);
                    conferencesJournals.add("ieee " + tempLine);
                    otherFormats.add("ieee " + tempLine);
                    conferencesJournals.add("ifip ieee " + tempLine);
                    otherFormats.add("ifip ieee " + tempLine);
                    conferencesJournals.add("ieee/ifip " + tempLine);
                    otherFormats.add("ieee/ifip " + tempLine);
                    conferencesJournals.add("ifip/ieee " + tempLine);
                    otherFormats.add("ifip/ieee " + tempLine);
                    conferencesJournals.add("ieee ifip " + tempLine);
                    otherFormats.add("ieee ifip " + tempLine);
                }
            }
        }
        if(!checked){
            for(String lineCopy : copyOtherFormats) {
                if (lineCopy.indexOf("ieee/wic") == 0 || lineCopy.indexOf("ieee/wic") == 1) {
                    checked = true;
                    String tempLine = lineCopy.substring(9).trim();
                    tempLine = removeIndex(tempLine, false).trim();
                    conferencesJournals.add("wic " + tempLine);
                    otherFormats.add("wic " + tempLine);
                    conferencesJournals.add("ieee " + tempLine);
                    otherFormats.add("ieee " + tempLine);
                    conferencesJournals.add("wic ieee " + tempLine);
                    otherFormats.add("wic ieee " + tempLine);
                    conferencesJournals.add("ieee/wic " + tempLine);
                    otherFormats.add("ieee/wic " + tempLine);
                    conferencesJournals.add("wic/ieee " + tempLine);
                    otherFormats.add("wic/ieee " + tempLine);
                    conferencesJournals.add("ieee wic " + tempLine);
                    otherFormats.add("ieee wic " + tempLine);
                }
            }
        }
        if(!checked){
            for(String lineCopy : copyOtherFormats) {
                if (lineCopy.indexOf("acm/scs") == 0 || lineCopy.indexOf("acm/scs") == 1) {
                    checked = true;
                    String tempLine = lineCopy.substring(7).trim();
                    tempLine = removeIndex(tempLine, false).trim();
                    conferencesJournals.add("acm " + tempLine);
                    otherFormats.add("acm " + tempLine);
                    conferencesJournals.add("scs " + tempLine);
                    otherFormats.add("scs " + tempLine);
                    conferencesJournals.add("acm scs " + tempLine);
                    otherFormats.add("acm scs " + tempLine);
                    conferencesJournals.add("scs/acm " + tempLine);
                    otherFormats.add("scs/acm " + tempLine);
                    conferencesJournals.add("acm/scs " + tempLine);
                    otherFormats.add("acm/scs " + tempLine);
                    conferencesJournals.add("scs acm " + tempLine);
                    otherFormats.add("scs acm " + tempLine);
                }
            }
        }
        if(!checked){
            String[] acmIeees2 = {"ieee/wic/acm", "ieee / wic / acm"};
            for(String lineCopy : copyOtherFormats) {
                for (int i = 0; i < acmIeees2.length; i++) {
                    if (lineCopy.indexOf(acmIeees2[i])==0 || lineCopy.indexOf(acmIeees2[i])==1) {
                        checked = true;
                        String tempLine = lineCopy.substring(acmIeees2[i].length()).trim();
                        tempLine = removeExtraSpace(removeIndex(tempLine, false).trim());
                        conferencesJournals.add("acm " + tempLine);
                        otherFormats.add("acm " + tempLine);
                        conferencesJournals.add("ieee " + tempLine);
                        otherFormats.add("ieee " + tempLine);
                        conferencesJournals.add("acm ieee " + tempLine);
                        otherFormats.add("acm ieee " + tempLine);
                        conferencesJournals.add("ieee/acm " + tempLine);
                        otherFormats.add("ieee/acm " + tempLine);
                        conferencesJournals.add("acm/ieee " + tempLine);
                        otherFormats.add("acm/ieee " + tempLine);
                        conferencesJournals.add("ieee acm " + tempLine);
                        otherFormats.add("ieee acm " + tempLine);
                        addStr(conferencesJournals, otherFormats, tempLine, "wic");
                    }
                }
            }
        }
    }

    private static void addStr(Set<String> conferencesJournals, Set<String> otherFormats, String tempLine, String journal) {
        conferencesJournals.add(journal+" " + tempLine);
        otherFormats.add(journal+" " + tempLine);

        conferencesJournals.add("ieee/" +journal+" " + tempLine);
        otherFormats.add("ieee/"+journal+" " + tempLine);
        conferencesJournals.add(journal+ "/ieee " + tempLine);
        otherFormats.add(journal+ "/ieee " + tempLine);
        conferencesJournals.add("acm/"+journal+" " + tempLine);
        otherFormats.add("acm/"+journal+" " + tempLine);
        conferencesJournals.add(journal+ "/acm " + tempLine);
        otherFormats.add(journal+ "/acm " + tempLine);

        conferencesJournals.add("ieee "+journal+" " + tempLine);
        otherFormats.add("ieee " +journal+" " + tempLine);
        conferencesJournals.add(journal+" " +"ieee " + tempLine);
        otherFormats.add(journal+" " +"ieee " + tempLine);
        conferencesJournals.add("acm " +journal+" " + tempLine);
        otherFormats.add("acm " +journal+" " + tempLine);
        conferencesJournals.add(journal+" " +"acm " + tempLine);
        otherFormats.add(journal+" " +"acm " + tempLine);

        conferencesJournals.add("acm ieee "+journal+" " + tempLine);
        otherFormats.add("acm ieee "+journal+" " + tempLine);
        conferencesJournals.add("ieee acm "+journal+" " + tempLine);
        otherFormats.add("ieee acm "+journal+" " + tempLine);
        conferencesJournals.add("ieee "+journal+" "+"acm " + tempLine);
        otherFormats.add("ieee "+journal+" "+"acm " + tempLine);
        conferencesJournals.add("acm "+journal+" "+"ieee " + tempLine);
        otherFormats.add("acm "+journal+" "+"ieee " + tempLine);
        conferencesJournals.add(journal+" ieee acm " + tempLine);
        otherFormats.add(journal+" ieee acm " + tempLine);

        conferencesJournals.add("ieee/acm/"+journal+" " + tempLine);
        otherFormats.add("ieee/acm/"+journal+" " + tempLine);
        conferencesJournals.add("acm/ieee/"+journal+" " + tempLine);
        otherFormats.add("acm/ieee/"+journal+" " + tempLine);
        conferencesJournals.add("acm/"+journal+"/ieee " + tempLine);
        otherFormats.add("acm/"+journal+"/ieee " + tempLine);
        conferencesJournals.add("ieee/"+journal+"/acm " + tempLine);
        otherFormats.add("ieee/"+journal+"/acm " + tempLine);
        conferencesJournals.add(journal+"/ieee/acm " + tempLine);
        otherFormats.add(journal+"/ieee/acm " + tempLine);
        conferencesJournals.add(journal+"/acm/ieee " + tempLine);
        otherFormats.add(journal+"/acm/ieee " + tempLine);
    }

    private static String removeIndex(String line, boolean isMiddle) {
        String index = containsIndex(line.trim(), isMiddle);
        while(index!=null){
            String tempLine = line.substring(0,line.indexOf(index+""))+line.substring(line.indexOf(index+"")+index.length());
            line = tempLine;
            index = containsIndex(line, isMiddle);
        }
        return line;
    }

    private static String removeExtraSpace(String line) {
        while (line.contains("  ")){
            String tempLine = line.substring(0,line.indexOf("  "))+line.substring(line.indexOf("  ")+1);
            line = tempLine;
        }
        return line;
    }

    private static String removeSubstring(String line, String subString) {
        while(line.contains(subString)){
            String tempLine = line.substring(0,line.indexOf(subString)) + line.substring(line.indexOf(subString)+subString.length());
            line = tempLine;
        }
        return line;
    }

    private static String removeYear(String line) {
        int year = containsYear(line);
        while(year>0){
            String tempLine = line.substring(0,line.indexOf(year+""))+line.substring(line.indexOf(year+"")+4);
            line = tempLine;
            year = containsYear(line);

        }
        return line;
    }

    private static String removeParanthesis(String line, String s, String s2) {
        while (line.contains(s)) {
            int endIndex = line.indexOf(s2);
            if (!line.contains(s2)) {
                endIndex = line.length() - 2;
            }
            String tempLine = line.substring(0, line.indexOf(s)) + line.substring(endIndex + 1);
            line = tempLine;
        }
        return line;
    }

    private static String containsIndex(String line, boolean isMiddle) {
        line = line.trim();
        String[] indexes = {"twenty-first", "twenty-fifth", "twenty-second", "twenty-seventh", "twenty-third","thirty-first", "thirty-seventh", "first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth", "eleventh", "twelfth", "thirteenth", "fourteenth", "fifteenth", "seventeenth", "sixteenth", "eighteenth", "nineteenth", "twentieth"};
        for(int i=0; i<indexes.length; i++){
            if(isMiddle)
                if(line.contains(indexes[i]))
                    return indexes[i];
            if(line.indexOf(indexes[i])==0 || line.indexOf(indexes[i])==1)
                return indexes[i];
        }
        String[] indexes2 = {"40th", "41st", "42th", "42nd", "43nd", "43rd", "43th", "44th", "45th", "46th", "47th", "48th", "49th", "50th", "30th", "31st", "32nd", "33th", "34th", "35th", "36th", "37th", "38th", "39th", "21st", "22nd", "33rd", "23rd", "24th", "25th", "26th", "27th", "28th", "29th", "11th", "12th", "13th", "14th","15th" , "16th","17th", "18th", "19th", "1st", "2nd", "3rd", "4th","5th" , "6th","7th" , "8th", "9th", "10th", "20th"};
        for(int i=0; i<indexes2.length; i++){
            if(isMiddle)
                if(line.contains(indexes2[i]))
                    return indexes2[i];
            if(line.indexOf(indexes2[i])==0 || line.indexOf(indexes2[i])==1){
                return indexes2[i];
            }
        }
        String[] indexes3 = { "xxxiii", "xxvi", "xxv", "xxix", "xxiv", "xxiii", "xxii","xxi",  "xx", "xviii", "xviii", "xvii", "xvi", "xv", "xiv", "xiii", "xii","viii", "vii", "vi", "ii.", "v", "x"};
        for(int i=0; i<indexes3.length; i++){
            if(isMiddle)
                if(line.contains(indexes3[i]+" "))
                    return indexes3[i];
            if(line.indexOf(indexes3[i]+" ")==0 || line.indexOf(indexes3[i]+" ")==1){
                return indexes3[i];
            }
        }
        if(line.contains(" ii")){
            return " ii";
        }
        String[] indexes4 = {"\'88", "\'90" , "\'92", "\'93", "\'98", "\'99", "'97"};
        for(int i=0; i<indexes4.length; i++){
            if(line.contains(indexes4[i])){
                return indexes4[i];
            }
        }
        return null;
    }

    private static int containsYear(String line) {
        for(int i=1964; i<2019; i++){
            if(line.contains(i+"")){
                return i;
            }
        }
        if(line.contains("1951"))
            return 1951;
        return -1;
    }

    private static void fillHyperedgeLogsFromVenues(){
        hyperedgeLogs = new ArrayList<HyperedgeLog>();

        JSONParser parser = new JSONParser();

        try {
            int fileIndex=0;
            for(int folderIndex = 0; folderIndex<9; folderIndex++){
                int fIndex;
                for(fIndex = fileIndex; fIndex<fileIndex+20; fIndex++){
                    String pathname = "C://Users/Maryam/Microsoftdataset/venue_papers_" + folderIndex + "/mag_venue_papers_"+fIndex+".txt";
                    print("Reading: "+pathname);
                    BufferedReader br = new BufferedReader(new FileReader(new File(pathname)));
                    String line;
                    while ((line = br.readLine()) != null) {
                        Object obj = parser.parse(line);
                        JSONObject jsonObject = (JSONObject) obj;
                        JSONArray authors = (JSONArray) jsonObject.get("authors");
                        if (authors != null) {
                            long year = (Long) jsonObject.get("year");
                            List<String> authorNames = new ArrayList<String>();
                            for (Object authorObject : authors) {
                                JSONObject author = (JSONObject) authorObject;
                                authorNames.add((String) author.get("name"));
                            }
                            HyperedgeLog hyperedgeLog = new HyperedgeLog(year, authorNames);
                            int firstLogIndex =(int) year-2001-4;
                            if(firstLogIndex<0)
                                firstLogIndex=0;

                            int lastLogIndex = firstLogIndex + 5;
                            if(lastLogIndex>12){
                                lastLogIndex  = 13;
                            }
                            for(int firstIndex=firstLogIndex; firstIndex< lastLogIndex; firstIndex++){
                                if(allHyperLogs[firstIndex]==null){
                                    allHyperLogs[firstIndex] = new ArrayList<HyperedgeLog>();
                                }
                                allHyperLogs[firstIndex].add(hyperedgeLog);
                            }

                            if((int) year>=2006){
                                int testIndex = (int) year-2006;
                                if(testHyperLogs[testIndex]==null){
                                    testHyperLogs[testIndex] = new ArrayList<HyperedgeLog>();
                                }
                                testHyperLogs[testIndex].add(hyperedgeLog);
//                                if((int)year==2012 || (int)year==2013 || (int)year==2014|| (int)year==2015|| (int)year==2016|| (int)year==2017){
//                                    print("YEAR: "+(int)year);
//                                    print(firstLogIndex+"-"+lastLogIndex+"-"+testIndex);
//                                }
                            }

                        }
                    }
                }
                fileIndex = fIndex;
                break;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
