package edu.wpi.grip.ui.analysis;

import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.StepIndexer;
import edu.wpi.grip.core.events.BenchmarkEvent;
import edu.wpi.grip.core.events.RunStoppedEvent;
import edu.wpi.grip.core.events.StepRemovedEvent;
import edu.wpi.grip.core.events.TimerEvent;
import edu.wpi.grip.core.metrics.BenchmarkRunner;
import edu.wpi.grip.core.metrics.CsvExporter;
import edu.wpi.grip.core.metrics.Statistics;

import com.google.common.collect.EvictingQueue;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import javax.annotation.Nullable;

/**
 * Controller for the analysis view.
 */
public class AnalysisController {

  // Table
  @FXML
  private TableView<StepStatisticsEntry> table;
  @FXML
  private TableColumn<StepStatisticsEntry, String> operationColumn;
  @FXML
  private TableColumn<StepStatisticsEntry, Statistics> timeColumn;

  // Benchmarking
  @FXML
  private Button benchmarkButton;
  @FXML
  private TextField benchmarkRunsField;
  @Inject
  private BenchmarkRunner benchmarker;

  private final Callback<StepStatisticsEntry, Observable[]> extractor =
      entry -> new Observable[]{entry.stepProperty(), entry.analysisProperty()};
  private final ObservableList<StepStatisticsEntry> tableItems
      = FXCollections.observableArrayList(extractor);

  @Inject
  private StepIndexer stepIndexer;
  private Statistics lastStats = Statistics.NIL;
  private final Map<Step, TimeView> timeViewMap = new HashMap<>();
  private final Map<Step, Collection<Long>> sampleMap = new HashMap<>();
  private static final int DEFAULT_NUM_RECENT_SAMPLES = 16;
  private int numRecentSamples = DEFAULT_NUM_RECENT_SAMPLES;
  private String csvReport = "";
  private final CsvExporter csvExporter = new CsvExporter(4,
      "Step", "% Time", "Average Time (ms)", "Standard Deviation");

