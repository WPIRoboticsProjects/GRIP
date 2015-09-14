package edu.wpi.grip.ui.controllers;

import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.events.SocketPublishedEvent;
import edu.wpi.grip.ui.models.SocketModel;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller class for the Socket Table window.  This class subscribes to SocketPublishedEvents
 * and updates the view whenever a socket is published.
 */
public class SocketTableController implements Initializable {

    @FXML
    private TableView<SocketModel> socketTable;
    @FXML
    private TableColumn<SocketModel, String> identifierColumn;
    @FXML
    private TableColumn<SocketModel, String> typeColumn;
    @FXML
    private TableColumn<SocketModel, Object> valueColumn;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.identifierColumn.setCellValueFactory(new PropertyValueFactory<SocketModel, String>("identifier"));
        this.typeColumn.setCellValueFactory(new PropertyValueFactory<SocketModel, String>("type"));
        this.valueColumn.setCellValueFactory(new PropertyValueFactory<SocketModel, Object>("value"));
    }

    @Subscribe
    public void onSocketPublished(SocketPublishedEvent event) {
        ObservableList<SocketModel> models = this.socketTable.getItems();
        Socket socket = event.getSocket();

        // If there is already a model in the table corresponding to this socket, update its data from the reference
        // to the socket it already has.
        for (SocketModel model : models) {
            if (model.getSocket() == socket) {
                model.setFromSocket();
                return;
            }
        }

        // Otherwise, add a new model to the list with the newly-published socket
        models.add(new SocketModel(socket));
    }
}
