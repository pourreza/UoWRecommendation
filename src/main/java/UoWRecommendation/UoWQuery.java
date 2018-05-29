package UoWRecommendation;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import serviceWorkflowNetwork.OOperation;
import serviceWorkflowNetwork.SService;
import serviceWorkflowNetwork.WorkflowVersion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class UoWQuery {
    private WorkflowVersion goalWorkflow;
    private Graph<String, DefaultEdge> currentUOW;

    public UoWQuery(WorkflowVersion goalWorkflow, Graph<String, DefaultEdge> curretnUOW) {
        this.goalWorkflow = goalWorkflow;
        this.currentUOW = curretnUOW;
    }

    public Graph<String, DefaultEdge> getUoW() {
        return currentUOW;
    }

    public double computeAggressiveRate() {
        ArrayList<OOperation> externalOperations = goalWorkflow.getExternalOperations();
        Set<SService> goalServices = new HashSet<SService>();
        for(OOperation operation: externalOperations){
            goalServices.add(operation.getService());
        }
        int goalSize = goalServices.size();
        double numberOfCurrentGoalServices = goalSize - remainingServices().size();
        return numberOfCurrentGoalServices/ goalSize;
    }

    public WorkflowVersion getGoalWorkflow() {
        return goalWorkflow;
    }

    public Set<> remainingServices() {
        Set<OOperation> goalProcessors = new HashSet<OOperation>(goalWorkflow.getExternalOperations());
        Set<String> remainingProcessors = new HashSet<String>();
        for(OOperation operation: goalProcessors){
            if(UoWRecommendation.similar)
                remainingServices.add(service);
        }
        return remainingServices;
    }
}
