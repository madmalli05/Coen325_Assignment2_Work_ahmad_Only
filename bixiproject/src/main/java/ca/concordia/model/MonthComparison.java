package ca.concordia.model;

public class MonthComparison {
    private final MonthStats firstMonth;
    private final MonthStats secondMonth;

    public MonthComparison(MonthStats firstMonth, MonthStats secondMonth) {
        this.firstMonth = firstMonth;
        this.secondMonth = secondMonth;
    }

    public MonthStats getFirstMonth() {
        return firstMonth;
    }

    public MonthStats getSecondMonth() {
        return secondMonth;
    }

    public static class MonthStats {
        private final int month;
        private final int totalTrips;
        private final SimpleList<BixiStation> topStartStations;
        private final SimpleList<BixiStation> topEndStations;
        private final RushHour rushHour;

        public MonthStats(int month,
                          int totalTrips,
                          SimpleList<BixiStation> topStartStations,
                          SimpleList<BixiStation> topEndStations,
                          RushHour rushHour) {
            this.month = month;
            this.totalTrips = totalTrips;
            this.topStartStations = topStartStations;
            this.topEndStations = topEndStations;
            this.rushHour = rushHour;
        }

        public int getMonth() {
            return month;
        }

        public int getTotalTrips() {
            return totalTrips;
        }

        public Iterable<BixiStation> getTopStartStations() {
            return topStartStations;
        }

        public Iterable<BixiStation> getTopEndStations() {
            return topEndStations;
        }

        public RushHour getRushHour() {
            return rushHour;
        }
    }
}
