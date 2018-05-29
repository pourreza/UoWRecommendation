package serviceWorkflowNetwork;

public class SService {
    private String url;
    private ServiceType type;
    private String intent;

    public SService(String url, ServiceType type) {
        this.url = url;
        this.type = type;
    }

    @Override
    public String toString() {
        return "TYPE: "+ type+" &&&&&&&&&&&&&&&&&&&& url: " + url;
    }

    @Override
    public boolean equals(Object obj) {
        return this.url.equals(((SService)obj).url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    public String getURL() {
        return url;
    }

    public ServiceType getType() {
        return type;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getIntent() {
        return intent;
    }
}
