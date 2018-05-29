package serviceWorkflowNetwork;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.Date;

public class WorkflowVersion {
    private final int version;
    private final Workflow workflow;
    private final String description;
    private final String versionUrl;

    private Date creationDate;
    private Date updateDate;
    private int numberOfService;
    private int numberOfExternalServices;
    private ArrayList<OOperation> externalOperations = new ArrayList<OOperation>();
    private ArrayList<String> processorNames = new ArrayList<String>();
    private String intent;
    private DirectedGraph<String, DefaultEdge> directedProcessorGraph;

    public WorkflowVersion(Workflow workflow, int version, Date createDate, String workflowVersionURL, String description) {
        this.workflow = workflow;
        this.version = version;
        this.creationDate = createDate;
        this.versionUrl = workflowVersionURL;
        this.description = description;
    }

    @Override
    public String toString() {
        return versionUrl;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public void setNumberOfService(int numberOfService) {
        this.numberOfService = numberOfService;
    }

    public void setNumberOfExternalServices(int numberOfExternalServices) {
        this.numberOfExternalServices = numberOfExternalServices;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    @Override
    public boolean equals(Object obj) {
        return this.workflow.equals(((WorkflowVersion)obj).workflow) && (this.version == (((WorkflowVersion)obj).version));
    }

    @Override
    public int hashCode() {
        return versionUrl.hashCode();
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public int getNumExternalServices() {
        return numberOfExternalServices;
    }

    public int getNumServices() {
        return numberOfService;
    }

    public Integer getVersionIndex() {
        return version;
    }

    public void addExternalOperation(OOperation operation) {
        externalOperations.add(operation);
    }

    public ArrayList<OOperation> getExternalOperations() {
        return externalOperations;
    }

    public void addProcessorNames(String processorName) {
        this.processorNames.add(processorName);
    }

    public void setIntent() {
        this.intent = description;
        for(String processorName : processorNames){
            intent += " "+processorName;
        }
    }

    public String getIntent() {
        return intent;
    }

    public Date getDate() {
        return updateDate;
    }

    public void setDirectedProcessorGraph(DirectedGraph<String, DefaultEdge> directedProcessorGraph) {
        this.directedProcessorGraph = directedProcessorGraph;
    }

    public DirectedGraph<String, DefaultEdge> getDirectedProcessorGraph() {
        return directedProcessorGraph;
    }
}
