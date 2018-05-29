package UoWRecommendation;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultEdge;
import serviceWorkflowNetwork.*;

import java.io.IOException;
import java.util.*;

import static utilities.RestCall.getCosineValue;


public class UoWRecommendation {
    private final Double USEFULLNESS_THRESHOLD = 0.2;
    private final Double CONTEXT_SIMILARITY_THRESHOLD = 0.75;

    public Graph<String, DefaultEdge> uowNetwork;
    public Map<String, SService> serviceMap;
    public Set<SService> services;
    public Set<WorkflowVersion> workflows;
    public Set<OOperation> operations;

    //set of services, workflows, and operations related to the uowNetwork
    public UoWRecommendation(Graph<String, DefaultEdge> uowNetwork, Set<SService> services, Set<WorkflowVersion> workflows, Set<OOperation> operations) {
        this.uowNetwork = uowNetwork;
        this.services = services;
        this.workflows = workflows;
        this.operations = operations;
    }

    public List<Graph<String, DefaultEdge>> recommendUOWs(UoWQuery userQuery) {
        double aggressiveness = userQuery.computeAggressiveRate();
        Set<SService>[] candidateServiceGroups = findCandidateServiceGroups(userQuery);
        Set<Graph<String, DefaultEdge>> initialUoWCandidates = initialUoWCandidateSet(userQuery);
        return sort(pruneCandidates(initialUoWCandidates, aggressiveness, userQuery.getGoalWorkflow()));
    }

    private Set<SService>[] findCandidateServiceGroups(UoWQuery userQuery) {
        Set<SService> remainingServicesToGoal = userQuery.remainingServices();
        Set<SService>[] candidateServiceGroups = new Set[remainingServicesToGoal.size()];

        for(SService remainingService: remainingServicesToGoal){

        }
        return candidateServiceGroups;
    }

    private List<Graph<String, DefaultEdge>> sort(List<Graph<String, DefaultEdge>> graphs) {
        return null;
    }

    private Set<Graph<String, DefaultEdge>> initialUoWCandidateSet(UoWQuery userQuery) {

        Graph<String, DefaultEdge> currentUoW = userQuery.getUoW();
        Set<Graph<String, DefaultEdge>> candidateList = new HashSet<Graph<String, DefaultEdge>>();

        Set<String> usefulCandidateServices = findUsefulServices(userQuery);
        usefulCandidateServices.removeAll(currentUoW.vertexSet()); //removing existing services

        AllDirectedPaths<String, DefaultEdge> allDirectedPaths = new AllDirectedPaths<String, DefaultEdge>(uowNetwork);
        for(String sourceService: currentUoW.vertexSet()){
            for(String targetService: usefulCandidateServices){
                int maxPathLength = services.size();
                List<GraphPath<String, DefaultEdge>> allPaths = allDirectedPaths.getAllPaths(sourceService, targetService, false, maxPathLength);
                for(GraphPath<String, DefaultEdge> path: allPaths)
                    candidateList.add(path.getGraph());
                //now swap source and target
                allPaths = allDirectedPaths.getAllPaths(targetService, sourceService, false, maxPathLength);
                for(GraphPath<String, DefaultEdge> path: allPaths)
                    candidateList.add(path.getGraph());
            }
        }
        return candidateList;
    }

    public Set<String> findUsefulServices(UoWQuery userQuery) {
        Set<String> usefullServices = new HashSet<String>();
        for(SService service: services){
            try {
                if(getCosineValue(service.getIntent(), userQuery.getGoalWorkflow())>USEFULLNESS_THRESHOLD)
                    usefullServices.add(service.getURL());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return usefullServices;
    }

    private List<Graph<String, DefaultEdge>> pruneCandidates(Set<Graph<String, DefaultEdge>> candidateUoWs, double aggressiveness, String goal) {
        ArrayList<Graph<String, DefaultEdge>> initialCandidateList = new ArrayList<Graph<String, DefaultEdge>>(candidateUoWs);
        ArrayList<Graph<String, DefaultEdge>> finalCandidates = new ArrayList<Graph<String, DefaultEdge>>();
        for(Graph<String, DefaultEdge> candidate: initialCandidateList){
            if(contextsAreSimilar(candidate, goal, aggressiveness) && isUsefulEnough(candidate, goal, aggressiveness)){
                finalCandidates.add(candidate);
            }
        }
        return finalCandidates;
    }

    private boolean isUsefulEnough(Graph<String, DefaultEdge> candidate, String goal, double aggressiveness) {
        int numberOfUsefullServices = 0;
        for(String serviceName: candidate.vertexSet()){
            try {
                if(getCosineValue(serviceMap.get(serviceName).getIntent(), goal)>USEFULLNESS_THRESHOLD)
                    numberOfUsefullServices++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        double totalNumberOfServices = candidate.vertexSet().size();
        return (numberOfUsefullServices/ totalNumberOfServices) <= aggressiveness;
    }

    private boolean contextsAreSimilar(Graph<String, DefaultEdge> candidate, String goal, double aggressiveness) {
        int numberOfContextSimilarServices = 0;
        for(String serviceName: candidate.vertexSet()){
            ArrayList<String> serviceContexts = new ArrayList<String>();
            //find the workflows having this service
            for(WorkflowVersion workflow: workflows){
                ArrayList<OOperation> operations = workflow.getExternalOperations();
                for( OOperation operation: operations) {
                    if (operation.getService().getURL().equals(serviceName)) {
                        serviceContexts.add(workflow.getIntent());
                        break;
                    }
                }
            }
            String serviceContext = findIntersection(serviceContexts);
            try {
                if(getCosineValue(goal, serviceContext)>CONTEXT_SIMILARITY_THRESHOLD){
                    numberOfContextSimilarServices++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        double totalNumberOfServices = candidate.vertexSet().size();
        return (numberOfContextSimilarServices/ totalNumberOfServices)<=aggressiveness;
    }

    private String findIntersection(ArrayList<String> serviceContexts) {
        String[] intersection = serviceContexts.get(0).split(" ");
        if(serviceContexts.size()>=2){
            for(int i=1; i<serviceContexts.size(); i++){
                String[] strArray2 = serviceContexts.get(i).split(" ");
                intersection = arrayIntersection(intersection, strArray2);
            }
        }
        String intersectionCombined = "";
        for(String str: intersection){
            intersectionCombined += str + " ";
        }
        return intersectionCombined;
    }

    private String[] arrayIntersection(String[] strArray1, String[] strArray2) {
        List<String> strList = new ArrayList<String>();
        for(String str: strArray1){
            for(String str2: strArray2){
                if(str.equals(str2)){
                    strList.add(str);
                    break;
                }
            }
        }
        String[] intersection = new String[strList.size()];
        return strList.toArray(intersection);
    }
}
