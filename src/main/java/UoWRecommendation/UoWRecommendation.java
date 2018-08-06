package UoWRecommendation;

import org.apache.commons.math3.util.Pair;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import serviceWorkflowNetwork.*;
import utilities.PythonInterpreter2;

import java.io.IOException;
import java.util.*;

import static utilities.PythonInterpreter2.getCosine;
import static utilities.PythonInterpreter2.getCosine2;
import static utilities.RestCall.getCosineValue;


public class UoWRecommendation {
    public static final Double SERVICE_SIMILARITY_THRESHOLD = 0.2;
    private final Double CONTEXT_SIMILARITY_THRESHOLD = 0.01;

    public Graph<String, DefaultEdge> uowNetwork;
    public Map<String, SService> serviceMap;
    public Set<SService> networkServices;
    public Set<WorkflowVersion> workflows;
    public Set<OOperation> operations;

    //set of networkServices, workflows, and operations related to the uowNetwork
    //all services contains all services in system
    public UoWRecommendation(Graph<String, DefaultEdge> uowNetwork, Set<SService> services, Set<WorkflowVersion> workflows, Set<OOperation> operations) {
        this.uowNetwork = uowNetwork;
        this.networkServices = services;
        this.workflows = workflows;
        this.operations = operations;
        initializeServiceMap();
    }

    private void initializeServiceMap() {
        serviceMap = new HashMap<String, SService>();
        for(SService service: networkServices){
            serviceMap.put(service.getURL(), service);
        }
    }

    public List<Graph<String, DefaultEdge>> recommendUOWs(UoWQuery userQuery) {
        Set<Graph<String, DefaultEdge>> recommendedUoWs = new HashSet<Graph<String, DefaultEdge>>();

        //find candidate services for every processor in the user query and then select different combinations from them for satisfying the user query
        Set<SService>[] candidateServiceGroups = findCandidateServiceGroups(userQuery);
        Set<SService[]> candidateSelections = findCombinationsOfCandidateServices(new HashSet<SService[]>(), candidateServiceGroups);

        //find candidate UoW that are some how connected to existing services in the current UoW
        Set<SService> currentServices = userQuery.getCurrentServices();
        if(currentServices!=null)
            addCandidateUOWsConnectedToCurrentUOW(recommendedUoWs, candidateSelections, currentServices);
        //find candidate UoW which do not have any connection to the existing UoW and the user needs to find the connection himself
        addIndependentCandidateUoWs(recommendedUoWs, candidateSelections, getAllServiceURLs(currentServices));

        double aggressiveness = userQuery.computeAggressiveness();
        return sort(pruneCandidates(recommendedUoWs, aggressiveness, userQuery, candidateServiceGroups), candidateServiceGroups);
    }

    public List<Graph<String, DefaultEdge>> recommendFlow(UoWQuery userQuery, Map<String, ArrayList<String>> patternTable, Map<String, ArrayList<Double>> scores) {
        Set<SService>[] candidateServiceGroups = findCandidateServiceGroups(userQuery);
        return null;
    }

    private void addCandidateUOWsConnectedToCurrentUOW(Set<Graph<String, DefaultEdge>> recommendedUoWs, Set<SService[]> candidateSelections, Set<SService> currentServices) {

        for (SService connectingPointService : currentServices) {
            Set<String> servicesWithoutCurrentOnes = new HashSet<String>(uowNetwork.vertexSet());
            servicesWithoutCurrentOnes.remove(getAllServiceURLs(currentServices));
            // we want to keep the connecting point service from the existing services in the service set
            servicesWithoutCurrentOnes.add(connectingPointService.getURL());

            //get a subgraph from the network by removing all the existing services except the connection point service
            AsSubgraph<String, DefaultEdge> subnetwork = new AsSubgraph<String, DefaultEdge>(uowNetwork, servicesWithoutCurrentOnes);
            ConnectivityInspector<String, DefaultEdge> connectivityInspector = new ConnectivityInspector<String, DefaultEdge>(subnetwork);
            //find all the weakly connected components
            List<Set<String>> connectedNetworkComponents = connectivityInspector.connectedSets();
            Set<String> componentContainingConnectionPoint = findComponentContainingConnectingPoint(connectedNetworkComponents, connectingPointService);
            AsSubgraph<String, DefaultEdge> connectedGraphContainingComponent = new AsSubgraph<String, DefaultEdge>(subnetwork, componentContainingConnectionPoint);

            for (SService[] candidateServices : candidateSelections) {
                Set<SService> servicesInComponent = findServicesInComponent(candidateServices, componentContainingConnectionPoint, connectingPointService);
                if(servicesInComponent.size()==1 && servicesInComponent.contains(connectingPointService)){
                    continue;
                }
                if(servicesInComponent.size()>0)
                    addCandidateUoWs(recommendedUoWs, connectedGraphContainingComponent, servicesInComponent, connectingPointService);
            }
        }
    }

