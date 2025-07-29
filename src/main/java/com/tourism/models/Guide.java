package com.tourism.models;

import java.util.ArrayList;
import java.util.List;

public class Guide extends Person {
    private List<String> languages;
    private int experienceYears;
    private List<String> specializations;
    private double totalEarnings;
    private List<Booking> assignedBookings;
    private boolean isAvailable;
    private String bio;
    private String profileImagePath;

    public Guide(String username, String password, String fullName, String email, String phone,
                 List<String> languages, int experienceYears) {
        super(username, password, fullName, email, phone);

        this.languages = languages != null ? new ArrayList<>(languages) : new ArrayList<>();
        this.experienceYears = experienceYears;
        this.specializations = new ArrayList<>();
        this.totalEarnings = 0.0;
        this.assignedBookings = new ArrayList<>();
        this.isAvailable = true;
        this.bio = "Tell us something about yourself!";
        this.profileImagePath = "";
    }

    // Language methods
    public List<String> getLanguages() {
        return new ArrayList<>(languages);
    }

    public void addLanguage(String language) {
        if (language != null && !language.trim().isEmpty()) {
            this.languages.add(language.trim());
        }
    }

    public String getLanguagesString() {
        return String.join(", ", languages);
    }

    // Experience methods
    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = Math.max(0, experienceYears);
    }

    // Specialization methods
    public List<String> getSpecializations() {
        return new ArrayList<>(specializations);
    }

    public void addSpecialization(String specialization) {
        if (specialization != null && !specialization.trim().isEmpty()) {
            this.specializations.add(specialization.trim());
        }
    }

    // Earnings methods
    public double getTotalEarnings() {
        return totalEarnings;
    }

    public void addEarnings(double earnings) {
        if (earnings > 0) {
            this.totalEarnings += earnings;
        }
    }

    public void setTotalEarnings(double totalEarnings) {
        this.totalEarnings = Math.max(0, totalEarnings);
    }

    // Booking methods
    public List<Booking> getAssignedBookings() {
        return new ArrayList<>(assignedBookings);
    }

    public void assignBooking(Booking booking) {
        if (booking == null) return;

        if (this.assignedBookings == null) {
            this.assignedBookings = new ArrayList<>();
        }

        boolean alreadyAssigned = this.assignedBookings.stream()
                .anyMatch(b -> b.getBookingId() == booking.getBookingId());

        if (!alreadyAssigned) {
            this.assignedBookings.add(booking);
            double commission = booking.getTotalPrice() * 0.30;
            addEarnings(commission);
            System.out.printf("Guide %s earned $%.2f from booking %d. Total earnings: $%.2f%n",
                    getUsername(), commission, booking.getBookingId(), totalEarnings);
        }
    }

    public void removeBooking(Booking booking) {
        if (booking == null || this.assignedBookings == null) return;

        if (this.assignedBookings.remove(booking)) {
            double commission = booking.getTotalPrice() * 0.30;
            this.totalEarnings = Math.max(0, this.totalEarnings - commission);
            System.out.printf("Guide %s lost $%.2f from cancelled booking %d%n",
                    getUsername(), commission, booking.getBookingId());
        }
    }

    // Availability methods
    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }

    public boolean canTakeBooking() {
        return isAvailable && (assignedBookings == null || assignedBookings.size() < 5);
    }

    // Bio methods
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio != null ? bio : "Tell us something about yourself!";
    }

    // Profile image methods
    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath != null ? profileImagePath : "";
    }

    // Role implementation
    @Override
    public String getRole() {
        return "Guide";
    }

    // Dashboard info
    @Override
    public String getDashboardInfo() {
        return String.format(
                "Welcome Guide %s!%n" +
                        "Experience: %d years%n" +
                        "Languages: %s%n" +
                        "Total Earnings: $%.2f%n" +
                        "Assigned Bookings: %d%n" +
                        "Status: %s%n" +
                        "%s",
                getFullName(),
                experienceYears,
                getLanguagesString(),
                totalEarnings,
                assignedBookings.size(),
                isAvailable ? "Available" : "Busy",
                (bio != null && !bio.isEmpty() ? "Bio: " + bio : "")
        );
    }

    // Commission calculation
    public double calculateCommission(double bookingPrice) {
        return Math.max(0, bookingPrice) * 0.30;
    }

    @Override
    public String toString() {
        return String.format(
                "%s%n" +
                        "Languages: %s%n" +
                        "Experience: %d years%n" +
                        "Specializations: %s%n" +
                        "Total Earnings: $%.2f%n" +
                        "Available: %s%n" +
                        "Bio: %s%n" +
                        "Profile Image: %s",
                super.toString(),
                String.join(", ", languages),
                experienceYears,
                String.join(", ", specializations),
                totalEarnings,
                isAvailable ? "Yes" : "No",
                bio,
                profileImagePath.isEmpty() ? "Default" : "Custom"
        );
    }
}