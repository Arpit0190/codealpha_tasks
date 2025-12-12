import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;



public class HotelReservationSystem {
    public static void main(String[] args) {
        CLI cli = new CLI();
        cli.run();
    }
}

// ----------------------- Models -----------------------
class Room implements Serializable {
    private static final long serialVersionUID = 1L;
    private String roomId;
    private String number;
    private String category;
    private double pricePerNight;

    public Room(String roomId, String number, String category, double pricePerNight) {
        this.roomId = roomId;
        this.number = number;
        this.category = category;
        this.pricePerNight = pricePerNight;
    }

    public String getRoomId() { return roomId; }
    public String getNumber() { return number; }
    public String getCategory() { return category; }
    public double getPricePerNight() { return pricePerNight; }

    @Override
    public String toString() {
        return String.format("Room[number=%s, category=%s, price=%.2f, id=%s]", number, category, pricePerNight, roomId);
    }
}

class Booking implements Serializable {
    private static final long serialVersionUID = 1L;
    private String bookingId;
    private String roomId;
    private String guestName;
    private String guestEmail;
    private LocalDate startDate;
    private LocalDate endDate; // exclusive
    private double totalPrice;
    private boolean paid;
    private Date createdAt;

    public Booking(String bookingId, String roomId, String guestName, String guestEmail, LocalDate startDate, LocalDate endDate, double totalPrice, boolean paid) {
        this.bookingId = bookingId;
        this.roomId = roomId;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.paid = paid;
        this.createdAt = new Date();
    }

    public String getBookingId() { return bookingId; }
    public String getRoomId() { return roomId; }
    public String getGuestName() { return guestName; }
    public String getGuestEmail() { return guestEmail; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getTotalPrice() { return totalPrice; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
    public Date getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return String.format("Booking[id=%s, roomId=%s, guest=%s, email=%s, %s -> %s, total=%.2f, paid=%s]",
                bookingId, roomId, guestName, guestEmail, startDate, endDate, totalPrice, paid);
    }
}

// ----------------------- Persistence -----------------------
class Storage {
    private static final String ROOMS_FILE = "rooms.dat";
    private static final String BOOKINGS_FILE = "bookings.dat";

    public static List<Room> loadRooms() {
        List<Room> rooms = (List<Room>) readObjectFromFile(ROOMS_FILE);
        if (rooms == null) {
            rooms = createSampleRooms();
            saveRooms(rooms);
        }
        return rooms;
    }

    public static void saveRooms(List<Room> rooms) {
        writeObjectToFile(ROOMS_FILE, rooms);
    }

    public static List<Booking> loadBookings() {
        List<Booking> bookings = (List<Booking>) readObjectFromFile(BOOKINGS_FILE);
        if (bookings == null) bookings = new ArrayList<>();
        return bookings;
    }

    public static void saveBookings(List<Booking> bookings) {
        writeObjectToFile(BOOKINGS_FILE, bookings);
    }

    private static Object readObjectFromFile(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return ois.readObject();
        } catch (FileNotFoundException e) {
            return null;
        } catch (Exception e) {
            System.err.println("Failed to read " + filename + ": " + e.getMessage());
            return null;
        }
    }

    private static void writeObjectToFile(String filename, Object obj) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(obj);
        } catch (Exception e) {
            System.err.println("Failed to write " + filename + ": " + e.getMessage());
        }
    }

    private static List<Room> createSampleRooms() {
        List<Room> sample = new ArrayList<>();
        sample.add(new Room(UUID.randomUUID().toString(), "101", "Standard", 1500.0));
        sample.add(new Room(UUID.randomUUID().toString(), "102", "Standard", 1600.0));
        sample.add(new Room(UUID.randomUUID().toString(), "201", "Deluxe", 1800.0));
        sample.add(new Room(UUID.randomUUID().toString(), "202", "Deluxe", 1850.0));
        sample.add(new Room(UUID.randomUUID().toString(), "301", "Suite", 15000.0));
        return sample;
    }
}

// ----------------------- Business Logic -----------------------
class Hotel {
    private List<Room> rooms;
    private List<Booking> bookings;

