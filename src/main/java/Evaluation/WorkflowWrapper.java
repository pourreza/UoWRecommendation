package Evaluation;

import serviceWorkflowNetwork.WorkflowVersion;

import java.io.Serializable;

public class WorkflowWrapper implements Serializable, Comparable {
    private WorkflowVersion workflow;
    private double precision =0;
    private double recall = 0;
    private double fscore = 0;
    private double structure = 0;
    private double steps = 0;
    private double links = 0;
    private double hits = 0;
    private double workflowStructer = 0;
    private int recommendedGraphServices;

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getFscore() {
        return fscore;
    }

    public double getStructure() {
        return structure;
    }

    public double getWorkflowStructer() {
        return workflowStructer;
    }

    public void setWorkflow(WorkflowVersion workflow) {
        this.workflow = workflow;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public void setFscore(double fscore) {
        this.fscore = fscore;
    }

    public void setStructure(double structure) {
        this.structure = structure;
    }

    public void setLinks(double links) {
        this.links = links;
    }

    public void setHits(double hits) {
        this.hits = hits;
    }

    public void setWorkflowStructer(double workflowStructer) {
        this.workflowStructer = workflowStructer;
    }

    public double getSteps() {
        return steps;
    }

    public double getLinks() {
        return links;
    }

    public double getHits() {
        return hits;
    }

    public WorkflowWrapper(WorkflowVersion workflow) {
        this.workflow = workflow;
    }

    public void setPrecisionRecall(double precision, double recall) {
        this.precision = precision;
        this.recall = recall;
        this.fscore = 2*precision*recall/(precision+recall);
    }

    public void setSteps(double stepsSaved) {
        this.steps = stepsSaved;
    }

    public void setLinksNeedDecision(double linksSaved) {
        this.links = linksSaved;
    }

    public void setHitCounts(double hitRate) {
        this.hits = hitRate;
    }

    public int compareTo(Object o) {
        return (int)((workflow.getDate().getTime()-((WorkflowWrapper)o).workflow.getDate().getTime())/1000);
    }

    public WorkflowVersion getWorkflow() {
        return workflow;
    }

    public void setRecommendedGraphServices(int recommendedGraphServices) {
        this.recommendedGraphServices = recommendedGraphServices;
    }

    public int getRecommendedGraphServices() {
        return recommendedGraphServices;
    }
}