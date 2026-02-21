package ca.concordia.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BixiController implements IBixiController {

    @Override
    public void loadFile(String filePath) {
        // Implementation to load the file
        System.out.println("Loading file from: " + filePath);
        Path path = Path.of("data/bixi2025.csv");

        try (var lines = Files.lines(path)) {
            lines.forEach(this::parseLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseLine(String data) {
        // Implementation to parse data
        String[] fields = data.split(",");
        for (String field : fields) {
            System.out.println(field);
        }
    }
}
