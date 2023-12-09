package application;
import javafx.scene.input.MouseEvent;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
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
        double windowWidth = stage.getWidth();
        double windowHeight = stage.getHeight();

        //define the snapping margin (e.g., 40 pixels)
        double snapMargin = 40;

        //iterates through all screens
        for (Screen screen : Screen.getScreens()) {
            Rectangle2D screenBounds = screen.getBounds();

            //check if the window's bounds intersect with the current screen's bounds (i.e if the window is against the edge of another monitor)
            if (screenBounds.intersects(stage.getX(), stage.getY(), windowWidth, windowHeight)) {
               
            	//check if the window is close to the left edge
                if (stage.getX() < screenBounds.getMinX() + snapMargin) {
                    stage.setX(screenBounds.getMinX());
                }

                //check if the window is close to the right edge
                if (stage.getX() + windowWidth > screenBounds.getMaxX() - snapMargin) {
                    stage.setX(screenBounds.getMaxX() - windowWidth);
                }

                //check if the window is close to the top edge
                if (stage.getY() < screenBounds.getMinY() + snapMargin) {
                    stage.setY(screenBounds.getMinY());
                }

                //check if the window is close to the bottom edge
                if (stage.getY() + windowHeight > screenBounds.getMaxY() - snapMargin) {
                    stage.setY(screenBounds.getMaxY() - windowHeight);
                }

                //exit the loop once the current screen is found
                break;
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
