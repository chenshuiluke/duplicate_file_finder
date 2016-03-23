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
import javafx.scene.control.ProgressIndicator;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextField;
import java.util.HashSet;

public class DSProjectController {

	private LinkedList fileList = new LinkedList();
	private File searchDirectory = null;
	private boolean filterMD5 = true;
	private boolean filterSize = true;
	private boolean filterExtension = false;
	@FXML
    private Button applyFilterButton;
	
    @FXML
    private TextArea statusText;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private HBox extensionFilterHBox;
	
    @FXML
    void toggleExtensionFilter() {
		filterExtension = !filterExtension;
    }
	
	
	Task<Void> returnNewTask(){
		return new Task<Void>(){
			@Override
			protected Void call() throws Exception{
		        Platform.runLater(new Runnable() {
		            @Override public void run() {
		            	progressIndicator.setVisible(true);
						folderButton.setDisable(true);
						duplicateList.setDisable(true);
						sizeCheckBox.setDisable(true);
						md5CheckBox.setDisable(true);
						extensionFilterHBox.setDisable(true);
						applyFilterButton.setDisable(true);
						removeOtherDuplicatesButton.setDisable(true);
						removeDuplicateButton.setDisable(true);
		            }
		        });
				populateTreeViewAndRemoveExcess(searchDirectory);
        Platform.runLater(new Runnable() {
            @Override public void run() {

				sizeCheckBox.setDisable(false);
				md5CheckBox.setDisable(false);
				applyFilterButton.setDisable(false);
				progressIndicator.setVisible(false);
				folderButton.setDisable(false);
				duplicateList.setDisable(false);
				extensionFilterHBox.setDisable(false);
            }
        });
				return null;
			}
		};		
	} 
    @FXML
    void removeOthersOnClick(ActionEvent event) {
    	try{

	     	TreeItem<String> treeItem= duplicateList.getSelectionModel().getSelectedItem();
	     	TreeItem<String> parent = treeItem.getParent();
	     	if(parent != null && treeItem.getChildren().size() == 0){
		     	ObservableList<TreeItem<String>> parentChildList = parent.getChildren();
		     	ArrayList<TreeItem> excess = new ArrayList<>();
		     	for(TreeItem<String> item : parentChildList){
		     		//Will get concurrent modification exception if we remove an item while iterating
		     		if(item != treeItem){
						excess.add(item);	     			
		     		}
		     	}
		     	printToStatus("Deleting copies of " + treeItem.getValue());
		     	for(TreeItem<String> excessItem : excess){
		     			String name = excessItem.getValue();
		     			Files.delete(Paths.get(name));
		     			System.out.println("Deleted " + name);
		     			parentChildList.remove(excessItem);
		     	}	     		
	     	}

    	}
    	catch(java.io.IOException e){
    		System.out.println(e.getMessage());
    		clearList();
    		(new Thread(returnNewTask())).start();
    	}

    }
    @FXML
    void removeDuplicateOnClick(ActionEvent event) {
    	try{
	     	TreeItem<String> treeItem= duplicateList.getSelectionModel().getSelectedItem();
	     	if(treeItem.getChildren().size() == 0){
		    	String name = treeItem.getValue();
		    	Files.delete(Paths.get(name));
		    	System.out.println("Deleted " + name);
		    	TreeItem<String> parent = treeItem.getParent();
		    	printToStatus("Deleting " + treeItem.getValue());
		    	parent.getChildren().remove(treeItem);  
		    	if(parent.getChildren().size() == 0 ){
		    		printToStatus("Deleting parent item " + parent.getValue() + " because it has no more copies.");
		    		duplicateList.getRoot().getChildren().remove(parent);
		    	} 			     		
	     	}

    	}
    	catch(java.io.IOException e){
    		System.out.println(e.getMessage());
    		clearList();
    		(new Thread(returnNewTask())).start();
    	}


    }
    @FXML
    private Button removeOtherDuplicatesButton;
    @FXML
    private Button removeDuplicateButton;
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
    private TextField extensionFilterTextBox;
    @FXML
    void toggleMD5Filter() {
    	printToStatus("Toggling MD5 filter.");
		filterMD5 = !filterMD5;
		clearList();
    }

    @FXML
    void toggleSizeFilter() {
     	printToStatus("Toggling Size filter.");
		filterSize = !filterSize;
    }

	
    @FXML
    void applyFilter() {
		clearList();
		(new Thread(returnNewTask())).start();
    }

