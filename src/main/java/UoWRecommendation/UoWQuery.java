package UoWRecommendation;

import serviceWorkflowNetwork.OOperation;
import serviceWorkflowNetwork.SService;
import serviceWorkflowNetwork.WorkflowVersion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static UoWRecommendation.UoWRecommendation.areSimilar;

public class UoWQuery {
    private WorkflowVersion goalWorkflow;
    private Set<SService> currentServices;

    public UoWQuery(WorkflowVersion goalWorkflow, Set<SService> currentServices) {
        this.goalWorkflow = goalWorkflow;
        this.currentServices = currentServices;
    }

    public Set<SService> getCurrentServices() {
        return currentServices;
    }

    public double computeAggressiveness() {
//        Set<OOperation> goalProcessors = new HashSet<OOperation>(goalWorkflow.getExternalOperations());
//        int goalSize = goalProcessors.size();
//        double numberOfFoundProcessors = goalSize - remainingProcessors().size();
//        return numberOfFoundProcessors / goalSize;
        return (double) remainingProcessors().size();
    }

    public WorkflowVersion getGoalWorkflow() {
        return goalWorkflow;
    }

    public Set<String> remainingProcessors() {
        Set<OOperation> goalProcessors = new HashSet<OOperation>(goalWorkflow.getExternalOperations());
        Set<String> remainingProcessors = new HashSet<String>();

        for (OOperation operation : goalProcessors) {
            boolean foundInCurrentUoW = false;
            if(currentServices!=null) {
                for (SService currentService : currentServices) {
                    if (areSimilar(currentService, operation.getProcessorName())) {
                        foundInCurrentUoW = true;
                        break;
                    }
                }
            }
            if (!foundInCurrentUoW)
                remainingProcessors.add(operation.getProcessorName());
        }
        return remainingProcessors;
    }
}