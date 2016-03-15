package dsproject;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileInputStream;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.collections.FXCollections;

public class DSProjectController {

	private File originalFile = null;
	private File searchDirectory = null;
	private String originalMD5 = "";
	private boolean filterMD5 = false;
	private boolean filterSize = false;

    @FXML
    void toggleMD5Filter() {
		filterMD5 = !filterMD5;
    }

    @FXML
    void toggleSizeFilter() {
		filterSize = !filterSize;
    }


    @FXML
    private Button folderButton;
    @FXML
    private Text originalFileName;

    @FXML
    private Text originalFileSize;

    @FXML
    private Text originalFileHash;

    @FXML
    private Text statusText;

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
			originalFileName.setText("Name: " + originalFile.getName());
			originalFileSize.setText("Size: " + String.valueOf(originalFile.length() + " bytes"));
			try{
				FileInputStream fis = new FileInputStream(originalFile);
				String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
				originalMD5 = md5;
				originalFileHash.setText("Hash: " + md5);
				folderButton.setDisable(false);
			}
			catch(java.io.IOException i_exc){
				i_exc.printStackTrace();
			}
		}
    }

    @FXML
    void selectFolder(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose Directory");
		File directory = chooser.showDialog(null);
		if(directory != null){
			searchDirectory = directory; 
			duplicateList.setItems(FXCollections.observableArrayList()); //Empties the current duplicate list
			populateList(searchDirectory);
		}
	}
	boolean verifyMD5(File file){
		try{
			FileInputStream fis = new FileInputStream(file);
			String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
			return md5.equals(originalMD5);
		}
		catch(java.io.IOException i_exc){
			i_exc.printStackTrace();
		}
		return false;

	}
	boolean isAbsoluteFilePathEqual(File file){
		return file.getAbsoluteFile().toString().equals(originalFile.getAbsoluteFile().toString());
	}
	void populateList(File file){
		//add file only
		if(file.isFile()){
			if(!isAbsoluteFilePathEqual(file)){
				if(filterSize && filterMD5){
					if(file.length() == originalFile.length() && verifyMD5(file)){
							duplicateList.getItems().add(file.getAbsoluteFile().toString());	
					}
				}
				else if(filterSize){
					if(file.length() == originalFile.length()){
						duplicateList.getItems().add(file.getAbsoluteFile().toString());	
					}
				}
				else if(filterMD5){
					if(verifyMD5(file)){
						duplicateList.getItems().add(file.getAbsoluteFile().toString());	
					}
				}
				else{
					duplicateList.getItems().add(file.getAbsoluteFile().toString());	
				}
			}
			
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
