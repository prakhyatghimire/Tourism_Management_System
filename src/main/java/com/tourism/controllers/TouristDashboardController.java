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
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public class TouristDashboardController {
    // Existing fields from FXML
    @FXML private Label welcomeLabel;
    @FXML private Label dashboardInfoLabel;
    @FXML private ComboBox<Attraction> attractionComboBox;
    @FXML private ComboBox<Guide> guideComboBox;
    @FXML private DatePicker trekDatePicker;
    @FXML private Label attractionPriceLabel;
    @FXML private Label festivalDiscountLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Label guideFeeLabel;
    @FXML private Button bookButton;
    @FXML private Button languageToggleButton;
    @FXML private Button logoutButton;
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, Integer> bookingIdColumn;
    @FXML private TableColumn<Booking, String> attractionColumn;
    @FXML private TableColumn<Booking, String> guideColumn;
    @FXML private TableColumn<Booking, LocalDate> dateColumn;
    @FXML private TableColumn<Booking, String> statusColumn;
    @FXML private TableColumn<Booking, Double> priceColumn;
    @FXML private Button updateBookingButton;
    @FXML private Button cancelBookingButton;

    private Tourist currentUser;
    private ObservableList<Attraction> attractions;
    private ObservableList<Guide> guides;
    private ObservableList<Booking> userBookings;

    // Constants
    private static final double GUIDE_FEE_PERCENTAGE = 0.15; // 15% guide fee

    public void setCurrentUser(Tourist user) {
        this.currentUser = user;
        initializeDashboard();
    }

    @FXML
    private void initialize() {
        setupTableColumns();
        setupEventHandlers();
        updateLanguage();
    }

    private void initializeDashboard() {
        welcomeLabel.setText(LanguageManager.getText("Welcome") + ", " + currentUser.getFullName() + "!");
        dashboardInfoLabel.setText(currentUser.getDashboardInfo());

        loadAttractions();
        loadGuides();
        loadUserBookings();
        updatePriceCalculation();
    }

    private void setupTableColumns() {
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        attractionColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAttraction().getName()));

        // FIXED: Using getFullName() instead of getName()
        guideColumn.setCellValueFactory(cellData -> {
            Guide guide = cellData.getValue().getGuide();
            String guideName = (guide != null) ? guide.getFullName() : "No Guide";
            return new javafx.beans.property.SimpleStringProperty(guideName);
        });

        dateColumn.setCellValueFactory(new PropertyValueFactory<>("trekDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
    }

    private void setupEventHandlers() {
        attractionComboBox.setOnAction(e -> updatePriceCalculation());
        guideComboBox.setOnAction(e -> updatePriceCalculation());
        trekDatePicker.setOnAction(e -> updatePriceCalculation());

        // Custom cell factory for attraction ComboBox
        attractionComboBox.setCellFactory(listView -> new ListCell<Attraction>() {
            @Override
            protected void updateItem(Attraction attraction, boolean empty) {
                super.updateItem(attraction, empty);
                if (empty || attraction == null) {
                    setText(null);
                } else {
                    setText(attraction.getName() + " - " + attraction.getAltitudeLevel() + " Altitude");
                }
            }
        });

        attractionComboBox.setButtonCell(new ListCell<Attraction>() {
            @Override
            protected void updateItem(Attraction attraction, boolean empty) {
                super.updateItem(attraction, empty);
                if (empty || attraction == null) {
                    setText(null);
                } else {
                    setText(attraction.getName());
                }
            }
        });

        // FIXED: Custom cell factory for guide ComboBox using correct methods
        guideComboBox.setCellFactory(listView -> new ListCell<Guide>() {
            @Override
            protected void updateItem(Guide guide, boolean empty) {
                super.updateItem(guide, empty);
                if (empty || guide == null) {
                    setText(null);
                } else {
                    // Using getFullName() and getSpecializations() (returns List<String>)
                    String specialization = guide.getSpecializations().isEmpty() ?
                            "General Guide" : guide.getSpecializations().get(0);
                    setText(guide.getFullName() + " (" + specialization + ")");
                }
            }
        });

        guideComboBox.setButtonCell(new ListCell<Guide>() {
            @Override
            protected void updateItem(Guide guide, boolean empty) {
                super.updateItem(guide, empty);
                if (empty || guide == null) {
                    setText(null);
                } else {
                    // Using getFullName() instead of getName()
                    setText(guide.getFullName());
                }
            }
        });
    }

    private void loadAttractions() {
        List<Attraction> attractionList = FileHandler.loadAttractions();
        attractions = FXCollections.observableArrayList(attractionList);
        attractionComboBox.setItems(attractions);
    }

    private void loadGuides() {
        List<Guide> guideList = FileHandler.loadGuides();
        guides = FXCollections.observableArrayList(guideList);
        guideComboBox.setItems(guides);
    }

    private void loadUserBookings() {
        List<Booking> allBookings = FileHandler.loadBookings();
        userBookings = FXCollections.observableArrayList();

        for (Booking booking : allBookings) {
            if (booking.getTouristUsername().equals(currentUser.getUsername())) {
                userBookings.add(booking);
                currentUser.addBooking(booking);
            }
        }

        bookingsTable.setItems(userBookings);
    }

    private void updatePriceCalculation() {
        Attraction selectedAttraction = attractionComboBox.getValue();
        Guide selectedGuide = guideComboBox.getValue();
        LocalDate selectedDate = trekDatePicker.getValue();

        if (selectedAttraction != null && selectedDate != null) {
            boolean isFestivalSeason = isFestivalSeason(selectedDate);
            double basePrice = selectedAttraction.calculatePrice(isFestivalSeason);
            double guideFee = 0.0;

            // Calculate guide fee if guide is selected
            if (selectedGuide != null) {
                guideFee = basePrice * GUIDE_FEE_PERCENTAGE;
            }

            double totalPrice = basePrice + guideFee;

            // Update price labels
            attractionPriceLabel.setText("$" + String.format("%.2f", basePrice));
            totalPriceLabel.setText("$" + String.format("%.2f", totalPrice));

            // Update guide fee label
            if (selectedGuide != null) {
                guideFeeLabel.setText("(+$" + String.format("%.2f", guideFee) + " guide fee)");
            } else {
                guideFeeLabel.setText("(no guide selected)");
            }

            // Update festival discount label
            if (isFestivalSeason) {
                festivalDiscountLabel.setText("20% Festival Discount Applied!");
                festivalDiscountLabel.setVisible(true);
            } else {
                festivalDiscountLabel.setVisible(false);
            }
        } else {
            attractionPriceLabel.setText("$0.00");
            totalPriceLabel.setText("$0.00");
            guideFeeLabel.setText("");
            festivalDiscountLabel.setVisible(false);
        }
    }

    private boolean isFestivalSeason(LocalDate date) {
        Month month = date.getMonth();
        return month == Month.AUGUST || month == Month.SEPTEMBER || month == Month.OCTOBER;
    }

    @FXML
    private void handleBooking() {
        Attraction selectedAttraction = attractionComboBox.getValue();
        Guide selectedGuide = guideComboBox.getValue();
        LocalDate selectedDate = trekDatePicker.getValue();

        if (selectedAttraction == null || selectedDate == null) {
            DialogUtils.showError("Error", "Please select attraction and date!");
            return;
        }

        if (selectedDate.isBefore(LocalDate.now())) {
            DialogUtils.showError("Error", "Cannot book for past dates!");
            return;
        }

        if (!selectedAttraction.isAvailable()) {
            DialogUtils.showError("Error", "This attraction is fully booked!");
            return;
        }

        // Check if guide is available and can take bookings (if selected)
        if (selectedGuide != null && !selectedGuide.canTakeBooking()) {
            DialogUtils.showError("Error", "Selected guide is not available or has reached maximum bookings!");
            return;
        }

        // High altitude warning
        if (selectedAttraction.isHighAltitude()) {
            Alert alert = DialogUtils.createAlert(Alert.AlertType.WARNING,
                    LanguageManager.getText("High Altitude Warning"),
                    "This trek involves high altitude. Please ensure you are physically fit and consult a doctor if you have any health concerns.");

            alert.setHeaderText("High Altitude Trek Selected!");

            ButtonType continueButton = new ButtonType("Continue Booking");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(continueButton, cancelButton);

            if (alert.showAndWait().orElse(cancelButton) == cancelButton) {
                return;
            }
        }

        // Create booking with guide

        Booking newBooking = new Booking(
                currentUser.getUsername(),
                selectedGuide,  // This can be null
                selectedAttraction,
                selectedDate
        );
        newBooking.confirmBooking();

        // Assign booking to guide if selected
        if (selectedGuide != null) {
            selectedGuide.assignBooking(newBooking);
        }

        // Show festival discount popup if applicable
        if (newBooking.isFestivalDiscountApplied()) {
            Alert festivalAlert = DialogUtils.createAlert(Alert.AlertType.INFORMATION,
                    LanguageManager.getText("Festival Discount Applied"),
                    "Congratulations! You've received a 20% discount for booking during the festival season.");

            festivalAlert.setHeaderText("🎉 Festival Discount!");
            festivalAlert.showAndWait();
        }

        // Save booking
        FileHandler.saveBooking(newBooking);
        currentUser.addBooking(newBooking);
        userBookings.add(newBooking);

        // Update dashboard
        dashboardInfoLabel.setText(currentUser.getDashboardInfo());

        String guideInfo = selectedGuide != null ?
                "\nGuide: " + selectedGuide.getFullName() : "\nNo guide selected";

        DialogUtils.showInfo("Success", "Booking confirmed!" +
                "\nBooking ID: " + newBooking.getBookingId() + guideInfo);

        // Clear selection
        attractionComboBox.setValue(null);
        guideComboBox.setValue(null);
        trekDatePicker.setValue(null);
        updatePriceCalculation();
    }

    @FXML
    private void handleUpdateBooking() {
        Booking selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();

        if (selectedBooking == null) {
            DialogUtils.showError("Error", "Please select a booking to update!");
            return;
        }

        if (!"Confirmed".equals(selectedBooking.getStatus())) {
            DialogUtils.showError("Error", "Only confirmed bookings can be updated!");
            return;
        }

        showBookingUpdateDialog(selectedBooking);
    }

    private void showBookingUpdateDialog(Booking booking) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Booking");
        dialog.setHeaderText("Update booking details for ID: " + booking.getBookingId());

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Date picker for new date
        DatePicker newDatePicker = new DatePicker(booking.getTrekDate());

        // Guide selection
        ComboBox<Guide> newGuideComboBox = new ComboBox<>();
        newGuideComboBox.setItems(guides);
        newGuideComboBox.setValue(booking.getGuide());

        // Set up guide ComboBox display
        newGuideComboBox.setCellFactory(listView -> new ListCell<Guide>() {
            @Override
            protected void updateItem(Guide guide, boolean empty) {
                super.updateItem(guide, empty);
                if (empty || guide == null) {
                    setText(null);
                } else {
                    String specialization = guide.getSpecializations().isEmpty() ?
                            "General Guide" : guide.getSpecializations().get(0);
                    setText(guide.getFullName() + " (" + specialization + ")");
                }
            }
        });

        newGuideComboBox.setButtonCell(new ListCell<Guide>() {
            @Override
            protected void updateItem(Guide guide, boolean empty) {
                super.updateItem(guide, empty);
                if (empty || guide == null) {
                    setText("No Guide");
                } else {
                    setText(guide.getFullName());
                }
            }
        });

        grid.add(new Label("New Date:"), 0, 0);
        grid.add(newDatePicker, 1, 0);
        grid.add(new Label("Guide:"), 0, 1);
        grid.add(newGuideComboBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == updateButtonType) {
                processBookingUpdate(booking, newDatePicker.getValue(), newGuideComboBox.getValue());
            }
        });
    }

    private void processBookingUpdate(Booking booking, LocalDate newDate, Guide newGuide) {
        if (newDate == null) {
            DialogUtils.showError("Error", "Please select a valid date!");
            return;
        }

        if (newDate.isBefore(LocalDate.now())) {
            DialogUtils.showError("Error", "Cannot update to a past date!");
            return;
        }

        // Check if new guide can take booking (if changed)
        if (newGuide != null && !newGuide.equals(booking.getGuide()) && !newGuide.canTakeBooking()) {
            DialogUtils.showError("Error", "Selected guide is not available or has reached maximum bookings!");
            return;
        }

        // Remove booking from old guide
        if (booking.getGuide() != null) {
            booking.getGuide().removeBooking(booking);
        }

        // Update booking
        booking.setTrekDate(newDate);
        booking.setGuide(newGuide);

        // Assign to new guide
        if (newGuide != null) {
            newGuide.assignBooking(booking);
        }

        // Recalculate price
        boolean isFestivalSeason = isFestivalSeason(newDate);
        double basePrice = booking.getAttraction().calculatePrice(isFestivalSeason);
        double guideFee = (newGuide != null) ? basePrice * GUIDE_FEE_PERCENTAGE : 0.0;
        booking.setTotalPrice(basePrice + guideFee);

        // Save updated booking
        FileHandler.saveBooking(booking);

        // Refresh table
        bookingsTable.refresh();

        String guideInfo = newGuide != null ?
                "\nNew Guide: " + newGuide.getFullName() : "\nNo guide assigned";

        DialogUtils.showInfo("Success", "Booking updated successfully!" + guideInfo);
    }

    @FXML
    private void handleCancelBooking() {
        Booking selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();

        if (selectedBooking == null) {
            DialogUtils.showError("Error", "Please select a booking to cancel!");
            return;
        }

        if ("Cancelled".equals(selectedBooking.getStatus())) {
            DialogUtils.showError("Error", "This booking is already cancelled!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Booking");
        confirmAlert.setHeaderText("Are you sure you want to cancel this booking?");
        confirmAlert.setContentText("Booking ID: " + selectedBooking.getBookingId() +
                "\nAttraction: " + selectedBooking.getAttraction().getName());

        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // Remove from guide if assigned
                if (selectedBooking.getGuide() != null) {
                    selectedBooking.getGuide().removeBooking(selectedBooking);
                }

                selectedBooking.cancelBooking();
                FileHandler.saveBooking(selectedBooking);

                // Update dashboard
                dashboardInfoLabel.setText(currentUser.getDashboardInfo());

                // Refresh table
                bookingsTable.refresh();

                DialogUtils.showInfo("Success", "Booking cancelled successfully!");
            }
        });
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
            e.printStackTrace();
        }
    }

    private void updateLanguage() {
        bookButton.setText(LanguageManager.getText("Book Now"));
        updateBookingButton.setText(LanguageManager.getText("Update"));
        cancelBookingButton.setText(LanguageManager.getText("Cancel"));
        logoutButton.setText(LanguageManager.getText("Logout"));
        languageToggleButton.setText(LanguageManager.getCurrentLanguage());
    }
}