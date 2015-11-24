package edu.wpi.grip.ui.pipeline;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.ui.dialogs.NetworkTablesSettingsDialogView;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.layout.HBox;

import static com.google.common.base.Preconditions.checkNotNull;

public class AddSinkView extends HBox {
    private final EventBus eventBus;

    public AddSinkView(EventBus eventBus) {
        this.eventBus = checkNotNull(eventBus, "Event Bus can not be null");
        this.setFillHeight(true);


        final Button networkTablesButton = new Button("Network Tables");
        networkTablesButton.setOnMouseClicked(event -> {
            Dialog dialog = new Dialog<>();
            dialog.setDialogPane(new NetworkTablesSettingsDialogView());
            dialog.showAndWait();
        });

        this.getChildren().add(networkTablesButton);
    }
}
