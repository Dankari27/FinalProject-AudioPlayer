package application;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
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

    private File[] files;
    private ArrayList<File> songs;

    private int songNumber;

    private int[] speeds = {25, 50, 75, 100, 125, 150, 175, 200};

    private Timer timer;
    private TimerTask task;
    private boolean running;

    private Media media;
    private MediaPlayer mediaPlayer = null;

    //variable to store the current playback speed
    private double currentSpeed = 1.0;

    //changes speed of song
    @FXML
    void changeSpeed(ActionEvent event) {
        if (mediaPlayer != null) {
            currentSpeed = Integer.parseInt(speedBox.getValue().substring(0, speedBox.getValue().length() - 1)) * 0.01;
            mediaPlayer.setRate(currentSpeed);
        } else {
            System.out.println("Media player is not initialized. No song is currently loaded.");
        }
    }

    //options menu 
    @FXML
    void options(ActionEvent event) {
        String selectedOption = optionsDrop.getValue();

        if ("Open Folder".equals(selectedOption)) {
            openFolder();
        } if ("Quit".equals(selectedOption)) {
            quitApplication();
        } if ("Minimize".equals(selectedOption)) {
            minimizeApp();
        } else {
            // Handle default case or do nothing
        }

        optionsDrop.setValue(null);
    }
    
    //method to quit application
    private void quitApplication() {
        Stage stage = (Stage) optionsDrop.getScene().getWindow();
        stage.close();
    }
    
    //method to minimize application
    private void minimizeApp() {
        ((Stage) optionsDrop.getScene().getWindow()).setIconified(true);
    }
    
    //deals with file input/output when selecting custom song folder
    private void openFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");

        //shows the directory chooser dialog
        Stage stage = (Stage) optionsDrop.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);

        //handles the selected directory
        if (selectedDirectory != null) {
            System.out.println("Selected folder: " + selectedDirectory.getAbsolutePath());

            //load songs from the selected directory
            loadSongs(selectedDirectory);

            //stop the current media player if it's playing
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer = null; //set mediaPlayer to null after stopping
            }

            //set default speed to 100%
            speedBox.setValue("100%");

            //update the files array
            files = selectedDirectory.listFiles();

            //check if there are songs in the new directory
            if (!songs.isEmpty()) {
                songNumber = 0;
                media = new Media(songs.get(songNumber).toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                songLabel.setText("Now Playing: " + songs.get(songNumber).getName());
                playMedia();
            } else {
                //handle case where no songs are found in the selected directory
                System.out.println("No songs found in the selected folder.");

                //display an alert to the user
                showAlert("Empty Folder", "Please select a folder with at least one file.");

                //stop the timer and reset UI elements
                cancelTimer();
                songProgressBar.setProgress(0);
                songLabel.setText("Now Playing: ");
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        //uses Platform.runLater to execute the clearing action on the JavaFX Application Thread
        //prevents bug regarding the focus for the drop menu
        alert.setOnHidden(e -> {
            Platform.runLater(() -> {
                optionsDrop.getSelectionModel().clearSelection();
            });
        });

        alert.showAndWait();
    }
    
    //next song method
    @FXML
    void nextMedia() {
        if (songNumber < songs.size() - 1) {
            songNumber++;
        } else {
            songNumber = 0;
        }
        playSelectedMedia();
    }
    
   //plays song
    @FXML
    void playMedia() {
        beginTimer();
        mediaPlayer.play();
        mediaPlayer.setVolume(volSlider.getValue() * 0.01);
        mediaPlayer.setRate(currentSpeed); // Set the speed

        String songName = songs.get(songNumber).getName();
        songName = songName.replaceAll("\\.(mp3|wav|m4a|flac|ogg|aac|wma|aiff|alac|opus)$", "");

        songLabel.setText("Now Playing: " + songName);

  
    }

    //plays selected song (custom)
    private void playSelectedMedia() {
        mediaPlayer.stop();

        if (running) {
            cancelTimer();
        }

        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setRate(currentSpeed); // Set the speed
        songLabel.setText("Now Playing: " + songs.get(songNumber).getName());
        playMedia();
    }
    
    //method to handle pausing
    @FXML
    void pauseMedia() {
        cancelTimer();
        mediaPlayer.pause();
    }
    
    //method to handle timer for progress bar fill
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

                if (current / end == 1) {
                    cancelTimer();
                    Platform.runLater(() -> {
                        nextMedia();
                    });
                }
            }
        };

        timer.scheduleAtFixedRate(task, 0, 1000);
    }
    
    //method to cancel timer for when song ends
    public void cancelTimer() {
        running = false;
        timer.cancel();
    }
    
    //plays previous song
    @FXML
    void prevMedia() {
        if (songNumber > 0) {
            songNumber--;
        } else {
            songNumber = songs.size() - 1;
        }
        playSelectedMedia();
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        // creates arraylist to store songs
        songs = new ArrayList<>();

        mediaPlayer = null;

        // if the files are not null, add them to the songs list
        if (files != null) {
            for (File file : files) {
                songs.add(file);
                System.out.println(file);
            }
        }

        for (int i = 0; i < speeds.length; i++) {
            speedBox.getItems().add(Integer.toString(speeds[i]) + "%");
        }
        speedBox.setOnAction(this::changeSpeed);

        optionsDrop.getItems().addAll("Open Folder", "Minimize", "Quit");

        volSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(volSlider.getValue() * 0.01);
                }
            }
        });

        printDirectoryStructure(new File("Music Playlist Test"));
        
        //sets styles for prog bar, drop down menus, and vol slider
        optionsDrop.setStyle("-fx-control-inner-background: #ff0092");
        songProgressBar.setStyle("-fx-control-inner-background: #ff0092");

        volSlider.setStyle("-fx-control-inner-background: #ff0092");

        speedBox.setStyle("-fx-control-inner-background: #ff0092");
    }

    //recursive method that shows file path and directory path for testing purposes
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

    private void loadSongs(File directory) {
        songs.clear();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                songs.add(file);
                System.out.println(file);
            }
        }
    }
}
