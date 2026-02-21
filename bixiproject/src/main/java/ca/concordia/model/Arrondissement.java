package ca.concordia.model;

public class Arrondissement {
    private final String name;
    private int departures;

    public Arrondissement(String name, int departures) {
        this.name = name;
        this.departures = departures;
    }

    public String getName() {
        return name;
    }

    public int getDepartures() {
        return departures;
    }

    public void setDepartures(int departures) {
        this.departures = departures;
    }

    @Override
    public String toString() {
        return name + " - " + departures;
    }
}
