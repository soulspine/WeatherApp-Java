package com.example.jpo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.Map;

public class HelloController {
    @FXML
    private FlowPane checkboxContainer;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private LineChart<String, Number> lineChart;

    private final LocalDate START_MIN_DATE = LocalDate.of(2016, 1, 1);
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
    protected void onHelloButtonClick() {
        long selectedMask = getSelectedApiFetchTypesMask();

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        String result = ApiHandler.GetData(selectedMask, 52.2297f, 21.0122f, startDate, endDate);

        try {
            JsonNode root = objectMapper.readTree(result);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}
