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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextField;
import java.util.HashSet;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.io.comparator.SizeFileComparator;
import org.apache.commons.io.FilenameUtils;
import com.google.common.collect.HashBiMap;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.IOException;
public class DSProjectController {

	private LinkedList fileList = new LinkedList();
	private File searchDirectory = null;
	private boolean filterMD5 = true;
	private boolean filterSize = true;
	private boolean filterExtension = false;
	private boolean hashLargeMD5Files = false;
	private HashMap<String, String> fileNameToMD5HashMap = new HashMap<>();

	@FXML
    private Button applyFilterButton; //When clicked, it starts a search with all enabled filters
	
    @FXML
    private TextArea statusText; //This is where status logs are posted

    @FXML
    private ProgressBar progressBarIndicator;

    @FXML
    private HBox extensionFilterHBox;  

    @FXML
    private ProgressIndicator hashCircle;	//Is displayed when hashing a file.
	//The following two are enabled when a non-group duplicate file node is selected.
    @FXML
    private Button removeOtherDuplicatesButton; //This removes all siblings of the selected nodes.
    @FXML
    private Button removeDuplicateButton; //This removes the selected child node.
    @FXML
    private CheckBox md5CheckBox; //When clicked, it toggles whether or not to filter by md5 hash.

    @FXML
    private CheckBox sizeCheckBox; //When clicked, it toggles whether or not to filter by file size.

	
	//The following 3 Text elements display information on the selected file
    @FXML
    private Text selectedFileName;

    @FXML
    private Text selectedFileSize;

    @FXML
    private Text selectedFileHash;

	//This is where the user enters the extension to filter by
	@FXML
    private TextField extensionFilterTextBox;

	//When clicked, it takes the applied filters and starts the search for duplicates
    @FXML
    private Button folderButton;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TreeView<String> duplicateList; //The TreeView that shows all duplications


    @FXML
    void toggleExtensionFilter() {
		filterExtension = !filterExtension;
	}


    @FXML
    void toggleHashLargeFiles() {
    	hashLargeMD5Files = !hashLargeMD5Files;
    }	

	//Returns a JavaFX task, which runs the surrounded code in a separate thread from the main GUI thread
	Task<Void> returnNewTask(){
		return new Task<Void>(){
			@Override
			protected Void call() throws Exception{
				
				/*
				Platform.runLater runs the surrounded code on the GUI thread.
				This is because modifications to the GUI should ideally be run on the GUI thread.
				*/
		        Platform.runLater(new Runnable() {
		            @Override public void run() {
		            	progressBarIndicator.setVisible(true);
						folderButton.setDisable(true);
						//duplicateList.setDisable(true);
						applyFilterButton.setDisable(true);
						removeOtherDuplicatesButton.setDisable(true);
						removeDuplicateButton.setDisable(true);
		            }
		        });
				populateTreeViewAndRemoveExcess(searchDirectory); //Populates TreeView with duplicates
        Platform.runLater(new Runnable() {
            @Override public void run() {
				progressBarIndicator.setVisible(false);
				folderButton.setDisable(false);
				//duplicateList.setDisable(false);
				applyFilterButton.setDisable(false);
            }
        });
				return null;
			}
		};		
	} 
    @FXML
    void removeOthersOnClick(ActionEvent event) {//Removes siblings of the selected child node
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
    	catch(IOException e){
    		System.out.println(e.getMessage());
    		clearList();
    		(new Thread(returnNewTask())).start();
    	}

    }
    private void toggleHashProgressBar(){ //toggles whether the hash progress indicator is shown
    	Platform.runLater(new Runnable(){
    		@Override public void run(){
    			hashCircle.setVisible(!hashCircle.isVisible());
    		}
    	});
    }
    @FXML
    void removeDuplicateOnClick(ActionEvent event) { //Removes the selected child node
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
    	catch(IOException e){
    		System.out.println(e.getMessage());
    		clearList();
    		(new Thread(returnNewTask())).start();
    	}


    }

