package com.tourism.controllers;

import com.tourism.Main;
import com.tourism.models.*;
import com.tourism.utils.DialogUtils;
import com.tourism.utils.FileHandler;
import com.tourism.utils.LanguageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> roleComboBox;

    @FXML private VBox roleSpecificSection;
    @FXML private Label roleSpecificLabel;
    @FXML private VBox touristFields;
    @FXML private VBox guideFields;

    @FXML private TextField nationalityField;
    @FXML private Label nationalityLabel;

    @FXML private TextField languagesField;  // Changed from CheckBox container to TextField
    @FXML private TextField experienceField;
    @FXML private Label languagesLabel;
    @FXML private Label experienceLabel;

    @FXML private Button registerButton;
    @FXML private Button backButton;

    @FXML
    private void initialize() {
        roleComboBox.getItems().addAll("Tourist", "Guide");
        roleComboBox.setOnAction(e -> toggleRoleFields());

        // Set prompt text for languages field
        languagesField.setPromptText("English, Nepali, Hindi, etc.");

        roleSpecificSection.setVisible(false);
        roleSpecificSection.setManaged(false);

        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                if (!newVal.equals(passwordField.getText())) {
                    confirmPasswordField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 1;");
                    registerButton.setDisable(true);
                } else {
                    confirmPasswordField.setStyle("-fx-border-color: #2ecc71; -fx-border-width: 1;");
                    registerButton.setDisable(false);
                }
            } else {
                confirmPasswordField.setStyle("");
                registerButton.setDisable(false);
            }
        });

        updateLanguage();
    }

    private void toggleRoleFields() {
        String selectedRole = roleComboBox.getValue();
        if (selectedRole == null) {
            roleSpecificSection.setVisible(false);
            roleSpecificSection.setManaged(false);
            return;
        }

        roleSpecificSection.setVisible(true);
        roleSpecificSection.setManaged(true);

        boolean isTourist = "Tourist".equals(selectedRole);
        boolean isGuide = "Guide".equals(selectedRole);

        touristFields.setVisible(isTourist);
        touristFields.setManaged(isTourist);
        guideFields.setVisible(isGuide);
        guideFields.setManaged(isGuide);

        if (isTourist) {
            nationalityField.clear();
        } else if (isGuide) {
            languagesField.clear();
            experienceField.clear();
        }

        roleSpecificLabel.setText(isTourist ? "Tourist Information" : "Guide Information");
    }

    @FXML
    private void handleRegister() {
        if (!validateFields()) return;

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String role = roleComboBox.getValue();

        if (isUsernameExists(username)) {
            DialogUtils.showError("Error", "Username already exists!");
            return;
        }

        try {
            if ("Tourist".equals(role)) {
                Tourist tourist = new Tourist(username, password, fullName, email, phone,
                        nationalityField.getText().trim());
                FileHandler.saveTourist(tourist);
            } else {
                List<String> selectedLanguages = parseLanguagesInput(languagesField.getText().trim());
                int experience = Integer.parseInt(experienceField.getText().trim());

                Guide guide = new Guide(username, password, fullName, email, phone,
                        selectedLanguages, experience);
                FileHandler.saveGuide(guide);
            }

            DialogUtils.showInfo("Success", "Registration successful!");
            handleBack();
        } catch (Exception e) {
            DialogUtils.showError("Error", "Registration failed: " + e.getMessage());
        }
    }

    private List<String> parseLanguagesInput(String input) {
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(lang -> !lang.isEmpty())
                .collect(Collectors.toList());
    }

    private boolean validateFields() {
        if (usernameField.getText().trim().isEmpty() ||
                passwordField.getText().trim().isEmpty() ||
                confirmPasswordField.getText().trim().isEmpty() ||
                fullNameField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                phoneField.getText().trim().isEmpty() ||
                roleComboBox.getValue() == null) {

            DialogUtils.showError("Error", "Please fill all required fields!");
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            DialogUtils.showError("Password Error", "Passwords don't match!");
            return false;
        }

        if (usernameField.getText().trim().length() < 3) {
            DialogUtils.showError("Error", "Username too short (min 3 chars)!");
            return false;
        }

        if (passwordField.getText().trim().length() < 3) {
            DialogUtils.showError("Error", "Password too short (min 3 chars)!");
            return false;
        }

        if (!emailField.getText().trim().matches(".+@.+\\..+")) {
            DialogUtils.showError("Error", "Invalid email format!");
            return false;
        }

        String role = roleComboBox.getValue();
        if ("Tourist".equals(role) && nationalityField.getText().trim().isEmpty()) {
            DialogUtils.showError("Error", "Nationality required!");
            return false;
        }

        if ("Guide".equals(role)) {
            List<String> selectedLanguages = parseLanguagesInput(languagesField.getText().trim());
            if (selectedLanguages.isEmpty()) {
                DialogUtils.showError("Error", "Please enter at least one language!");
                return false;
            }
            try {
                int exp = Integer.parseInt(experienceField.getText().trim());
                if (exp < 0 || exp > 50) {
                    DialogUtils.showError("Error", "Experience must be 0-50 years!");
                    return false;
                }
            } catch (NumberFormatException e) {
                DialogUtils.showError("Error", "Invalid experience format!");
                return false;
            }
        }

        return true;
    }

    private boolean isUsernameExists(String username) {
        return FileHandler.loadTourists().stream().anyMatch(t -> t.getUsername().equals(username)) ||
                FileHandler.loadGuides().stream().anyMatch(g -> g.getUsername().equals(username)) ||
                "admin".equals(username);
    }

    @FXML
    private void handleBack() {
        try {
            Main.switchScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/login.fxml"))),
                    "Himalayan Legacy - Login");
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to load login screen");
        }
    }

    private void updateLanguage() {
        registerButton.setText(LanguageManager.getText("Register"));
        backButton.setText(LanguageManager.getText("Back"));
        nationalityLabel.setText(LanguageManager.getText("Nationality"));
        languagesLabel.setText(LanguageManager.getText("Languages"));
        experienceLabel.setText(LanguageManager.getText("Experience"));
    }
}