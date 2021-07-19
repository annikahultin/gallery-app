package cs1302.gallery;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.TilePane;
import java.net.URL;
import java.io.InputStreamReader;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.MalformedURLException;
import javafx.geometry.Orientation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import java.util.Random;
import javafx.scene.control.ProgressBar;
import javafx.application.Platform;
import javafx.scene.text.Text;
import java.util.Scanner;
import java.util.LinkedList;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Represents an iTunes GalleryApp!.
 */
public class GalleryApp extends Application {

    TilePane pane;
    Scene scene;
    MenuBar menuBar;
    ToolBar toolbar;
    HBox progressBox;
    ProgressBar progressBar;
    VBox vbox = new VBox();
    Button pause;
    Button play;
    Button updateImages;
    Button exit;
    TextField query;
    ImageLoader[] images;
    String search;
    InputStreamReader reader;
    JsonArray results;
    String[] urls;
    List<String> list;
    String sUrl;
    Scanner encoder;
    String encodedSearch;
    List<String> inUse;
    List<String> notInUse;

    /**
     * Creates and immediately starts a new daemon thread that executes
     * {@code target.run()}. This method, which may be called from any thread,
     * will return immediately its the caller.
     * @param target the object whose {@code run} method is invoked when this
     *               thread is started
     */
    public static void runNow(Runnable target) {
        Thread t = new Thread(target); //creates the thread
        t.setDaemon(true);
        t.start();
    } // runNow

