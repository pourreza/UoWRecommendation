package serviceWorkflowNetwork;

import java.util.Date;

public class OWRelation {
    OOperation operation;
    WorkflowVersion workflowVersion;
    Date time;

    public OWRelation(OOperation operation, WorkflowVersion workflowVersion, Date time) {
        this.operation = operation;
        this.workflowVersion = workflowVersion;
        this.time = time;
    }

    @Override
    public boolean equals(Object obj) {
        return this.operation.equals(((OWRelation)obj).operation) && this.workflowVersion.equals(((OWRelation)obj).workflowVersion);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + operation.hashCode();
        hash = hash * 31 + workflowVersion.hashCode();
        return hash;
    }
}
