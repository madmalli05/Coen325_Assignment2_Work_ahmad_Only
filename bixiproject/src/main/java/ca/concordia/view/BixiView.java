package ca.concordia.view;

import ca.concordia.controller.BixiController;
import ca.concordia.controller.IBixiController;

import java.util.Scanner;

public class BixiView {

    private IBixiController controller;

    /**
     * Constructor for BixiView.
     * Initializes the controller
     */
    public BixiView(){
        controller = new BixiController();
    }

    /**
     * Starts the Bixi data viewer application.
     */
    public void start() {
        String message = "Welcome to the Bixi Data Viewer!";
        System.out.println(message);
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter the path to the Bixi data file: ");
        String filePath = scanner.nextLine();
        controller.loadFile(filePath);
        //TODO - Complete
    }
}