    private void addIndependentCandidateUoWs(Set<Graph<String, DefaultEdge>> recommendedUoWs, Set<SService[]> candidateSelections, Set<String> currentServiceURLs) {
        Set<String> servicesWithoutCurrentOnes = new HashSet<String>(uowNetwork.vertexSet());
        if(currentServiceURLs!=null)
            servicesWithoutCurrentOnes.remove(currentServiceURLs);

        AsSubgraph<String, DefaultEdge> subnetwork = new AsSubgraph<String, DefaultEdge>(uowNetwork, servicesWithoutCurrentOnes);
        ConnectivityInspector<String, DefaultEdge> connectivityInspector = new ConnectivityInspector<String, DefaultEdge>(subnetwork);
        List<Set<String>> connectedNetworkComponents = connectivityInspector.connectedSets();

        for (SService[] candidateServices : candidateSelections) {
            for (Set<String> component : connectedNetworkComponents) {
                AsSubgraph<String, DefaultEdge> connectedGraphContainingComponent = new AsSubgraph<String, DefaultEdge>(subnetwork, component);
                Set<SService> serivesInComponent = findServicesInComponent(candidateServices, component, null);
                if(serivesInComponent.size()>0)
                    addCandidateUoWs(recommendedUoWs, connectedGraphContainingComponent, serivesInComponent, null);
            }
        }
    }

    private Set<SService> findServicesInComponent(SService[] candidateServices, Set<String> component, SService connectingPointService) {
        Set<SService> servicesInComponent = new HashSet<SService>();
        for (SService service : candidateServices) {
            if(service!=null) {
                if (component.contains(service.getURL()))
                    servicesInComponent.add(service);
            }
        }
        if (connectingPointService != null)
            servicesInComponent.add(connectingPointService);
        return servicesInComponent;
    }

    private void addCandidateUoWs(Set<Graph<String, DefaultEdge>> recommendedUoWs, AsSubgraph<String, DefaultEdge> connectedGraphContainingConnectionPoint, Set<SService> candidateServices, SService connectingPointService) {
        //adding single services
        DefaultDirectedGraph<String, DefaultEdge> hypothesisUoW = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        for (SService service : candidateServices) {
            if(service!=null) {
                Graph<String, DefaultEdge> graphWithOneNode = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
                graphWithOneNode.addVertex(service.getURL());
                hypothesisUoW.addVertex(service.getURL());
                recommendedUoWs.add(graphWithOneNode);
            }
        }

        ArrayList<Pair<SService, SService>> allCandidatePairs = allServicePairs(candidateServices);
        for (Pair<SService, SService> candidatePair : allCandidatePairs) {
            ArrayList<Pair<SService, SService>> remainingCandidatePairs = new ArrayList<Pair<SService, SService>>(allCandidatePairs);
//            remainingCandidatePairs.remove(candidatePair);
            addCandidateUoWs(recommendedUoWs, connectedGraphContainingConnectionPoint, remainingCandidatePairs, hypothesisUoW, connectingPointService);
        }
    }

