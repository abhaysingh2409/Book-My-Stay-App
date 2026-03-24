import java.util.*;

abstract class Room {
    String type;
    int beds;
    int size;
    double price;

    Room(String type, int beds, int size, double price) {
        this.type = type;
        this.beds = beds;
        this.size = size;
        this.price = price;
    }

    String getType() {
        return type;
    }

    void displayRoom(int availability) {
        System.out.println(type + ":");
        System.out.println("Beds: " + beds);
        System.out.println("Size: " + size + " sqft");
        System.out.println("Price per night: " + price);
        System.out.println("Available Rooms: " + availability);
        System.out.println();
    }
}

class SingleRoom extends Room {
    SingleRoom() {
        super("Single", 1, 250, 1500.0);
    }
}

class DoubleRoom extends Room {
    DoubleRoom() {
        super("Double", 2, 400, 2500.0);
    }
}

class SuiteRoom extends Room {
    SuiteRoom() {
        super("Suite", 3, 750, 5000.0);
    }
}

class RoomInventory {
    HashMap<String, Integer> inventory;

    RoomInventory() {
        inventory = new HashMap<>();
        inventory.put("Single", 5);
        inventory.put("Double", 3);
        inventory.put("Suite", 2);
    }

    int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    void decrement(String roomType) throws InvalidBookingException {
        int count = getAvailability(roomType);
        if (count <= 0) throw new InvalidBookingException("No rooms available.");
        inventory.put(roomType, count - 1);
    }

    boolean isValidRoomType(String roomType) {
        return inventory.containsKey(roomType);
    }
}

class RoomSearchService {
    RoomInventory inventory;

    RoomSearchService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    void search(Room[] rooms) {
        System.out.println("Available Rooms\n");
        for (Room r : rooms) {
            int avail = inventory.getAvailability(r.getType());
            if (avail > 0) r.displayRoom(avail);
        }
    }
}

class Reservation {
    String guestName;
    String roomType;
    String reservationId;

    Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }
}

class BookingRequestQueue {
    Queue<Reservation> queue = new LinkedList<>();

    void add(Reservation r) {
        queue.add(r);
    }

    Reservation next() {
        return queue.poll();
    }

    boolean has() {
        return !queue.isEmpty();
    }
}

class InvalidBookingException extends Exception {
    InvalidBookingException(String msg) {
        super(msg);
    }
}

class BookingService {
    RoomInventory inventory;
    Set<String> usedIds = new HashSet<>();
    int counter = 1;

    BookingService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    String allocate(Reservation r) throws InvalidBookingException {
        if (!inventory.isValidRoomType(r.roomType))
            throw new InvalidBookingException("Invalid room type selected.");

        int avail = inventory.getAvailability(r.roomType);
        if (avail <= 0)
            throw new InvalidBookingException("No rooms available.");

        String id = r.roomType + "-" + counter++;
        if (usedIds.contains(id))
            throw new InvalidBookingException("Duplicate allocation.");

        usedIds.add(id);
        inventory.decrement(r.roomType);
        return id;
    }
}

class AddOnService {
    String name;
    double cost;

    AddOnService(String name, double cost) {
        this.name = name;
        this.cost = cost;
    }
}

class AddOnServiceManager {
    Map<String, List<AddOnService>> map = new HashMap<>();

    void add(String resId, AddOnService s) {
        map.putIfAbsent(resId, new ArrayList<>());
        map.get(resId).add(s);
    }

    double total(String resId) {
        double sum = 0;
        if (map.containsKey(resId)) {
            for (AddOnService s : map.get(resId)) sum += s.cost;
        }
        return sum;
    }
}

class BookingHistory {
    List<Reservation> list = new ArrayList<>();

    void add(Reservation r) {
        list.add(r);
    }

    List<Reservation> all() {
        return list;
    }
}

class BookingReportService {
    void report(List<Reservation> list) {
        System.out.println("\nBooking History Report");
        for (Reservation r : list) {
            System.out.println("Guest: " + r.guestName + ", Room Type: " + r.roomType);
        }
    }
}

public class Book_My_Stay_App {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Welcome to Book My Stay v9.0\n");

        RoomInventory inventory = new RoomInventory();
        BookingService bookingService = new BookingService(inventory);
        BookingRequestQueue queue = new BookingRequestQueue();
        BookingHistory history = new BookingHistory();
        AddOnServiceManager serviceManager = new AddOnServiceManager();

        Room[] rooms = { new SingleRoom(), new DoubleRoom(), new SuiteRoom() };

        RoomSearchService search = new RoomSearchService(inventory);
        search.search(rooms);

        System.out.print("Enter guest name: ");
        String name = sc.nextLine();

        System.out.print("Enter room type (Single/Double/Suite): ");
        String type = sc.nextLine();

        Reservation r = new Reservation(name, type);
        queue.add(r);

        try {
            Reservation req = queue.next();
            String id = bookingService.allocate(req);
            req.reservationId = id;

            System.out.println("Booking Confirmed. ID: " + id);

            serviceManager.add(id, new AddOnService("Breakfast", 500));
            serviceManager.add(id, new AddOnService("Spa", 1000));

            double cost = serviceManager.total(id);
            System.out.println("Add-On Cost: " + cost);

            history.add(req);

        } catch (InvalidBookingException e) {
            System.out.println("Booking failed: " + e.getMessage());
        }

        BookingReportService report = new BookingReportService();
        report.report(history.all());

        sc.close();
    }
}