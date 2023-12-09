package application;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class Controller implements Initializable {

	
	@FXML 
	private Pane pane;
	
	@FXML
	private Label songLabel;
  
    @FXML
    private Button nextButton, pauseButton, playButton, prevButton;

    @FXML
    private Slider volSlider;
    
    @FXML
    private ProgressBar songProgressBar;
    
    @FXML
    private ComboBox<String> speedBox;
    
    @FXML
    private ComboBox<String> optionsDrop;
    	
    private File directory;
    
    private File[] files;
    
    private ArrayList<File> songs;

    private int songNumber;
    
    private int[] speeds = {25, 50, 75, 100, 125, 150, 175, 200};
    
    private Timer timer;
    private TimerTask task;
    private boolean running;
    
    private Media media;
    private MediaPlayer mediaPlayer = null;

    @FXML
    private ImageView background;

    

    @FXML
    void changeSpeed(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.setRate(Integer.parseInt(speedBox.getValue().substring(0, speedBox.getValue().length() - 1)) * 0.01);
        } else {
            System.out.println("Media player is not initialized. No song is currently loaded.");
        }
    }

    
    //drop down fxml method
    @FXML
    void options(ActionEvent event) {
        String selectedOption = optionsDrop.getValue();

        if ("Open Folder".equals(selectedOption)) {
            openFolder();
        }if ("Quit".equals(selectedOption)) {
            quitApplication();
            }
        if ("Minimize".equals(selectedOption)) {
        	minimizeApp();
        } else {
            // Handle default case or do nothing
        }
        
        optionsDrop.setValue(null);
    }
     
    //quits the application using drop down
        private void quitApplication() {
        	Stage stage = (Stage) optionsDrop.getScene().getWindow();
            stage.close();
        }
        
        ////TO DO: Create settings menu and add misc settings such as audio device output and custom themes
        
        private void minimizeApp() {

        	 ((Stage) optionsDrop.getScene().getWindow()).setIconified(true);

        }
        private void openFolder() {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Folder");

            // Show the directory chooser dialog
            Stage stage = (Stage) optionsDrop.getScene().getWindow();
            File selectedDirectory = directoryChooser.showDialog(stage);

            // Handle the selected directory
            if (selectedDirectory != null) {
                System.out.println("Selected folder: " + selectedDirectory.getAbsolutePath());

                // Load songs from the selected directory
                loadSongs(selectedDirectory);

                // Stop the current media player if it's playing
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer = null; // Set mediaPlayer to null after stopping
                }

                // Set default speed to 100%
                speedBox.setValue("100%");

                // Update the files array
                files = selectedDirectory.listFiles();

                // Check if there are songs in the new directory
                if (!songs.isEmpty()) {
                    songNumber = 0;
                    media = new Media(songs.get(songNumber).toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    songLabel.setText("Now Playing: " + songs.get(songNumber).getName());
                    playMedia();
                } else {
                    // Handle case where no songs are found in the selected directory
                    System.out.println("No songs found in the selected folder.");

                    // Display an alert to the user
                    showAlert("Empty Folder", "Please select a folder with at least one file.");

                    // Stop the timer and reset UI elements
                    cancelTimer();
                    songProgressBar.setProgress(0);
                    songLabel.setText("Now Playing: ");
                }
            }
        }
     // Method to show an alert
        private void showAlert(String title, String content) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);

            // Use Platform.runLater to execute the clearing action on the JavaFX Application Thread
            alert.setOnHidden(e -> {
                Platform.runLater(() -> {
                    optionsDrop.getSelectionModel().clearSelection();
                });
            });

            alert.showAndWait();
        }


        
        //fxml method which handles skipping the current song
    @FXML
    void nextMedia() {
if(songNumber < songs.size() - 1) { //checks size of selected playlist
	songNumber++;
	mediaPlayer.stop();
	if(running) {
		cancelTimer();
	}
    media = new Media(songs.get(songNumber).toURI().toString()); 
    mediaPlayer = new MediaPlayer(media);
    songLabel.setText("Now Playing: " + songs.get(songNumber).getName());
    playMedia();
}
else {
	songNumber = 0;
	mediaPlayer.stop();
	
	if(running) {
		cancelTimer();
	}
    media = new Media(songs.get(songNumber).toURI().toString());
    mediaPlayer = new MediaPlayer(media);
    songLabel.setText("Now Playing: " + songs.get(songNumber).getName());
    playMedia();
}
    }

    //fxml method that handles playing the song
    @FXML
    void playMedia() {
    	beginTimer();
    	mediaPlayer.play();
    	mediaPlayer.setVolume(volSlider.getValue() * 0.01);
        songLabel.setText("Now Playing: " + songs.get(songNumber).getName());
    }
    