    @FXML
    private Button folderButton;

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
					removeDuplicateButton.setDisable(false);
					removeOtherDuplicatesButton.setDisable(false);
				}
				else{
					selectedFileName.setText("Name: ");
					selectedFileSize.setText("Size: ");
					selectedFileHash.setText("Hash: ");
					removeDuplicateButton.setDisable(true);
					removeOtherDuplicatesButton.setDisable(true);
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
	LinkedList getFileList(File file){
		LinkedList list = new LinkedList();
		
		if(file.isFile()){	
			if(filterExtension && extensionFilterTextBox.getText().length() > 0){
				String fileName = file.getAbsoluteFile().toString();
				System.out.println("Trying to filter: " + fileName);
				String filter = extensionFilterTextBox.getText();
				
				String lastPartOfFileName = fileName.substring(fileName.length() - filter.length(), fileName.length());
			
				if(!filter.equals(lastPartOfFileName)){ //trtr.doc doc filter
					System.out.println("Excluding " + fileName);
					return list;
				}
			}

			System.out.println("Is file: " + file.getAbsoluteFile().toString());
			list.insertf(file);
		}
		else if(file.isDirectory()){
			String[] files = file.list();
			if(files != null){
				for(String entry : files){
					File temp = new File(file, entry);
					list.concat(getFileList(temp));
				}
			}
		}
		System.gc();
		return list;
	}
	void printToStatus(String input){
        Platform.runLater(new Runnable() {
            @Override public void run() {
            	statusText.appendText(input + System.getProperty("line.separator"));
            }
        });		
	}
	void populateTreeViewAndRemoveExcess(File file){

		printToStatus("Getting file list.");
		fileList = getFileList(searchDirectory);
		printToStatus("Populating tree view.");
		HashSet<String> nodeCopyList = new HashSet<>();
		//A list of all nodes that were previously added to the tree
		/*
			offers constant time performance for the basic operations 
			(add, remove, contains and size).
			So checking for extras is generally 0(1)
		*/
		for(int counter = 0; counter < fileList.size(); counter++){
			final float progress = counter;

			File singleFile = fileList.returna(counter);
			if(nodeCopyList.add(singleFile.getName()) == false){ //If the item doesn't already exist
				continue;
			}
	        Platform.runLater(new Runnable() {
	            @Override public void run() {
	            	progressIndicator.setProgress((progress /nodeCopyList.size()));
	            }
	        });	        

//			System.out.println(singleFile);
			TreeItem<String> fileItem = new TreeItem<String> (singleFile.getAbsolutePath());
			
			/*ArrayList<String> duplicates = */
			populateList(singleFile, searchDirectory, fileItem, nodeCopyList);
			/*
			for(String duplicate : duplicates){

				TreeItem<String> item = new TreeItem<String>(duplicate);
				fileItem.getChildren().add(item);
			}
			*/
			if(fileItem.getChildren().size() > 0){
				fileItem.getChildren().add(new TreeItem<String>(singleFile.getAbsoluteFile().toString()));
				duplicateList.getRoot().getChildren().add(fileItem);			}
			
		}
        Platform.runLater(new Runnable() {
            @Override public void run() {
            	progressIndicator.setProgress(-1);
            }
        });
		printToStatus("Done!");
		System.gc();
	}
    @FXML
    void selectFolder(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose Directory");
		File directory = chooser.showDialog(sizeCheckBox.getScene().getWindow());
		if(directory != null){
			
			searchDirectory = directory; 
			clearList();


			(new Thread(returnNewTask())).start();

			System.gc();
		}
	}
	boolean verifyMD5(File origin, File file){
		return getMD5(file).equals(getMD5(origin));
	}
	boolean isAbsoluteFilePathEqual(File origin, File file){
		return file.getAbsoluteFile().toString().equals(origin.getAbsoluteFile().toString());
	}
	void populateList(File origin, File file, TreeItem<String> node, HashSet<String> nodeList){
		//add file only
		//ArrayList<String> list = new ArrayList<>();
		if(file.isFile()){
			if(!isAbsoluteFilePathEqual(origin, file)){
				//System.out.println("Verifying " + file.getName());
				//printToStatus("Verifying " + file.getName());
				if(filterSize){
					if(!(file.length() == origin.length())){
						return;
					}
				}
				if(filterMD5){
					if(!verifyMD5(origin, file)){
						return;
					}
				}
				if(filterExtension && extensionFilterTextBox.getText().length() > 0){
					String fileName = file.getAbsoluteFile().toString();
					System.out.println("Trying to filter: " + fileName);
					String filter = extensionFilterTextBox.getText();
					
					String lastPartOfFileName = fileName.substring(fileName.length() - filter.length(), fileName.length());
				
					if(!filter.equals(lastPartOfFileName)){ //trtr.doc doc filter
						System.out.println("Excluding " + fileName);
						return;
					}
				}
				//System.out.println("Adding " + file.getName());
				//printToStatus("Adding " + file.getName());
				TreeItem<String> newTreeItem = new TreeItem<>(file.getAbsolutePath());

				
				node.getChildren().add(newTreeItem);
				
				
			}
			
		}
		else if(file.isDirectory()){
			String[] subNote = file.list();
			if(subNote != null){
				for(String filename : subNote){
					File temp = new File(file, filename);
					populateList(origin, temp, node, nodeList);
					
				}
			}
		}
	}
    @FXML
    void initialize() {
        assert duplicateList != null : "fx:id=\"duplicateList\" was not injected: check your FXML file 'DSProject.fxml'.";
		TreeItem<String> rootItem = new TreeItem<String> ("Duplicates");
		rootItem.setExpanded(true);
		duplicateList.setRoot(rootItem);
		

    }
}
