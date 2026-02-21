package ca.concordia.controller;

import ca.concordia.model.Arrondissement;
import ca.concordia.model.BixiStation;
import ca.concordia.model.BixiTrip;
import ca.concordia.model.RushHour;
import ca.concordia.model.MonthComparison;

public interface IBixiController {

    /**
     * Loads a Bixi data file from the specified file path.
     * @param filePath the path to the Bixi data file
     */
    void loadFile(String filePath);

    /**
     * Retrieves trips by station name and mode (start or end).
     * @param stationName
     * @param mode
     * @return
     */
    Iterable<BixiTrip> getTripsByStation(String stationName, String mode);

    /**
     * Retrieves trips by month.
     * @param month the month in "YYYY-MM" format
     * @return an iterable of BixiTrip objects for the specified month
     */
    Iterable<BixiTrip> getTripsByMonth(String month);


    /**
     * Retrieves trips with a duration greater than or equal to the specified minimum duration.
     * @param minDuration the minimum duration in minutes
     * @return an iterable of BixiTrip objects with duration >= minDuration
     */
    Iterable<BixiTrip> getTripsByDuration(float minDuration);

    /**
     * Retrieves trips that started within the specified time range.
     * @param startTime the start time in "YYYY-MM-DD HH:MM:SS" format
     * @param finalTime the end time in "YYYY-MM-DD HH:MM:SS" format
     * @return an iterable of BixiTrip objects that started within the specified time range
     */
    Iterable<BixiTrip> getTripsByStartTime(String startTime, String finalTime);


    /**
     * Retrieves the top k arrondissements with the highest number of trips.
     * @param k the number of top arrondissements to retrieve
     * @return an iterable of arrondissement names
     */
    Iterable<Arrondissement> getTopArrondissements(int k);


    /**
     * Retrieves the top k stations with the highest number of trips within the specified date range.
     * @param k the number of top stations to retrieve
     * @param startDate  the start date in "YYYY-MM-DD" format
     * @param endDate the end date in "YYYY-MM-DD" format
     * @return an iterable of station names
     */
    Iterable<BixiStation> getTopStations(int k, String startDate, String endDate);

    /**
     * Retrieves the rush hour (hour with the highest average number of trips) for the specified month.
     * @param month the month as an integer (1-12)
     * @return the hour (0-23) with the highest number of trips in the specified month
     */
    RushHour getRushHourOfMonth(int month);

    /**
     * Compares two months by total trips, top K start/end stations and rush-hour stats.
     * @param month1 first month (1-12)
     * @param month2 second month (1-12)
     * @param k number of top start/end stations
     * @return comparison payload with one section per month
     */
    MonthComparison compareMonths(int month1, int month2, int k);

}