//fxml method that handles pausing the song
    @FXML
    void pauseMedia() {
    	cancelTimer();
    	mediaPlayer.pause();
    }
    
    
    
    //fxml method that creates a timer and displays progress bar
    @FXML
    private void beginTimer() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                running = true;
                double current = mediaPlayer.getCurrentTime().toSeconds();
                double end = media.getDuration().toSeconds();
                System.out.println(current / end);
                songProgressBar.setProgress(current / end);
                
                //if song ends then auto play next soin
                if (current / end == 1) {
                    cancelTimer();
                    Platform.runLater(() -> { // Ensure UI updates on the JavaFX Application Thread
                        nextMedia(); // Call nextMedia on the JavaFX Application Thread
                    });
                }
            }
        };

        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    
    //fxml method to hand timer cancellation to reset timer
    @FXML
    public void cancelTimer() {
    	running = false;
    	timer.cancel();
    }

    //fxml method that skips to previous song
    @FXML
    void prevMedia() {
    	if(songNumber > 0) {
    		songNumber--;
    		mediaPlayer.stop();
    		
    		if(running) {
    			cancelTimer();
    		}
    		
    	    media = new Media(songs.get(songNumber).toURI().toString());
    	    mediaPlayer = new MediaPlayer(media);
    	    songLabel.setText("Now Playing: " + songs.get(songNumber).getName());
    	    playMedia();
    	}
    	else {
    		songNumber = songs.size() - 1;
    		mediaPlayer.stop();
    		
    		if(running) {
    			cancelTimer();
    		}
    	    media = new Media(songs.get(songNumber).toURI().toString());
    	    mediaPlayer = new MediaPlayer(media);
    	    songLabel.setText("Now Playing: " + songs.get(songNumber).getName());
    	    playMedia();
    	}
    }

    //main method that initializes during program startup
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
    	
        // creates arraylist to store songs
        songs = new ArrayList<>();

        mediaPlayer = null;
        // sets default directory
        // directory = new File("music");

        // location to load files from
        // files = directory.listFiles();

        // if the files are not null, add them to the songs list
        if (files != null) {
            for (File file : files) {
                songs.add(file);
                System.out.println(file);
            }
        }

        // change speed multiplier
        for (int i = 0; i < speeds.length; i++) {
            speedBox.getItems().add(Integer.toString(speeds[i]) + "%");
        }
        // on action event for changing speed
        speedBox.setOnAction(this::changeSpeed);

        // adds options to drop down
        optionsDrop.getItems().addAll("Open Folder", "Minimize", "Quit");

        // volume adjustment
        volSlider.valueProperty().addListener(new ChangeListener<Number>() {

            // volume adjustment behaviour
            @Override
            public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(volSlider.getValue() * 0.01);
                }
            }
        });

        // sets custom styles
        optionsDrop.setStyle("-fx-control-inner-background: #ff0092");
        songProgressBar.setStyle("-fx-accent: pink");
        songProgressBar.setStyle("-fx-control-inner-background: #ff0092");

        volSlider.setStyle("-fx-control-inner-background: #ff0092");

        speedBox.setStyle("-fx-control-inner-background: #ff0092");

    
    }
    
    //Recursive method to print directory folder files and subdirectory files for testing purposes. 
    private void printDirectoryStructure(File directory) {
        printDirectoryStructureHelper(directory, 0);
    }

    private void printDirectoryStructureHelper(File file, int depth) {
        StringBuilder indentation = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indentation.append("  "); 
        }

        System.out.println(indentation + file.getName());

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    printDirectoryStructureHelper(subFile, depth + 1);
                }
            }
        }
    }

    //custom song folder method 
    private void loadSongs(File directory) {
        songs.clear(); // Clear the existing song list
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                songs.add(file);
                System.out.println(file);
            }
        }
    }

}