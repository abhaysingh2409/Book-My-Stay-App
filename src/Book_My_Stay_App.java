import java.util.HashMap;
import java.util.Map;

public class Book_My_Stay_App {

    // Abstract Room class
    public abstract class Room {
        protected int numberOfBeds;
        protected int squareFeet;
        protected double pricePerNight;
        protected int available;

        public Room(int numberOfBeds, int squareFeet, double pricePerNight, int available) {
            this.numberOfBeds = numberOfBeds;
            this.squareFeet = squareFeet;
            this.pricePerNight = pricePerNight;
            this.available = available;
        }

        public void displayRoomDetails() {
            System.out.println("Beds: " + numberOfBeds);
            System.out.println("Size: " + squareFeet + " sqft");
            System.out.println("Price per night: " + pricePerNight);
            System.out.println("Available Rooms: " + available);
            System.out.println();
        }
    }

    // Single Room
    public class SingleRoom extends Room {
        public SingleRoom() {
            super(1, 250, 1500.0, 5);
        }
    }

    // Double Room
    public class DoubleRoom extends Room {
        public DoubleRoom() {
            super(2, 400, 2500.0, 3);
        }
    }

    // Suite Room
    public class SuiteRoom extends Room {
        public SuiteRoom() {
            super(3, 750, 5000.0, 2);
        }
    }

    // Room Inventory Class
    public static class RoomInventory {

        // Key -> Room type, Value -> Available count
        private Map<String, Integer> roomAvailability;

        // Constructor
        public RoomInventory() {
            roomAvailability = new HashMap<>();
            initializeInventory();
        }

        // Initialize room data
        private void initializeInventory() {
            roomAvailability.put("Single Room", 5);
            roomAvailability.put("Double Room", 3);
            roomAvailability.put("Suite Room", 2);
        }

        // Get availability map
        public Map<String, Integer> getRoomAvailability() {
            return roomAvailability;
        }

        // Update availability
        public void updateAvailability(String roomType, int count) {
            roomAvailability.put(roomType, count);
        }
    }

    // Main method
    public static void main(String[] args) {

        Book_My_Stay_App app = new Book_My_Stay_App();
        RoomInventory inventory = new RoomInventory();

        System.out.println("Hotel Room Inventory Status\n");

        Room single = app.new SingleRoom();
        Room doubleRoom = app.new DoubleRoom();
        Room suite = app.new SuiteRoom();

        System.out.println("Single Room:");
        single.displayRoomDetails();

        System.out.println("Double Room:");
        doubleRoom.displayRoomDetails();

        System.out.println("Suite Room:");
        suite.displayRoomDetails();
    }
}