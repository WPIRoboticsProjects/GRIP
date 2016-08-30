package edu.wpi.grip.ui.analysis;

import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.StepIndexer;
import edu.wpi.grip.core.events.BenchmarkEvent;
import edu.wpi.grip.core.events.RunStoppedEvent;
import edu.wpi.grip.core.events.TimerEvent;
import edu.wpi.grip.core.metrics.Analysis;
import edu.wpi.grip.core.metrics.BenchmarkRunner;
import edu.wpi.grip.core.metrics.Statistics;

import com.google.common.collect.EvictingQueue;
import com.google.common.eventbus.Subscribe;

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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Controller for the analysis window.
 */
public class AnalysisWindowController {

  // Table
  @FXML
  private TableView<StepAnalysisEntry> table;
  @FXML
  private TableColumn<StepAnalysisEntry, String> operationColumn;
  @FXML
  private TableColumn<StepAnalysisEntry, Analysis> timeColumn;

  // Benchmarking
  @FXML
  private Button benchmarkButton;
  @FXML
  private TextField benchmarkRunsField;
  private BenchmarkRunner benchmarker;

  private final Callback<StepAnalysisEntry, Observable[]> extractor =
      entry -> new Observable[] {entry.stepProperty(), entry.analysisProperty()};
  private final ObservableList<StepAnalysisEntry> tableItems
      = FXCollections.observableArrayList(extractor);

  private StepIndexer stepIndexer = null;
  private Statistics lastStats = Statistics.NIL;
  private final Map<Step, TimeView> timeViewMap = new HashMap<>();
  private final Map<Step, Collection<Long>> sampleMap = new HashMap<>();
  private static final int DEFAULT_NUM_RECENT_SAMPLES = 16;
  private int numRecentSamples = DEFAULT_NUM_RECENT_SAMPLES;
  private static final String CSV_REPORT_HEADER
      = "Step,% Time,Average Time (ms),Standard deviation\n";
  private String csvReport = CSV_REPORT_HEADER;

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
    timeColumn.setCellFactory(col -> new TableCell<StepAnalysisEntry, Analysis>() {
      @Override
      protected void updateItem(Analysis analysis, boolean empty) {
        if (analysis == null || empty) {
          setGraphic(null);
        } else {
          Step step = tableItems.get(this.getIndex()).getStep();
          TimeView view = timeViewMap.computeIfAbsent(step, s -> new TimeView());
          view.update(analysis.getAverage(),
              analysis.getAverage() / lastStats.getSum(),
              lastStats.hotness(analysis.getAverage()));
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
      Optional<StepAnalysisEntry> possibleEntry
          = tableItems.stream().filter(e -> e.getStep() == source).findAny();
      Collection<Long> samples = sampleMap.computeIfAbsent(source,
          s -> EvictingQueue.create(numRecentSamples));
      samples.add(event.getElapsedTime());
      Analysis stepAnalysis = Analysis.of(samples);
      if (possibleEntry.isPresent()) {
        possibleEntry.get().setAnalysis(stepAnalysis);
      } else {
        StepAnalysisEntry entry = new StepAnalysisEntry();
        entry.setStep(source);
        entry.setAnalysis(stepAnalysis);
        tableItems.add(entry);
      }
    }
  }

  @Subscribe
  @SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.UnusedFormalParameter"})
  private void onPipelineFinish(@Nullable RunStoppedEvent event) {
    double[] averageRunTimes = sortedStream(sampleMap)
        .parallel()
        .map(Map.Entry::getValue)
        .mapToDouble(s -> s.parallelStream().mapToLong(Long::longValue).average().orElse(0))
        .toArray();
    Analysis analysis = Analysis.of(averageRunTimes);
    // Update the stats after the pipeline finishes
    lastStats = analysis.getStatistics();
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
    }
  }

  /**
   * Sets the benchmark runner.
   */
  public void setBenchmarker(BenchmarkRunner benchmarker) {
    this.benchmarker = checkNotNull(benchmarker, "benchmarker");
  }

  /**
   * Sets the step indexer.
   */
  public void setStepIndexer(StepIndexer stepIndexer) {
    this.stepIndexer = stepIndexer;
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
   * @return a stream of the entries in the map, sorted by their key's index.
   */
  private <E> Stream<Map.Entry<Step, E>> sortedStream(Map<Step, E> m) {
    return m.entrySet()
        .stream()
        .sorted((e1, e2) -> stepIndexer.compare(e1.getKey(), e2.getKey()));
  }

  /**
   * Creates a CSV report of the most recent benchmark.
   */
  private String createReport() {
    StringBuilder sb = new StringBuilder(CSV_REPORT_HEADER);

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
      sb.append(stepNames.get(i));
      sb.append(',');
      sb.append(percentRunTimes.get(i));
      sb.append(',');
      sb.append(averageRunTimes.get(i));
      sb.append(',');
      sb.append(stddev.get(i));
      sb.append('\n');
    }
    return sb.toString();
  }


  private static class StepAnalysisEntry {

    private final Property<Step> stepProperty = new SimpleObjectProperty<>();
    private final Property<Analysis> analysisProperty = new SimpleObjectProperty<>();

    public Property<Step> stepProperty() {
      return stepProperty;
    }

    public Step getStep() {
      return stepProperty.getValue();
    }

    public void setStep(Step step) {
      stepProperty.setValue(step);
    }

    public Property<Analysis> analysisProperty() {
      return analysisProperty;
    }

    public Analysis getAnalysis() {
      return analysisProperty.getValue();
    }

    public void setAnalysis(Analysis analysis) {
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
