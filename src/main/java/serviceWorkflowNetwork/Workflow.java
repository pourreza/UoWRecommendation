package serviceWorkflowNetwork;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Workflow {
    private String url;
    private int index;
    private ArrayList<WorkflowVersion> versions;
    private Set<Person> contributors;

    public Workflow(String url, int index, Person person) {
        this.url = url;
        this.index = index;
        this.versions = new ArrayList<WorkflowVersion>();
        this.contributors = new HashSet<Person>();
        contributors.add(person);
    }
    
    public void addVersion(WorkflowVersion version){
        versions.add(version);
    }

    @Override
    public boolean equals(Object obj) {
        return this.index == ((Workflow)obj).index;
    }

    @Override
    public int hashCode() {
        Integer indexInt = index;
        return indexInt.hashCode();
    }

    public ArrayList<WorkflowVersion> getVersions() {
        return versions;
    }

    public Integer getIndex() {
        return index;
    }

    public String getURL() {
        return url;
    }

    public void addContributor(Person person) {
        contributors.add(person);
    }

    public Set<Person> getContributors() {
        return contributors;
    }
}
