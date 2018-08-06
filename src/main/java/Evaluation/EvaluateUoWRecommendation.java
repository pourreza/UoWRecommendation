package Evaluation;

import UoWRecommendation.UoWQuery;
import UoWRecommendation.UoWRecommendation;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import serviceWorkflowNetwork.AnalyzeDataOptimized;
import serviceWorkflowNetwork.OOperation;
import serviceWorkflowNetwork.SService;
import serviceWorkflowNetwork.WorkflowVersion;
import utilities.Printer;

import java.io.*;
import java.util.*;

import static utilities.Printer.print;

public class EvaluateUoWRecommendation {
    private static final int TEST_SIZE = 11;
    private static final int MIN_YEAR = 2007;
    private static final double TOP_RECOMMENDATION_SIZE = 5;

    private static Set<WorkflowVersion> workflows;
    private static Set<SService> services;
    private static Set<OOperation> operations;
    private static Map<String, SService> serviceMap;

    public static Graph<String, DefaultEdge>[] uowNetworkHistory;
    public static Set<WorkflowVersion>[] workflowsForTest;
    private static Set<WorkflowVersion>[] uowNetworkWorkflowsHistory;

    private static ArrayList<WorkflowWrapper> allSortedWorkflows;

    private static Graph<String, DefaultEdge>[] uowNetworkTest;
    private static Set<SService>[] servicesTest ;
    private static Set<OOperation>[] operationsTest ;
    private static Set<WorkflowVersion>[] workflowVersionsTest ;

    private static Map<WorkflowVersion, Graph<String, DefaultEdge>> workflowDirectedGraphs = new HashMap<WorkflowVersion, Graph<String, DefaultEdge>>();
    private static int maxUpstreamSubPathSize = 4;


