package serviceWorkflowNetwork;

import java.io.Serializable;
import java.util.Date;

public class SORelation implements Serializable{
    private OOperation operation;
    private SService service;
    private Date time;

    public SORelation(OOperation operation, SService service, Date time) {
        this.operation = operation;
        this.service = service;
        this.time = time;
    }

    @Override
    public boolean equals(Object obj) {
        return this.operation.equals(((SORelation)obj).operation) && this.service.equals(((SORelation)obj).service);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + operation.hashCode();
        hash = hash * 31 + service.hashCode();
        return hash;
    }
}