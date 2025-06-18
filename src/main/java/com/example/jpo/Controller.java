package com.example.jpo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.Map;

import javafx.stage.FileChooser;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileNotFoundException;

public class Controller {
    @FXML
    private VBox checkboxContainer;

    @FXML
    private Button submitButton;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private LineChart<String, Number> lineChart;

    @FXML private TextField cityInput;
    @FXML private Label locationStatusLabel;

    private Float currentLatitude = null;
    private Float currentLongitude = null;


    private final LocalDate START_MIN_DATE = LocalDate.now().minusDays(92);
    private final LocalDate MAX_DATE = LocalDate.now().plusDays(15);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        startDatePicker.setDayCellFactory(getStartDateCellFactory());
        startDatePicker.setValue(LocalDate.now().isBefore(START_MIN_DATE) ? START_MIN_DATE : LocalDate.now());
        updateEndDatePickerLimits(startDatePicker.getValue());

        startDatePicker.setOnAction(e -> {
            LocalDate start = startDatePicker.getValue();
            updateEndDatePickerLimits(start);
            LocalDate end = endDatePicker.getValue();
            if (end == null || end.isBefore(start) || end.isAfter(MAX_DATE)) {
                endDatePicker.setValue(start);
            }
        });

        endDatePicker.setOnAction(e -> System.out.println("End date: " + endDatePicker.getValue()));

        for (ApiFetchTypes type : ApiFetchTypes.values()) {
            CheckBox checkBox = new CheckBox(type.polishDescription);
            checkBox.setUserData(type);
            checkboxContainer.getChildren().add(checkBox);
        }

        lineChart.getData().clear();
    }

    @FXML
    protected void onSubmitButtonClick() {
        submitButton.setDisable(true);

        long selectedMask = getSelectedApiFetchTypesMask();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        new Thread(() -> {
            String result = ApiHandler.GetData(selectedMask, currentLatitude, currentLongitude, startDate, endDate);

            try {
                JsonNode root = objectMapper.readTree(result);

                javafx.application.Platform.runLater(() -> {
                    lineChart.getData().clear();

                    Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> entry = fields.next();
                        String measurementType = entry.getKey();
                        JsonNode timeValueMap = entry.getValue();

                        ApiFetchTypes matchedType = null;
                        for (ApiFetchTypes t : ApiFetchTypes.values()) {
                            if (t.description.equals(measurementType)) {
                                matchedType = t;
                                break;
                            }
                        }
                        String seriesName = matchedType != null ? matchedType.polishDescription + "(" + matchedType.unit + ")" : measurementType;

                        XYChart.Series<String, Number> series = new XYChart.Series<>();
                        series.setName(seriesName);

                        Iterator<Map.Entry<String, JsonNode>> times = timeValueMap.fields();
                        while (times.hasNext()) {
                            Map.Entry<String, JsonNode> timeEntry = times.next();
                            String time = timeEntry.getKey();
                            String valueStr = timeEntry.getValue().asText();
                            try {
                                double value = Double.parseDouble(valueStr);
                                series.getData().add(new XYChart.Data<>(time, value));
                            } catch (NumberFormatException ignored) {
                            }
                        }

                        lineChart.getData().add(series);
                    }

                    var xAxis = (javafx.scene.chart.CategoryAxis) lineChart.getXAxis();
                    xAxis.setTickLabelRotation(45);
                    xAxis.setTickLabelsVisible(true);
                    xAxis.setTickMarkVisible(true);

                    submitButton.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                javafx.application.Platform.runLater(() -> submitButton.setDisable(false));
            }
        }).start();
    }

    private Callback<DatePicker, DateCell> getStartDateCellFactory() {
        return dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isBefore(START_MIN_DATE) || item.isAfter(MAX_DATE)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #EEEEEE;");
                }
            }
        };
    }

    private void updateEndDatePickerLimits(LocalDate startDate) {
        final LocalDate minEndDate = startDate;
        final LocalDate maxEndDate = MAX_DATE;

        Callback<DatePicker, DateCell> dayCellFactory = dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isBefore(minEndDate) || item.isAfter(maxEndDate)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #EEEEEE;");
                }
            }
        };

        endDatePicker.setDayCellFactory(dayCellFactory);

        if (endDatePicker.getValue() == null || endDatePicker.getValue().isBefore(minEndDate) || endDatePicker.getValue().isAfter(maxEndDate)) {
            endDatePicker.setValue(minEndDate);
        }
    }

    private long getSelectedApiFetchTypesMask() {
        long mask = 0L;
        for (var node : checkboxContainer.getChildren()) {
            if (node instanceof CheckBox checkBox && checkBox.isSelected()) {
                ApiFetchTypes type = (ApiFetchTypes) checkBox.getUserData();
                mask |= type.bit;
            }
        }
        return mask;
    }

    @FXML
    protected void onExportButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Chart Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        File file = fileChooser.showSaveDialog(lineChart.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                for (XYChart.Series<String, Number> series : lineChart.getData()) {
                    writer.println(series.getName() + ":");
                    for (XYChart.Data<String, Number> data : series.getData()) {
                        writer.printf("%s = %s%n", data.getXValue(), data.getYValue());
                    }
                    writer.println();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void onSearchCityButtonClick() {
        String cityName = cityInput.getText().trim();
        if (cityName.isEmpty()) {
            locationStatusLabel.setText("Podaj nazwę miasta.");
            submitButton.setDisable(true);
            return;
        }

        locationStatusLabel.setText("Szukanie miasta...");
        submitButton.setDisable(true);

        new Thread(() -> {
            try {
                float[] coords = ApiHandler.GetCoordinatesForCity(cityName);
                currentLatitude = coords[0];
                currentLongitude = coords[1];

                String locationInfo = String.format("Znaleziono: %s (lat: %.4f, lon: %.4f)", cityName, currentLatitude, currentLongitude);

                javafx.application.Platform.runLater(() -> {
                    locationStatusLabel.setText(locationInfo);
                    submitButton.setDisable(false);
                });
            } catch (IllegalArgumentException e) {
                javafx.application.Platform.runLater(() -> {
                    locationStatusLabel.setText("Nie znaleziono miasta: " + cityName);
                    submitButton.setDisable(true);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    locationStatusLabel.setText("Błąd podczas wyszukiwania miasta.");
                    submitButton.setDisable(true);
                });
            }
        }).start();
    }
}