    private void addCandidateUoWs(Set<Graph<String, DefaultEdge>> recommendedUoWs, AsSubgraph<String, DefaultEdge> connectedGraph, ArrayList<Pair<SService, SService>> candidateServicePairs, Graph<String, DefaultEdge> hypothesisUoW, SService connectingPointService) {
        ConnectivityInspector<String, DefaultEdge> connectivityInspector = new ConnectivityInspector<String, DefaultEdge>(hypothesisUoW);
        if (connectivityInspector.connectedSets().size() == 1) {
            if (connectingPointService != null && !hypothesisUoW.containsVertex(connectingPointService.getURL())) {
                return;
            }
            recommendedUoWs.add(hypothesisUoW);
            return;
        }
        if (candidateServicePairs == null || candidateServicePairs.size() == 0) {
            return;
        }
        DijkstraShortestPath<String, DefaultEdge> dijkstraAlg = new DijkstraShortestPath<String, DefaultEdge>(connectedGraph);
        for (Pair<SService, SService> candidatePair : candidateServicePairs) {
            GraphPath<String, DefaultEdge> path = dijkstraAlg.getPaths(candidatePair.getFirst().getURL()).getPath(candidatePair.getSecond().getURL());
            if (path == null) {
                path = dijkstraAlg.getPaths(candidatePair.getSecond().getURL()).getPath(candidatePair.getFirst().getURL());
            }
            ArrayList<Pair<SService, SService>> remainingCandidatePairs = new ArrayList<Pair<SService, SService>>(candidateServicePairs);
            remainingCandidatePairs.remove(candidatePair);
            addCandidateUoWs(recommendedUoWs, connectedGraph, remainingCandidatePairs, combineGraphs(path, hypothesisUoW, connectedGraph), connectingPointService);
        }

    }

    private Graph<String, DefaultEdge> combineGraphs(GraphPath<String, DefaultEdge> path, Graph<String, DefaultEdge> hypothesisUoW, AsSubgraph<String, DefaultEdge> component) {
        if (path != null) {
            for (DefaultEdge edge : path.getEdgeList()) {
                String edgeSource = component.getEdgeSource(edge);
                String edgeTarget = component.getEdgeTarget(edge);
                hypothesisUoW.addVertex(edgeSource);
                hypothesisUoW.addVertex(edgeTarget);
                hypothesisUoW.addEdge(edgeSource, edgeTarget);
            }
        }
        return hypothesisUoW;
    }

    private SService[] removeFromList(SService[] candidateServices, Pair<SService, SService> servicePair) {
        SService[] newServiceList = new SService[candidateServices.length - 2];
        int newServiceListIndex = 0;
        for (int i = 0; i < candidateServices.length; i++) {
            if (!candidateServices[i].equals(servicePair.getFirst()) && !candidateServices[i].equals(servicePair.getSecond())) {
                newServiceList[newServiceListIndex] = candidateServices[i];
                newServiceListIndex++;
            }
        }
        return newServiceList;
    }

    private ArrayList<Pair<SService, SService>> allServicePairs(Set<SService> candidateServices) {
        ArrayList<Pair<SService, SService>> allServicePairs = new ArrayList<Pair<SService, SService>>();
        ArrayList<SService> candidateServicesList = new ArrayList<SService>(candidateServices);
        for (int first = 0; first < candidateServicesList.size()-1; first++) {
            for (int second = first + 1; second < candidateServicesList.size(); second++) {
                allServicePairs.add(new Pair<SService, SService>(candidateServicesList.get(first), candidateServicesList.get(second)));
            }
        }
        return allServicePairs;
    }

    private Set<String> findComponentContainingConnectingPoint(List<Set<String>> networkServiceComponents, SService connectingPointService) {
        for (Set<String> component : networkServiceComponents) {
            if (component.contains(connectingPointService.getURL()))
                return component;
        }
        return null;
    }

    private Set<SService[]> findCombinationsOfCandidateServices(Set<SService[]> candidateSelections, Set<SService>[] candidateServiceGroups) {
        if (candidateServiceGroups.length < 1) {
            return candidateSelections;
        }

        if(candidateServiceGroups[0]==null){
            return candidateSelections;
        }
        ArrayList<SService[]> candidateSelectionList = new ArrayList<SService[]>(candidateSelections);
        ArrayList<SService> candidateServices = new ArrayList<SService>(candidateServiceGroups[0]);

        Set<SService[]> extendedCandidateSelections = new HashSet<SService[]>();
        if (candidateSelectionList.size() == 0) {
            for (int serviceIndex = 0; serviceIndex < candidateServiceGroups[0].size(); serviceIndex++) {
                SService[] selection = new SService[candidateServiceGroups.length];
                selection[0] = candidateServices.get(serviceIndex);
                extendedCandidateSelections.add(selection);
            }
        } else {
            for (int selectionIndex = 0; selectionIndex < candidateSelectionList.size(); selectionIndex++) {
                if(candidateServiceGroups[0].size()==0){
                    extendedCandidateSelections.add(candidateSelectionList.get(selectionIndex));
                }
                for (int serviceIndex = 0; serviceIndex < candidateServiceGroups[0].size(); serviceIndex++) {
                    SService[] selection = candidateSelectionList.get(selectionIndex);
                    selection[selection.length - candidateServiceGroups.length] = candidateServices.get(serviceIndex);
                    extendedCandidateSelections.add(selection);
                }
            }
        }
        Set<SService>[] remainingGroups = remainingCandidateServiceGroups(candidateServiceGroups);
        return findCombinationsOfCandidateServices(extendedCandidateSelections, remainingGroups);
    }