  /**
   * Initializes the controller. This should only be called by the FXML loader.
   */
  public void initialize() {
    table.setPlaceholder(new Label("Waiting for steps"));
    operationColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.499));
    timeColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.499));
    operationColumn.setCellValueFactory(
        e -> new SimpleStringProperty(e.getValue().getStep().getOperationDescription().name()));
    timeColumn.setCellValueFactory(e -> e.getValue().analysisProperty());
    timeColumn.setCellFactory(col -> new TableCell<StepStatisticsEntry, Statistics>() {
      @Override
      protected void updateItem(Statistics statistics, boolean empty) {
        if (statistics == null || empty) {
          setGraphic(null);
        } else {
          Step step = tableItems.get(this.getIndex()).getStep();
          TimeView view = timeViewMap.computeIfAbsent(step, s -> new TimeView());
          view.update(statistics.getMean(),
              statistics.getMean() / lastStats.getSum(),
              lastStats.hotness(statistics.getMean()));
          setGraphic(null);
          setGraphic(view);
        }
      }
    });
    table.setItems(tableItems);

    benchmarkRunsField.textProperty().addListener((observable, oldValue, newValue) -> {
      if ((oldValue.isEmpty() && "0".equals(newValue)) || !newValue.matches("[\\d]*")) {
        benchmarkRunsField.setText(oldValue);
        return;
      }
      benchmarkButton.setDisable(newValue.isEmpty());
    });
  }

  @Subscribe
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void onRun(TimerEvent event) {
    if (event.getTarget() instanceof Step) {
      Step source = (Step) event.getTarget();
      Optional<StepStatisticsEntry> possibleEntry
          = tableItems.stream().filter(e -> e.getStep() == source).findAny();
      Collection<Long> samples = sampleMap.computeIfAbsent(source,
          s -> EvictingQueue.create(numRecentSamples));
      samples.add(event.getElapsedTime());
      Statistics stepStatistics = Statistics.of(samples);
      if (possibleEntry.isPresent()) {
        possibleEntry.get().setStatistics(stepStatistics);
      } else {
        StepStatisticsEntry entry = new StepStatisticsEntry();
        entry.setStep(source);
        entry.setStatistics(stepStatistics);
        tableItems.add(entry);
      }
    }
  }

  @Subscribe
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void onStepRemoved(StepRemovedEvent e) {
    sampleMap.remove(e.getStep());
    timeViewMap.remove(e.getStep());
    tableItems.removeIf(entry -> entry.getStep() == e.getStep());
  }

  @Subscribe
  @SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.UnusedFormalParameter"})
  private void onPipelineFinish(@Nullable RunStoppedEvent event) {
    double[] averageRunTimes = sortedStream(sampleMap)
        .parallel()
        .map(Map.Entry::getValue)
        .mapToDouble(s -> s.parallelStream().mapToLong(Long::longValue).average().orElse(0))
        .toArray();
    Statistics statistics = Statistics.of(averageRunTimes);
    // Update the stats after the pipeline finishes
    lastStats = statistics;
  }

  @Subscribe
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void onBenchmark(BenchmarkEvent event) {
    benchmarkButton.setDisable(event.isStart());
    benchmarkRunsField.setDisable(event.isStart());
    if (!event.isStart()) {
      csvReport = createReport();
      numRecentSamples = DEFAULT_NUM_RECENT_SAMPLES; // reset queue size
      sampleMap.clear();
      csvExporter.clear();
    }
  }

  @FXML
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void runBenchmark() {
    if (benchmarkRunsField.getText().length() > 0) {
      final int numRuns = Integer.parseInt(benchmarkRunsField.getText());
      numRecentSamples = numRuns;
      benchmarker.run(numRuns);
    }
  }

  @FXML
  private void exportCsv() {
    // Show benchmarking results
    Platform.runLater(() -> {
      Alert a = new Alert(Alert.AlertType.INFORMATION);
      a.setHeaderText("Benchmarking results");
      TextArea resultArea = new TextArea(csvReport);
      a.getDialogPane().setContent(resultArea);
      a.showAndWait();
    });
  }

  /**
   * Streams the entries in a {@code Map<Step, *>} sorted by the step's index according to the
   * {@link #stepIndexer}.
   *
   * @param m   the map to stream
   * @param <E> the type of the values in the map
   *
   * @return a stream of the entries in the map, sorted by their key's index.
   */
  private <E> Stream<Map.Entry<Step, E>> sortedStream(Map<Step, E> m) {
    return m.entrySet()
        .stream()
        .filter(e -> stepIndexer.indexOf(e.getKey()) >= 0)
        .sorted((e1, e2) -> stepIndexer.compare(e1.getKey(), e2.getKey()));
  }

  /**
   * Creates a CSV report of the most recent benchmark.
   */
  private String createReport() {
    // List of step names, in the order they run in
    final List<String> stepNames = sortedStream(sampleMap)
        .map(Map.Entry::getKey)
        .map(Step::getOperationDescription)
        .map(OperationDescription::name)
        .collect(Collectors.toList());

    // Statistics of each step's run time
    final List<Statistics> statistics = sortedStream(sampleMap)
        .map(Map.Entry::getValue)
        .map(s -> s.stream().mapToDouble(Long::doubleValue).toArray())
        .map(Statistics::of)
        .collect(Collectors.toList());

    // Average run times for each step
    final List<Double> averageRunTimes = statistics
        .stream()
        .map(Statistics::getMean)
        .map(t -> t / 1000) // convert us to ms
        .collect(Collectors.toList());

    // Average total run time for the whole pipeline
    final double averageTotalRunTime = statistics
        .stream()
        .mapToDouble(Statistics::getMean)
        .map(t -> t / 1000) // convert us to ms
        .sum();

    // What percent of the total each step took
    final List<Double> percentRunTimes = averageRunTimes
        .stream()
        .map(t -> 100 * t / averageTotalRunTime)
        .collect(Collectors.toList());

    // Standard deviation in run times for each step
    final List<Double> stddev = statistics
        .stream()
        .map(Statistics::getStandardDeviation)
        .map(t -> t / 1000) // convert us to ms
        .collect(Collectors.toList());
    for (int i = 0; i < statistics.size(); i++) {
      csvExporter.addRow(
          stepNames.get(i),
          percentRunTimes.get(i),
          averageRunTimes.get(i),
          stddev.get(i)
      );
    }
    return csvExporter.export();
  }


  private static class StepStatisticsEntry {

    private final Property<Step> stepProperty = new SimpleObjectProperty<>();
    private final Property<Statistics> analysisProperty = new SimpleObjectProperty<>();

    public Property<Step> stepProperty() {
      return stepProperty;
    }

    public Step getStep() {
      return stepProperty.getValue();
    }

    public void setStep(Step step) {
      stepProperty.setValue(step);
    }

    public Property<Statistics> analysisProperty() {
      return analysisProperty;
    }

    public Statistics getStatistics() {
      return analysisProperty.getValue();
    }

    public void setStatistics(Statistics analysis) {
      analysisProperty.setValue(analysis);
    }

  }

  private static class TimeView extends HBox {

    private static final double BAR_LENGTH = 150; // pixels

    private final Label text = new Label("0.0ms");
    private final ProgressBar progressBar = new ProgressBar(0);

    TimeView() {
      super();
      progressBar.setPrefWidth(BAR_LENGTH);
      progressBar.setPadding(new Insets(0, 10, 0, 5));
      progressBar.setStyle("-fx-accent: hsb(180, 100%, 75%);"); // blue-gray
      getChildren().addAll(progressBar, text);
    }

    void update(double time, double relativeAmount, double hotness) {
      text.setText(String.format("%.1fms", time / 1e3));
      progressBar.setProgress(relativeAmount);
      if (hotness > 0) {
        final double max = 3; // highest value before being clamped
        double h = ((max - Math.min(hotness, max)) * 270 / max) - 45;
        final String formatStyle = "-fx-accent: hsb(%.2f, 100%%, 100%%);";
        progressBar.setStyle(String.format(formatStyle, h));
      } else {
        progressBar.setStyle("-fx-accent: hsb(180, 100%, 75%);"); // blue-gray
      }
    }

  }

}
