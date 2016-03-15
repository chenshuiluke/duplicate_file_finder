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
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeItem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import javafx.collections.ObservableList;

public class DSProjectController {

	private ArrayList<File> fileList = new ArrayList<>();
	private File searchDirectory = null;
	private boolean filterMD5 = true;
	private boolean filterSize = false;


    @FXML
    private CheckBox md5CheckBox;

    @FXML
    private CheckBox sizeCheckBox;

    @FXML
    private Text selectedFileName;

    @FXML
    private Text selectedFileSize;

    @FXML
    private Text selectedFileHash;

    @FXML
    void toggleMD5Filter() {
		filterMD5 = !filterMD5;
		clearList();
		populateTreeViewAndRemoveExcess(searchDirectory);
    }

    @FXML
    void toggleSizeFilter() {
		filterSize = !filterSize;
		clearList();
		populateTreeViewAndRemoveExcess(searchDirectory);
    }


    @FXML
    private Button folderButton;
    @FXML
    private Text statusText;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TreeView<String> duplicateList;

    @FXML
    void getSelected(){
		TreeItem<String> treeItem= duplicateList.getSelectionModel().getSelectedItem();
		if(treeItem != null){
			String item = treeItem.getValue();
			
			if(item != null){
				File file = new File(item);
				if(file.isFile()){
					selectedFileName.setText("Name: " + file.getName());
					selectedFileSize.setText("Size: " + file.length() + " bytes");
					selectedFileHash.setText("Hash: " + getMD5(file));
				}
				else{
					selectedFileName.setText("Name: ");
					selectedFileSize.setText("Size: ");
					selectedFileHash.setText("Hash: ");
				}
			}
		}
    }
	private String getMD5(File file){
		String md5 = "";
		try{
			FileInputStream fis = new FileInputStream(file);
			md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
			fis.close();
		}
		catch(java.io.IOException i_exc){
			i_exc.printStackTrace();
		}
		return md5;
	}
	private void clearList(){
		duplicateList.getRoot().getChildren().setAll(FXCollections.observableArrayList()); //Empties the current duplicate list
	}
	ArrayList<File> getFileList(File file){
		ArrayList list = new ArrayList<>();
		if(file.isFile()){
			list.add(file);
		}
		else if(file.isDirectory()){
			String[] files = file.list();
			if(files != null){
				for(String entry : files){
					System.out.println(entry);
					File temp = new File(file, entry);
					list.addAll(getFileList(temp));
				}
			}
		}
		return list;
	}
	void populateTreeViewAndRemoveExcess(File file){
		fileList = getFileList(searchDirectory);
		for(File singleFile : fileList){
			System.out.println(singleFile);
			TreeItem<String> fileItem = new TreeItem<String> (singleFile.getName());
			fileItem.getChildren().add(new TreeItem<String>(singleFile.getAbsoluteFile().toString()));
			ArrayList<String> duplicates = populateList(singleFile, searchDirectory);
			for(String duplicate : duplicates){
				TreeItem<String> item = new TreeItem<String>(duplicate);
				fileItem.getChildren().add(item);
			}
			duplicateList.getRoot().getChildren().add(fileItem);
		}
		ArrayList<TreeItem> excess = new ArrayList<>();
		ObservableList<TreeItem<String>> nodeList= duplicateList.getRoot().getChildren();
		for(int counter1 = 0; counter1 < nodeList.size(); counter1++){
			if(nodeList.get(counter1).getChildren().size() < 1){
					excess.add(nodeList.get(counter1));
					continue;
			}
			for(int counter2 = counter1+1; counter2 < nodeList.size(); counter2++){
				if(nodeList.get(counter2).getValue().equals(nodeList.get(counter1).getValue())){
					System.out.println("equals");
					excess.add(nodeList.get(counter2));
				}
			}
		}
		nodeList.removeAll(excess);
	}
    @FXML
    void selectFolder(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose Directory");
		File directory = chooser.showDialog(null);
		if(directory != null){
			searchDirectory = directory; 
			clearList();
			populateTreeViewAndRemoveExcess(searchDirectory);
			sizeCheckBox.setDisable(false);
			md5CheckBox.setDisable(false);
		}
	}
	boolean verifyMD5(File origin, File file){
		return getMD5(file).equals(getMD5(origin));
	}
	boolean isAbsoluteFilePathEqual(File origin, File file){
		return file.getAbsoluteFile().toString().equals(origin.getAbsoluteFile().toString());
	}
	ArrayList<String> populateList(File origin, File file){
		//add file only
		ArrayList<String> list = new ArrayList<>();
		if(file.isFile()){
			if(!isAbsoluteFilePathEqual(origin, file)){
				if(filterSize && filterMD5){
					if(file.length() == origin.length() && verifyMD5(origin, file)){
						list.add(file.getAbsoluteFile().toString());
					}
				}
				else if(filterSize){
					if(file.length() == origin.length()){
						list.add(file.getAbsoluteFile().toString());
					}
				}
				else if(filterMD5){
					if(verifyMD5(origin, file)){
						list.add(file.getAbsoluteFile().toString());
					}
				}
				else{
						list.add(file.getAbsoluteFile().toString());
				}
			}
			
		}
		else if(file.isDirectory()){
			String[] subNote = file.list();
			if(subNote != null){
				for(String filename : subNote){
					File temp = new File(file, filename);
					ArrayList subFiles = populateList(origin, temp);
					list.addAll(subFiles);
				}
			}
		}
		return list;
	}
    @FXML
    void initialize() {
        assert duplicateList != null : "fx:id=\"duplicateList\" was not injected: check your FXML file 'DSProject.fxml'.";
		TreeItem<String> rootItem = new TreeItem<String> ("Duplicates");
		rootItem.setExpanded(true);
		duplicateList.setRoot(rootItem);
		

    }
}