    public static void main(String[] args) {
//        setupEnvironment();


        //for first experiment
        try {
            FileInputStream fout = new FileInputStream("test2-uowNetwork");
            FileInputStream fout2 = new FileInputStream("test2-allServices");
            FileInputStream fout3 = new FileInputStream("test2-allOperations");
            FileInputStream fout4 = new FileInputStream("test2-allWorkflows");
            FileInputStream fout5 = new FileInputStream("allSortedWorkflowWrappers");
            FileInputStream fout6 = new FileInputStream("allServices");
            ObjectInputStream oos = new ObjectInputStream(fout);
            ObjectInputStream oos2 = new ObjectInputStream(fout2);
            ObjectInputStream oos3 = new ObjectInputStream(fout3);
            ObjectInputStream oos4 = new ObjectInputStream(fout4);
            ObjectInputStream oos5 = new ObjectInputStream(fout5);
            ObjectInputStream oos6 = new ObjectInputStream(fout6);
            uowNetworkTest = (Graph<String, DefaultEdge>[]) oos.readObject();
            servicesTest = (Set<SService>[]) oos2.readObject();
            operationsTest = (Set<OOperation>[]) oos3.readObject();
            workflowVersionsTest = (Set<WorkflowVersion>[]) oos4.readObject();
            allSortedWorkflows = (ArrayList<WorkflowWrapper>) oos5.readObject();
            services = (Set<SService>) oos6.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        print("*******************************************");
        print("TEST 1: ");

        int largest = 0;
        int workflowCount = 0;

//        /***********************************************************
//         * This part is added for the flow recommendation analysis
//         */
//        ArrayList<ArrayList<String>> upstreamPaths = new ArrayList<ArrayList<String>>();
//        for(int testIndexx = 1718; testIndexx>=0; testIndexx--){
//            print("testIndex: "+ testIndexx);
////            Set<WorkflowVersion> networkWorkflows = workflowVersionsTest[testIndexx];
////            workflow id ye seri node dare va har node ye seri
//            WorkflowVersion testWorkflow = allSortedWorkflows.get(testIndexx).getWorkflow();
//            getUpstreamPaths(testWorkflow, upstreamPaths, workflowDirectedGraphs.get(testWorkflow));
//        }
//        Map<String, ArrayList<String>> patternTable = combineUpstreams(upstreamPaths);
//        Map<String, ArrayList<Double>> scores = findScores(upstreamPaths, patternTable);

        ArrayList<ArrayList<String>> allServiceWorkflowIndexs = new ArrayList<ArrayList<String>>();

        int from = 1718;
        for (int testIndex = 1718; testIndex >=0; testIndex--) {
            if(testIndex==143 || testIndex==115)
                continue;
            print("Test index: " + testIndex);
            Graph<String, DefaultEdge> uowNetwork = uowNetworkTest[testIndex];
            Set<WorkflowVersion> networkWorkflows = workflowVersionsTest[testIndex];
            Set<SService> networkServices = new HashSet<SService>(servicesTest[testIndex]);
            Set<OOperation> networkOperations = new HashSet<OOperation>(operationsTest[testIndex]);
            UoWRecommendation recommendation = new UoWRecommendation(uowNetwork, networkServices, networkWorkflows, networkOperations);

            WorkflowVersion testWorkflow = allSortedWorkflows.get(testIndex).getWorkflow();

            if(testWorkflow.getWorkflow().getIndex()==916 && testWorkflow.getVersionIndex()==1){
                print("Found itttt");
            }
            ArrayList<SService> goalServices = new ArrayList<SService>(getWorkflowServices(testWorkflow));
            print(testWorkflow.getWorkflow().getURL());

            boolean foundLargeUOW = false;
            if(goalServices.size()>0) { // It is definitley true cause allSortedWorkflows only has has workflows with more services
                List<Graph<String, DefaultEdge>> recommendedUoWs = null;
                UoWQuery query = null;
                Set<SService> currentServices = null;
                Graph<String, DefaultEdge> currentGraph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
                double steps = 0;
                double linksNeedToBeDecided = 0;
                double countHits = 0;
                boolean breaked = false;


                int savedStepssss = 0;
                int savedLinkssss = 0;

                ArrayList<String> servicesWorkflows = new ArrayList<String>();
                do {
                    if (recommendedUoWs == null) {
                        if (steps > 1) {
                            print("HHEEEEEEEEEEEELP, I COULD NOT FIND ANYTHING FOR RECOMMENDATIONNNNNNN!!!!!!!!!!!!!!!!!");
                        }
                        query = new UoWQuery(testWorkflow, null);
                        currentServices = new HashSet<SService>();
                    }
                    steps++;
                    /****************************
                     * This part is added so that we can compare flow recommendation with our results
                     */
                    recommendedUoWs = recommendation.recommendUOWs(query);
//                    recommendedUoWs = recommendation.recommendFlow(query, patternTable, scores);
                    if (recommendedUoWs.size() == 0) {
                        //this means that we can not find more
                        breaked = true;
                        break;
                    }
                    Set<SService> recommendedServices = services(recommendedUoWs.get(0).vertexSet());
                    if(steps>=1){
                        servicesWorkflows.add(getworkflowIndexes(recommendedServices, networkWorkflows));
                    }
                    if(recommendedServices.size()>1)
                        foundLargeUOW = true;
                    if(recommendedServices.size()>largest)
                        largest = recommendedServices.size();
                    savedStepssss += recommendedServices.size()-1;
                    savedLinkssss += recommendedServices.size() * (recommendedServices.size()-1)/2;

                    // for finding the number of hits
                    ArrayList<SService> foundServices = new ArrayList<SService>();
                    for (SService service : goalServices) {
                        if (recommendedServices.contains(service)) {
                            foundServices.add(service);
                        }
                    }
                    if (foundServices.size() > 0) {
                        countHits++;
                        goalServices.removeAll(foundServices);
                    }
                    //for finding number of links the user has to decide himself
                    if (currentServices != null && currentServices.size() > 0) {
                        boolean isAttachedToCurrentServices = false;
                        for (SService service : currentServices) {
                            if (recommendedServices.contains(service)) {
                                isAttachedToCurrentServices = true;
                            }
                        }
                        if (!isAttachedToCurrentServices) {
                            linksNeedToBeDecided++;
                        }
                    }
                    int currentServiceSize = currentServices.size();
                    currentServices.addAll(recommendedServices);
                    if(currentServices.size()==currentServiceSize){
                        // the algorithm cannot find more usefulServices
                        // or we should go to the next of the recommended list
                        breaked = true;
                        break;
                    }
                    Graphs.addGraph(currentGraph, recommendedUoWs.get(0));
                    query = new UoWQuery(testWorkflow, currentServices);
                } while (query.remainingProcessors().size() > 0);

                if(foundLargeUOW)
                    workflowCount++;

                int workflowServicesSize = getWorkflowServices(testWorkflow).size();
                double hitRate = countHits / steps;
                if (breaked)
                    steps--; // it means that our last step was unsuccessful so it shouldn't be counted
                double linksSaved = (steps - 1 - linksNeedToBeDecided) / (steps - 1); // first step for the first recommendation there is no service that need to be attached to the recommended one
                if(steps==1){
                    linksSaved = 0;
                }
                double stepsSaved = (workflowServicesSize - steps) / workflowServicesSize;
                if (steps == 0) {
                    stepsSaved = 0;
                    linksSaved = 0;
                }
//                avgStructureSimilarity += recommendedStructure(recommendedUoWs, workflow);
                double recommendedStructure = structure(currentGraph);
                double workflowStructure = structure(testWorkflow.getDirectedProcessorGraph(), testWorkflow);
                double precision = precision(currentGraph, testWorkflow);
                double recall = recall(currentGraph, testWorkflow);
                allSortedWorkflows.get(testIndex).setPrecisionRecall(precision, recall);
                allSortedWorkflows.get(testIndex).setHitCounts(countHits);
                allSortedWorkflows.get(testIndex).setLinksNeedDecision(savedLinkssss);
                allSortedWorkflows.get(testIndex).setSteps(savedStepssss);
                allSortedWorkflows.get(testIndex).setStructure(recommendedStructure);
                allSortedWorkflows.get(testIndex).setWorkflowStructer(workflowStructure);
                allSortedWorkflows.get(testIndex).setRecommendedGraphServices(currentGraph.vertexSet().size());

                allServiceWorkflowIndexs.add(servicesWorkflows);
            }
//            print("Recommendation Avg Structure Similarity: " + avgStructureSimilarity);

        }

        print("largest: "+ largest);
        print("count workflows : "+ workflowCount);
//        if(testIndex%100==0) {
            saveResults(1718, 0, allServiceWorkflowIndexs);
//            from = testIndex;
//        }

    }

    private static void saveResults(int from, int testIndex, ArrayList<ArrayList<String>> allServiceWorkflowIndexs) {
        WorkflowVersion[] workflowVersions = new WorkflowVersion[from-testIndex+1];
        double[] goalServicesNum = new double[from-testIndex+1];
        double[] precisions = new double[from-testIndex+1];
        double[] recalls = new double[from-testIndex+1];
        double[] fscores = new double[from-testIndex+1];
        double[] steps = new double[from-testIndex+1];
        double[] links = new double[from-testIndex+1];
        double[] hits = new double[from-testIndex+1];
        double[] recommendedStructure = new double[from-testIndex+1];
        double[] workflowStructures = new double[from-testIndex+1];
        double[] recommendedGraphServices = new double[from-testIndex+1];

        int start = 0;
        int end = from-testIndex+1;
        for(int i=start; i<end; i++){
            workflowVersions[i] = allSortedWorkflows.get(from-i).getWorkflow();
            goalServicesNum[i] = getWorkflowServices(allSortedWorkflows.get(from-i).getWorkflow()).size();
            precisions[i] = allSortedWorkflows.get(from-i).getPrecision();
            recalls[i] = allSortedWorkflows.get(from-i).getRecall();
            fscores[i] = allSortedWorkflows.get(from-i).getRecall();
            steps[i] = allSortedWorkflows.get(from-i).getSteps();
            links[i] = allSortedWorkflows.get(from-i).getLinks();
            hits[i] = allSortedWorkflows.get(from-i).getHits();
            recommendedStructure[i] = allSortedWorkflows.get(from-i).getStructure();
            workflowStructures[i] = allSortedWorkflows.get(from-i).getWorkflowStructer();
            recommendedGraphServices[i] = allSortedWorkflows.get(from-i).getRecommendedGraphServices();
        }
        Printer.saveToExcel("Test2-Results-New-Metrics-Compared-With-Flow-Recommendation"+from+"-to-"+testIndex, workflowVersions, goalServicesNum, precisions, recalls, fscores, steps, links, allServiceWorkflowIndexs, hits, recommendedStructure, workflowStructures, recommendedGraphServices);
    }

    private static String getworkflowIndexes(Set<SService> recommendedServices, Set<WorkflowVersion> networkWorkflows) {
        String servicesWorkflows = "";
        for(SService service: recommendedServices){
            String serviceInfo = service.getURL();
            for(WorkflowVersion workflowVersion: networkWorkflows){
                ArrayList<OOperation> externalOperations = workflowVersion.getExternalOperations();
                for(OOperation operation: externalOperations){
                    if(operation.getService().equals(service)){
                        serviceInfo+=("!!!!"+workflowVersion.getWorkflow().getIndex()+"-"+workflowVersion.getVersionIndex());
                        break;
                    }
                }
            }
            serviceInfo+="$$$$";
            servicesWorkflows += serviceInfo;
        }
        return servicesWorkflows;
    }

    private static Map<String, ArrayList<Double>> findScores(ArrayList<ArrayList<String>> upstreamPaths, Map<String, ArrayList<String>> patternTable) {
        Map<String, ArrayList<Double>> scoreMap = new HashMap<String, ArrayList<Double>>();
        for(String node: patternTable.keySet()){
            ArrayList<String> upstreams = patternTable.get(node);
            ArrayList<Double> scores = new ArrayList<Double>();
            for(int i=0; i<upstreams.size(); i++){
                scores.add(confidence(node, upstreams.get(i), upstreamPaths));
            }
            scoreMap.put(node, scores);
        }
        return scoreMap;
    }

    private static Double confidence(String node, String path, ArrayList<ArrayList<String>> upstreamPaths) {
        int confidence = 0;
        double occurenceTime = 0;
        for(int i=0; i<upstreamPaths.size(); i++){
            for(int j=1; j<upstreamPaths.get(i).size(); j++){
                if(upstreamPaths.get(i).get(j).equals(path)){
                    occurenceTime++;
                }
                if(upstreamPaths.get(i).get(j).contains(path) && upstreamPaths.get(i).get(j).contains(node)){
                    confidence++;
                }
            }
        }
        return confidence/occurenceTime;
    }

    private static Map<String, ArrayList<String>> combineUpstreams(ArrayList<ArrayList<String>> upstreamPaths) {
        Map<String, ArrayList<String>> patternTable = new HashMap<String, ArrayList<String>>();
        for(int i=0; i<upstreamPaths.size(); i++){
            String node = upstreamPaths.get(i).get(0);
            ArrayList<String> upstreams = new ArrayList<String>();
            if(patternTable.containsKey(node)){
                upstreams = patternTable.get(node);
            }
            for(int j=1; j<upstreamPaths.get(i).size(); j++){
                String newUpstream = upstreamPaths.get(i).get(j);
                if(!upstreams.contains(newUpstream)){
                    upstreams.add(newUpstream);
                }
            }
            patternTable.put(node, upstreams);
        }
        return patternTable;
    }

    private static void getUpstreamPaths(WorkflowVersion testWorkflow, ArrayList<ArrayList<String>> upstreamPaths, Graph<String, DefaultEdge> directedGraph) {
        ArrayList<SService> goalServices = new ArrayList<SService>(getWorkflowServices(testWorkflow));
        for(SService goalService: goalServices){
            ArrayList<String> upstreamPathsForNode = findUpstreamSubPathforService(goalService, directedGraph);
            if(upstreamPaths!=null) {
                upstreamPaths.add(upstreamPathsForNode);
            }
        }
    }

    private static ArrayList<String> findUpstreamSubPathforService(SService goalService, Graph<String, DefaultEdge> directedGraph) {
        ArrayList<String> upstreamPaths = new ArrayList<String>();
        Graph<String, DefaultEdge> graph = reverseGraph(directedGraph);
        String sourceNode = goalService.getURL();
        for(String targetNode: graph.vertexSet()){
            if(!targetNode.equals(sourceNode)) {
                DijkstraShortestPath<String, DefaultEdge> dijkstraAlg = new DijkstraShortestPath<String, DefaultEdge>(graph);
                GraphPath<String, DefaultEdge> path = dijkstraAlg.getPaths(sourceNode).getPath(targetNode);
                if (path.getLength() < maxUpstreamSubPathSize) {
                    String upstreamSubPath = "";
                    for (DefaultEdge pathEdge : path.getEdgeList()) {
                        String edgeSource = path.getGraph().getEdgeSource(pathEdge);
                        String edgeTarget = path.getGraph().getEdgeTarget(pathEdge);
                        if(!upstreamSubPath.contains(edgeSource)){
                            upstreamSubPath+="@"+edgeSource;
                        }
                        upstreamSubPath+="@"+ edgeTarget;
                    }
                    if (upstreamPaths.size() > 0) {
                        upstreamPaths.add(sourceNode);
                    }
                    upstreamPaths.add(upstreamSubPath);
                }
            }
        }
        if(upstreamPaths.size()>0)
            return upstreamPaths;
        return null;
    }

    private static Graph<String, DefaultEdge> reverseGraph(Graph<String, DefaultEdge> directedGraph) {
        Graph<String, DefaultEdge> reverseGraph = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        for(String node: directedGraph.vertexSet()){
            reverseGraph.addVertex(node);
        }
        for(DefaultEdge edge: directedGraph.edgeSet()){
            String edgeSource = directedGraph.getEdgeSource(edge);
            String edgeTarget = directedGraph.getEdgeTarget(edge);
            reverseGraph.addEdge(edgeTarget, edgeSource);
        }
        return reverseGraph;
    }

    private static void saveResults(int from, int testIndex) {
        WorkflowVersion[] workflowVersions = new WorkflowVersion[from-testIndex+1];
        double[] goalServicesNum = new double[from-testIndex+1];
        double[] precisions = new double[from-testIndex+1];
        double[] recalls = new double[from-testIndex+1];
        double[] fscores = new double[from-testIndex+1];
        double[] steps = new double[from-testIndex+1];
        double[] links = new double[from-testIndex+1];
        double[] hits = new double[from-testIndex+1];
        double[] recommendedStructure = new double[from-testIndex+1];
        double[] workflowStructures = new double[from-testIndex+1];
        double[] recommendedGraphServices = new double[from-testIndex+1];

        int start = 0;
        int end = from-testIndex+1;
        for(int i=start; i<end; i++){
            workflowVersions[i] = allSortedWorkflows.get(from-i).getWorkflow();
            goalServicesNum[i] = getWorkflowServices(allSortedWorkflows.get(from-i).getWorkflow()).size();
            precisions[i] = allSortedWorkflows.get(from-i).getPrecision();
            recalls[i] = allSortedWorkflows.get(from-i).getRecall();
            fscores[i] = allSortedWorkflows.get(from-i).getRecall();
            steps[i] = allSortedWorkflows.get(from-i).getSteps();
            links[i] = allSortedWorkflows.get(from-i).getLinks();
            hits[i] = allSortedWorkflows.get(from-i).getHits();
            recommendedStructure[i] = allSortedWorkflows.get(from-i).getStructure();
            workflowStructures[i] = allSortedWorkflows.get(from-i).getWorkflowStructer();
            recommendedGraphServices[i] = allSortedWorkflows.get(from-i).getRecommendedGraphServices();
        }
        Printer.saveToExcel("Test1-Results-New-Metrics-Compared-With-Flow-Recommendation"+from+"-to-"+testIndex, workflowVersions, goalServicesNum, precisions, recalls, fscores, steps, links, hits, recommendedStructure, workflowStructures, recommendedGraphServices);
    }

    private static double structure(DirectedGraph<String, DefaultEdge> directedProcessorGraph, WorkflowVersion workflowVersion) {
        DefaultDirectedGraph<String, DefaultEdge> serviceDirectedGraph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        for (int i = 0; i < workflowVersion.getExternalOperations().size(); i++) {
            OOperation source = workflowVersion.getExternalOperations().get(i);
            for (int j = 0; j < workflowVersion.getExternalOperations().size(); j++) {
                if (!workflowVersion.getExternalOperations().get(j).equals(workflowVersion.getExternalOperations().get(i))) {
                    OOperation sink = workflowVersion.getExternalOperations().get(j);
                    GraphPath<String, DefaultEdge> path = DijkstraShortestPath.findPathBetween(directedProcessorGraph, source.getProcessorName(), sink.getProcessorName());
                    serviceDirectedGraph.addVertex(source.getService().getURL());
                    serviceDirectedGraph.addVertex(sink.getService().getURL());
                    if(path==null){
                        path = DijkstraShortestPath.findPathBetween(directedProcessorGraph, sink.getProcessorName(), source.getProcessorName());
                    }
                    if (path != null) {
                        serviceDirectedGraph.addEdge(source.getService().getURL(), sink.getService().getURL());
                    }
                }
            }
        }
        return structure(serviceDirectedGraph);
    }

    private static double structure(Graph<String, DefaultEdge> graph) {
        double structureSimilarity = 0;
        AsUndirectedGraph<String, DefaultEdge> undirectedGraph = new AsUndirectedGraph<String, DefaultEdge>(graph);
        DijkstraShortestPath<String, DefaultEdge> dijkstraAlg = new DijkstraShortestPath<String, DefaultEdge>(undirectedGraph);
        for(String source: graph.vertexSet()){
            for(String sink: graph.vertexSet()){
                if(!source.equals(sink)){
                    GraphPath<String, DefaultEdge> path = dijkstraAlg.getPaths(source).getPath(sink);
                    if(path!=null)
                        structureSimilarity += 1;
                }
            }
        }
//        for (Graph<String, DefaultEdge> recommendedUoW : recommendedUoWs) {
//            if (areSimilar(recommendedUoW, workflow))
//                structureSimilarity++;
//        }
//        return structureSimilarity / recommendedUoWs.size();
        return structureSimilarity/2;
    }

    private static Set<OOperation> operationsInNetwork(Set<WorkflowVersion> uowNetworkWorkflows) {
        Set<OOperation> operationsInNetwork = new HashSet<OOperation>();
        for(WorkflowVersion workflow: uowNetworkWorkflows){
            operationsInNetwork.addAll(workflow.getExternalOperations());
        }
        return operationsInNetwork;
    }

    private static Set<SService> servicesInNetwork(Graph<String, DefaultEdge> uowNetwork) {
        Set<SService> servicesInNetwork = new HashSet<SService>();
        for(String serviceURL: uowNetwork.vertexSet()){
            servicesInNetwork.add(serviceMap.get(serviceURL));
        }
        return servicesInNetwork;
    }

    private static Set<SService> services(Set<String> graphNodes) {
        Set<SService> graphServices = new HashSet<SService>();
        for (String node : graphNodes) {
            for (SService service : services) {
                if (service.getURL().equals(node)) {
                    graphServices.add(service);
                    break;
                }
            }
        }
        return graphServices;
    }

    private static void setupEnvironment() {
        AnalyzeDataOptimized analyzeDataOptimized = new AnalyzeDataOptimized();
//        workflows = analyzeDataOptimized.getAllWorkflowVersions();
//        services = analyzeDataOptimized.getAllServices();
//        operations = analyzeDataOptimized.getAllOperations();
//        setServiceIntents();
//        setServiceMap();
//        // since the min year is 2007 and last is 2018: I want the first test to be from 2008 and last be 2017 ==> it will be 11 tests;
//        workflowsForTest = new Set[TEST_SIZE + 1];
//        uowNetworkWorkflowsHistory = new Set[TEST_SIZE + 1];
//
//
//        allSortedWorkflows = new ArrayList<WorkflowWrapper>();
//
//        for (WorkflowVersion workflow : workflows) {
//            workflow.setIntent();
//            int year = workflow.getDate().getYear();
//            int yearIndex = year - MIN_YEAR;
//            if (workflowsForTest[yearIndex] == null) {
//                workflowsForTest[yearIndex] = new HashSet<WorkflowVersion>();
//                uowNetworkWorkflowsHistory[yearIndex] = new HashSet<WorkflowVersion>();
//            }
//            workflowsForTest[yearIndex].add(workflow);
//            uowNetworkWorkflowsHistory[yearIndex].add(workflow);
//
//
//            //**********************************
//            if (getWorkflowServices(workflow).size() > 0) {
//                allSortedWorkflows.add(new WorkflowWrapper(workflow));
//            }
//        }
//
//        Collections.sort(allSortedWorkflows);
//        print(allSortedWorkflows.size() + " all workflows with services more than 0");
//        print(allSortedWorkflows.get(0).getWorkflow().getDate().getTime() + " min time for workflow");
//        print(allSortedWorkflows.get(allSortedWorkflows.size()-1).getWorkflow().getDate().getTime() + " max time for workflow");
//
//        uowNetworkHistory = analyzeDataOptimized.getDirectedServiceGraph();
//        //pile up the history so that the more recent years have all the history
//        for (int i = 0; i < uowNetworkHistory.length - 1; i++) {
//            for (int j = i + 1; j < uowNetworkHistory.length; j++) {
//                Graphs.addGraph(uowNetworkHistory[j], uowNetworkHistory[i]);
//                uowNetworkWorkflowsHistory[j].addAll(uowNetworkWorkflowsHistory[i]);
//            }
//        }
//
//        try {
//            FileOutputStream fout = new FileOutputStream("allUoWNetwork");
//            FileOutputStream fout2 = new FileOutputStream("allServices");
//            FileOutputStream fout3 = new FileOutputStream("allOperations");
//            FileOutputStream fout4 = new FileOutputStream("allWorkflows");
//            FileOutputStream fout5 = new FileOutputStream("allSortedWorkflowWrappers");
//            ObjectOutputStream oos = new ObjectOutputStream(fout);
//            ObjectOutputStream oos2 = new ObjectOutputStream(fout2);
//            ObjectOutputStream oos3 = new ObjectOutputStream(fout3);
//            ObjectOutputStream oos4 = new ObjectOutputStream(fout4);
//            ObjectOutputStream oos5 = new ObjectOutputStream(fout5);
//            oos.writeObject(uowNetworkHistory);
//            oos2.writeObject(services);
//            oos3.writeObject(operations);
//            oos4.writeObject(workflows);
//            oos5.writeObject(allSortedWorkflows);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        //for first experiment
        Graph<String, DefaultEdge>[] uowNetworkTest1 = analyzeDataOptimized.getUoWnetwork(allSortedWorkflows, false);
        Set<SService>[] servicesTest1 = analyzeDataOptimized.getAllNetworkServices();
        Set<OOperation>[] operationsTest1 = analyzeDataOptimized.getAllNetworkOperations();
        Set<WorkflowVersion>[] workflowVersionsTest1 = analyzeDataOptimized.getAllNetworkWorkflowVersions();
        setServiceIntents(servicesTest1, operationsTest1);
        setWorkflowIntents(workflowVersionsTest1);
        workflowDirectedGraphs = analyzeDataOptimized.getWorkflowDirectedGraphs();

//        print(uowNetworkTest1[0].vertexSet().size()+" first network size");
//        print(workflowVersionsTest1[allSortedWorkflows.size()-1].size() + " Size of last Workflows");
//
//        try {
//            FileOutputStream fout = new FileOutputStream("test1-uowNetwork");
//            FileOutputStream fout2 = new FileOutputStream("test1-allServices");
//            FileOutputStream fout3 = new FileOutputStream("test1-allOperations");
//            FileOutputStream fout4 = new FileOutputStream("test1-allWorkflows");
//            ObjectOutputStream oos = new ObjectOutputStream(fout);
//            ObjectOutputStream oos2 = new ObjectOutputStream(fout2);
//            ObjectOutputStream oos3 = new ObjectOutputStream(fout3);
//            ObjectOutputStream oos4 = new ObjectOutputStream(fout4);
//            oos.writeObject(uowNetworkTest1);
//            oos2.writeObject(servicesTest1);
//            oos3.writeObject(operationsTest1);
//            oos4.writeObject(workflowVersionsTest1);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        //for second experiment
//        Graph<String, DefaultEdge>[] uowNetworkTest2 = analyzeDataOptimized.getUoWnetwork(allSortedWorkflows, true);
//        Set<SService>[] servicesTest2 = analyzeDataOptimized.getAllNetworkServices();
//        Set<OOperation>[] operationsTest2 = analyzeDataOptimized.getAllNetworkOperations();
//        Set<WorkflowVersion>[] workflowVersionsTest2 = analyzeDataOptimized.getAllNetworkWorkflowVersions();
//        setServiceIntents(servicesTest2, operationsTest2);
//        setWorkflowIntents(workflowVersionsTest2);
//
//        try {
//            FileOutputStream fout = new FileOutputStream("test2-uowNetwork");
//            FileOutputStream fout2 = new FileOutputStream("test2-allServices");
//            FileOutputStream fout3 = new FileOutputStream("test2-allOperations");
//            FileOutputStream fout4 = new FileOutputStream("test2-allWorkflows");
//            ObjectOutputStream oos = new ObjectOutputStream(fout);
//            ObjectOutputStream oos2 = new ObjectOutputStream(fout2);
//            ObjectOutputStream oos3 = new ObjectOutputStream(fout3);
//            ObjectOutputStream oos4 = new ObjectOutputStream(fout4);
//            oos.writeObject(uowNetworkTest2);
//            oos2.writeObject(servicesTest2);
//            oos3.writeObject(operationsTest2);
//            oos4.writeObject(workflowVersionsTest2);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        print("wrote in file");

        // creating uowNetworks for the test 2 for just removing one single workflow from the network

    }

    private static void setWorkflowIntents(Set<WorkflowVersion>[] workflowVersionsTest1) {
        for(int i=0; i<workflowVersionsTest1.length; i++){
            if(workflowVersionsTest1[i]!=null && workflowVersionsTest1[i].size()>0) {
                ArrayList<WorkflowVersion> workflowList = new ArrayList<WorkflowVersion>(workflowVersionsTest1[i]);
                for (WorkflowVersion workflowVersion : workflowList) {
                    workflowVersion.setIntent();
                }
            }
        }
    }

    private static void setServiceIntents(Set<SService>[] servicesTest1, Set<OOperation>[] operationsTest1) {
        for(int i=0; i<servicesTest1.length; i++){
            ArrayList<SService> serviceList = new ArrayList<SService>(servicesTest1[i]);
            for(SService serivce: serviceList){
                String intent = "";
                for(OOperation operation: operationsTest1[i]){
                    if (operation.getService().equals(serivce)) {
                        intent += operation.getProcessorName() + " " + operation.getName() + " ";
                    }
                }
                serivce.setIntent(intent);
            }
        }
    }

    private static void setServiceMap() {
        serviceMap = new HashMap<String, SService>();
        for(SService service: services){
            serviceMap.put(service.getURL(), service);
        }
    }

    private static void setServiceIntents() {
        for (SService service : services) {
            String intent = "";
            for (OOperation operation : operations) {
                if (operation.getService().equals(service)) {
                    intent += operation.getProcessorName() + " " + operation.getName() + " ";
                }
            }
            service.setIntent(intent);
        }
    }

    private static double precision(Graph<String, DefaultEdge> recommendedGraph, WorkflowVersion workflow) {
        ArrayList<SService> workflowServices = new ArrayList<SService>(getWorkflowServices(workflow));

//        double totalCandidates = TOP_RECOMMENDATION_SIZE;
//        if (recommendedGraph.size() < TOP_RECOMMENDATION_SIZE) {
//            totalCandidates = recommendedGraph.size();
//        }
        //TODO: Remove if necessary
//        totalCandidates = 1;

        double precisionsSum = 0;
//        for (int recommendedIndex = 0; recommendedIndex < recommendedGraph.vertexSet().size(); recommendedIndex++) {
//            Graph<String, DefaultEdge> recommendedUOW = recommendedGraph.get(recommendedIndex);
        double correctlyPredicted = 0;
        for (SService workflowService : workflowServices) {
            if (recommendedGraph.vertexSet().contains(workflowService.getURL()))
                correctlyPredicted++;
        }
        double allPredicted = recommendedGraph.vertexSet().size();
        if(allPredicted>0)
            precisionsSum += (correctlyPredicted / allPredicted);
//        }

        return precisionsSum / 1;
    }

    private static double recall(Graph<String, DefaultEdge> recommendedGraph, WorkflowVersion workflow) {
        ArrayList<SService> workflowServices = new ArrayList<SService>(getWorkflowServices(workflow));
//        double totalCandidates = TOP_RECOMMENDATION_SIZE;
//        if (recommendedUoWs.size() < TOP_RECOMMENDATION_SIZE) {
//            totalCandidates = recommendedUoWs.size();
//        }
//
//        //TODO: Remove if necessary
//        totalCandidates = 1;

        double recallSum = 0;
//        for (int recommendedIndex = 0; recommendedIndex < totalCandidates; recommendedIndex++) {
//            Graph<String, DefaultEdge> recommendedUOW = recommendedUoWs.get(recommendedIndex);
        double correctlyPredicted = 0;
        for (SService workflowService : workflowServices) {
            if (recommendedGraph.vertexSet().contains(workflowService.getURL()))
                correctlyPredicted++;
        }
        recallSum += (correctlyPredicted / workflowServices.size());
//        }
        return recallSum / 1;
    }



    private static boolean areSimilar(Graph<String, DefaultEdge> recommendedUoW, WorkflowVersion workflow) {
        ArrayList<SService> workflowServices = new ArrayList<SService>(getWorkflowServices(workflow));
        AllDirectedPaths<String, DefaultEdge> allDirectedWorkflowPaths = new AllDirectedPaths<String, DefaultEdge>(workflow.getDirectedProcessorGraph());
        AllDirectedPaths<String, DefaultEdge> allDirectedRecommendedUoWPaths = new AllDirectedPaths<String, DefaultEdge>(recommendedUoW);
        for (SService service1 : workflowServices) {
            for (SService service2 : workflowServices) {
                if (allDirectedWorkflowPaths.getAllPaths(service1.getURL(), service2.getURL(), false, workflow.getDirectedProcessorGraph().vertexSet().size()) != null && allDirectedRecommendedUoWPaths.getAllPaths(service1.getURL(), service2.getURL(), false, recommendedUoW.vertexSet().size()) == null)
                    return false;
            }
        }
        return true;
    }

    private static Set<SService> getWorkflowServices(WorkflowVersion workflow) {
        ArrayList<OOperation> workflowOperations = workflow.getExternalOperations();
        Set<SService> workflowServices = new HashSet<SService>();
        for (OOperation operation : workflowOperations) {
            workflowServices.add(operation.getService());
        }
        return workflowServices;
    }
}