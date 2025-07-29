package com.tourism.controllers;

import com.tourism.Main;
import com.tourism.models.*;
import com.tourism.utils.FileHandler;
import com.tourism.utils.LanguageManager;
import com.tourism.utils.DialogUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.util.List;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button languageToggleButton;
    @FXML private Button exitButton;
    @FXML private Label titleLabel;
    @FXML private Label usernameLabel;
    @FXML private Label passwordLabel;

    
    @FXML
    private void initialize() {

    updateLanguage();
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            DialogUtils.showError("Error", "Please fill in all fields!");
            return;
        }
        
        // Check admin login (hardcoded)
        if ("admin".equals(username) && "123".equals(password)) {
            Admin admin = new Admin(username, password, "admin", "admin@tourism.com", "9808829472");
            openDashboard(admin);
            return;
        }
        
        // Check tourist login
        List<Tourist> tourists = FileHandler.loadTourists();
        for (Tourist tourist : tourists) {
            if (tourist.getUsername().equals(username) && tourist.getPassword().equals(password)) {
                openDashboard(tourist);
                return;
            }
        }
        
        // Check guide login
        List<Guide> guides = FileHandler.loadGuides();
        for (Guide guide : guides) {
            if (guide.getUsername().equals(username) && guide.getPassword().equals(password)) {
                openDashboard(guide);
                return;
            }
        }
        
        DialogUtils.showError("Error", "Invalid username or password!");
    }
    
    private void openDashboard(Person user) {
        try {
            String fxmlFile = "";
            String title = "";
        
            System.out.println("Opening dashboard for user: " + user.getFullName() + " (Role: " + user.getRole() + ")");
        
            // Polymorphism in action - using the same method for different user types
            switch (user.getRole()) {
                case "Tourist":
                    fxmlFile = "/fxml/touristDashboard.fxml";
                    title = "Journey - Tourist Dashboard";
                    break;
                case "Guide":
                    fxmlFile = "/fxml/guideDashboard.fxml";
                    title = "Journey - Guide Dashboard";
                    break;
                case "Admin":
                    fxmlFile = "/fxml/adminDashboard.fxml";
                    title = "Journey - Admin Dashboard";
                    break;
                default:
                    DialogUtils.showError("Error", "Unknown user role: " + user.getRole());
                    return;
            }
        
            System.out.println("Loading FXML file: " + fxmlFile);
        
            // Check if resource exists
            if (getClass().getResource(fxmlFile) == null) {
                System.err.println("FXML file not found: " + fxmlFile);
                DialogUtils.showError("Error", "Dashboard file not found: " + fxmlFile);
                return;
            }
        
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());
        
            // Get the controller and set user data
            Object controller = loader.getController();
            System.out.println("Controller loaded: " + (controller != null ? controller.getClass().getSimpleName() : "null"));
        
            if (controller != null) {
                if (user instanceof Tourist && controller instanceof TouristDashboardController) {
                    ((TouristDashboardController) controller).setCurrentUser((Tourist) user);
                } else if (user instanceof Guide && controller instanceof GuideDashboardController) {
                    System.out.println("Setting Guide user in GuideDashboardController");
                    ((GuideDashboardController) controller).setCurrentUser((Guide) user);
                } else if (user instanceof Admin && controller instanceof AdminDashboardController) {
                    ((AdminDashboardController) controller).setCurrentUser((Admin) user);
                } else {
                    System.err.println("Controller type mismatch: " + controller.getClass() + " for user " + user.getClass());
                }
            } else {
                System.err.println("Controller is null for " + fxmlFile);
            }
        
            // Use the new scene switching method to maintain full screen
            Main.switchScene(scene, title);
        
            System.out.println("Dashboard loaded successfully for " + user.getRole());
        
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtils.showError("Error", "Failed to open dashboard: " + e.getMessage());
            System.err.println("Dashboard loading error details:");
            System.err.println("User: " + user.getClass().getSimpleName());
            System.err.println("Role: " + user.getRole());
            System.err.println("Error: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Scene scene = new Scene(loader.load());
            
            // Use the new scene switching method to maintain full screen
            Main.switchScene(scene, "Himalayan Legacy - Create Account");
            
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtils.showError("Error", "Failed to open registration form!");
        }
    }
    
    @FXML
    private void handleExit() {
        // Show confirmation dialog before exiting
        if (DialogUtils.showConfirmation("Exit Application", 
            "Are you sure you want to exit?\nThis will close the Journey application.")) {
            Platform.exit();
            System.exit(0);
        }
    }
    
    @FXML
    private void toggleLanguage() {
        LanguageManager.toggleLanguage();
        updateLanguage();
    }
    
    private void updateLanguage() {
        titleLabel.setText("Himalayan Legacy");
        usernameLabel.setText(LanguageManager.getText("Username"));
        passwordLabel.setText(LanguageManager.getText("Password"));
        loginButton.setText(LanguageManager.getText("Login"));
        registerButton.setText(LanguageManager.getText("Register"));
        languageToggleButton.setText(LanguageManager.getCurrentLanguage());
        exitButton.setText(LanguageManager.getText("Exit"));
    }
    
    private void showAlert(String title, String message) {
        DialogUtils.showInfo(title, message);
    }
}
