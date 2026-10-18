package aiSafe.flight.dsl;

import java.nio.file.Path;
import java.util.List;

public class DslValidationReproduction {
    public static void main(String[] args) {
        try {
            Path filePath = Path.of("FlightPlans/test-width-wind-invalid.flight");
            System.out.println("Validating file: " + filePath.toAbsolutePath());
            
            FlightDslValidationResult result = FlightDslValidator.validateFile(filePath);
            
            if (result.isValid()) {
                System.out.println("SUCCESS: File is valid (NOT EXPECTED)");
            } else {
                System.out.println("FAILURE: File is invalid (EXPECTED)");
                List<FlightDslError> errors = result.errors();
                System.out.println("Found " + errors.size() + " errors:");
                for (FlightDslError error : errors) {
                    System.out.println("- " + error.format());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
