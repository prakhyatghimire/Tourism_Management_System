package com.tourism.models;

import java.time.LocalDate;
import java.time.Month;

public class Booking {
    private static int nextId = 1;

    private int bookingId;
    private String touristUsername;
    private Guide guide;  // Changed from guideUsername to Guide object
    private Attraction attraction;
    private LocalDate bookingDate;
    private LocalDate trekDate;
    private String status; // "Confirmed", "Pending", "Cancelled", "Completed"
    private double totalPrice;
    private boolean festivalDiscountApplied;
    private String notes;

    // Constructors
    public Booking(String touristUsername, Attraction attraction, LocalDate trekDate) {
        this(touristUsername, null, attraction, trekDate);
    }

    public Booking(String touristUsername, Guide guide, Attraction attraction, LocalDate trekDate) {
        this.bookingId = nextId++;
        this.touristUsername = touristUsername;
        this.guide = guide;
        this.attraction = attraction;
        this.bookingDate = LocalDate.now();
        this.trekDate = trekDate;
        this.status = "Pending";
        this.notes = "";

        this.festivalDiscountApplied = isFestivalSeason(trekDate);
        this.totalPrice = calculateTotalPrice();
    }

    // Price calculation
    private double calculateTotalPrice() {
        double basePrice = attraction.calculatePrice(festivalDiscountApplied);
        if (guide != null) {
            basePrice += basePrice * 0.30; // 30% guide commission
        }
        return basePrice;
    }

    // Getters and Setters
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public String getTouristUsername() { return touristUsername; }
    public void setTouristUsername(String touristUsername) {
        this.touristUsername = touristUsername;
    }

    public Guide getGuide() { return guide; }
    public void setGuide(Guide guide) {
        this.guide = guide;
        this.totalPrice = calculateTotalPrice(); // Recalculate price
    }

    // Maintain backward compatibility for username-based access
    public String getGuideUsername() {
        return guide != null ? guide.getUsername() : "";
    }

    public Attraction getAttraction() { return attraction; }
    public void setAttraction(Attraction attraction) {
        this.attraction = attraction;
        this.totalPrice = calculateTotalPrice(); // Recalculate price
    }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public LocalDate getTrekDate() { return trekDate; }
    public void setTrekDate(LocalDate trekDate) {
        this.trekDate = trekDate;
        this.festivalDiscountApplied = isFestivalSeason(trekDate);
        this.totalPrice = calculateTotalPrice(); // Recalculate price
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public boolean isFestivalDiscountApplied() { return festivalDiscountApplied; }
    public void setFestivalDiscountApplied(boolean festivalDiscountApplied) {
        this.festivalDiscountApplied = festivalDiscountApplied;
        this.totalPrice = calculateTotalPrice(); // Recalculate price
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Business Logic Methods
    private boolean isFestivalSeason(LocalDate date) {
        Month month = date.getMonth();
        return month == Month.AUGUST || month == Month.SEPTEMBER || month == Month.OCTOBER;
    }

    public boolean isUpcoming() {
        return trekDate.isAfter(LocalDate.now()) &&
                ("Confirmed".equals(status) || "Pending".equals(status));
    }

    public boolean canBeCancelled() {
        return trekDate.isAfter(LocalDate.now().plusDays(7)) &&
                ("Confirmed".equals(status) || "Pending".equals(status));
    }

    public boolean canBeModified() {
        return trekDate.isAfter(LocalDate.now().plusDays(3)) &&
                ("Confirmed".equals(status) || "Pending".equals(status));
    }

    public void confirmBooking() {
        if ("Pending".equals(status)) {
            this.status = "Confirmed";
            attraction.incrementBookings();
            if (guide != null) {
                guide.assignBooking(this);
            }
        }
    }

    public void cancelBooking() {
        if (canBeCancelled()) {
            this.status = "Cancelled";
            attraction.decrementBookings();
            if (guide != null) {
                guide.removeBooking(this);
            }
        }
    }

    public String getFestivalDiscountMessage() {
        return festivalDiscountApplied ?
                "🎉 Festival Discount Applied! (Dashain & Tihar Season - 20% OFF)" : "";
    }

    @Override
    public String toString() {
        return String.format(
                "Booking ID: %d%n" +
                        "Tourist: %s%n" +
                        "Guide: %s%n" +
                        "Attraction: %s%n" +
                        "Trek Date: %s%n" +
                        "Status: %s%n" +
                        "Total Price: $%.2f%n" +
                        "%s" +
                        "Booking Date: %s%n" +
                        "Notes: %s",
                bookingId,
                touristUsername,
                guide != null ? guide.getFullName() : "No Guide",
                attraction.getName(),
                trekDate,
                status,
                totalPrice,
                festivalDiscountApplied ? "Festival Discount: Applied (20% OFF)\n" : "",
                bookingDate,
                notes
        );
    }
}