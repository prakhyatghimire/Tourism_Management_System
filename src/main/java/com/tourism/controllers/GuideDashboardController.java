package com.tourism.controllers;

import com.tourism.Main;
import com.tourism.models.*;
import com.tourism.utils.DialogUtils;
import com.tourism.utils.FileHandler;
import com.tourism.utils.LanguageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class GuideDashboardController implements Initializable {
    // UI Components
    @FXML private Label welcomeLabel;
    @FXML private Label dashboardInfoLabel;
    @FXML private Label earningsLabel;
    @FXML private Label languagesLabel;
    @FXML private Label experienceLabel;
    @FXML private TableView<Booking> upcomingTreksTable;
    @FXML private TableColumn<Booking, Integer> bookingIdColumn;
    @FXML private TableColumn<Booking, String> touristColumn;
    @FXML private TableColumn<Booking, String> attractionColumn;
    @FXML private TableColumn<Booking, LocalDate> dateColumn;
    @FXML private TableColumn<Booking, String> difficultyColumn;
    @FXML private TextArea updatesTextArea;
    @FXML private Button languageToggleButton;
    @FXML private Button logoutButton;
    @FXML private Button refreshButton;
    @FXML private ImageView profileImageView;
    @FXML private Button changeProfilePictureButton;
    @FXML private TextArea bioTextArea;
    @FXML private Button editBioButton;
    @FXML private Button saveBioButton;
    @FXML private Button cancelBioButton;
    @FXML private Button deleteBioButton;

    // Data fields
    private Guide currentUser;
    private ObservableList<Booking> assignedBookings;
    private String originalBio;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadImportantUpdates();
        updateLanguage();
        initializeUIComponents();
    }

    public void setCurrentUser(Guide user) {
        this.currentUser = user;
        initializeDashboard();
    }

    private void initializeUIComponents() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome Guide!");
        }
        if (dashboardInfoLabel != null) {
            dashboardInfoLabel.setText("Loading guide information...");
        }
        if (earningsLabel != null) {
            earningsLabel.setText("Total Earnings: $0.00");
        }
        if (languagesLabel != null) {
            languagesLabel.setText("Languages: Loading...");
        }
        if (experienceLabel != null) {
            experienceLabel.setText("Experience: Loading...");
        }
        if (saveBioButton != null) saveBioButton.setVisible(false);
        if (cancelBioButton != null) cancelBioButton.setVisible(false);
    }

    private void initializeDashboard() {
        try {
            if (currentUser == null) return;

            // Load latest guide data
            List<Guide> allGuides = FileHandler.loadGuides();
            Guide latestGuideData = allGuides.stream()
                    .filter(g -> g.getUsername().equals(currentUser.getUsername()))
                    .findFirst()
                    .orElse(currentUser); // Fallback to current user if not found

            currentUser = latestGuideData; // Update with latest data

            // Update UI with user info
            if (welcomeLabel != null) {
                welcomeLabel.setText(LanguageManager.getText("Welcome") + ", " + currentUser.getFullName() + "!");
            }

            if (dashboardInfoLabel != null) {
                dashboardInfoLabel.setText(currentUser.getDashboardInfo());
            }

            if (earningsLabel != null) {
                earningsLabel.setText("Total Earnings: $" + String.format("%.2f", currentUser.getTotalEarnings()));
            }

            if (languagesLabel != null) {
                languagesLabel.setText("Languages: " + currentUser.getLanguagesString());
            }

            if (experienceLabel != null) {
                experienceLabel.setText("Experience: " + currentUser.getExperienceYears() + " years");
            }

            // Load profile picture if exists
            if (currentUser.getProfileImagePath() != null && !currentUser.getProfileImagePath().isEmpty()) {
                try {
                    File file = new File(currentUser.getProfileImagePath());
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString());
                        profileImageView.setImage(image);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading profile image: " + e.getMessage());
                }
            }

            // Load data
            loadAssignedBookings();
            loadBio();

        } catch (Exception e) {
            System.err.println("Error initializing guide dashboard: " + e.getMessage());
            DialogUtils.showError("Error", "Failed to load guide dashboard");
        }
    }

    private void setupTableColumns() {
        try {
            bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
            touristColumn.setCellValueFactory(new PropertyValueFactory<>("touristUsername"));
            attractionColumn.setCellValueFactory(cellData -> {
                Attraction attraction = cellData.getValue().getAttraction();
                return new javafx.beans.property.SimpleStringProperty(
                        attraction != null ? attraction.getName() : "N/A");
            });
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("trekDate"));
            difficultyColumn.setCellValueFactory(cellData -> {
                Attraction attraction = cellData.getValue().getAttraction();
                return new javafx.beans.property.SimpleStringProperty(
                        attraction != null ? attraction.getDifficulty() : "N/A");
            });
        } catch (Exception e) {
            System.err.println("Error setting up table columns: " + e.getMessage());
        }
    }

    private void loadAssignedBookings() {
        try {
            if (currentUser == null) return;

            List<Booking> allBookings = FileHandler.loadBookings();
            assignedBookings = FXCollections.observableArrayList();

            for (Booking booking : allBookings) {
                if (currentUser.getUsername().equals(booking.getGuideUsername())) {
                    assignedBookings.add(booking);
                }
            }

            upcomingTreksTable.setItems(assignedBookings);

            // Update UI
            if (earningsLabel != null) {
                earningsLabel.setText("Total Earnings: $" +
                        String.format("%.2f", currentUser.getTotalEarnings()));
            }

        } catch (Exception e) {
            System.err.println("Error loading assigned bookings: " + e.getMessage());
        }
    }

    private void loadBio() {
        if (currentUser == null || bioTextArea == null) return;

        String bio = currentUser.getBio();
        if (bio == null || bio.isBlank()) {
            bio = "Tell us something about yourself!";
        }
        bioTextArea.setText(bio);
        bioTextArea.setEditable(false);
        originalBio = bio;

        if (saveBioButton != null) saveBioButton.setVisible(false);
        if (cancelBioButton != null) cancelBioButton.setVisible(false);
    }

    private void loadImportantUpdates() {
        try {
            StringBuilder updates = new StringBuilder();
            updates.append("🌤️ WEATHER :\n");
            updates.append("• Clear skies expected across trekking regions\n");
            updates.append("• Mild snowfall forecast above 4,500m\n\n");

            updates.append("⚠️ SAFETY GUIDELINES:\n");
            updates.append("• Carry altitude sickness medication\n");
            updates.append("• Check-in twice daily\n\n");

            updates.append("📢 GUIDE ANNOUNCEMENTS:\n");
            updates.append("• Certification renewal deadline approaching\n");

            updatesTextArea.setText(updates.toString());
            updatesTextArea.setEditable(false);
        } catch (Exception e) {
            System.err.println("Error loading updates: " + e.getMessage());
        }
    }

    // ===== Event Handlers =====
    @FXML
    private void handleEditBio() {
        bioTextArea.setEditable(true);
        saveBioButton.setVisible(true);
        cancelBioButton.setVisible(true);
    }

    @FXML
    private void handleSaveBio() {
        currentUser.setBio(bioTextArea.getText());
        bioTextArea.setEditable(false);
        saveBioButton.setVisible(false);
        cancelBioButton.setVisible(false);

        // Save the updated guide
        List<Guide> guides = FileHandler.loadGuides();
        guides.removeIf(g -> g.getUsername().equals(currentUser.getUsername()));
        guides.add(currentUser);
        FileHandler.saveAllGuides(guides);

        DialogUtils.showInfo("Success", "Bio updated successfully!");
    }

    @FXML
    private void handleCancelBio() {
        bioTextArea.setText(originalBio);
        bioTextArea.setEditable(false);
        saveBioButton.setVisible(false);
        cancelBioButton.setVisible(false);
    }

    @FXML
    private void handleDeleteBio() {
        bioTextArea.setText("Tell us something about yourself!");
        currentUser.setBio("");

        // Save the updated guide
        List<Guide> guides = FileHandler.loadGuides();
        guides.removeIf(g -> g.getUsername().equals(currentUser.getUsername()));
        guides.add(currentUser);
        FileHandler.saveAllGuides(guides);
    }

    @FXML
    private void handleChangeProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                profileImageView.setImage(image);
                currentUser.setProfileImagePath(file.getAbsolutePath());

                // Save the updated guide
                List<Guide> guides = FileHandler.loadGuides();
                guides.removeIf(g -> g.getUsername().equals(currentUser.getUsername()));
                guides.add(currentUser);
                FileHandler.saveAllGuides(guides);

                DialogUtils.showInfo("Success", "Profile picture updated!");
            } catch (Exception e) {
                DialogUtils.showError("Error", "Failed to update profile picture");
            }
        }
    }

    @FXML
    private void handleRefresh() {
        initializeDashboard();
        DialogUtils.showInfo("Refreshed", "Dashboard data updated");
    }

    @FXML
    private void toggleLanguage() {
        LanguageManager.toggleLanguage();
        updateLanguage();
        initializeDashboard();
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load());
            Main.switchScene(scene, "Journey - Nepal Tourism System");
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to logout");
        }
    }

    private void updateLanguage() {
        if (refreshButton != null) refreshButton.setText(LanguageManager.getText("Refresh"));
        if (logoutButton != null) logoutButton.setText(LanguageManager.getText("Logout"));
        if (languageToggleButton != null) {
            languageToggleButton.setText(LanguageManager.getCurrentLanguage());
        }
    }
}