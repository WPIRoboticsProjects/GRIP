package edu.wpi.grip.ui;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import edu.wpi.grip.ui.codegeneration.Language;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.stage.DirectoryChooser;

public class GenerateController implements Initializable{
	@FXML
	private ComboBox language;
    @FXML
    private Parent gen;
	@FXML
	void generate(){
		final DirectoryChooser dir = new DirectoryChooser();
		File temp = dir.showDialog(gen.getScene().getWindow());
		if(temp == null)
			return;
		String path = temp.getAbsolutePath();
		System.out.println("Saving to file " + path + Language.get((String)language.getValue()));
	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		for(Language lang : Language.values()){		
			language.getItems().add(lang.name);
		}
	}
}
