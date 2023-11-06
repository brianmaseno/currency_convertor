import javafx.application.Application;
/*THIS*/import javafx.collections.FXCollections;
/*THIS*/import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * CurrencyConverterGUI - Currency Conversion Graphical User Interface
 *
 * This JavaFX application provides a user-friendly graphical interface for
 * converting
 * currency based on exchange rate data stored in a JSON file. It allows users
 * to select
 * source and target currencies, enter an amount, and calculate the converted
 * amount.
 *
 * Features:
 * - Load exchange rate data from a JSON file.
 * - Select source and target currencies from dropdown lists.
 * - Enter the amount to be converted.
 * - Calculate and display the converted amount.
 *
 * Usage:
 * - Launch the application, providing a JSON file containing exchange rate
 * data.
 * - The application will display a window with currency conversion options.
 * - Select source and target currencies, enter an amount, and click the
 * "Convert" button.
 * - The application will calculate and display the converted amount.
 *
 * Class Structure:
 * - `CurrencyConverterGUI` extends the JavaFX `Application` class and provides
 * the
 * graphical user interface for the currency converter.
 *
 * Methods:
 * - `start(Stage primaryStage)`: The main entry point for the JavaFX
 * application.
 * It sets up the GUI components, including dropdown lists, input fields, and
 * buttons.
 * The user can select currencies, enter an amount, and initiate currency
 * conversion.
 *
 * - `showAlert(String title, String message)`: Helper method to display alert
 * dialogs
 * for error messages and information.
 *
 * Dependencies:
 * - JavaFX for the graphical user interface.
 * - Google Gson library for JSON parsing and data handling.
 *
 * Note:
 * Before using this application, you need to provide the path to the JSON file
 * containing
 * exchange rate data in the `FileReader` constructor. Ensure that the JSON file
 * has a
 * "rates" field that contains currency conversion rates.
 */

public class CurrencyConverterGUI extends Application {
    private JsonObject ratesObject;
    /* THIS */ private CustomComboBox sourceCurrencyComboBox;
    /* THIS */ private CustomComboBox targetCurrencyComboBox;

    // HISTORY VARIABLE INITIALIZATION
    private static final int MAX_HISTORY_SIZE = 5; // Setting the maximum history size

    private static List<String> inputHistory = new ArrayList<>();
    private static String lastInputDate = "";

