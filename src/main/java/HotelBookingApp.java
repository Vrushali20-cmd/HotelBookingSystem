import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class HotelBookingApp {

    private static final String URL =
            "jdbc:mysql://localhost:3306/hoteldb";
    private static final String USER = "root";
    private static final String PASSWORD =
            System.getenv("DB_PASSWORD");

    public static void main(String[] args) {

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
                    System.out.print("Enter your choice: ");

                    int choice = sc.nextInt();

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
                                    "Thank you for using the system!");
                            break;

                        default:
                            System.out.println("Invalid choice.");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // BOOK A ROOM
    private static void bookARoom(Connection con, Scanner sc) {

        sc.nextLine();

        System.out.print("Enter customer name: ");
        String name = sc.nextLine();

        System.out.print("Enter phone number: ");
        String phoneNumber = sc.nextLine();

        System.out.print("Enter room number: ");
        int roomNumber = sc.nextInt();

        String query =
                "INSERT INTO booking_table " +
                        "(name, phone_number, room_number) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setString(1, name);
            pstmt.setString(2, phoneNumber);
            pstmt.setInt(3, roomNumber);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Room booked successfully!");
            }

        } catch (Exception e) {
            System.out.println("Booking failed: " + e.getMessage());
        }
    }

    // VIEW ONE BOOKING
    private static void viewBooking(Connection con, Scanner sc) {

        System.out.print("Enter booking ID: ");
        int bookingId = sc.nextInt();

        String query =
                "SELECT id, name, phone_number, room_number, timestamp " +
                        "FROM booking_table WHERE id = ?";

        try (PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setInt(1, bookingId);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    System.out.println("\n===== BOOKING DETAILS =====");
                    System.out.println("Booking ID: " + rs.getInt("id"));
                    System.out.println("Customer Name: " + rs.getString("name"));
                    System.out.println("Phone Number: " + rs.getString("phone_number"));
                    System.out.println("Room Number: " + rs.getInt("room_number"));
                    System.out.println("Booking Time: " + rs.getTimestamp("timestamp"));
                } else {
                    System.out.println(
                            "No booking found with ID: " + bookingId);
                }
            }

        } catch (Exception e) {
            System.out.println("Unable to fetch booking: " + e.getMessage());
        }
    }

    // VIEW ALL BOOKINGS
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

                System.out.println("\nBooking ID: " + rs.getInt("id"));
                System.out.println("Customer Name: " + rs.getString("name"));
                System.out.println("Phone Number: " + rs.getString("phone_number"));
                System.out.println("Room Number: " + rs.getInt("room_number"));
                System.out.println("Booking Time: " + rs.getTimestamp("timestamp"));
                System.out.println("----------------------------------");
            }

            if (!found) {
                System.out.println("No bookings found.");
            }

        } catch (Exception e) {
            System.out.println("Unable to fetch bookings: " + e.getMessage());
        }
    }

    // UPDATE BOOKING
    private static void updateBooking(Connection con, Scanner sc) {

        System.out.print("Enter booking ID to update: ");
        int bookingId = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter new customer name: ");
        String name = sc.nextLine();

        System.out.print("Enter new phone number: ");
        String phoneNumber = sc.nextLine();

        System.out.print("Enter new room number: ");
        int roomNumber = sc.nextInt();

        String query =
                "UPDATE booking_table " +
                        "SET name = ?, phone_number = ?, room_number = ? " +
                        "WHERE id = ?";

        try (PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setString(1, name);
            pstmt.setString(2, phoneNumber);
            pstmt.setInt(3, roomNumber);
            pstmt.setInt(4, bookingId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Booking updated successfully!");
            } else {
                System.out.println(
                        "No booking found with ID: " + bookingId);
            }

        } catch (Exception e) {
            System.out.println("Update failed: " + e.getMessage());
        }
    }

    // DELETE BOOKING
    private static void deleteBooking(Connection con, Scanner sc) {

        System.out.print("Enter booking ID to delete: ");
        int bookingId = sc.nextInt();

        String query = "DELETE FROM booking_table WHERE id = ?";

        try (PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setInt(1, bookingId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Booking deleted successfully!");
            } else {
                System.out.println(
                        "No booking found with ID: " + bookingId);
            }

        } catch (Exception e) {
            System.out.println("Delete failed: " + e.getMessage());
        }
    }
}