    public Hotel() {
        this.rooms = Storage.loadRooms();
        this.bookings = Storage.loadBookings();
    }

    public List<String> listCategories() {
        return rooms.stream().map(Room::getCategory).distinct().sorted().collect(Collectors.toList());
    }

    public List<Room> searchAvailableRooms(LocalDate start, LocalDate end, String category) {
        List<Room> result = new ArrayList<>();
        for (Room r : rooms) {
            if (category != null && !category.isEmpty() && !r.getCategory().equalsIgnoreCase(category)) continue;
            boolean blocked = false;
            for (Booking b : bookings) {
                if (b.getRoomId().equals(r.getRoomId()) && datesOverlap(b.getStartDate(), b.getEndDate(), start, end)) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked) result.add(r);
        }
        return result;
    }

    private boolean datesOverlap(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
        return (aStart.isBefore(bEnd)) && (bStart.isBefore(aEnd)); // end exclusive
    }

    public double calculatePrice(Room room, LocalDate start, LocalDate end) {
        long nights = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        if (nights <= 0) throw new IllegalArgumentException("End date must be after start date");
        return Math.round(room.getPricePerNight() * nights * 100.0) / 100.0;
    }

    public Booking createBooking(String roomId, String guestName, String guestEmail, LocalDate start, LocalDate end) {
        Room room = rooms.stream().filter(r -> r.getRoomId().equals(roomId)).findFirst().orElse(null);
        if (room == null) throw new IllegalArgumentException("Room not found");
        // check overlap for this room
        for (Booking b : bookings) {
            if (b.getRoomId().equals(roomId) && datesOverlap(b.getStartDate(), b.getEndDate(), start, end)) {
                throw new IllegalArgumentException("Room not available for the selected dates");
            }
        }
        double total = calculatePrice(room, start, end);
        Booking booking = new Booking(UUID.randomUUID().toString(), roomId, guestName, guestEmail, start, end, total, false);
        bookings.add(booking);
        Storage.saveBookings(bookings);
        return booking;
    }

    public boolean cancelBooking(String bookingId) {
        Iterator<Booking> it = bookings.iterator();
        while (it.hasNext()) {
            Booking b = it.next();
            if (b.getBookingId().equals(bookingId)) {
                it.remove();
                Storage.saveBookings(bookings);
                return true;
            }
        }
        return false;
    }

    public List<Booking> findBookingsByEmail(String email) {
        return bookings.stream().filter(b -> b.getGuestEmail().equalsIgnoreCase(email)).collect(Collectors.toList());
    }

    public Booking getBooking(String bookingId) {
        return bookings.stream().filter(b -> b.getBookingId().equals(bookingId)).findFirst().orElse(null);
    }

    public List<Room> getRooms() { return rooms; }
}

// ----------------------- Payment Simulation -----------------------
class PaymentGatewaySimulator {
    private final double successRate;
    private final Random rnd = new Random();

    public PaymentGatewaySimulator(double successRate) { this.successRate = successRate; }

    public PaymentResult charge(double amount) {
        boolean success = rnd.nextDouble() < successRate;
        String txnId = UUID.randomUUID().toString();
        String message = success ? "Payment successful" : "Payment failed (simulated)";
        return new PaymentResult(success, txnId, amount, message);
    }
}

class PaymentResult {
    public final boolean success;
    public final String transactionId;
    public final double amount;
    public final String message;

    public PaymentResult(boolean success, String transactionId, double amount, String message) {
        this.success = success; this.transactionId = transactionId; this.amount = amount; this.message = message;
    }
}

// ----------------------- CLI / UI -----------------------
class CLI {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final Scanner scanner = new Scanner(System.in);
    private final Hotel hotel = new Hotel();
    private final PaymentGatewaySimulator gateway = new PaymentGatewaySimulator(0.9);