    private Set<SService>[] remainingCandidateServiceGroups(Set<SService>[] candidateServiceGroups) {
        Set<SService>[] remainingGroups = new Set[candidateServiceGroups.length - 1];
        for (int groupIndex = 0; groupIndex < remainingGroups.length; groupIndex++) {
            if(candidateServiceGroups[groupIndex + 1]!=null)
                remainingGroups[groupIndex] = new HashSet<SService>(candidateServiceGroups[groupIndex + 1]);
            else
                remainingGroups[groupIndex] = new HashSet<SService>();
        }
        return remainingGroups;
    }

    private Set<String> getAllServiceURLs(Set<SService> services) {
        Set<String> serviceUrls = new HashSet<String>();
        if(services==null)
            return null;
        for (SService service : services)
            serviceUrls.add(service.getURL());
        return serviceUrls;
    }

    private Set<SService>[] findCandidateServiceGroups(UoWQuery userQuery) {
        ArrayList<String> remainingProcessorsToGoal = new ArrayList<String>(userQuery.remainingProcessors());
        Set<SService>[] candidateServiceGroups = new Set[remainingProcessorsToGoal.size()];

        for (int processorIndex = 0; processorIndex < remainingProcessorsToGoal.size(); processorIndex++) {
            for (SService service : networkServices) {
                if (areSimilar(service, remainingProcessorsToGoal.get(processorIndex))) {
                    if(candidateServiceGroups[processorIndex]==null){
                        candidateServiceGroups[processorIndex] = new HashSet<SService>();
                    }
                    candidateServiceGroups[processorIndex].add(service);
                }
            }
        }

        return candidateServiceGroups;
    }

    private List<Graph<String, DefaultEdge>> sort(List<Graph<String, DefaultEdge>> graphs, Set<SService>[] candidateServiceGroups) {
        ArrayList<GraphWrapper> graphsWithUsefullness = new ArrayList<GraphWrapper>();
        for (Graph<String, DefaultEdge> graph : graphs) {
            graphsWithUsefullness.add(new GraphWrapper(graph, score(graph, candidateServiceGroups)));
        }
        Collections.sort(graphsWithUsefullness, Collections.reverseOrder());
        List<Graph<String, DefaultEdge>> sortedList = new ArrayList<Graph<String, DefaultEdge>>();
        for (int i=0; i<graphsWithUsefullness.size(); i++) {
            sortedList.add(graphsWithUsefullness.get(i).getGraph());
        }
        return sortedList;
    }


    private List<Graph<String, DefaultEdge>> pruneCandidates(Set<Graph<String, DefaultEdge>> candidateUoWs, double aggressiveness, UoWQuery userQuery, Set<SService>[] candidateServiceGroups) {
        ArrayList<Graph<String, DefaultEdge>> initialCandidateList = new ArrayList<Graph<String, DefaultEdge>>(candidateUoWs);
        ArrayList<Graph<String, DefaultEdge>> finalCandidates = new ArrayList<Graph<String, DefaultEdge>>();
        for (Graph<String, DefaultEdge> candidate : initialCandidateList) {
            if (contextsAreSimilar(candidate, userQuery.getGoalWorkflow().getIntent()) && score(candidate, candidateServiceGroups)<=aggressiveness) {
                finalCandidates.add(candidate);
            }
        }
        return finalCandidates;
    }

    private double score(Graph<String, DefaultEdge> candidate, Set<SService>[] candidateServiceGroups) {
        Set<String> userfulServices = new HashSet<String>();
        for (Set<SService> candidateServices : candidateServiceGroups) {
            if(candidateServices!=null) {
                for (String serviceName : candidate.vertexSet()) {
                    if (candidateServices.contains(serviceMap.get(serviceName))) {
                        userfulServices.add(serviceName);
                        break;
                    }
                }
            }
        }
        double noiseServices = candidate.vertexSet().size() - userfulServices.size();
        if (noiseServices == 0)
            noiseServices = 1;
        return (userfulServices.size() / noiseServices);
    }

