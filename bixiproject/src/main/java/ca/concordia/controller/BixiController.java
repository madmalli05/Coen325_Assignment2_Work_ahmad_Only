package ca.concordia.controller;

import ca.concordia.model.Arrondissement;
import ca.concordia.model.BixiStation;
import ca.concordia.model.BixiTrip;
import ca.concordia.model.RushHour;
import ca.concordia.model.SimpleList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class BixiController implements IBixiController {
    private SimpleList<BixiTrip> trips;
    private final DateTimeFormatter dateTimeFormatter;

    public BixiController() {
        trips = new SimpleList<>();
        dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public void loadFile(String filePath) {
        clearTrips();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // header
            if (line == null) {
                return;
            }
            while ((line = reader.readLine()) != null) {
                parseLine(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }

    private void clearTrips() {
        trips = new SimpleList<>();
    }

    private void parseLine(String data) {
        if (data == null || data.isBlank() || data.startsWith("STARTSTATIONNAME")) {
            return;
        }

        String[] fields = data.split(",");
        if (fields.length < 10) {
            return;
        }

        try {
            BixiTrip trip = new BixiTrip(
                    fields[0].trim(),
                    fields[1].trim(),
                    Double.parseDouble(fields[2].trim()),
                    Double.parseDouble(fields[3].trim()),
                    fields[4].trim(),
                    fields[5].trim(),
                    Double.parseDouble(fields[6].trim()),
                    Double.parseDouble(fields[7].trim()),
                    Long.parseLong(fields[8].trim()),
                    Long.parseLong(fields[9].trim())
            );
            trips.add(trip);
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public Iterable<BixiTrip> getTripsByStation(String stationName, String mode) {
        SimpleList<BixiTrip> results = new SimpleList<>();
        String normalizedMode = mode == null ? "" : mode.trim().toLowerCase();

        for (BixiTrip trip : trips) {
            boolean add = false;
            if ("start".equals(normalizedMode) || "both".equals(normalizedMode)) {
                add = stationName.equalsIgnoreCase(trip.getStartStationName());
            }
            if (!add && ("end".equals(normalizedMode) || "both".equals(normalizedMode))) {
                add = stationName.equalsIgnoreCase(trip.getEndStationName());
            }
            if (add) {
                results.add(trip);
            }
        }
        return results;
    }

    @Override
    public Iterable<BixiTrip> getTripsByMonth(String month) {
        SimpleList<BixiTrip> results = new SimpleList<>();
        for (BixiTrip trip : trips) {
            YearMonth ym = yearMonthFromMs(trip.getStartTimeMs());
            if (month.equals(ym.toString())) {
                results.add(trip);
            }
        }
        sortTripsByStartTime(results, true);
        return results;
    }

    @Override
    public Iterable<BixiTrip> getTripsByDuration(float minDuration) {
        SimpleList<BixiTrip> results = new SimpleList<>();
        for (BixiTrip trip : trips) {
            if (trip.getDurationMinutes() > minDuration) {
                results.add(trip);
            }
        }
        sortTripsByDuration(results, false);
        return results;
    }

    @Override
    public Iterable<BixiTrip> getTripsByStartTime(String startTime, String finalTime) {
        long startMs = toEpochMs(startTime);
        long endMs = toEpochMs(finalTime);

        SimpleList<BixiTrip> results = new SimpleList<>();
        for (BixiTrip trip : trips) {
            long t = trip.getStartTimeMs();
            if (t >= startMs && t <= endMs) {
                results.add(trip);
            }
        }

        sortTripsByStartTime(results, true);
        return results;
    }

    @Override
    public Iterable<Arrondissement> getTopArrondissements(int k) {
        SimpleList<Arrondissement> counts = new SimpleList<>();

        for (BixiTrip trip : trips) {
            String name = trip.getStartStationArrondissement();
            int idx = findArrondissementIndex(counts, name);
            if (idx == -1) {
                counts.add(new Arrondissement(name, 1));
            } else {
                Arrondissement a = counts.get(idx);
                a.setDepartures(a.getDepartures() + 1);
            }
        }

        sortArrondissements(counts);
        return firstKArrondissements(counts, k);
    }

    @Override
    public Iterable<BixiStation> getTopStations(int k, String startDate, String endDate) {
        long startMs = toEpochMs(startDate);
        long endMs = toEpochMs(endDate);

        SimpleList<BixiStation> counts = new SimpleList<>();

        for (BixiTrip trip : trips) {
            if (trip.getStartTimeMs() < startMs || trip.getStartTimeMs() > endMs) {
                continue;
            }
            String station = trip.getStartStationName();
            int idx = findStationIndex(counts, station);
            if (idx == -1) {
                counts.add(new BixiStation(station, 1));
            } else {
                BixiStation s = counts.get(idx);
                s.setTripCount(s.getTripCount() + 1);
            }
        }

        sortStationsByCountDesc(counts);
        SimpleList<BixiStation> top = firstKStations(counts, k);
        sortStationsAlphabetical(top);
        return top;
    }

    @Override
    public RushHour getRushHourOfMonth(int month) {
        int[] hourlyCounts = new int[24];
        int[] uniqueDays = new int[4096];
        int dayCount = 0;

        for (BixiTrip trip : trips) {
            LocalDateTime dt = dateTimeFromMs(trip.getStartTimeMs());
            if (dt.getMonthValue() != month) {
                continue;
            }

            hourlyCounts[dt.getHour()]++;

            int dayKey = dt.getYear() * 10000 + dt.getMonthValue() * 100 + dt.getDayOfMonth();
            if (!contains(uniqueDays, dayCount, dayKey)) {
                uniqueDays[dayCount++] = dayKey;
            }
        }

        if (dayCount == 0) {
            return new RushHour(-1, 0.0);
        }

        int bestHour = 0;
        double bestAvg = -1;
        for (int hour = 0; hour < 24; hour++) {
            double avg = (double) hourlyCounts[hour] / dayCount;
            if (avg > bestAvg) {
                bestAvg = avg;
                bestHour = hour;
            }
        }

        return new RushHour(bestHour, bestAvg);
    }

    private int findArrondissementIndex(SimpleList<Arrondissement> arrondissements, String name) {
        for (int i = 0; i < arrondissements.size(); i++) {
            if (arrondissements.get(i).getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    private int findStationIndex(SimpleList<BixiStation> stations, String name) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.get(i).getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    private void sortTripsByStartTime(SimpleList<BixiTrip> list, boolean ascending) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size() - 1 - i; j++) {
                BixiTrip a = list.get(j);
                BixiTrip b = list.get(j + 1);
                boolean shouldSwap = ascending ? a.getStartTimeMs() > b.getStartTimeMs() : a.getStartTimeMs() < b.getStartTimeMs();
                if (shouldSwap) {
                    swapTrips(list, j, j + 1);
                }
            }
        }
    }

    private void sortTripsByDuration(SimpleList<BixiTrip> list, boolean ascending) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size() - 1 - i; j++) {
                double da = list.get(j).getDurationMinutes();
                double db = list.get(j + 1).getDurationMinutes();
                boolean shouldSwap = ascending ? da > db : da < db;
                if (shouldSwap) {
                    swapTrips(list, j, j + 1);
                }
            }
        }
    }

    private void swapTrips(SimpleList<BixiTrip> list, int i, int j) {
        BixiTrip left = list.get(i);
        BixiTrip right = list.get(j);
        list.rawArray()[i] = right;
        list.rawArray()[j] = left;
    }

    private void sortArrondissements(SimpleList<Arrondissement> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size() - 1 - i; j++) {
                Arrondissement a = list.get(j);
                Arrondissement b = list.get(j + 1);
                boolean shouldSwap = a.getDepartures() < b.getDepartures()
                        || (a.getDepartures() == b.getDepartures() && a.getName().compareToIgnoreCase(b.getName()) > 0);
                if (shouldSwap) {
                    list.rawArray()[j] = b;
                    list.rawArray()[j + 1] = a;
                }
            }
        }
    }

    private void sortStationsByCountDesc(SimpleList<BixiStation> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size() - 1 - i; j++) {
                BixiStation a = list.get(j);
                BixiStation b = list.get(j + 1);
                boolean shouldSwap = a.getTripCount() < b.getTripCount()
                        || (a.getTripCount() == b.getTripCount() && a.getName().compareToIgnoreCase(b.getName()) > 0);
                if (shouldSwap) {
                    list.rawArray()[j] = b;
                    list.rawArray()[j + 1] = a;
                }
            }
        }
    }

    private void sortStationsAlphabetical(SimpleList<BixiStation> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size() - 1 - i; j++) {
                BixiStation a = list.get(j);
                BixiStation b = list.get(j + 1);
                if (a.getName().compareToIgnoreCase(b.getName()) > 0) {
                    list.rawArray()[j] = b;
                    list.rawArray()[j + 1] = a;
                }
            }
        }
    }

    private SimpleList<Arrondissement> firstKArrondissements(SimpleList<Arrondissement> list, int k) {
        SimpleList<Arrondissement> top = new SimpleList<>();
        int limit = Math.min(k, list.size());
        for (int i = 0; i < limit; i++) {
            top.add(list.get(i));
        }
        return top;
    }

    private SimpleList<BixiStation> firstKStations(SimpleList<BixiStation> list, int k) {
        SimpleList<BixiStation> top = new SimpleList<>();
        int limit = Math.min(k, list.size());
        for (int i = 0; i < limit; i++) {
            top.add(list.get(i));
        }
        return top;
    }

    private YearMonth yearMonthFromMs(long timeMs) {
        LocalDateTime dt = dateTimeFromMs(timeMs);
        return YearMonth.of(dt.getYear(), dt.getMonthValue());
    }

    private LocalDateTime dateTimeFromMs(long timeMs) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMs), ZoneId.systemDefault());
    }

    private long toEpochMs(String input) {
        String trimmed = input.trim();
        if (trimmed.length() == 10) {
            LocalDate date = LocalDate.parse(trimmed);
            return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        LocalDateTime dateTime = LocalDateTime.parse(trimmed, dateTimeFormatter);
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private boolean contains(int[] values, int size, int target) {
        for (int i = 0; i < size; i++) {
            if (values[i] == target) {
                return true;
            }
        }
        return false;
    }
}
