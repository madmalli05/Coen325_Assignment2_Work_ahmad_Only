package ca.concordia.view;

import ca.concordia.controller.BixiController;
import ca.concordia.controller.IBixiController;
import ca.concordia.model.Arrondissement;
import ca.concordia.model.BixiStation;
import ca.concordia.model.BixiTrip;
import ca.concordia.model.MonthComparison;
import ca.concordia.model.RushHour;

import java.util.Scanner;

public class BixiView {

    private final IBixiController controller;

    /**
     * Constructor for BixiView.
     * Initializes the controller
     */
    public BixiView() {
        controller = new BixiController();
    }

    /**
     * Starts the Bixi data viewer application.
     */
    public void start() {
        System.out.println("Welcome to the Bixi Data Viewer!");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter the path to the Bixi data file: ");
        String filePath = scanner.nextLine();

        try {
            controller.loadFile(filePath);
        } catch (RuntimeException ex) {
            System.out.println("Error while loading file: " + ex.getMessage());
            return;
        }

        System.out.println("\nData loaded successfully.");
        System.out.println("Total trips loaded: " + controller.getTotalTripsLoaded());
        System.out.println("Unique stations loaded: " + controller.getUniqueStationsLoaded());

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Select an option: ");
            String option = scanner.nextLine().trim();

            switch (option) {
                case "1" -> executeTimed("Req.1", () -> handleReq1(scanner));
                case "2" -> executeTimed("Req.2", () -> handleReq2(scanner));
                case "3" -> executeTimed("Req.3", () -> handleReq3(scanner));
                case "4" -> executeTimed("Req.4", () -> handleReq4(scanner));
                case "5" -> executeTimed("Req.5", () -> handleReq5(scanner));
                case "6" -> executeTimed("Req.6", () -> handleReq6(scanner));
                case "7" -> executeTimed("Req.7", () -> handleReq7(scanner));
                case "8" -> executeTimed("Req.8", () -> handleReq8(scanner));
                case "0" -> {
                    running = false;
                    System.out.println("Goodbye.");
                }
                default -> System.out.println("Invalid option. Try again.");
            }
            System.out.println();
        }
    }

    private void executeTimed(String label, Runnable action) {
        long startNs = System.nanoTime();
        try {
            action.run();
        } finally {
            long elapsedNs = System.nanoTime() - startNs;
            double elapsedMs = elapsedNs / 1_000_000.0;
            System.out.printf("Execution time (%s): %.3f ms%n", label, elapsedMs);
        }
    }

    private void printMenu() {
        System.out.println("\n========== MENU ==========");
        System.out.println("1) Req.1 - List trips by station + mode");
        System.out.println("2) Req.2 - List trips by month");
        System.out.println("3) Req.3 - List trips with duration > X minutes");
        System.out.println("4) Req.4 - List trips by start-time interval");
        System.out.println("5) Req.5 - Top N arrondissements by departures");
        System.out.println("6) Req.6 - Top K start stations in period");
        System.out.println("7) Req.7 - Rush hour of a month");
        System.out.println("8) Req.8 - Compare two months");
        System.out.println("0) Exit");
    }

    private void handleReq1(Scanner scanner) {
        System.out.print("Station name: ");
        String station = scanner.nextLine();
        System.out.print("Mode (start/end/both): ");
        String mode = scanner.nextLine();

        Iterable<BixiTrip> trips = controller.getTripsByStation(station, mode);
        printTrips(trips);
    }

    private void handleReq2(Scanner scanner) {
        System.out.print("Month (YYYY-MM): ");
        String month = scanner.nextLine();
        Iterable<BixiTrip> trips = controller.getTripsByMonth(month);
        printTrips(trips);
    }

    private void handleReq3(Scanner scanner) {
        System.out.print("Minimum duration in minutes (X): ");
        float duration = Float.parseFloat(scanner.nextLine());
        Iterable<BixiTrip> trips = controller.getTripsByDuration(duration);
        printTrips(trips);
    }

    private void handleReq4(Scanner scanner) {
        System.out.print("Start time (YYYY-MM-DD HH:mm:ss): ");
        String start = scanner.nextLine();
        System.out.print("End time (YYYY-MM-DD HH:mm:ss): ");
        String end = scanner.nextLine();
        Iterable<BixiTrip> trips = controller.getTripsByStartTime(start, end);
        printTrips(trips);
    }

    private void handleReq5(Scanner scanner) {
        System.out.print("Top N arrondissements: ");
        int n = Integer.parseInt(scanner.nextLine());
        Iterable<Arrondissement> arrondissements = controller.getTopArrondissements(n);

        int count = 0;
        for (Arrondissement a : arrondissements) {
            System.out.println(a);
            count++;
        }
        System.out.println("Total results: " + count);
    }

    private void handleReq6(Scanner scanner) {
        System.out.print("Top K start stations: ");
        int k = Integer.parseInt(scanner.nextLine());
        System.out.print("Start date (YYYY-MM-DD): ");
        String startDate = scanner.nextLine();
        System.out.print("End date (YYYY-MM-DD): ");
        String endDate = scanner.nextLine();

        Iterable<BixiStation> stations = controller.getTopStations(k, startDate, endDate);
        int count = 0;
        for (BixiStation s : stations) {
            System.out.println(s);
            count++;
        }
        System.out.println("Total results: " + count);
    }

    private void handleReq7(Scanner scanner) {
        System.out.print("Month number (1..12): ");
        int month = Integer.parseInt(scanner.nextLine());
        RushHour rushHour = controller.getRushHourOfMonth(month);

        if (rushHour.getHour() == -1) {
            System.out.println("No trips found for this month.");
            return;
        }

        System.out.println("Rush hour: " + rushHour.getHour());
        System.out.println("Average trips/day during rush hour: " + rushHour.getAverageTrips());
    }

    private void handleReq8(Scanner scanner) {
        System.out.print("Month 1 (1..12): ");
        int month1 = Integer.parseInt(scanner.nextLine());
        System.out.print("Month 2 (1..12): ");
        int month2 = Integer.parseInt(scanner.nextLine());
        System.out.print("Top K stations: ");
        int k = Integer.parseInt(scanner.nextLine());

        MonthComparison comparison = controller.compareMonths(month1, month2, k);
        printMonthStats(comparison.getFirstMonth());
        printMonthStats(comparison.getSecondMonth());
    }

    private void printMonthStats(MonthComparison.MonthStats stats) {
        System.out.println("\n---- Month " + stats.getMonth() + " ----");
        System.out.println("Total trips: " + stats.getTotalTrips());

        System.out.println("Top start stations:");
        for (BixiStation station : stats.getTopStartStations()) {
            System.out.println("  " + station);
        }

        System.out.println("Top end stations:");
        for (BixiStation station : stats.getTopEndStations()) {
            System.out.println("  " + station);
        }

        RushHour rushHour = stats.getRushHour();
        if (rushHour.getHour() == -1) {
            System.out.println("Rush hour: none (no trips)");
        } else {
            System.out.println("Rush hour: " + rushHour.getHour() +
                    " (avg/day = " + rushHour.getAverageTrips() + ")");
        }
    }

    private void printTrips(Iterable<BixiTrip> trips) {
        int count = 0;
        for (BixiTrip trip : trips) {
            System.out.println(trip);
            count++;
        }
        System.out.println("Total results: " + count);
    }
}
