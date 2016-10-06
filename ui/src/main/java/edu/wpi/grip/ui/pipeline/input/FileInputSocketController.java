package edu.wpi.grip.ui.pipeline.input;

import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.ui.pipeline.SocketHandleView;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.io.File;

import javafx.scene.control.Button;
import javafx.stage.FileChooser;

/**
 * Controller for input sockets that require a file.
 */
public class FileInputSocketController extends InputSocketController<String> {

  private final FileChooser fc = new FileChooser();

  @Inject
  FileInputSocketController(SocketHandleView.Factory socketHandleViewFactory,
                            @Assisted InputSocket<String> socket) {
    super(socketHandleViewFactory, socket);
  }

  @Override
  protected void initialize() {
    super.initialize();
    final Button button = new Button("Select file");
    fc.setInitialDirectory(new File(System.getProperty("user.home")));
    button.setOnAction(e -> {
      File selected = fc.showOpenDialog(null);
      if (selected != null) {
        getSocket().setValue(selected.getAbsolutePath());
      }
    });
    setContent(button);
  }

  public interface Factory {

    FileInputSocketController create(InputSocket<String> socket);
  }

}
