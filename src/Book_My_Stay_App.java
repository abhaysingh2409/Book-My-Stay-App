import java.io.*;
import java.util.*;

abstract class Room implements Serializable {
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

class RoomInventory implements Serializable {
    private Map<String, Integer> inventory;

    RoomInventory() {
        inventory = new HashMap<>();
        inventory.put("Single", 5);
        inventory.put("Double", 3);
        inventory.put("Suite", 2);
    }

    synchronized int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }

    synchronized boolean isValidRoom(String type) {
        return inventory.containsKey(type);
    }

    synchronized void decrement(String type) throws Exception {
        if (getAvailability(type) <= 0) throw new Exception("No rooms available.");
        inventory.put(type, getAvailability(type) - 1);
    }

    synchronized void increment(String type) {
        inventory.put(type, getAvailability(type) + 1);
    }

    synchronized boolean allocate(String type) {
        int a = getAvailability(type);
        if (a > 0) {
            inventory.put(type, a - 1);
            return true;
        }
        return false;
    }

    Map<String, Integer> getAll() {
        return inventory;
    }
}

class Reservation implements Serializable {
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

    synchronized void add(Reservation r) {
        queue.add(r);
    }

    synchronized Reservation next() {
        return queue.poll();
    }

    synchronized boolean has() {
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

    synchronized String allocate(Reservation r) throws Exception {
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

    synchronized Reservation get(String id) {
        return confirmed.get(id);
    }

    synchronized void remove(String id) {
        confirmed.remove(id);
    }

    synchronized String allocateConcurrent(Reservation r) {
        if (inventory.allocate(r.roomType)) {
            return r.roomType + "-" + counter++;
        }
        return null;
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

class AddOnService implements Serializable {
    String name;
    double cost;

    AddOnService(String name, double cost) {
        this.name = name;
        this.cost = cost;
    }
}

class AddOnServiceManager implements Serializable {
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

class BookingHistory implements Serializable {
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

class BookingProcessor implements Runnable {
    Queue<Reservation> queue;
    BookingService service;

    BookingProcessor(Queue<Reservation> queue, BookingService service) {
        this.queue = queue;
        this.service = service;
    }

    public void run() {
        while (true) {
            Reservation r;
            synchronized (queue) {
                if (queue.isEmpty()) break;
                r = queue.poll();
            }

            String id = service.allocateConcurrent(r);
            if (id != null) {
                System.out.println("Booking confirmed for Guest: " + r.guestName + ", Room ID: " + id);
            }
        }
    }
}

class PersistenceService {
    private static final String FILE = "hotel.dat";

    void save(RoomInventory inventory, BookingHistory history) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE))) {
            oos.writeObject(inventory);
            oos.writeObject(history);
            System.out.println("Inventory saved successfully.");
        } catch (Exception e) {
            System.out.println("Error saving data.");
        }
    }

    Object[] load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE))) {
            return new Object[]{(RoomInventory) ois.readObject(), (BookingHistory) ois.readObject()};
        } catch (Exception e) {
            System.out.println("No valid inventory data found. Starting fresh.");
            return null;
        }
    }
}

public class Book_My_Stay_App {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.println("Welcome to Book My Stay v12.0\n");

        PersistenceService persistence = new PersistenceService();
        RoomInventory inventory;
        BookingHistory history;

        Object[] data = persistence.load();
        if (data == null) {
            inventory = new RoomInventory();
            history = new BookingHistory();
        } else {
            inventory = (RoomInventory) data[0];
            history = (BookingHistory) data[1];
        }

        BookingService bookingService = new BookingService(inventory);
        BookingRequestQueue queue = new BookingRequestQueue();
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

        System.out.println("\nConcurrent Booking Simulation");

        Queue<Reservation> q = new LinkedList<>();
        q.add(new Reservation("Abhi", "Single"));
        q.add(new Reservation("Vanmathi", "Double"));
        q.add(new Reservation("Kural", "Suite"));
        q.add(new Reservation("Subha", "Single"));

        Thread t1 = new Thread(new BookingProcessor(q, bookingService));
        Thread t2 = new Thread(new BookingProcessor(q, bookingService));

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("\nRemaining Inventory:");
        System.out.println("Single: " + inventory.getAvailability("Single"));
        System.out.println("Double: " + inventory.getAvailability("Double"));
        System.out.println("Suite: " + inventory.getAvailability("Suite"));

        System.out.println("\nCurrent Inventory:");
        System.out.println("Single: " + inventory.getAvailability("Single"));
        System.out.println("Double: " + inventory.getAvailability("Double"));
        System.out.println("Suite: " + inventory.getAvailability("Suite"));

        persistence.save(inventory, history);

        sc.close();
    }
}