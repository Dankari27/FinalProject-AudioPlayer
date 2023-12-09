package application;
import javafx.scene.input.MouseEvent;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;

public class Main extends Application {
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage primaryStage) {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("/Home.fxml"));

            root.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                }
            });
            
            //allows undecorated window to be dragged
            root.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    primaryStage.setX(event.getScreenX() - xOffset);
                    primaryStage.setY(event.getScreenY() - yOffset);

                    //snap to edge when dragging
                    snapToEdge(primaryStage);
                }
            });

            Scene scene = new Scene(root);
            
            //adds system icon
            Image icon = new Image("icon.png");
            primaryStage.getIcons().add(icon);
            
            primaryStage.setScene(scene);
            
            //makes window unable to be resized
            primaryStage.setResizable(false);
            //adds transparency
            primaryStage.setOpacity(0.76);
            primaryStage.initStyle(StageStyle.TRANSPARENT);
            
            //removes window border
            primaryStage.initStyle(StageStyle.UNDECORATED);

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //method to handle snapping feature
    private void snapToEdge(Stage stage) {
        Screen screen = Screen.getPrimary();
        double screenWidth = screen.getBounds().getWidth();
        double screenHeight = screen.getBounds().getHeight();

        double windowWidth = stage.getWidth();
        double windowHeight = stage.getHeight();

        //define the snapping margin (e.g., 10 pixels)
        //i.e how far away the window must be from the edge of the screen in order to snap to it
        double snapMargin = 40;

        //check if the window is close to the left edge
        if (stage.getX() < snapMargin) {
            stage.setX(0);
        }

        //check if the window is close to the right edge
        if (stage.getX() + windowWidth > screenWidth - snapMargin) {
            stage.setX(screenWidth - windowWidth);
        }

        //check if the window is close to the top edge
        if (stage.getY() < snapMargin) {
            stage.setY(0);
        }

        //check if the window is close to the bottom edge
        if (stage.getY() + windowHeight > screenHeight - snapMargin) {
            stage.setY(screenHeight - windowHeight);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
