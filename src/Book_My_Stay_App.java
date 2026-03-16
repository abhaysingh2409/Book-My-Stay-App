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
            System.out.println("Available: " + available);
            System.out.println("---------------------------");
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

    // Main method
    public static void main(String[] args) {
        Book_My_Stay_App app = new Book_My_Stay_App();

        System.out.println("Welcome to the Hotel Booking Management System");
        System.out.println("System initialized successfully\n");

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
