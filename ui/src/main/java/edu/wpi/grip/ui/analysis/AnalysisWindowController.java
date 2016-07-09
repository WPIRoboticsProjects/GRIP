package edu.wpi.grip.ui.analysis;

import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.RunStoppedEvent;
import edu.wpi.grip.core.events.StepRemovedEvent;
import edu.wpi.grip.core.events.TimerEvent;
import edu.wpi.grip.core.metrics.Analysis;
import edu.wpi.grip.core.metrics.HotnessCalculator;
import edu.wpi.grip.core.metrics.Statistics;

import com.google.common.eventbus.Subscribe;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import javax.annotation.Nullable;

/**
 * Controller for the analysis window.
 */
public class AnalysisWindowController {

  @FXML
  private TableView<Pair<Step, Analysis>> table;
  @FXML
  private TableColumn<Pair<Step, Analysis>, String> operationColumn;
  @FXML
  private TableColumn<Pair<Step, Analysis>, TimeView> timeColumn;

  private List<Pair<Step, Analysis>> stepData = new ArrayList<>();
  private ObservableList<Pair<Step, Analysis>> tableItems = FXCollections.observableArrayList();

  private final HotnessCalculator hotnessCalculator = new HotnessCalculator();
  private final Analysis analysis = new Analysis();
  private Statistics lastStats;

  /**
   * Initializes the controller. This should only be called by the FXML loader.
   */
  public void initialize() {
    table.setPlaceholder(new Label("Waiting for steps"));
    operationColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.499));
    timeColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.499));
    operationColumn.setCellValueFactory(
        e -> new SimpleStringProperty(e.getValue().getKey().getOperationDescription().name()));
    timeColumn.setCellValueFactory(
        e -> {
          Analysis a = e.getValue().getValue();
          final double avg = a.getAverage();
          return new SimpleObjectProperty<>(
              new TimeView(avg, avg / lastStats.getSum(), hotnessCalculator.hotness(lastStats, avg))
          );
        });
    table.setItems(tableItems);
  }

  @Subscribe
  private void onRun(TimerEvent<?> event) {
    if (event.getSource() instanceof Step) {
      Step source = (Step) event.getSource();
      stepData.add(Pair.of(source, event.getData()));
      analysis.add(event.getData().getAverage());
    }
  }

  @Subscribe
  private void onStepRemoved(StepRemovedEvent event) {
    stepData.stream()
        .filter(p -> p.getLeft() == event.getStep()) // Want reference equality
        .findAny()
        .ifPresent(stepData::remove);
    analysis.reset();
  }

  @Subscribe
  private void onPipelineFinish(@Nullable RunStoppedEvent event) {
    // Update the table after the pipeline finishes
    tableItems.clear();
    tableItems.addAll(stepData);
    lastStats = analysis.getStatistics();
    // Reset for the next run
    stepData.clear();
    analysis.reset();
  }

  private static class TimeView extends HBox {

    private static final double BAR_LENGTH = 150; // pixels

    private final Label text = new Label();
    private final ProgressBar progressBar = new ProgressBar();

    TimeView(double time, double relativeAmount, double hotness) {
      text.setText(String.format("%.1fms", time / 1e3));
      progressBar.setProgress(relativeAmount);
      progressBar.setPrefWidth(BAR_LENGTH);
      progressBar.setPadding(new Insets(0, 10, 0, 5));
      if (hotness > 0) {
        final double max = 3; // highest value before being clamped
        if (hotness > max) {
          hotness = max; // clamp
        }
        double h = ((max - hotness) * 270 / max) - 45;
        final String formatStyle = "-fx-accent: hsb(%.2f, 100%%, 100%%);";
        progressBar.setStyle(String.format(formatStyle, h));
      }
      getChildren().addAll(progressBar, text);
    }

  }

}
