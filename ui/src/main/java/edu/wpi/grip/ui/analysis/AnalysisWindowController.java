package edu.wpi.grip.ui.analysis;

import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.BenchmarkEvent;
import edu.wpi.grip.core.events.RunStoppedEvent;
import edu.wpi.grip.core.events.TimerEvent;
import edu.wpi.grip.core.metrics.Analysis;
import edu.wpi.grip.core.metrics.BenchmarkRunner;
import edu.wpi.grip.core.metrics.FixedSizeQueue;
import edu.wpi.grip.core.metrics.Statistics;

import com.google.common.eventbus.Subscribe;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

  private Statistics lastStats = Statistics.NIL;
  private final Map<Step, TimeView> timeViewMap = new HashMap<>();
  private final Map<Step, Collection<Long>> sampleMap = new HashMap<>();
  private Function<Step, FixedSizeQueue<Long>> computeFunction = s -> new FixedSizeQueue<>(1);

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
      Collection<Long> samples = sampleMap.computeIfAbsent(source, computeFunction);
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
    double[] averageRunTimes = sampleMap.values()
        .parallelStream()
        .mapToDouble(samples ->
            samples.parallelStream().mapToLong(Long::longValue).average().orElse(0)
        )
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
      sampleMap.clear();
    }
  }

  public void setBenchmarker(BenchmarkRunner benchmarker) {
    this.benchmarker = checkNotNull(benchmarker, "benchmarker");
  }

  @FXML
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void runBenchmark() {
    if (benchmarkRunsField.getText().length() > 0) {
      final int numRuns = Integer.parseInt(benchmarkRunsField.getText());
      computeFunction = s -> new FixedSizeQueue<>(numRuns);
      benchmarker.run(numRuns);
    }
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
