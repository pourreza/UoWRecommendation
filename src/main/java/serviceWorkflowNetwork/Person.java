package serviceWorkflowNetwork;

import java.io.Serializable;

public class Person implements Serializable{
    private String name;
    private int userID; // This is his myExperimentId

    public Person(String name, int id) {
        this.name = name;
        this.userID = id;
    }

    @Override
    public boolean equals(Object obj) {
        return this.userID == (((Person) obj).userID);
    }

    @Override
    public int hashCode() {
        Integer id = userID;
        return id.hashCode();
    }
}