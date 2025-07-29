package com.tourism.utils;

import com.tourism.models.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class FileHandler {
    private static final String DATA_DIR = "data/";
    private static final String TOURISTS_FILE = DATA_DIR + "tourists.dat";
    private static final String GUIDES_FILE = DATA_DIR + "guides.dat";
    private static final String ATTRACTIONS_FILE = DATA_DIR + "attractions.dat";
    private static final String BOOKINGS_FILE = DATA_DIR + "bookings.dat";
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
    public static void saveTourist(Tourist tourist) {
        List<Tourist> tourists = loadTourists();
        tourists.removeIf(t -> t.getUsername().equals(tourist.getUsername()));
        tourists.add(tourist);
        saveAllTourists(tourists);
    }

    public static List<Tourist> loadTourists() {
        List<Tourist> tourists = new ArrayList<>();
        if (!new File(TOURISTS_FILE).exists()) {
            return tourists;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(TOURISTS_FILE))) {
            tourists = (List<Tourist>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Error loading tourists: " + e.getMessage());
        }
        return tourists;
    }

    private static void saveAllTourists(List<Tourist> tourists) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TOURISTS_FILE))) {
            oos.writeObject(tourists);
        } catch (Exception e) {
            System.err.println("Error saving tourists: " + e.getMessage());
        }
    }

    // ================= Guide Operations =================
    public static void saveGuide(Guide guide) {
        List<Guide> guides = loadGuides();
        guides.removeIf(g -> g.getUsername().equals(guide.getUsername()));
        guides.add(guide);
        saveAllGuides(guides);
    }

    public static List<Guide> loadGuides() {
        List<Guide> guides = new ArrayList<>();
        if (!new File(GUIDES_FILE).exists()) {
            return guides;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(GUIDES_FILE))) {
            guides = (List<Guide>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Error loading guides: " + e.getMessage());
        }
        return guides;
    }

    public static void saveAllGuides(List<Guide> guides) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(GUIDES_FILE))) {
            oos.writeObject(guides);
        } catch (Exception e) {
            System.err.println("Error saving guides: " + e.getMessage());
        }
    }

    // ================= Attraction Operations =================
    public static void saveAttraction(Attraction attraction) {
        List<Attraction> attractions = loadAttractions();
        attractions.removeIf(a -> a.getName().equals(attraction.getName()));
        attractions.add(attraction);
        saveAllAttractions(attractions);
    }

    public static List<Attraction> loadAttractions() {
        List<Attraction> attractions = new ArrayList<>();
        if (!new File(ATTRACTIONS_FILE).exists()) {
            return attractions;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ATTRACTIONS_FILE))) {
            attractions = (List<Attraction>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Error loading attractions: " + e.getMessage());
        }
        return attractions;
    }

    private static void saveAllAttractions(List<Attraction> attractions) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ATTRACTIONS_FILE))) {
            oos.writeObject(attractions);
        } catch (Exception e) {
            System.err.println("Error saving attractions: " + e.getMessage());
        }
    }

    // ================= Booking Operations =================
    public static void saveBooking(Booking booking) {
        List<Booking> bookings = loadBookings();
        bookings.removeIf(b -> b.getBookingId() == booking.getBookingId());
        bookings.add(booking);
        saveAllBookings(bookings);
    }

    public static List<Booking> loadBookings() {
        List<Booking> bookings = new ArrayList<>();
        if (!new File(BOOKINGS_FILE).exists()) {
            return bookings;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BOOKINGS_FILE))) {
            List<BookingData> bookingDataList = (List<BookingData>) ois.readObject();

            // Convert BookingData to Booking objects
            List<Attraction> attractions = loadAttractions();
            List<Guide> guides = loadGuides();

            for (BookingData data : bookingDataList) {
                Attraction attraction = attractions.stream()
                        .filter(a -> a.getName().equals(data.attractionName))
                        .findFirst()
                        .orElse(null);

                if (attraction == null) {
                    System.err.println("Attraction not found: " + data.attractionName);
                    continue;
                }

                Booking booking = new Booking(
                        data.touristUsername,
                        attraction,
                        data.trekDate
                );
                booking.setBookingId(data.bookingId);
                booking.setStatus(data.status);
                booking.setTotalPrice(data.totalPrice);
                booking.setFestivalDiscountApplied(data.festivalDiscountApplied);

                if (data.guideUsername != null && !data.guideUsername.isEmpty()) {
                    Guide guide = guides.stream()
                            .filter(g -> g.getUsername().equals(data.guideUsername))
                            .findFirst()
                            .orElse(null);
                    booking.setGuide(guide);
                }

                bookings.add(booking);
            }
        } catch (Exception e) {
            System.err.println("Error loading bookings: " + e.getMessage());
        }
        return bookings;
    }

    public static void saveAllBookings(List<Booking> bookings) {
        // Convert to BookingData for serialization
        List<BookingData> bookingDataList = bookings.stream()
                .map(b -> new BookingData(
                        b.getBookingId(),
                        b.getTouristUsername(),
                        b.getGuide() != null ? b.getGuide().getUsername() : null,
                        b.getAttraction().getName(),
                        b.getTrekDate(),
                        b.getStatus(),
                        b.getTotalPrice(),
                        b.isFestivalDiscountApplied()
                ))
                .collect(Collectors.toList());

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BOOKINGS_FILE))) {
            oos.writeObject(bookingDataList);
        } catch (Exception e) {
            System.err.println("Error saving bookings: " + e.getMessage());
        }
    }

    // Helper class for booking serialization
    private static class BookingData implements Serializable {
        private static final long serialVersionUID = 1L;
        int bookingId;
        String touristUsername;
        String guideUsername;
        String attractionName;
        LocalDate trekDate;
        String status;
        double totalPrice;
        boolean festivalDiscountApplied;

        public BookingData(int bookingId, String touristUsername, String guideUsername,
                           String attractionName, LocalDate trekDate, String status,
                           double totalPrice, boolean festivalDiscountApplied) {
            this.bookingId = bookingId;
            this.touristUsername = touristUsername;
            this.guideUsername = guideUsername;
            this.attractionName = attractionName;
            this.trekDate = trekDate;
            this.status = status;
            this.totalPrice = totalPrice;
            this.festivalDiscountApplied = festivalDiscountApplied;
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