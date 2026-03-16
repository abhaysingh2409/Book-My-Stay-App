import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.LinkedList;

public class Book_My_Stay_App {

    public abstract static class Room {
        protected int numberOfBeds;
        protected int squareFeet;
        protected double pricePerNight;

        public Room(int numberOfBeds, int squareFeet, double pricePerNight) {
            this.numberOfBeds = numberOfBeds;
            this.squareFeet = squareFeet;
            this.pricePerNight = pricePerNight;
        }

        public void displayRoomDetails() {
            System.out.println("Beds: " + numberOfBeds);
            System.out.println("Size: " + squareFeet + " sqft");
            System.out.println("Price per night: " + pricePerNight);
        }
    }

    // Single Room
    public static class SingleRoom extends Room {
        public SingleRoom() {
            super(1, 250, 1500.0);
        }
    }

    // Double Room
    public static class DoubleRoom extends Room {
        public DoubleRoom() {
            super(2, 400, 2500.0);
        }
    }

    // Suite Room
    public static class SuiteRoom extends Room {
        public SuiteRoom() {
            super(3, 750, 5000.0);
        }
    }

    // Room Inventory
    public static class RoomInventory {

        private Map<String, Integer> roomAvailability;

        public RoomInventory() {
            roomAvailability = new HashMap<>();
            initializeInventory();
        }

        private void initializeInventory() {
            roomAvailability.put("Single", 5);
            roomAvailability.put("Double", 3);
            roomAvailability.put("Suite", 2);
        }

        public Map<String, Integer> getRoomAvailability() {
            return roomAvailability;
        }

        public void updateAvailability(String roomType, int count) {
            roomAvailability.put(roomType, count);
        }
    }

    // Room Search Service (UC4)
    public static class RoomSearchService {

        public void searchAvailableRooms(
                RoomInventory inventory,
                Room singleRoom,
                Room doubleRoom,
                Room suiteRoom) {

            Map<String, Integer> availability = inventory.getRoomAvailability();

            System.out.println("Room Search\n");

            if (availability.get("Single") > 0) {
                System.out.println("Single Room:");
                singleRoom.displayRoomDetails();
                System.out.println("Available: " + availability.get("Single"));
                System.out.println();
            }

            if (availability.get("Double") > 0) {
                System.out.println("Double Room:");
                doubleRoom.displayRoomDetails();
                System.out.println("Available: " + availability.get("Double"));
                System.out.println();
            }

            if (availability.get("Suite") > 0) {
                System.out.println("Suite Room:");
                suiteRoom.displayRoomDetails();
                System.out.println("Available: " + availability.get("Suite"));
                System.out.println();
            }
        }
    }

    // ==============================
    // UC5 - Reservation Class
    // ==============================

    public static class Reservation {

        private String guestName;
        private String roomType;

        public Reservation(String guestName, String roomType) {
            this.guestName = guestName;
            this.roomType = roomType;
        }

        public String getGuestName() {
            return guestName;
        }

        public String getRoomType() {
            return roomType;
        }
    }

    // ==============================
    // UC5 - Booking Request Queue
    // ==============================

    public static class BookingRequestQueue {

        private Queue<Reservation> requestQueue;

        public BookingRequestQueue() {
            requestQueue = new LinkedList<>();
        }

        public void addRequest(Reservation reservation) {
            requestQueue.offer(reservation);
        }

        public Reservation getNextRequest() {
            return requestQueue.poll();
        }

        public boolean hasPendingRequests() {
            return !requestQueue.isEmpty();
        }
    }

    // ==============================
    // MAIN METHOD (UC4 + UC5)
    // ==============================

    public static void main(String[] args) {

        // -------- UC4 : Room Search --------

        Room singleRoom = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suiteRoom = new SuiteRoom();

        RoomInventory inventory = new RoomInventory();
        RoomSearchService searchService = new RoomSearchService();

        searchService.searchAvailableRooms(
                inventory,
                singleRoom,
                doubleRoom,
                suiteRoom
        );
        System.out.println("Booking Request Queue");

        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        Reservation r1 = new Reservation("Abhi", "Single");
        Reservation r2 = new Reservation("Subha", "Double");
        Reservation r3 = new Reservation("Vanmathi", "Suite");

        bookingQueue.addRequest(r1);
        bookingQueue.addRequest(r2);
        bookingQueue.addRequest(r3);

        while (bookingQueue.hasPendingRequests()) {

            Reservation current = bookingQueue.getNextRequest();

            System.out.println(
                    "Processing booking for Guest: "
                            + current.getGuestName()
                            + ", Room Type: "
                            + current.getRoomType()
            );
        }
    }
}