package dsproject;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import java.io.File;

public class DSProjectController {

	private File originalFile = null;
	private File searchDirectory = null;
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ListView<String> duplicateList;

    @FXML
    void selectFile(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Choose the original file");
		File file = chooser.showOpenDialog(null);
		if(file != null){
			originalFile = file;	
		}
    }

    @FXML
    void selectFolder(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose Directory");
		File directory = chooser.showDialog(null);
		if(directory != null){
			searchDirectory = directory; 
			populateList(searchDirectory);
		}
	}
	void populateList(File file){
		//add file only
		if(file.isFile()){
			duplicateList.getItems().add(file.getAbsoluteFile().toString());	
		}
		else if(file.isDirectory()){
			String[] subNote = file.list();
			if(subNote != null){
				for(String filename : subNote){
					File temp = new File(file, filename);
					populateList(temp);
				}
			}
		}
	}
    @FXML
    void initialize() {
        assert duplicateList != null : "fx:id=\"duplicateList\" was not injected: check your FXML file 'DSProject.fxml'.";

    }
}
