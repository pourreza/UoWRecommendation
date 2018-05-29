package Evaluation;

import UoWRecommendation.UoWQuery;
import UoWRecommendation.UoWRecommendation;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultEdge;
import serviceWorkflowNetwork.OOperation;
import serviceWorkflowNetwork.SService;
import serviceWorkflowNetwork.WorkflowVersion;

import java.util.*;

import static java.lang.Math.pow;
import static serviceWorkflowNetwork.AnalyzeDataOptimized.*;
import static utilities.Printer.print;

public class EvaluateUoWRecommendation {
    private static final int TEST_SIZE = 11;
    private static final int MIN_TEST_YEAR = 2008;
    private static final double TOP_RECOMMENDATION_SIZE = 5;

    private static Set<WorkflowVersion> workflows;
    private static Set<SService> services;
    private static Set<OOperation> operations;

    public static Graph<String, DefaultEdge>[] uowNetworkHistory;
    public static Set<WorkflowVersion>[] workflowsForTest;

    public static void main(String[] args) {
        setupEnvironment();

        for (int testIndex = 0; testIndex < TEST_SIZE; testIndex++) {
            print("Test index: " + testIndex);
            Graph<String, DefaultEdge> uowNetwork = uowNetworkHistory[testIndex];
            ArrayList<WorkflowVersion> testWorkflows = new ArrayList<WorkflowVersion>(workflowsForTest[testIndex]);
            UoWRecommendation recommendation = new UoWRecommendation(uowNetwork, services, workflows, operations);

            double avgPrecision = 0;
            double avgRecall = 0;
            double avgStructureSimilarity = 0;

            for (WorkflowVersion workflow : testWorkflows) {
                UoWQuery query = new UoWQuery(workflow.getIntent(), null);
                List<Graph<String, DefaultEdge>> recommendedUoWs = recommendation.recommendUOWs(query);
                avgStructureSimilarity += structureSimilarity(recommendedUoWs, workflow);
                double precision = precision(recommendedUoWs, workflow);
                double recall = recall(recommendedUoWs, workflow, recommendation.findUsefulServices(query).size());
                avgPrecision += precision;
                avgRecall += recall;
            }
            double avgFScore = 2 * avgPrecision * avgRecall / (avgPrecision + avgRecall);

            print("Recommendation Avg Precision: " + avgPrecision);
            print("Recommendation Avg Recall: " + avgRecall);
            print("Recommendation Avg F-Score: " + avgFScore);
            print("Recommendation Avg Structure Similarity: " + avgStructureSimilarity);
        }
    }

    private static void setupEnvironment() {
        workflows = getAllWorkflowVersions();
        services = getAllServices();
        operations = getAllOperations();
        // since the min year is 2007 and last is 2018: I want the first test to be from 2008 and last be 2017 ==> it will be 11 tests;
        workflowsForTest = new Set[TEST_SIZE];
        for (WorkflowVersion workflow : workflows) {
            workflow.setIntent();
            int year = workflow.getDate().getYear();
            int testIndex = year - MIN_TEST_YEAR;
            if (workflowsForTest[testIndex] == null)
                workflowsForTest[testIndex] = new HashSet<WorkflowVersion>();
            workflowsForTest[testIndex].add(workflow);
        }
        uowNetworkHistory = getDirectedServiceGraph();
    }

    private static double precision(List<Graph<String, DefaultEdge>> recommendedUoWs, WorkflowVersion workflow) {
        ArrayList<SService> workflowServices = getWorkflowServices(workflow);

        double totalPredicted = TOP_RECOMMENDATION_SIZE;
        if (recommendedUoWs.size() < TOP_RECOMMENDATION_SIZE) {
            totalPredicted = recommendedUoWs.size();
        }

        double correctlyPredicted = getCorrectlyPredictedNumber(recommendedUoWs, workflowServices, totalPredicted);
        return correctlyPredicted / totalPredicted;
    }

    private static double recall(List<Graph<String, DefaultEdge>> recommendedUoWs, WorkflowVersion workflow, double totalShouldHaveBeenPredicted) {
        ArrayList<SService> workflowServices = getWorkflowServices(workflow);
        double correctlyPredicted = getCorrectlyPredictedNumber(recommendedUoWs, workflowServices, recommendedUoWs.size());
        return correctlyPredicted/ pow(2.0, totalShouldHaveBeenPredicted);
    }

    private static double structureSimilarity(List<Graph<String, DefaultEdge>> recommendedUoWs, WorkflowVersion workflow) {
        double structureSimilarity = 0;
        for(Graph<String, DefaultEdge> recommendedUoW: recommendedUoWs){
            if(areSimilar(recommendedUoW, workflow))
                structureSimilarity++;
        }
        return structureSimilarity/recommendedUoWs.size();
    }

    private static boolean areSimilar(Graph<String, DefaultEdge> recommendedUoW, WorkflowVersion workflow) {
        ArrayList<SService> workflowServices = getWorkflowServices(workflow);
        AllDirectedPaths<String, DefaultEdge> allDirectedWorkflowPaths = new AllDirectedPaths<String, DefaultEdge>(workflow.getDirectedProcessorGraph());
        AllDirectedPaths<String, DefaultEdge> allDirectedRecommendedUoWPaths = new AllDirectedPaths<String, DefaultEdge>(recommendedUoW);
        for(SService service1: workflowServices){
            for(SService service2: workflowServices){
                if(allDirectedWorkflowPaths.getAllPaths(service1.getURL(), service2.getURL(), false, workflow.getDirectedProcessorGraph().vertexSet().size())!=null && allDirectedRecommendedUoWPaths.getAllPaths(service1.getURL(), service2.getURL(), false, recommendedUoW.vertexSet().size())==null)
                    return false;
            }
        }
        return true;
    }

    private static double getCorrectlyPredictedNumber(List<Graph<String, DefaultEdge>> recommendedUoWs, ArrayList<SService> workflowServices, double totalPredicted) {
        double correctlyPredicted = 0;
        for (int recommendedIndex = 0; recommendedIndex < totalPredicted; recommendedIndex++) {
            Graph<String, DefaultEdge> recommendedUOW = recommendedUoWs.get(recommendedIndex);
            boolean hasAllServices = true;
            for (SService workflowService : workflowServices) {
                if (!recommendedUOW.vertexSet().contains(workflowService))
                    hasAllServices = false;
            }
            if (hasAllServices) {
                correctlyPredicted++;
            }
        }
        return correctlyPredicted;
    }

    private static ArrayList<SService> getWorkflowServices(WorkflowVersion workflow) {
        ArrayList<OOperation> workflowOperations = workflow.getExternalOperations();
        ArrayList<SService> workflowServices = new ArrayList<SService>();
        for (OOperation operation : workflowOperations) {
            workflowServices.add(operation.getService());
        }
        return workflowServices;
    }
}