    private boolean contextsAreSimilar(Graph<String, DefaultEdge> candidate, String goal) {
        String candidateContext = "";
        for (String serviceName : candidate.vertexSet()) {
            ArrayList<String> serviceContexts = new ArrayList<String>();
            //TODO: MAY NEED TO REMOVE
            String serviceContext1 = "";
            //find the workflows having this service
            for (WorkflowVersion workflow : workflows) {
                ArrayList<OOperation> operations = workflow.getExternalOperations();
                for (OOperation operation : operations) {
                    if (operation.getService().getURL().equals(serviceName)) {
                        serviceContexts.add(workflow.getIntent());
                        serviceContext1 += workflow.getIntent()+" ";
                        break;
                    }
                }
            }
//            String serviceContext = findIntersection(serviceContexts);
            candidateContext += " " + serviceContext1;
        }
//        try {
            if (getCosine2(candidateContext, goal) > CONTEXT_SIMILARITY_THRESHOLD) {
                return true;
            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return false;
    }

    private String findIntersection(ArrayList<String> serviceContexts) {
        String[] intersection = serviceContexts.get(0).split(" ");
        if (serviceContexts.size() >= 2) {
            for (int i = 1; i < serviceContexts.size(); i++) {
                String[] strArray2 = serviceContexts.get(i).split(" ");
                intersection = arrayIntersection(intersection, strArray2);
            }
        }
        String intersectionCombined = "";
        for (String str : intersection) {
            intersectionCombined += str + " ";
        }
        return intersectionCombined;
    }

    private String[] arrayIntersection(String[] strArray1, String[] strArray2) {
        List<String> strList = new ArrayList<String>();
        for (String str : strArray1) {
            for (String str2 : strArray2) {
                if (str.equals(str2)) {
                    strList.add(str);
                    break;
                }
            }
        }
        String[] intersection = new String[strList.size()];
        return strList.toArray(intersection);
    }

    public static boolean areSimilar(SService service, String processorName) {
//        try {
//            if (getCosineValue(processorName.toLowerCase(), service.getIntent().toLowerCase()) > SERVICE_SIMILARITY_THRESHOLD)
            if (getCosine2(processorName.toLowerCase(), service.getIntent().toLowerCase()) > SERVICE_SIMILARITY_THRESHOLD)
                return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return false;
    }

    public class GraphWrapper implements Comparable{
        private Graph<String, DefaultEdge> graph;
        private double usefullness;

        public GraphWrapper(Graph<String, DefaultEdge> graph, double usefullness) {
            this.graph = graph;
            this.usefullness = usefullness;
        }

        public Graph<String,DefaultEdge> getGraph() {
            return graph;
        }

        public int compareTo(Object o) {
            return (int)(usefullness - ((GraphWrapper)o).usefullness);
        }
    }
//    private Set<Graph<String, DefaultEdge>> initialUoWCandidateSet(UoWQuery userQuery) {
//
//        Graph<String, DefaultEdge> currentUoW = userQuery.getCurrentServices();
//        Set<Graph<String, DefaultEdge>> candidateList = new HashSet<Graph<String, DefaultEdge>>();
//
//        Set<String> usefulCandidateServices = findUsefulServices(userQuery);
//        usefulCandidateServices.removeAll(currentUoW.vertexSet()); //removing existing networkServices
//
//        AllDirectedPaths<String, DefaultEdge> allDirectedPaths = new AllDirectedPaths<String, DefaultEdge>(uowNetwork);
//        for(String sourceService: currentUoW.vertexSet()){
//            for(String targetService: usefulCandidateServices){
//                int maxPathLength = networkServices.size();
//                List<GraphPath<String, DefaultEdge>> allPaths = allDirectedPaths.getAllPaths(sourceService, targetService, false, maxPathLength);
//                for(GraphPath<String, DefaultEdge> path: allPaths)
//                    candidateList.add(path.getGraph());
//                //now swap source and target
//                allPaths = allDirectedPaths.getAllPaths(targetService, sourceService, false, maxPathLength);
//                for(GraphPath<String, DefaultEdge> path: allPaths)
//                    candidateList.add(path.getGraph());
//            }
//        }
//        return candidateList;
//    }
}