	//The following, when called, toggles their respective duplicate filter
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
    	if(searchDirectory != null){
	  		clearList();
			(new Thread(returnNewTask())).start();  		
    	}
    }

	
    @FXML
    void getSelected(){ //Displays the information about the selected file
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
				else{ //If the selected item is null, reset the file description text boxes.
					selectedFileName.setText("Name: ");
					selectedFileSize.setText("Size: ");
					selectedFileHash.setText("Hash: ");
					removeDuplicateButton.setDisable(true);
					removeOtherDuplicatesButton.setDisable(true);
				}
			}

		}
    }
	private String getMD5(File file){ //Returns the hash of the selected file
		String md5 = "";
		try{
<<<<<<< HEAD
			if(!hashLargeMD5Files){ //Doesn't hash a large file unless the checkbox is checked
				if(file.length() > 104857600){
=======
			if(!hashLargeMD5Files){
				if(file.length() > 104857600){ //100MB
>>>>>>> f5a66ba0b810e38aa7194911ada8f491fa685d39
					return file.getAbsolutePath();
				}
			}
			//Checks if the hash of the file already exists in the map
			String temp = fileNameToMD5HashMap.get(file.getAbsolutePath());
			if(temp == null){ //If no hash exists...
				toggleHashProgressBar();
				
				System.out.println("Hashing " + file.getAbsolutePath());
<<<<<<< HEAD
				//Notifies the user if a very large file is being hashed, and tells them to be patient.
				if(file.length() > 1073741824)
=======
				if(file.length() > 1073741824) //1GB
>>>>>>> f5a66ba0b810e38aa7194911ada8f491fa685d39
					printToStatus("Hashing an EXTREMELY large file. This could take a rather long time: " + file.getAbsolutePath());
				if(file.length() > 52428800 && file.length() < 1073741824)
					printToStatus("Hashing a somewhat large file. This might take a little while: " + file.getAbsolutePath());
				
				HashCode hashCode = com.google.common.io.Files.hash(file, Hashing.md5());
				md5 = hashCode.toString();
				fileNameToMD5HashMap.put(file.getAbsolutePath(), md5);
				toggleHashProgressBar();
			}
			else{//If the hash is in the map, just return it.
				md5 = temp;
			}		
			
		}
		catch(IOException i_exc){
			i_exc.printStackTrace();
		}
		return md5;
	}
	private void clearList(){ //Clears the tableview
		duplicateList.getRoot().getChildren().setAll(FXCollections.observableArrayList()); //Empties the current duplicate list
	}
<<<<<<< HEAD
	LinkedList getFileList(File file){
		//Recursively gets contents of the search directory
=======
	LinkedList getFileList(File currFile){
>>>>>>> f5a66ba0b810e38aa7194911ada8f491fa685d39
		LinkedList list = new LinkedList();
		try{
			
			Files.walkFileTree(Paths.get(currFile.getAbsolutePath()), new FileVisitor<Path>(){
	             // Called after a directory visit is complete.
	            @Override
	            public FileVisitResult postVisitDirectory(Path dir, IOException exc){
	                return FileVisitResult.CONTINUE;
	            }
	            // called before a directory visit.
	            @Override
	            public FileVisitResult preVisitDirectory(Path dir,
	            		  BasicFileAttributes attrs) throws IOException {
	                return FileVisitResult.CONTINUE;
	            }

	            @Override
	            public FileVisitResult visitFile(Path currFile,
	                    BasicFileAttributes attrs) throws IOException {
					if(filterExtension && extensionFilterTextBox.getText().length() > 0){
						String currFileName = currFile.toFile().getAbsolutePath();
						System.out.println("Trying to filter: " + currFileName);
						String filter = extensionFilterTextBox.getText();
						
						String lastPartOfFileName = currFileName.substring(currFileName.length() - filter.length(), currFileName.length());
					
						if(!filter.equals(lastPartOfFileName)){ //trtr.doc doc filter
							System.out.println("Excluding " + currFileName);
							return FileVisitResult.CONTINUE;
						}
					}
					System.out.println(currFile.getFileName());
					list.insertf(currFile.toFile());
	                return FileVisitResult.CONTINUE;
	            }
	    		@Override
	            public FileVisitResult visitFileFailed(Path file, IOException exc)
	                    throws IOException {
	                return FileVisitResult.CONTINUE;
	            }	
			});
		}
		catch(IOException exc){
			exc.printStackTrace();
		}

		/*
		if(currFile.isFile() && currFile.canRead()){	
			if(filterExtension && extensionFilterTextBox.getText().length() > 0){
				String currFileName = currFile.getAbsoluteFile().toString();
				System.out.println("Trying to filter: " + currFileName);
				String filter = extensionFilterTextBox.getText();
				
				String lastPartOfFileName = currFileName.substring(currFileName.length() - filter.length(), currFileName.length());
			
				if(!filter.equals(lastPartOfFileName)){ //trtr.doc doc filter
					System.out.println("Excluding " + currFileName);
					return list;
				}
			}

			System.out.println("Is currFile: " + currFile.getAbsoluteFile().toString());
			list.insertf(currFile);
		}
		else if(currFile.isDirectory()){
			String[] currFiles = currFile.list();
			if(currFiles != null){
				for(String entry : currFiles){
					File temp = new File(currFile, entry);
					list.concat(getFileList(temp));
				}
			}
		}
		*/
		System.gc();
		return list;
	}

	void printToStatus(String input){ //A convenience method to modify the status textbox on the gui thread that's called from the non-gui thread.
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
		if(filterMD5){
			printToStatus("May not appear to move at start because all files will be hashed at the start");
		}
		HashSet<String> nodeCopyList = new HashSet<>();
		//A list of all nodes that were previously added to the tree
		/*
			offers constant time performance for the basic operations 
			(add, remove, contains and size).
		*/
		HashBiMap<String, String> nodeCopyListHashes = HashBiMap.create();
		for(int counter = 0; counter < fileList.size(); counter++){
			final float progress = counter; //final because its required
	        Platform.runLater(new Runnable() {
	            @Override public void run() {
	            	progressBarIndicator.setProgress((progress / fileList.size()));
	            }
	        });	      
			File singleFile = fileList.returna(counter);
			if(nodeCopyList.add(singleFile.getAbsolutePath()) == false){ //If the item doesn't already exist
				System.out.println("Excluding " + singleFile.getName());
				continue;
			}

//			System.out.println(singleFile);
			TreeItem<String> fileItem = new TreeItem<String> (singleFile.getAbsolutePath());
			//Kinda like the direct sub folder after the Root node called "Duplicates"

<<<<<<< HEAD
			getListOfDuplicates(singleFile, searchDirectory, fileItem, nodeCopyList);
			
=======
			populateList(searchDirectory, singleFile, fileItem, nodeCopyList);
			System.gc();
>>>>>>> f5a66ba0b810e38aa7194911ada8f491fa685d39
			
			if(fileItem.getChildren().size() > 0){ //If the current node is a parent node...aka has a child or two..
				if(filterMD5){
					if(nodeCopyListHashes.get(getMD5(singleFile)) != null){ //
						continue;
					}	
					else{
						nodeCopyListHashes.put(getMD5(singleFile), singleFile.getAbsolutePath());
						System.out.println("Adding " + singleFile.getName());
						fileItem.getChildren().add(new TreeItem<String>(singleFile.getAbsoluteFile().toString()));
						
						/*
						Won't require rehashing in the getMd5 function, because, for each file hashed,
						said getMD5 function adds the file's absolute path and its hash to a global HashMap.
						This means it won't need to rehash the file if a hash for the file exists in the map.
						*/
						
					}			
				}
				Platform.runLater(new Runnable(){ //If all is okay, add the new parent node to the root node.
					@Override public void run() {
						duplicateList.getRoot().getChildren().add(fileItem);
					}
				});
			}
			
		}
        Platform.runLater(new Runnable() { //Sets the progressbar to indeterminate
            @Override public void run() {
            	progressBarIndicator.setProgress(-1);
            }
        });
		printToStatus("Done!");
		System.gc();
	}
    @FXML
    void selectFolder(ActionEvent event) { //Prompts the user to choose a directory to search.
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose Directory");
		File directory = chooser.showDialog(sizeCheckBox.getScene().getWindow());
		if(directory != null){ //If the user didn't press cancel and selected a valid folder...
			
			searchDirectory = directory; 
			clearList();


			(new Thread(returnNewTask())).start(); //Starts the duplicate file search in a new thread

			System.gc();
		}
	}
	boolean verifyMD5(File origin, File file){
		return getMD5(file).equals(getMD5(origin));
	}
	boolean isAbsoluteFilePathEqual(File origin, File file){
		return file.getAbsoluteFile().toString().equals(origin.getAbsoluteFile().toString());
	}
<<<<<<< HEAD
	void getListOfDuplicates(File original, File file, TreeItem<String> node, HashSet<String> nodeList){
		/*
		Accepts a treeitem parent node, then recursively checks if they are duplictes of the original file
		if the file is a duplicate, it adds a child node to the new passed node.
		*/
		if(file.isFile()){
			if(!isAbsoluteFilePathEqual(original, file)){

				if(filterSize){ 
					/*
					Files need to be of the same size to have the same hashes, so this filter weeds out the majority 
					of all duplicate candidates before a hash even needs to be calculated 
					*/
					if(!(file.length() == original.length())){
						return;
=======
	void populateList(File original, File compare, TreeItem<String> node, HashSet<String> nodeList){

		try{
			
			Files.walkFileTree(Paths.get(original.getAbsolutePath()), new FileVisitor<Path>(){
	             // Called after a directory visit is complete.
	            @Override
	            public FileVisitResult postVisitDirectory(Path dir, IOException exc){
	                return FileVisitResult.CONTINUE;
	            }
	            // called before a directory visit.
	            @Override
	            public FileVisitResult preVisitDirectory(Path dir,
	            		  BasicFileAttributes attrs) throws IOException {
	                return FileVisitResult.CONTINUE;
	            }

	            @Override
	            public FileVisitResult visitFile(Path currFile,
	                    BasicFileAttributes attrs) throws IOException {
	            	File file = currFile.toFile().getAbsoluteFile();
					if(file.isFile()){
						//System.out.println(compare.getAbsolutePath() + " - " + file.getAbsolutePath());
						if(!isAbsoluteFilePathEqual(compare, file)){
							//System.out.println("Verifying " + file.getName());
							//printToStatus("Verifying " + file.getName());

							if(filterSize){ 
								
								//Files need to be of the same size to have the same hashes, so this filter weeds out the majority 
								//of all duplicate candidates before a hash even needs to be calculated 
								
								if(file.length() != compare.length()){
									return FileVisitResult.CONTINUE;
								}
							}
							if(filterMD5){
								if(!verifyMD5(compare, file)){
									return FileVisitResult.CONTINUE;
								}
							}
							if(filterExtension && extensionFilterTextBox.getText().length() > 0){
								String fileName = file.getAbsoluteFile().toString();
								System.out.println("Trying to filter: " + fileName);
								String filter = extensionFilterTextBox.getText();
								
								String lastPartOfFileName = fileName.substring(fileName.length() - filter.length(), fileName.length());
							
								if(!filter.equals(lastPartOfFileName)){ //trtr.doc doc filter
									System.out.println("Excluding " + fileName);
									return FileVisitResult.CONTINUE;
								}
							}
							//System.out.println("Adding " + file.getName());
							//printToStatus("Adding " + file.getName());
							TreeItem<String> newTreeItem = new TreeItem<>(file.getAbsolutePath());
							
							node.getChildren().add(newTreeItem);
							
							
						}
						
					}
	                return FileVisitResult.CONTINUE;
	            }
	    		@Override
	            public FileVisitResult visitFileFailed(Path file, IOException exc)
	                    throws IOException {
	                return FileVisitResult.CONTINUE;
	            }	
			});
		}
		catch(IOException exc){
			exc.printStackTrace();
		}
/*		//add file only
		//ArrayList<String> list = new ArrayList<>();
		for(int counter = 0; counter < fileList.size(); counter++){
			File file = fileList.returna(counter);
			if(file.isFile()){
				if(!isAbsoluteFilePathEqual(original, file)){
					//System.out.println("Verifying " + file.getName());
					//printToStatus("Verifying " + file.getName());

					if(filterSize){ 
						
						//Files need to be of the same size to have the same hashes, so this filter weeds out the majority 
						//of all duplicate candidates before a hash even needs to be calculated 
						
						if(file.length() != original.length()){
							continue;
						}
>>>>>>> f5a66ba0b810e38aa7194911ada8f491fa685d39
					}
					if(filterMD5){
						if(!verifyMD5(original, file)){
							continue;
						}
					}
					if(filterExtension && extensionFilterTextBox.getText().length() > 0){
						String fileName = file.getAbsoluteFile().toString();
						System.out.println("Trying to filter: " + fileName);
						String filter = extensionFilterTextBox.getText();
						
						String lastPartOfFileName = fileName.substring(fileName.length() - filter.length(), fileName.length());
					
						if(!filter.equals(lastPartOfFileName)){ //trtr.doc doc filter
							System.out.println("Excluding " + fileName);
							continue;
						}
					}
					//System.out.println("Adding " + file.getName());
					//printToStatus("Adding " + file.getName());
					TreeItem<String> newTreeItem = new TreeItem<>(file.getAbsolutePath());

<<<<<<< HEAD
				
				node.getChildren().add(newTreeItem);
				
				
			}
			
		}
		else if(file.isDirectory()){
			String[] subNote = file.list();
			if(subNote != null){
				for(String filename : subNote){
					File temp = new File(file, filename);
					getListOfDuplicates(original, temp, node, nodeList);
=======
					
					node.getChildren().add(newTreeItem);
					
>>>>>>> f5a66ba0b810e38aa7194911ada8f491fa685d39
					
				}
				
			}

		}*/
	}
    @FXML
    void initialize() {
        assert duplicateList != null : "fx:id=\"duplicateList\" was not injected: check your FXML file 'DSProject.fxml'.";
		TreeItem<String> rootItem = new TreeItem<String> ("Duplicates");
		rootItem.setExpanded(true);
		duplicateList.setRoot(rootItem);
    }
}
