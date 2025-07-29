package com.tourism;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;
import com.tourism.utils.FileHandler;

public class Main extends Application {
    
    public static Stage primaryStage; // Make it accessible globally
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.primaryStage = primaryStage;
        
        // Initialize data files
        FileHandler.initializeDataFiles();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 1920, 1080);
        
        primaryStage.setTitle("Journey - Nepal Tourism System");
        primaryStage.setScene(scene);
        
        // Disable resizing to maintain full screen
        primaryStage.setResizable(false);
        
        // Set to full screen immediately
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint(""); // Remove exit hint
        
        // Disable all exit combinations
        primaryStage.setFullScreenExitKeyCombination(KeyCodeCombination.NO_MATCH);
        
        // Ensure it stays full screen
        primaryStage.fullScreenProperty().addListener((obs, wasFullScreen, isFullScreen) -> {
            if (!isFullScreen) {
                primaryStage.setFullScreen(true);
            }
        });
        
        // Only allow ESC to minimize (not exit full screen)
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.setIconified(true); // Minimize instead of exit full screen
            }
        });
        
        primaryStage.show();
        
        // Force full screen after showing
        primaryStage.setFullScreen(true);
    }
    
    // Utility method to switch scenes while maintaining full screen
    public static void switchScene(Scene newScene, String title) {
        if (primaryStage != null) {
            primaryStage.setScene(newScene);
            primaryStage.setTitle(title);
            primaryStage.setFullScreen(true); // Ensure full screen is maintained
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
