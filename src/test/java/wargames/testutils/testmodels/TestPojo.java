package wargames.testutils.testmodels;

import java.util.*;

public class TestPojo {

    private String id;
    private String name;
    private int    score;
    private List<String> tags = new ArrayList<>();
    private Map<String, Integer> counters = new LinkedHashMap<>();

    public TestPojo() {}

    public TestPojo(String id, String name, int score) {
        this.id = id; this.name = name; this.score = score;
    }

    public TestPojo withTags(String... arr) { 
        this.tags = new ArrayList<>(Arrays.asList(arr)); 
        return this;
    }

    public TestPojo withCounter(String k, int v) {
        this.counters.put(k, v); 
        return this;
    }

    public String       getId()    { return id; }
    public String       getName()  { return name; }
    public int          getScore() { return score; }
    public List<String> getTags()  { return tags; }
    public Map<String, Integer> getCounters() { return counters; }

    public void setId(String id)           { this.id = id; }
    public void setName(String name)       { this.name = name; }
    public void setScore(int score)        { this.score = score; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setCounters(Map<String, Integer> counters) { this.counters = counters; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestPojo)) return false;
        TestPojo t = (TestPojo) o;
        return score == t.score &&
               Objects.equals(id, t.id) &&
               Objects.equals(name, t.name) &&
               Objects.equals(tags, t.tags) &&
               Objects.equals(counters, t.counters);
    }

    @Override 
    public int hashCode() { 
        return Objects.hash(id, name, score, tags, counters);
    }

    @Override 
    public String toString() { 
        return "TestPojo{id=" + id + ", name=" + name + ", score=" + score + "}";
    }

}