    public void run() {
        System.out.println("Welcome to the Hotel Reservation System (Java CLI)");
        while (true) {
            System.out.println("\nMenu:\n1) Search rooms\n2) Book a room\n3) Cancel booking\n4) View my bookings\n5) List room categories\n6) Exit");
            System.out.print("Select an option: ");
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1": cmdSearch(); break;
                    case "2": cmdBook(); break;
                    case "3": cmdCancel(); break;
                    case "4": cmdViewBookings(); break;
                    case "5": cmdListCategories(); break;
                    case "6": System.out.println("Goodbye!"); return;
                    default: System.out.println("Invalid option");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void cmdListCategories() {
        List<String> cats = hotel.listCategories();
        System.out.println("Available categories:");
        cats.forEach(c -> System.out.println("- " + c));
    }

    private void cmdSearch() {
        LocalDate start = readDate("Enter check-in date (yyyy-MM-dd): ");
        LocalDate end = readDate("Enter check-out date (yyyy-MM-dd): ");
        System.out.print("Category (leave blank for any): ");
        String cat = scanner.nextLine().trim();
        if (cat.isEmpty()) cat = null;
        List<Room> available = hotel.searchAvailableRooms(start, end, cat);
        if (available.isEmpty()) {
            System.out.println("No rooms available for the selected criteria.");
            return;
        }
        System.out.println("Available rooms:");
        for (int i = 0; i < available.size(); i++) {
            Room r = available.get(i);
            System.out.printf("%d) Number: %s, Category: %s, Price/night: %.2f, RoomID: %s\n", i+1, r.getNumber(), r.getCategory(), r.getPricePerNight(), r.getRoomId());
        }
    }

    private void cmdBook() {
        LocalDate start = readDate("Enter check-in date (yyyy-MM-dd): ");
        LocalDate end = readDate("Enter check-out date (yyyy-MM-dd): ");
        System.out.print("Category (leave blank for any): ");
        String cat = scanner.nextLine().trim();
        if (cat.isEmpty()) cat = null;
        List<Room> available = hotel.searchAvailableRooms(start, end, cat);
        if (available.isEmpty()) {
            System.out.println("No rooms available for those dates/category.");
            return;
        }
        System.out.println("Select a room by number:");
        for (int i = 0; i < available.size(); i++) {
            Room r = available.get(i);
            System.out.printf("%d) %s - %s - %.2f\n", i+1, r.getNumber(), r.getCategory(), r.getPricePerNight());
        }
        int idx = readInt("Choice: ", 1, available.size()) - 1;
        Room chosen = available.get(idx);
        System.out.print("Guest name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Guest email: ");
        String email = scanner.nextLine().trim();

        double price = hotel.calculatePrice(chosen, start, end);
        System.out.printf("Total price: %.2f\n", price);
        System.out.print("Proceed to payment? (yes/no): ");
        String proceed = scanner.nextLine().trim().toLowerCase();
        if (!proceed.equals("yes") && !proceed.equals("y")) {
            System.out.println("Booking cancelled by user.");
            return;
        }
        PaymentResult res = gateway.charge(price);
        System.out.println(res.message + " (txn: " + res.transactionId + ")");
        if (!res.success) {
            System.out.println("Payment failed. Booking not completed.");
            return;
        }
        Booking booking = hotel.createBooking(chosen.getRoomId(), name, email, start, end);
        booking.setPaid(true);
        Storage.saveBookings(hotel.findBookingsByEmail(email)); // ensure persistence
        System.out.println("Booking successful! ID: " + booking.getBookingId());
    }

    private void cmdCancel() {
        System.out.print("Enter booking ID to cancel: ");
        String id = scanner.nextLine().trim();
        boolean ok = hotel.cancelBooking(id);
        if (ok) System.out.println("Booking cancelled."); else System.out.println("Booking not found.");
    }

    private void cmdViewBookings() {
        System.out.print("Enter your email to view bookings: ");
        String email = scanner.nextLine().trim();
        List<Booking> bs = hotel.findBookingsByEmail(email);
        if (bs.isEmpty()) {
            System.out.println("No bookings found for " + email);
            return;
        }
        System.out.println("Bookings:");
        for (Booking b : bs) {
            System.out.println(b);
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return LocalDate.parse(line, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use yyyy-MM-dd.");
            }
        }
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int v = Integer.parseInt(line);
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                System.out.printf("Enter a number between %d and %d.\n", min, max);
            }
        }
    }
}

