public class Actor {

    private final String name;

    private final int dob;

    private final String id;

    public Actor(String name, int dob, String id) {
        this.name = name;
        this.dob = dob;
        this.id = id;

    }

    public int getDob() {
        return dob;
    }

    public String getName() {
        return name;
    }

    public String getStarId() {
        return id;
    }


    public String toString() {

        return "Name:" + getName() + ", " +
                "Star Id: " + getStarId() + ", " +
                "Age:" + getDob() + ".";
    }
}
