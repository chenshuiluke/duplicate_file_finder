package dsproject;

import java.util.Scanner;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import java.net.URL;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.math.BigInteger;
import java.util.Random;
import java.util.ArrayList;
import com.aquafx_project.AquaFx;


public class DSProject extends Application{
	public static void main(String[] args){
		generateTests();
		Application.launch(args);
	}
	static void generateTests(){
		File testDir = new File("testDir");
		try{
				Random rand= new Random();
				if(!testDir.isDirectory()){
					System.out.println("testDir doesn't exist or is not a directory");
					return;
				}
				else{
					String[] dirNames = {"blahblah", "hehe", "okfgodfkgodfkg", "fdfdf", "dfdfdfv", "ASddd", "dffg"};
					for(String dir : dirNames){
						try{
							Files.createDirectory(Paths.get("testDir/" + dir));
							System.out.println("Making " + dir);
						}
						catch(java.nio.file.FileAlreadyExistsException e){

						}
					}
					for(int counter = 0; counter < 50; counter++){
						SecureRandom random1 = new SecureRandom();
						SecureRandom random2 = new SecureRandom();
						String temp1 = new BigInteger(130, random1).toString(32);
						String temp2 = new BigInteger(130, random2).toString(32);
						ArrayList<String> tempList = new ArrayList<String>();
						tempList.add(temp1);
						tempList.add(temp2);
						for(int counter1 = 0; counter1 < 10; counter1++){
							Files.write(Paths.get("testDir/" + dirNames[rand.nextInt(6)] + "/" + String.valueOf(counter)),tempList);

						}
					}
				}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	public void start(Stage primaryStage){
		AquaFx.style();
		primaryStage.setTitle("DS Project - Duplicate File Finder");
		Parent root = null;
		String sceneFile = "DSProject.fxml";
		URL url = null;
		try{
			url = getClass().getClassLoader().getResource(sceneFile);
			root = FXMLLoader.load(url);
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.show();
		}
		catch(Exception ex){
		    System.out.println( "Exception on FXMLLoader.load()" );
		    System.out.println( "  * url: " + url );
		    System.out.println( "  * " + ex );
		    System.out.println( "    ----------------------------------------\n" );

		}
	}
}
