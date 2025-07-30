package com.tourism.models;

import java.io.Serializable;

// Attraction class demonstrating encapsulation
public class Attraction {
    private String name;
    private String location;
    private String altitudeLevel; // "High" or "Low"
    private String difficulty; // "Easy", "Medium", "Hard"
    private double basePrice;
    private String description;
    private int maxCapacity;
    private int currentBookings;
    private boolean isActive;
   ;
    
    // Constructor
    public Attraction(String name, String location, String altitudeLevel, String difficulty, double basePrice) {
        this.name = name;
        this.location = location;
        this.altitudeLevel = altitudeLevel;
        this.difficulty = difficulty;
        this.basePrice = basePrice;
        this.description = "";
        this.maxCapacity = 50; // Default capacity
        this.currentBookings = 0;
        this.isActive = true;
    }
    
    // Encapsulation - Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getAltitudeLevel() { return altitudeLevel; }
    public void setAltitudeLevel(String altitudeLevel) { this.altitudeLevel = altitudeLevel; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    
    public int getCurrentBookings() { return currentBookings; }
    public void incrementBookings() { this.currentBookings++; }
    public void decrementBookings() { if (this.currentBookings > 0) this.currentBookings--; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    
    // Business logic methods
    public boolean isHighAltitude() {
        return "High".equalsIgnoreCase(altitudeLevel);
    }
    
    public boolean isAvailable() {
        return isActive && currentBookings < maxCapacity;
    }
    
    public double calculatePrice(boolean isFestivalSeason) {
        double price = basePrice;
        
        // Difficulty multiplier
        switch (difficulty.toLowerCase()) {
            case "easy":
                price *= 1.0;
                break;
            case "medium":
                price *= 1.3;
                break;
            case "hard":
                price *= 1.6;
                break;
        }
        
        // Festival discount (20% off during August-October)
        if (isFestivalSeason) {
            price *= 0.8; // 20% discount
        }
        
        return price;
    }
    
    public int getAvailableSpots() {
        return maxCapacity - currentBookings;
    }
    
    @Override
    public String toString() {
        return "Attraction: " + name + "\n" +
               "Location: " + location + "\n" +
               "Altitude: " + altitudeLevel + "\n" +
               "Difficulty: " + difficulty + "\n" +
               "Base Price: $" + String.format("%.2f", basePrice) + "\n" +
               "Available Spots: " + getAvailableSpots() + "/" + maxCapacity + "\n" +
               "Status: " + (isActive ? "Active" : "Inactive");
    }
}
