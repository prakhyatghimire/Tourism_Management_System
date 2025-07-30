package com.tourism.utils;

import com.tourism.models.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class FileHandler {
    private static final String DATA_DIR = "data/";
    private static final String TOURISTS_FILE = DATA_DIR + "tourists.txt";
    private static final String GUIDES_FILE = DATA_DIR + "guides.txt";
    private static final String ATTRACTIONS_FILE = DATA_DIR + "attractions.txt";
    private static final String BOOKINGS_FILE = DATA_DIR + "bookings.txt";
    private static final String SEPARATOR = "%%%";

    // Initialize data directory and default data
    public static void initializeDataFiles() {
        createDataDirectory();
        if (!new File(ATTRACTIONS_FILE).exists()) {
            initializeDefaultAttractions();
        }
        if (!new File(GUIDES_FILE).exists()) {
            initializeDefaultGuides();
        }
    }

    private static void createDataDirectory() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    // ================= Tourist Operations =================
    public static boolean saveTourist(Tourist tourist) {
        List<Tourist> tourists = loadTourists();
        tourists.removeIf(t -> t.getUsername().equals(tourist.getUsername()));
        tourists.add(tourist);
        return saveAllTourists(tourists);
    }

    public static List<Tourist> loadTourists() {
        List<Tourist> tourists = new ArrayList<>();
        if (!new File(TOURISTS_FILE).exists()) {
            return tourists;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(TOURISTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(SEPARATOR);
                if (parts.length == 6) {
                    Tourist tourist = new Tourist(
                            parts[0], // username
                            parts[1], // password
                            parts[2], // fullName
                            parts[3], // email
                            parts[4], // phone
                            parts[5]  // nationality
                    );
                    tourists.add(tourist);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading tourists: " + e.getMessage());
        }
        return tourists;
    }

    private static boolean saveAllTourists(List<Tourist> tourists) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TOURISTS_FILE))) {
            for (Tourist tourist : tourists) {
                String line = String.join(SEPARATOR,
                        tourist.getUsername(),
                        tourist.getPassword(),
                        tourist.getFullName(),
                        tourist.getEmail(),
                        tourist.getPhone(),
                        tourist.getNationality()
                );
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error saving tourists: " + e.getMessage());
            return false;
        }
    }

    // ================= Guide Operations =================
    public static boolean saveGuide(Guide guide) {
        List<Guide> guides = loadGuides();
        guides.removeIf(g -> g.getUsername().equals(guide.getUsername()));
        guides.add(guide);
        return saveAllGuides(guides);
    }

    public static List<Guide> loadGuides() {
        List<Guide> guides = new ArrayList<>();
        if (!new File(GUIDES_FILE).exists()) {
            return guides;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(GUIDES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(SEPARATOR);
                if (parts.length == 7) {
                    List<String> languages = Arrays.asList(parts[5].split(","));
                    int experienceYears = Integer.parseInt(parts[6]);

                    Guide guide = new Guide(
                            parts[0], // username
                            parts[1], // password
                            parts[2], // fullName
                            parts[3], // email
                            parts[4], // phone
                            languages,
                            experienceYears
                    );
                    guides.add(guide);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading guides: " + e.getMessage());
        }
        return guides;
    }

    public static boolean saveAllGuides(List<Guide> guides) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(GUIDES_FILE))) {
            for (Guide guide : guides) {
                String line = String.join(SEPARATOR,
                        guide.getUsername(),
                        guide.getPassword(),
                        guide.getFullName(),
                        guide.getEmail(),
                        guide.getPhone(),
                        String.join(",", guide.getLanguages()),
                        String.valueOf(guide.getExperienceYears())
                );
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error saving guides: " + e.getMessage());
            return false;
        }
    }

    // ================= Attraction Operations =================
    public static boolean saveAttraction(Attraction attraction) {
        List<Attraction> attractions = loadAttractions();
        attractions.removeIf(a -> a.getName().equals(attraction.getName()));
        attractions.add(attraction);
        return saveAllAttractions(attractions);
    }

    public static List<Attraction> loadAttractions() {
        List<Attraction> attractions = new ArrayList<>();
        if (!new File(ATTRACTIONS_FILE).exists()) {
            return attractions;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(ATTRACTIONS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(SEPARATOR);
                if (parts.length == 5) {
                    Attraction attraction = new Attraction(
                            parts[0], // name
                            parts[1], // location
                            parts[2], // difficulty
                            parts[3], // priceCategory
                            Double.parseDouble(parts[4]) // price
                    );
                    attractions.add(attraction);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading attractions: " + e.getMessage());
        }
        return attractions;
    }

    private static boolean saveAllAttractions(List<Attraction> attractions) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ATTRACTIONS_FILE))) {
            for (Attraction attraction : attractions) {
                String line = String.join(SEPARATOR,
                        attraction.getName(),
                        attraction.getLocation(),
                        attraction.getAltitudeLevel(),
                        attraction.getDifficulty(),
                        String.valueOf(attraction.getBasePrice())
                );
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error saving attractions: " + e.getMessage());
            return false;
        }
    }

    // ================= Booking Operations =================
    public static boolean saveBooking(Booking booking) {
        List<Booking> bookings = loadBookings();
        bookings.removeIf(b -> b.getBookingId() == booking.getBookingId());
        bookings.add(booking);
        return saveAllBookings(bookings);
    }

    public static List<Booking> loadBookings() {
        List<Booking> bookings = new ArrayList<>();
        if (!new File(BOOKINGS_FILE).exists()) {
            return bookings;
        }

        List<Attraction> attractions = loadAttractions();
        List<Guide> guides = loadGuides();

        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKINGS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(SEPARATOR);
                if (parts.length >= 8) {
                    int bookingId = Integer.parseInt(parts[0]);
                    String touristUsername = parts[1];
                    String guideUsername = parts[2];
                    String attractionName = parts[3];
                    LocalDate trekDate = LocalDate.parse(parts[4]);
                    String status = parts[5];
                    double totalPrice = Double.parseDouble(parts[6]);
                    boolean festivalDiscountApplied = Boolean.parseBoolean(parts[7]);

                    Attraction attraction = attractions.stream()
                            .filter(a -> a.getName().equals(attractionName))
                            .findFirst()
                            .orElse(null);

                    if (attraction == null) continue;

                    Booking booking = new Booking(touristUsername, attraction, trekDate);
                    booking.setBookingId(bookingId);
                    booking.setStatus(status);
                    booking.setTotalPrice(totalPrice);
                    booking.setFestivalDiscountApplied(festivalDiscountApplied);

                    if (guideUsername != null && !guideUsername.isEmpty()) {
                        Guide guide = guides.stream()
                                .filter(g -> g.getUsername().equals(guideUsername))
                                .findFirst()
                                .orElse(null);
                        booking.setGuide(guide);
                    }

                    bookings.add(booking);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading bookings: " + e.getMessage());
        }
        return bookings;
    }

    public static boolean saveAllBookings(List<Booking> bookings) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKINGS_FILE))) {
            for (Booking booking : bookings) {
                String line = String.join(SEPARATOR,
                        String.valueOf(booking.getBookingId()),
                        booking.getTouristUsername(),
                        booking.getGuide() != null ? booking.getGuide().getUsername() : "",
                        booking.getAttraction().getName(),
                        booking.getTrekDate().toString(),
                        booking.getStatus(),
                        String.valueOf(booking.getTotalPrice()),
                        String.valueOf(booking.isFestivalDiscountApplied())
                );
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error saving bookings: " + e.getMessage());
            return false;
        }
    }

    // ================= Default Data Initialization =================
    private static void initializeDefaultAttractions() {
        List<Attraction> defaultAttractions = Arrays.asList(
                new Attraction("Everest Base Camp", "Khumbu", "High", "Hard", 1200.0),
                new Attraction("Annapurna Circuit", "Annapurna", "High", "Medium", 800.0),
                new Attraction("Pokhara Sightseeing", "Pokhara", "Low", "Easy", 150.0)
        );
        saveAllAttractions(defaultAttractions);
    }

    private static void initializeDefaultGuides() {
        List<Guide> defaultGuides = Arrays.asList(
                new Guide("guide1", "password", "Ram Sharma", "ram@guide.com", "1234567890",
                        Arrays.asList("English", "Nepali"), 5),
                new Guide("guide2", "password", "Sita Gurung", "sita@guide.com", "9876543210",
                        Arrays.asList("English", "Hindi"), 3)
        );
        saveAllGuides(defaultGuides);
    }
}