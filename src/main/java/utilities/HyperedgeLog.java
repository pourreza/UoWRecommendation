package utilities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HyperedgeLog {

    public long date;
    public Set<String> nodes;

    public HyperedgeLog(long year, List<String> nodes) {
        date = year;
        this.nodes = new HashSet<String>(nodes);
    }

    @Override
    public String toString() {
        return date+" : "+ nodes;
    }
}
