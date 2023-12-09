package application;
import javafx.scene.input.MouseEvent;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;

// The main class that extends Application, required for JavaFX applications
public class Main extends Application {
	   private double xOffset = 0;
	    private double yOffset = 0;
	// The start method is the entry point for JavaFX applications
	@Override
	public void start(Stage primaryStage) {
		try {
			
			// Load the FXML file to create the UI layout
			AnchorPane root = FXMLLoader.load(getClass().getResource("/Home.fxml"));
			
			//make undecorated window movable
	        root.setOnMousePressed((EventHandler<? super MouseEvent>) new EventHandler<MouseEvent>() {
	            @Override
	            public void handle(MouseEvent event) {
	                xOffset = event.getSceneX();
	                yOffset = event.getSceneY();
	            }
	        });
	        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
	            @Override
	            public void handle(MouseEvent event) {
	                primaryStage.setX(event.getScreenX() - xOffset);
	                primaryStage.setY(event.getScreenY() - yOffset);
	            }
	        });
	        
	        
			
			// Create a scene with the UI layout and set its dimensions (width: 600, height: 400)
			Scene scene = new Scene(root);
			
			//sets system icon
			Image icon = new Image("icon.png");
			primaryStage.getIcons().add(icon);
			
			// Set the scene to the primary stage (the main window)
			primaryStage.setScene(scene);
			
		
			
			// Make the window not resizable
			primaryStage.setResizable(false);
			
			//set opacity
			primaryStage.setOpacity(0.76);
			
			primaryStage.initStyle(StageStyle.TRANSPARENT);
			
		
			//remove window handle bars
			primaryStage.initStyle(StageStyle.UNDECORATED);
		
			// Show the primary stage with the loaded scene
			primaryStage.show();
	
			
		} catch (Exception e) {
			// Print the stack trace in case of an exception
			e.printStackTrace();
		}
	}

	
	// The main method, which launches the JavaFX application
	public static void main(String[] args) {
		launch(args);
	}
}