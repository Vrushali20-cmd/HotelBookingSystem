
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class HotelBookingApp {

    private static final String URL =
            "jdbc:mysql://localhost:3306/hoteldb";

    private static final String USER = "root";

    private static final String PASSWORD =
            System.getenv("DB_PASSWORD");

    public static void main(String[] args) {

        if (PASSWORD == null || PASSWORD.isBlank()) {
            System.out.println(
                    "Database password not configured. " +
                            "Set DB_PASSWORD in IntelliJ Run Configuration."
            );
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (
                    Connection con = DriverManager.getConnection(
                            URL, USER, PASSWORD);
                    Scanner sc = new Scanner(System.in)
            ) {
                System.out.println("Connected to hotel database!");

                boolean running = true;

                while (running) {
                    System.out.println("\n===== HOTEL BOOKING SYSTEM =====");
                    System.out.println("1. Book a Room");
                    System.out.println("2. View Booking");
                    System.out.println("3. View All Bookings");
                    System.out.println("4. Update Booking");
                    System.out.println("5. Delete Booking");
                    System.out.println("6. Exit");

                    int choice = readInt(
                            sc, "Enter your choice: ", 1, 6
                    );

                    switch (choice) {
                        case 1:
                            bookARoom(con, sc);
                            break;

                        case 2:
                            viewBooking(con, sc);
                            break;

                        case 3:
                            viewAllBookings(con);
                            break;

                        case 4:
                            updateBooking(con, sc);
                            break;

                        case 5:
                            deleteBooking(con, sc);
                            break;

                        case 6:
                            running = false;
                            System.out.println(
                                    "Thank you for using the system!"
                            );
                            break;
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ================= INPUT VALIDATION =================

    private static int readInt(
            Scanner sc, String message, int min, int max) {

        while (true) {
            System.out.print(message);
            String input = sc.nextLine().trim();

            try {
                int number = Integer.parseInt(input);

                if (number >= min && number <= max) {
                    return number;
                }

                System.out.println(
                        "Enter a number between " + min + " and " + max
                );

            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Enter a whole number.");
            }
        }
    }

    private static String readName(Scanner sc) {

        while (true) {
            System.out.print("Enter customer name: ");
            String name = sc.nextLine().trim();

            if (name.matches("[a-zA-Z ]+")) {
                return name;
            }

            System.out.println(
                    "Invalid name! Use letters and spaces only."
            );
        }
    }

    private static String readPhoneNumber(Scanner sc) {

        while (true) {
            System.out.print("Enter phone number: ");
            String phone = sc.nextLine().trim();

            if (phone.matches("[0-9]{10}")) {
                return phone;
            }

            System.out.println(
                    "Enter a valid 10-digit phone number."
            );
        }
    }

    // ================= ROOM AVAILABILITY =================

    private static boolean isRoomAvailable(
            Connection con, int roomNumber) throws SQLException {

        String query =
                "SELECT 1 FROM booking_table WHERE room_number = ?";

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, roomNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                return !rs.next();
            }
        }
    }

    // Check room availability while updating.
    // The current booking must not conflict with itself.
    private static boolean isRoomAvailable(
            Connection con, int roomNumber, int bookingId)
            throws SQLException {

        String query =
                "SELECT 1 FROM booking_table " +
                        "WHERE room_number = ? AND id <> ?";

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, roomNumber);
            pstmt.setInt(2, bookingId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return !rs.next();
            }
        }
    }

    // ================= BOOK A ROOM =================

    private static void bookARoom(Connection con, Scanner sc) {

        String name = readName(sc);
        String phone = readPhoneNumber(sc);

        int roomNumber = readInt(
                sc, "Enter room number: ", 1, Integer.MAX_VALUE
        );

        try {
            if (!isRoomAvailable(con, roomNumber)) {
                System.out.println(
                        "Room " + roomNumber +
                                " is already booked. Choose another room."
                );
                return;
            }

            String query =
                    "INSERT INTO booking_table " +
                            "(name, phone_number, room_number) " +
                            "VALUES (?, ?, ?)";

            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, name);
                pstmt.setString(2, phone);
                pstmt.setInt(3, roomNumber);

                if (pstmt.executeUpdate() > 0) {
                    System.out.println("Room booked successfully!");
                }
            }

        } catch (SQLException e) {
            System.out.println("Booking failed: " + e.getMessage());
        }
    }

    // ================= BOOKING EXISTS =================

    private static boolean bookingExists(
            Connection con, int id) throws SQLException {

        String query =
                "SELECT 1 FROM booking_table WHERE id = ?";

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ================= VIEW ONE BOOKING =================

    private static void viewBooking(Connection con, Scanner sc) {

        int id = readInt(
                sc, "Enter booking ID: ", 1, Integer.MAX_VALUE
        );

        String query =
                "SELECT id, name, phone_number, room_number, timestamp " +
                        "FROM booking_table WHERE id = ?";

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    printBooking(rs);
                } else {
                    System.out.println(
                            "No booking found with ID: " + id
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Unable to fetch booking: " + e.getMessage());
        }
    }

    // ================= VIEW ALL BOOKINGS =================

    private static void viewAllBookings(Connection con) {

        String query =
                "SELECT id, name, phone_number, room_number, timestamp " +
                        "FROM booking_table ORDER BY id";

        try (
                PreparedStatement pstmt = con.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()
        ) {
            boolean found = false;

            System.out.println("\n========== ALL BOOKINGS ==========");

            while (rs.next()) {
                found = true;
                printBooking(rs);
                System.out.println("----------------------------------");
            }

            if (!found) {
                System.out.println("No bookings found.");
            }

        } catch (SQLException e) {
            System.out.println("Unable to fetch bookings: " + e.getMessage());
        }
    }

    private static void printBooking(ResultSet rs) throws SQLException {

        System.out.println("\nBooking ID: " + rs.getInt("id"));
        System.out.println("Customer Name: " + rs.getString("name"));
        System.out.println("Phone Number: " + rs.getString("phone_number"));
        System.out.println("Room Number: " + rs.getInt("room_number"));
        System.out.println("Booking Time: " + rs.getTimestamp("timestamp"));
    }

    // ================= UPDATE BOOKING =================

    private static void updateBooking(Connection con, Scanner sc) {

        int id = readInt(
                sc, "Enter booking ID to update: ",
                1, Integer.MAX_VALUE
        );

        try {
            if (!bookingExists(con, id)) {
                System.out.println("No booking found with ID: " + id);
                return;
            }

            String name = readName(sc);
            String phone = readPhoneNumber(sc);

            int roomNumber = readInt(
                    sc, "Enter new room number: ",
                    1, Integer.MAX_VALUE
            );

            if (!isRoomAvailable(con, roomNumber, id)) {
                System.out.println(
                        "Room " + roomNumber +
                                " is already booked. Update cancelled."
                );
                return;
            }

            String query =
                    "UPDATE booking_table " +
                            "SET name = ?, phone_number = ?, room_number = ? " +
                            "WHERE id = ?";

            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, name);
                pstmt.setString(2, phone);
                pstmt.setInt(3, roomNumber);
                pstmt.setInt(4, id);

                if (pstmt.executeUpdate() > 0) {
                    System.out.println("Booking updated successfully!");
                } else {
                    System.out.println("Booking was not updated.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Update failed: " + e.getMessage());
        }
    }

    // ================= DELETE BOOKING =================

    private static void deleteBooking(Connection con, Scanner sc) {

        int id = readInt(
                sc, "Enter booking ID to delete: ",
                1, Integer.MAX_VALUE
        );

        try {
            if (!bookingExists(con, id)) {
                System.out.println("No booking found with ID: " + id);
                return;
            }

            System.out.print(
                    "Are you sure you want to delete this booking? (yes/no): "
            );

            String confirmation = sc.nextLine().trim();

            if (!confirmation.equalsIgnoreCase("yes")) {
                System.out.println("Deletion cancelled.");
                return;
            }

            String query =
                    "DELETE FROM booking_table WHERE id = ?";

            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setInt(1, id);

                if (pstmt.executeUpdate() > 0) {
                    System.out.println("Booking deleted successfully!");
                } else {
                    System.out.println("Booking was not deleted.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Delete failed: " + e.getMessage());
        }
    }
}
