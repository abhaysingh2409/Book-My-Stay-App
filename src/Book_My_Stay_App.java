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
        System.out.println(type + " Room:");
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
    Map<String, Integer> inventory;

    RoomInventory() {
        inventory = new HashMap<>();
        inventory.put("Single", 5);
        inventory.put("Double", 3);
        inventory.put("Suite", 2);
    }

    int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }

    boolean isValidRoom(String type) {
        return inventory.containsKey(type);
    }

    void decrement(String type) throws Exception {
        if (getAvailability(type) <= 0) throw new Exception("No rooms available.");
        inventory.put(type, getAvailability(type) - 1);
    }

    void increment(String type) {
        inventory.put(type, getAvailability(type) + 1);
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

class BookingService {
    RoomInventory inventory;
    Set<String> usedIds = new HashSet<>();
    Map<String, Reservation> confirmed = new HashMap<>();
    int counter = 1;

    BookingService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    String allocate(Reservation r) throws Exception {
        if (!inventory.isValidRoom(r.roomType))
            throw new Exception("Invalid room type selected.");

        String id = r.roomType + "-" + counter++;
        if (usedIds.contains(id)) throw new Exception("Duplicate ID");

        inventory.decrement(r.roomType);
        usedIds.add(id);

        r.reservationId = id;
        confirmed.put(id, r);

        return id;
    }

    Reservation get(String id) {
        return confirmed.get(id);
    }

    void remove(String id) {
        confirmed.remove(id);
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
            int a = inventory.getAvailability(r.getType());
            if (a > 0) r.displayRoom(a);
        }
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

    void add(String id, AddOnService s) {
        map.putIfAbsent(id, new ArrayList<>());
        map.get(id).add(s);
    }

    double total(String id) {
        double sum = 0;
        if (map.containsKey(id)) {
            for (AddOnService s : map.get(id)) sum += s.cost;
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

class CancellationService {
    RoomInventory inventory;
    BookingService bookingService;
    Stack<String> stack = new Stack<>();

    CancellationService(RoomInventory inventory, BookingService bookingService) {
        this.inventory = inventory;
        this.bookingService = bookingService;
    }

    void cancel(String id) {
        Reservation r = bookingService.get(id);
        if (r == null) {
            System.out.println("Invalid cancellation request.");
            return;
        }

        stack.push(id);
        inventory.increment(r.roomType);
        bookingService.remove(id);

        System.out.println("\nBooking cancelled successfully. Inventory restored for room type: " + r.roomType);
        System.out.println("\nRollback History (Most Recent First):");
        while (!stack.isEmpty()) {
            System.out.println("Released Reservation ID: " + stack.pop());
        }
        System.out.println("\nUpdated " + r.roomType + " Room Availability: " + inventory.getAvailability(r.roomType));
    }
}

public class Book_My_Stay_App {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Welcome to Book My Stay v10.0\n");

        RoomInventory inventory = new RoomInventory();
        BookingService bookingService = new BookingService(inventory);
        BookingRequestQueue queue = new BookingRequestQueue();
        BookingHistory history = new BookingHistory();
        AddOnServiceManager addOn = new AddOnServiceManager();
        CancellationService cancelService = new CancellationService(inventory, bookingService);

        Room[] rooms = { new SingleRoom(), new DoubleRoom(), new SuiteRoom() };
        new RoomSearchService(inventory).search(rooms);

        System.out.print("Enter guest name: ");
        String name = sc.nextLine();

        System.out.print("Enter room type (Single/Double/Suite): ");
        String type = sc.nextLine();

        Reservation r = new Reservation(name, type);
        queue.add(r);

        try {
            Reservation req = queue.next();
            String id = bookingService.allocate(req);
            System.out.println("Booking Confirmed. ID: " + id);

            addOn.add(id, new AddOnService("Breakfast", 500));
            addOn.add(id, new AddOnService("Spa", 1000));

            System.out.println("Add-On Cost: " + addOn.total(id));

            history.add(req);

            cancelService.cancel(id);

        } catch (Exception e) {
            System.out.println("Booking failed: " + e.getMessage());
        }

        new BookingReportService().report(history.all());

        sc.close();
    }
}