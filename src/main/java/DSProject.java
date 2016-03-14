package dsproject;

import java.util.Scanner;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import java.net.URL;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;

public class DSProject extends Application{
	public static void main(String[] args){
		Application.launch(args);
	}
	public void start(Stage primaryStage){
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
