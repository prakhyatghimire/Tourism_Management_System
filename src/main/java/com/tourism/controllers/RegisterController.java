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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegisterController {
    // Form fields
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> roleComboBox;

    // Role-specific sections
    @FXML private VBox roleSpecificSection;
    @FXML private Label roleSpecificLabel;
    @FXML private VBox touristFields;
    @FXML private VBox guideFields;

    // Tourist fields
    @FXML private TextField nationalityField;
    @FXML private Label nationalityLabel;

    // Guide fields
    @FXML private TextField languagesField;
    @FXML private TextField experienceField;
    @FXML private Label languagesLabel;
    @FXML private Label experienceLabel;

    // Buttons
    @FXML private Button registerButton;
    @FXML private Button backButton;

    // Error labels
    @FXML private Label usernameError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label fullNameError;
    @FXML private Label emailError;
    @FXML private Label phoneError;
    @FXML private Label roleError;
    @FXML private Label nationalityError;
    @FXML private Label languagesError;
    @FXML private Label experienceError;

    // Validation patterns
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z ]{3,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{7,15}$");
    private static final Pattern NATIONALITY_PATTERN = Pattern.compile("^[a-zA-Z ]{3,50}$");

    @FXML
    private void initialize() {
        // Initialize role combo box
        roleComboBox.getItems().addAll("Tourist", "Guide");
        roleComboBox.setOnAction(e -> {
            toggleRoleFields();
            validateRole();
        });

        // Set prompt text
        languagesField.setPromptText("English, Nepali, Hindi, etc.");

        // Hide role-specific section initially
        roleSpecificSection.setVisible(false);
        roleSpecificSection.setManaged(false);

        // Set up field validation
        setupFieldValidation();

        // Initialize all error labels as invisible
        initializeErrorLabels();

        // Set up back button action
        backButton.setOnAction(e -> handleBack());

        // Update language texts
        updateLanguage();
    }

    private void initializeErrorLabels() {
        usernameError.setVisible(false);
        passwordError.setVisible(false);
        confirmPasswordError.setVisible(false);
        fullNameError.setVisible(false);
        emailError.setVisible(false);
        phoneError.setVisible(false);
        roleError.setVisible(false);
        nationalityError.setVisible(false);
        languagesError.setVisible(false);
        experienceError.setVisible(false);
    }

    private void setupFieldValidation() {
        // Add focus listeners to all fields
        addFocusValidation(usernameField, this::validateUsername);
        addFocusValidation(passwordField, this::validatePassword);
        addFocusValidation(confirmPasswordField, this::validateConfirmPassword);
        addFocusValidation(fullNameField, this::validateFullName);
        addFocusValidation(emailField, this::validateEmail);
        addFocusValidation(phoneField, this::validatePhone);
        addFocusValidation(nationalityField, this::validateNationality);
        addFocusValidation(languagesField, this::validateLanguages);
        addFocusValidation(experienceField, this::validateExperience);

        // Real-time validation for password match
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateConfirmPassword();
        });
    }

    private void addFocusValidation(Control field, Runnable validationMethod) {
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // When focus is lost
                validationMethod.run();
            }
        });
    }

    // Field validation methods
    public void validateUsername() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showError(usernameField, usernameError, "Username is required");
        } else if (!USERNAME_PATTERN.matcher(username).matches()) {
            showError(usernameField, usernameError, "3-20 chars (letters, numbers, _)");
        } else if (isUsernameExists(username)) {
            showError(usernameField, usernameError, "Username already exists");
        } else {
            clearError(usernameField, usernameError);
        }
        updateRegisterButtonState();
    }

    public void validatePassword() {
        String password = passwordField.getText().trim();
        if (password.isEmpty()) {
            showError(passwordField, passwordError, "Password is required");
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            showError(passwordField, passwordError, "Needs uppercase, lowercase, number & special char");
        } else {
            clearError(passwordField, passwordError);
        }
        updateRegisterButtonState();
    }

    public void validateConfirmPassword() {
        String confirm = confirmPasswordField.getText().trim();
        if (confirm.isEmpty()) {
            clearError(confirmPasswordField, confirmPasswordError);
        } else if (!confirm.equals(passwordField.getText().trim())) {
            showError(confirmPasswordField, confirmPasswordError, "Passwords don't match");
        } else {
            clearError(confirmPasswordField, confirmPasswordError);
        }
        updateRegisterButtonState();
    }

    public void validateFullName() {
        String name = fullNameField.getText().trim();
        if (name.isEmpty()) {
            showError(fullNameField, fullNameError, "Full name is required");
        } else if (!NAME_PATTERN.matcher(name).matches()) {
            showError(fullNameField, fullNameError, "3-50 alphabetic characters");
        } else {
            clearError(fullNameField, fullNameError);
        }
        updateRegisterButtonState();
    }

    public void validateEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showError(emailField, emailError, "Email is required");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError(emailField, emailError, "Invalid email format");
        } else {
            clearError(emailField, emailError);
        }
        updateRegisterButtonState();
    }

    public void validatePhone() {
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            showError(phoneField, phoneError, "Phone is required");
        } else if (!PHONE_PATTERN.matcher(phone).matches()) {
            showError(phoneField, phoneError, "7-15 digits only");
        } else {
            clearError(phoneField, phoneError);
        }
        updateRegisterButtonState();
    }

    public void validateRole() {
        if (roleComboBox.getValue() == null) {
            showError(roleComboBox, roleError, "Please select a role");
        } else {
            clearError(roleComboBox, roleError);
        }
        updateRegisterButtonState();
    }

    public void validateNationality() {
        String nationality = nationalityField.getText().trim();
        if (nationality.isEmpty()) {
            showError(nationalityField, nationalityError, "Nationality is required");
        } else if (!NATIONALITY_PATTERN.matcher(nationality).matches()) {
            showError(nationalityField, nationalityError, "3-50 alphabetic characters");
        } else {
            clearError(nationalityField, nationalityError);
        }
        updateRegisterButtonState();
    }

    public void validateLanguages() {
        List<String> languages = parseLanguagesInput(languagesField.getText().trim());
        if (languages.isEmpty()) {
            showError(languagesField, languagesError, "At least one language required");
        } else {
            clearError(languagesField, languagesError);
        }
        updateRegisterButtonState();
    }

    public void validateExperience() {
        try {
            String expText = experienceField.getText().trim();
            if (expText.isEmpty()) {
                showError(experienceField, experienceError, "Experience is required");
                return;
            }

            int exp = Integer.parseInt(expText);
            if (exp < 0 || exp > 50) {
                showError(experienceField, experienceError, "Must be 0-50 years");
            } else {
                clearError(experienceField, experienceError);
            }
        } catch (NumberFormatException e) {
            showError(experienceField, experienceError, "Must be a number");
        }
        updateRegisterButtonState();
    }

    private void showError(Control field, Label errorLabel, String message) {
        field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 1;");
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void clearError(Control field, Label errorLabel) {
        field.setStyle("");
        errorLabel.setVisible(false);
    }

    private void updateRegisterButtonState() {
        registerButton.setDisable(!validateFieldsSilent());
    }

    private boolean validateFieldsSilent() {
        // Basic fields
        if (usernameField.getText().trim().isEmpty() ||
                !USERNAME_PATTERN.matcher(usernameField.getText().trim()).matches() ||
                isUsernameExists(usernameField.getText().trim())) {
            return false;
        }

        if (passwordField.getText().trim().isEmpty() ||
                !PASSWORD_PATTERN.matcher(passwordField.getText().trim()).matches()) {
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            return false;
        }

        if (fullNameField.getText().trim().isEmpty() ||
                !NAME_PATTERN.matcher(fullNameField.getText().trim()).matches()) {
            return false;
        }

        if (emailField.getText().trim().isEmpty() ||
                !EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) {
            return false;
        }

        if (phoneField.getText().trim().isEmpty() ||
                !PHONE_PATTERN.matcher(phoneField.getText().trim()).matches()) {
            return false;
        }

        // Role validation
        if (roleComboBox.getValue() == null) {
            return false;
        }

        if ("Tourist".equals(roleComboBox.getValue())) {
            if (nationalityField.getText().trim().isEmpty() ||
                    !NATIONALITY_PATTERN.matcher(nationalityField.getText().trim()).matches()) {
                return false;
            }
        } else if ("Guide".equals(roleComboBox.getValue())) {
            List<String> languages = parseLanguagesInput(languagesField.getText().trim());
            if (languages.isEmpty()) {
                return false;
            }

            try {
                int exp = Integer.parseInt(experienceField.getText().trim());
                if (exp < 0 || exp > 50) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
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
            clearError(nationalityField, nationalityError);
        } else if (isGuide) {
            languagesField.clear();
            experienceField.clear();
            clearError(languagesField, languagesError);
            clearError(experienceField, experienceError);
        }

        roleSpecificLabel.setText(isTourist ? "Tourist Information" : "Guide Information");
    }

    @FXML
    private void handleRegister() {
        try {
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
            if ("Tourist".equals(role)) {
                Tourist tourist = new Tourist(username, password, fullName, email, phone,
                        nationalityField.getText().trim());
                if (!FileHandler.saveTourist(tourist)) {
                    DialogUtils.showError("Error", "Failed to save tourist data");
                    return;
                }
            } else {
                List<String> selectedLanguages = parseLanguagesInput(languagesField.getText().trim());
                int experience = Integer.parseInt(experienceField.getText().trim());

                Guide guide = new Guide(username, password, fullName, email, phone,
                        selectedLanguages, experience);
                if (!FileHandler.saveGuide(guide)) {
                    DialogUtils.showError("Error", "Failed to save guide data");
                    return;
                }
            }

// Verify the user was actually saved
            boolean userExists = FileHandler.loadTourists().stream()
                    .anyMatch(t -> t.getUsername().equals(username)) ||
                    FileHandler.loadGuides().stream()
                            .anyMatch(g -> g.getUsername().equals(username));

            if (!userExists) {
                DialogUtils.showError("Error", "User registration failed - data not persisted");
                return;
            }

            DialogUtils.showInfo("Success", "Registration successful!");
            handleBack();
        } catch (Exception e) {
            DialogUtils.showError("Error", "Registration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        boolean isValid = validateFieldsSilent();

        if (!isValid) {
            // Force validation of all fields to show errors
            validateUsername();
            validatePassword();
            validateConfirmPassword();
            validateFullName();
            validateEmail();
            validatePhone();
            validateRole();

            if (roleComboBox.getValue() != null) {
                if ("Tourist".equals(roleComboBox.getValue())) {
                    validateNationality();
                } else if ("Guide".equals(roleComboBox.getValue())) {
                    validateLanguages();
                    validateExperience();
                }
            }
        }

        return isValid;
    }

    private List<String> parseLanguagesInput(String input) {
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(lang -> !lang.isEmpty())
                .collect(Collectors.toList());
    }

    private boolean isUsernameExists(String username) {
        return FileHandler.loadTourists().stream().anyMatch(t -> t.getUsername().equals(username)) ||
                FileHandler.loadGuides().stream().anyMatch(g -> g.getUsername().equals(username)) ||
                "admin".equals(username);
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene loginScene = new Scene(loader.load());
            Main.switchScene(loginScene, "Himalayan Legacy - Login");
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to load login screen: " + e.getMessage());
            e.printStackTrace();
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