    public static void main(String[] args) {

        // Run the Python script to fetch currency rates
        runPythonScript();

        // Launch the JavaFX application
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Currency Converter");

        try (FileReader reader = new FileReader("currency_rates.json")) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            if (jsonObject.has("rates")) {
                ratesObject = jsonObject.getAsJsonObject("rates");
            } else {
                showAlert("Invalid JSON format", "'rates' field not found in JSON.");
                return;
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to read currency rates file: " + e.getMessage());
            return;
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
            return;
        }

        // GUI components
        BorderPane borderPane = new BorderPane();
        /* THIS */ sourceCurrencyComboBox = new CustomComboBox();
        /* THIS */ targetCurrencyComboBox = new CustomComboBox();
        TextField amountTextField = new TextField();
        Label resultLabel = new Label();
        TextArea lastInputsArea = new TextArea();

        // HISTORY IMPLEMENTATION

        // Creating a VBox to hold history
        VBox vbox1 = new VBox(lastInputsArea);

        // Fill sourceCurrencyComboBox and targetCurrencyComboBox from ratesObject
        Set<Map.Entry<String, JsonElement>> entries = ratesObject.entrySet();
        for (Map.Entry<String, JsonElement> entry : entries) {
            sourceCurrencyComboBox.getItems().add(entry.getKey());
            targetCurrencyComboBox.getItems().add(entry.getKey());
        }

        Button convertButton = new Button("Convert");
        convertButton.setOnAction(e -> {
            String sourceCurrency = sourceCurrencyComboBox.getValue();
            String targetCurrency = targetCurrencyComboBox.getValue();
            double amount;

            try {
                amount = Double.parseDouble(amountTextField.getText());
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter a valid numeric amount.");
                return;
            }

            if (ratesObject.has(sourceCurrency) && ratesObject.has(targetCurrency)) {
                double sourceToUSD = ratesObject.get(sourceCurrency).getAsDouble();
                double targetToUSD = ratesObject.get(targetCurrency).getAsDouble();
                double conversionRate = targetToUSD / sourceToUSD;

                double convertedAmount = amount * conversionRate;
                resultLabel.setText(
                        amount + " " + sourceCurrency + " is equal to " + convertedAmount + " " + targetCurrency);
                LocalDateTime now = LocalDateTime.now();
                // Define a custom format
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy - hh:mm a",
                        Locale.ENGLISH);
                String formattedDateTime = now.format(formatter);
                lastInputDate = formattedDateTime;

            } else {
                showAlert("Invalid Currency Codes", "Selected currency codes or conversion rates not found.");
            }

        });
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(event -> {

            lastInputsArea.appendText("\n" + resultLabel.getText() + "\n");
            lastInputsArea.appendText("As of: " + lastInputDate + "\n");
            sourceCurrencyComboBox.setValue(null);
            targetCurrencyComboBox.setValue(null);
            amountTextField.setText("");
            resultLabel.setText("");
            // Checking whether an input is present.

        });
        Button clearButton2 = new Button("Clear History");
        clearButton2.setOnAction(event -> {

            lastInputsArea.setText("");

        });

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("Source Currency:"),
                sourceCurrencyComboBox,
                new Label("Target Currency:"),
                targetCurrencyComboBox,
                new Label("Amount:"),
                amountTextField,
                convertButton,
                clearButton,
                new Label("Result:"),
                resultLabel,
                new Label("Input History:"),
                lastInputsArea,
                clearButton2);
        borderPane.setCenter(vbox);

        Scene scene = new Scene(borderPane, 400, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Specific exceptions, such as FileNotFoundException and IOException, are
    // caught to provide more informative error messages to the user
    // The showAlert method is enhanced to provide user-friendly error messages
    private void readExchangeRateData(String jsonFilePath) throws IOException {
        try (FileReader reader = new FileReader(jsonFilePath)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            if (jsonObject.has("rates")) {
                ratesObject = jsonObject.getAsJsonObject("rates");
            } else {
                throw new IOException("'rates' field not found in JSON.");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void runPythonScript() {
        try {
            // Specify the command to run the Python script
            String pythonScript = "python"; // Modify this if your Python interpreter has a different name
            String scriptPath = "fetch_currency_rates.py"; // Provide the correct path

            ProcessBuilder processBuilder = new ProcessBuilder(pythonScript, scriptPath);
            Process process = processBuilder.start();

            // Wait for the Python script to complete
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Python script executed successfully.");
            } else {
                System.err.println("Error running Python script. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* THIS IS THE SEARCH FUNCTION */
    private class CustomComboBox extends ComboBox<String> {
        private final TextField editor;
        private ObservableList<String> originalItems;
        private ObservableList<String> filteredItems;

        public CustomComboBox() {
            editor = getEditor();
            setEditable(true);

            editor.textProperty().addListener((observable, oldValue, newValue) -> {
                if (originalItems == null) {
                    originalItems = FXCollections.observableArrayList(getItems());
                }

                if (newValue.isEmpty()) {
                    filteredItems = originalItems;
                } else {
                    filteredItems = originalItems.filtered(item -> item.toLowerCase().contains(newValue.toLowerCase()));
                }
                setItems(filteredItems);

                if (!filteredItems.isEmpty()) {
                    show();
                } else {
                    hide();
                }
            });

            setOnHidden(e -> {
                setItems(originalItems);
                filteredItems = null;
            });

            // Add this part to set the text of the editor when an item is selected
            setOnAction(e -> {
                if (getSelectionModel().getSelectedItem() != null) {
                    editor.setText(getSelectionModel().getSelectedItem());
                }
                hide();
            });
        }
    }

}