    /**
     * Sets the progress of the progress bar.
     * @param progress  the proress of the task at hand
     */
    private void setProgress(final double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress)); //updates the progress bar
    } //setProgress

    /**
     * Alerts the user that there were less than 21 distinct images in the search results.
     */
    private void makeAlert() {
        Alert message = new Alert(AlertType.WARNING); //intializes the alert
        message.setResizable(true);
        message.setContentText("Query produced less than 21 distinct images.");
        message.showAndWait(); //displays the alert
    } //makeAlert

    /**
     * Loads the images into the tile pane.
     */
    private void loadImages() {
        runNow(() -> {
            jsonResponse(); //reads the response from iTunes
            jsonParser(); //parses the response
            setProgress(0);
            for (int i = 0; i < urls.length; i++) {
                JsonObject iResult = results.get(i).getAsJsonObject(); //gets json object
                String iArt = iResult.get("artworkUrl100").getAsString(); //gets string value
                urls[i] = iArt; //assigns string value of the url to the array
            } //for
            list = Arrays.stream(urls).distinct().collect(Collectors.toList()); //distinct list
            if (list.size() > 20) {
                for (int i = 0; i < images.length; i++) {
                    if (i >= inUse.size()) {
                        inUse.add(list.get(i)); //adds items to the list if the list is less than 20
                    } else {
                        inUse.set(i, list.get(i)); //sets the images that are currently being used
                    } //if
                    images[i].updateImage(inUse.get(i)); //updates image in the image view
                    setProgress(1.0 * i / images.length); //updates the progress bar
                } //for
                setProgress(1); //updates progress
            } else {
                Platform.runLater(() -> makeAlert()); //displays the alert
            } //if
        });
    } //loadImages

    /**
     * Handles the random image replacement.
     */
    public void randomReplacement() {
        if (list.size() > 20) {
            for (int i = 20; i < list.size(); i++) {
                notInUse.add(list.get(i)); //updates the images that are not being used
            } //for
        } //if
        inUse = inUse.stream().distinct().collect(Collectors.toList()); //removes duplicates
        notInUse = notInUse.stream().distinct().collect(Collectors.toList()); //removes duplicates
        Random index = new Random();
        int idx1 = index.nextInt(notInUse.size()); //random index for notInUse
        String notInUseIm = notInUse.get(idx1); //item at notInUse random index
        int idx2 = index.nextInt(inUse.size()); //random index for inUse
        String temp = inUse.get(idx2); //item for inUse index
        inUse.set(idx2, notInUseIm); //sets inUse equla to notInUse object
        notInUse.set(idx1, temp); //sets notInUse equla to inUse object
        images[idx2].updateImage(inUse.get(idx2)); //updates the image
    } //randomReplacement

    /**
     * Encodes the search query for the url.
     */
    private void encodeSearch() {
        encoder = new Scanner(search);
        encodedSearch = encoder.next();
        while (encoder.hasNext()) { //encodes the string so it can be put in the url
            encodedSearch += "+" + encoder.next();
        } //while
    } //encodeSearch

    /**
     * Displays error if the search query is empty.
     */
    private void displayError() {
        Alert error = new Alert(AlertType.ERROR); //intializes the alert
        error.setResizable(true);
        error.setContentText("Query cannot be empty.");
        error.showAndWait(); //displays the alert
    } //displayError

    /**
     * Sets up the tool bar for the app.
     */
    private void setUpToolBar() {
        pause = new Button("Pause");
        play = new Button("Play");
        query = new TextField("rock");
        search = query.getText();
        if (search.length() == 0 || search.equals("")) {
            displayError();
        } else {
            encodeSearch(); //encodes the search query
            updateImages = new Button("Update Images");
            //adds all items to the toolbar
            toolbar = new ToolBar(pause, new Separator(Orientation.VERTICAL),
                new Label("Search Query: "), query, updateImages);
        } //if
    } //setUpToolBar

    /**
     * Sets up the progress bar for the app.
     */
    private void setUpProgressBar() {
        progressBox = new HBox();
        progressBar = new ProgressBar();
        Text itunes = new Text("Images provided courtesy of iTunes");
        setProgress(1.0); //intial progress set to 1
        progressBox.getChildren().addAll(progressBar, itunes);
    } //setUpProgressBar

    /**
     * Downloads the response from the JSON library.
     */
    private void jsonResponse() {
        try {
            sUrl = "https://itunes.apple.com/search?term="
                + encodedSearch + "&limit=50&media=music";
            URL url = new URL(sUrl); //creates URL object
            reader = new InputStreamReader(url.openStream()); //creates InputStreamReader object
            // reads the url
        } catch (MalformedURLException mue) {
            System.out.println("An unknown protocol was specified.");
        } catch (IOException ioe) {
            System.out.println("An I/O exception occured.");
        } //try
    } //jsonResponse

    /**
     * Parses the response from JSON.
     */
    private void jsonParser() {
        JsonElement je = JsonParser.parseReader(reader); //parses the response from json
        JsonObject root = je.getAsJsonObject(); //gets a json object from the response
        results = root.getAsJsonArray("results"); //gets an array of the results from json
        int numResults = results.size();
        urls = new String[numResults]; //holds the image urls
    } //jsonParser

    /**
     * Sets up the tile pane to contain the 20 images.
     */
    private void setUpPane() {
        pane = new TilePane();
        pane.setPrefColumns(5);
        pane.setMaxHeight(400);
        images = new ImageLoader[20]; //holds the images for each image view
        jsonResponse(); //reads iTunes reponse
        jsonParser(); //parses the response
        for (int i = 0; i < urls.length; i++) {
            JsonObject iResult = results.get(i).getAsJsonObject(); //gets json object of each value
            String iArt = iResult.get("artworkUrl100").getAsString(); //gets string value of each
            urls[i] = iArt; //assigns string value of the url to the array
        } //for
        list = Arrays.stream(urls).distinct().collect(Collectors.toList()); //removes duplicates
        inUse = new LinkedList<String>(); //items in the app
        notInUse = new LinkedList<String>(); //item not in the app
        for (int i = 0; i < images.length; i++) {
            inUse.add(list.get(i)); //adds images that are currently in the app
            images[i] = new ImageLoader(inUse.get(i)); //createa a custom component with each url
            pane.getChildren().addAll(images[i]); //adds component to the tile pane
        } //for
        for (int i = 20; i < list.size(); i++) {
            notInUse.add(list.get(i)); //adds images that are not currently in the app
        } //for
    } //setUpPane

    /**
     * Sets up the menu bar.
     */
    private void setUpMenuBar() {
        menuBar = new MenuBar();
        Menu file = new Menu("File");
        MenuItem exit = new MenuItem("Exit");
        // exits the program when the button is pressed
        EventHandler<ActionEvent> exitHandler = event -> System.exit(0);
        exit.setOnAction(exitHandler); //adds action to the button
        file.getItems().add(exit); //adds item to the menu
        menuBar.getMenus().add(file); //adds items to the menu bar
    } //setUpMenuBar

    /**
     * Sets up the scene of the app.
     */
    private void setUpScene() {
        setUpToolBar();
        setUpProgressBar();
        setUpMenuBar();
        setUpPane();
        //adds all items to the vbox
        vbox.getChildren().addAll(menuBar, toolbar, pane, progressBox);
        scene = new Scene(vbox); //intializes the scene
    } //setUpScene

    /**
     * Sets up event handlers for the buttons.
     */
    private void setUpHandlers() {
        EventHandler<ActionEvent> handler = event -> randomReplacement();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(2), handler); //replaces every 2 seconds
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE); //continuous action until button is pressed
        timeline.getKeyFrames().add(keyFrame);
        timeline.play(); //starts the random replacement
        //updates the images when the button is pressed
        EventHandler<ActionEvent> updateHandler = event -> {
            timeline.stop(); //stops random replacement
            search = query.getText(); //updates the search topic
            if (search.length() == 0 || search.equals("")) {
                displayError();
            } else {
                encodeSearch();
                loadImages(); //loads images into the pane
            } //if
            timeline.play(); //starts random replacement
        };
        updateImages.setOnAction(updateHandler); //adds action to the button
        EventHandler<ActionEvent> pauseHandler = event -> {
            if (pause.getText().equals("Pause")) { //if in pause mode
                timeline.stop();
                pause.setText("Play");
            } else if (pause.getText().equals("Play")) { //if in play mode
                timeline.play();
                pause.setText("Pause");
            } //if
        };
        pause.setOnAction(pauseHandler); //adds action to the button
    } //setUpHandlers

    @Override
    public void start(Stage stage) {
        setUpScene();
        setUpHandlers();
        stage.setMaxWidth(640);
        stage.setMaxHeight(640);
        stage.setTitle("GalleryApp!");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    } // start

} // GalleryApp
