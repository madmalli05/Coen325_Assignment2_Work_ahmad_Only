package ca.concordia.model;

public class RushHour {
    private final int hour;
    private final double averageTrips;

    public RushHour(int hour, double averageTrips) {
        this.hour = hour;
        this.averageTrips = averageTrips;
    }

    public int getHour() {
        return hour;
    }

    public double getAverageTrips() {
        return averageTrips;
    }

    @Override
    public String toString() {
        return "RushHour{" +
                "hour=" + hour +
                ", averageTrips=" + averageTrips +
                '}';
    }
}
