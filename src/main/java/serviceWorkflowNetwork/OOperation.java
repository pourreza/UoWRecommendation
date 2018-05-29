package serviceWorkflowNetwork;

public class OOperation {
    private SService service;
    private String name;
    private String processorName;

    public OOperation(SService service, String name, String processorName) {
        this.service = service;
        this.name = name;
        this.processorName = processorName;
    }

    @Override
    public String toString() {
        return "name: "+ name + " processor name: "+ processorName +" @@@@@@@@@@@@ serviceURL: "+ service.getURL();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OOperation))
            return false;
        if(obj==this)
            return true;
        return (((OOperation)obj).name.equals(this.name)) && (((OOperation)obj).service.equals(this.service));
    }

    public int hashCode(){
        int hash = 17;
        hash = hash * 31 + name.hashCode();
        hash = hash * 31 + service.hashCode();
        return hash;//for simplicity reason
    }

    public SService getService() {
        return service;
    }

    public String getName() {
        return name;
    }

    public String getProcessorName() {
        return processorName;
    }
}
