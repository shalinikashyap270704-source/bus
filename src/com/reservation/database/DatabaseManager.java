package com.reservation.database;

import com.reservation.exception.CustomExceptions.InvalidInputException;
import com.reservation.exception.CustomExceptions.SeatBookingException;
import com.reservation.model.Booking;
import com.reservation.model.Bus;
import com.reservation.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JDBC Database Manager class.
 * Handles connections, schema definition, and transaction logic.
 */
public class DatabaseManager {
    private static final String DB_FILE = "bus_reservation.db";
    private static final String CONNECTION_URL = "jdbc:sqlite:" + DB_FILE;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load SQLite JDBC Driver.");
            e.printStackTrace();
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL);
    }

    /**
     * Initializes the database tables and runs schema migration tasks.
     */
    public static void initializeDatabase() {
        String createBusesTable = "CREATE TABLE IF NOT EXISTS buses ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "bus_number TEXT UNIQUE NOT NULL,"
                + "source TEXT NOT NULL,"
                + "destination TEXT NOT NULL,"
                + "departure_time TEXT NOT NULL,"
                + "total_seats INTEGER NOT NULL,"
                + "available_seats INTEGER NOT NULL,"
                + "fare REAL NOT NULL,"
                + "bus_type TEXT NOT NULL,"
                + "reserved_seats TEXT"
                + ");";

        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                + "username TEXT PRIMARY KEY,"
                + "password TEXT NOT NULL,"
                + "role TEXT NOT NULL,"
                + "name TEXT NOT NULL,"
                + "phone TEXT NOT NULL"
                + ");";

        String createBookingsTable = "CREATE TABLE IF NOT EXISTS bookings ("
                + "booking_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "customer_name TEXT NOT NULL,"
                + "customer_phone TEXT NOT NULL,"
                + "bus_id INTEGER NOT NULL,"
                + "seat_numbers TEXT NOT NULL,"
                + "booking_date TEXT NOT NULL,"
                + "status TEXT NOT NULL,"
                + "total_amount REAL NOT NULL,"
                + "username TEXT,"
                + "FOREIGN KEY (bus_id) REFERENCES buses (id),"
                + "FOREIGN KEY (username) REFERENCES users (username)"
                + ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createBusesTable);
            stmt.execute(createUsersTable);
            stmt.execute(createBookingsTable);

            // Schema Migration: Add username column to bookings if it doesn't exist
            try {
                stmt.execute("ALTER TABLE bookings ADD COLUMN username TEXT;");
                System.out.println("Database migration: added 'username' column to bookings table.");
            } catch (SQLException e) {
                // Column already exists, ignore
            }

            // Seed default users if empty
            ResultSet rsUsers = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rsUsers.next() && rsUsers.getInt(1) == 0) {
                seedDefaultUsers(conn);
            }
            
            // Seed sample buses if empty
            ResultSet rsBuses = stmt.executeQuery("SELECT COUNT(*) FROM buses");
            if (rsBuses.next() && rsBuses.getInt(1) == 0) {
                seedSampleBuses(conn);
            }
            
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedDefaultUsers(Connection conn) throws SQLException {
        String insertSQL = "INSERT INTO users (username, password, role, name, phone) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            // Admin
            pstmt.setString(1, "admin");
            pstmt.setString(2, "admin123");
            pstmt.setString(3, "ADMIN");
            pstmt.setString(4, "Administrator");
            pstmt.setString(5, "0000000000");
            pstmt.executeUpdate();

            // Default standard user
            pstmt.setString(1, "user");
            pstmt.setString(2, "user123");
            pstmt.setString(3, "USER");
            pstmt.setString(4, "Sample Customer");
            pstmt.setString(5, "9999999999");
            pstmt.executeUpdate();

            System.out.println("Database seeded with default users (admin & user).");
        }
    }

    private static void seedSampleBuses(Connection conn) throws SQLException {
        String insertSQL = "INSERT INTO buses (bus_number, source, destination, departure_time, total_seats, available_seats, fare, bus_type, reserved_seats) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            Object[][] sampleData = {
                {"BUS-101", "Mumbai", "Pune", "08:00 AM", 32, 32, 450.0, "AC Seater", ""},
                {"BUS-102", "Mumbai", "Pune", "02:00 PM", 32, 32, 500.0, "AC Sleeper", ""},
                {"BUS-201", "Delhi", "Jaipur", "06:30 AM", 32, 32, 650.0, "AC Sleeper", ""},
                {"BUS-202", "Delhi", "Jaipur", "09:00 PM", 32, 32, 550.0, "Non-AC Sleeper", ""},
                {"BUS-301", "Bangalore", "Chennai", "10:00 AM", 32, 32, 750.0, "AC Seater", ""},
                {"BUS-302", "Bangalore", "Hyderabad", "09:30 PM", 32, 32, 950.0, "AC Sleeper Multi-Axle", ""}
            };

            for (Object[] row : sampleData) {
                pstmt.setString(1, (String) row[0]);
                pstmt.setString(2, (String) row[1]);
                pstmt.setString(3, (String) row[2]);
                pstmt.setString(4, (String) row[3]);
                pstmt.setInt(5, (Integer) row[4]);
                pstmt.setInt(6, (Integer) row[5]);
                pstmt.setDouble(7, (Double) row[6]);
                pstmt.setString(8, (String) row[7]);
                pstmt.setString(9, (String) row[8]);
                pstmt.executeUpdate();
            }
            System.out.println("Database seeded with sample buses.");
        }
    }

    // --- Authentication & User Operations ---

    /**
     * Authenticates a user by credentials.
     * Returns User object if verified, null otherwise.
     */
    public static User authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("name"),
                            rs.getString("phone")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Registers a new User (Defaults role to 'USER').
     */
    public static void registerUser(String username, String password, String name, String phone) 
            throws InvalidInputException, SQLException {
        
        // Check if username already exists
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, username);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new InvalidInputException("Username '" + username + "' is already taken. Please choose another.");
                }
            }
        }

        // Insert new user
        String insertSql = "INSERT INTO users (username, password, role, name, phone) VALUES (?, ?, 'USER', ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, name);
            pstmt.setString(4, phone);
            pstmt.executeUpdate();
        }
    }

    // --- Bus Operations (Admin & General) ---

    public static List<Bus> getAllBuses() {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT * FROM buses ORDER BY id ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractBus(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Bus> searchBuses(String source, String dest) {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT * FROM buses WHERE source LIKE ? AND destination LIKE ? ORDER BY id ASC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + source + "%");
            pstmt.setString(2, "%" + dest + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractBus(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Bus getBusById(int busId) {
        String sql = "SELECT * FROM buses WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, busId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractBus(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Admin DML: Adds a new Bus.
     */
    public static void addBus(String busNo, String src, String dest, String time, int seats, double fare, String type) 
            throws InvalidInputException, SQLException {
        
        // Check if bus number already exists
        String checkSql = "SELECT COUNT(*) FROM buses WHERE bus_number = ?";
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, busNo);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new InvalidInputException("Bus Number '" + busNo + "' already exists.");
                }
            }
        }

        String sql = "INSERT INTO buses (bus_number, source, destination, departure_time, total_seats, available_seats, fare, bus_type, reserved_seats) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, busNo);
            pstmt.setString(2, src);
            pstmt.setString(3, dest);
            pstmt.setString(4, time);
            pstmt.setInt(5, seats);
            pstmt.setInt(6, seats); // all available initially
            pstmt.setDouble(7, fare);
            pstmt.setString(8, type);
            pstmt.setString(9, "");
            pstmt.executeUpdate();
        }
    }

    /**
     * Admin DML: Deletes a bus and cancels associated active bookings.
     */
    public static void deleteBus(int busId) throws SQLException {
        Connection conn = null;
        PreparedStatement cancelBookingsStmt = null;
        PreparedStatement deleteBusStmt = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1. Cancel active bookings for this bus
            String cancelBookingsSql = "UPDATE bookings SET status = 'CANCELLED' WHERE bus_id = ? AND status = 'ACTIVE'";
            cancelBookingsStmt = conn.prepareStatement(cancelBookingsSql);
            cancelBookingsStmt.setInt(1, busId);
            cancelBookingsStmt.executeUpdate();

            // 2. Delete the bus
            String deleteBusSql = "DELETE FROM buses WHERE id = ?";
            deleteBusStmt = conn.prepareStatement(deleteBusSql);
            deleteBusStmt.setInt(1, busId);
            deleteBusStmt.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (cancelBookingsStmt != null) cancelBookingsStmt.close();
            if (deleteBusStmt != null) deleteBusStmt.close();
            if (conn != null) conn.close();
        }
    }

    // --- Booking / Seat Reservation Operations (Transactional) ---

    /**
     * Transactional seat reservation linked to a username.
     */
    public static int reserveSeats(int busId, String name, String phone, List<Integer> seatNums, double totalAmount, String username) 
            throws SeatBookingException, SQLException {
        
        Connection conn = null;
        PreparedStatement selectBusStmt = null;
        PreparedStatement updateBusStmt = null;
        PreparedStatement insertBookingStmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String selectBusSql = "SELECT reserved_seats, total_seats, available_seats FROM buses WHERE id = ?";
            selectBusStmt = conn.prepareStatement(selectBusSql);
            selectBusStmt.setInt(1, busId);
            rs = selectBusStmt.executeQuery();

            if (!rs.next()) {
                throw new SeatBookingException("Bus not found with ID: " + busId);
            }

            String currentReservedStr = rs.getString("reserved_seats");
            int totalSeats = rs.getInt("total_seats");
            int currentAvailable = rs.getInt("available_seats");

            List<Integer> reservedList = new ArrayList<>();
            if (currentReservedStr != null && !currentReservedStr.trim().isEmpty()) {
                for (String s : currentReservedStr.split(",")) {
                    reservedList.add(Integer.parseInt(s.trim()));
                }
            }

            for (int seat : seatNums) {
                if (seat < 1 || seat > totalSeats) {
                    throw new SeatBookingException("Invalid seat number requested: " + seat);
                }
                if (reservedList.contains(seat)) {
                    throw new SeatBookingException("Seat " + seat + " is already reserved. Please select another seat.");
                }
            }

            reservedList.addAll(seatNums);
            String newReservedStr = reservedList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            int newAvailableSeats = currentAvailable - seatNums.size();

            String updateBusSql = "UPDATE buses SET reserved_seats = ?, available_seats = ? WHERE id = ?";
            updateBusStmt = conn.prepareStatement(updateBusSql);
            updateBusStmt.setString(1, newReservedStr);
            updateBusStmt.setInt(2, newAvailableSeats);
            updateBusStmt.setInt(3, busId);
            updateBusStmt.executeUpdate();

            String insertBookingSql = "INSERT INTO bookings (customer_name, customer_phone, bus_id, seat_numbers, booking_date, status, total_amount, username) "
                    + "VALUES (?, ?, ?, ?, datetime('now', 'localtime'), 'ACTIVE', ?, ?)";
            insertBookingStmt = conn.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS);
            insertBookingStmt.setString(1, name);
            insertBookingStmt.setString(2, phone);
            insertBookingStmt.setInt(3, busId);
            
            String bookedSeatsStr = seatNums.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            insertBookingStmt.setString(4, bookedSeatsStr);
            insertBookingStmt.setDouble(5, totalAmount);
            insertBookingStmt.setString(6, username); // Associated username
            insertBookingStmt.executeUpdate();

            int generatedBookingId = -1;
            try (ResultSet keys = insertBookingStmt.getGeneratedKeys()) {
                if (keys.next()) {
                    generatedBookingId = keys.getInt(1);
                }
            }

            conn.commit();
            return generatedBookingId;

        } catch (SQLException | SeatBookingException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (selectBusStmt != null) selectBusStmt.close();
            if (updateBusStmt != null) updateBusStmt.close();
            if (insertBookingStmt != null) insertBookingStmt.close();
            if (conn != null) conn.close();
        }
    }

    public static void cancelBooking(int bookingId) throws SeatBookingException, SQLException {
        Connection conn = null;
        PreparedStatement selectBookingStmt = null;
        PreparedStatement selectBusStmt = null;
        PreparedStatement updateBusStmt = null;
        PreparedStatement updateBookingStmt = null;
        ResultSet rsBooking = null;
        ResultSet rsBus = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String selectBookingSql = "SELECT * FROM bookings WHERE booking_id = ?";
            selectBookingStmt = conn.prepareStatement(selectBookingSql);
            selectBookingStmt.setInt(1, bookingId);
            rsBooking = selectBookingStmt.executeQuery();

            if (!rsBooking.next()) {
                throw new SeatBookingException("Booking ID " + bookingId + " not found.");
            }

            String status = rsBooking.getString("status");
            if ("CANCELLED".equals(status)) {
                throw new SeatBookingException("Booking ID " + bookingId + " is already cancelled.");
            }

            int busId = rsBooking.getInt("bus_id");
            String seatsToCancelStr = rsBooking.getString("seat_numbers");
            List<Integer> seatsToCancel = Arrays.stream(seatsToCancelStr.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            String selectBusSql = "SELECT reserved_seats, available_seats FROM buses WHERE id = ?";
            selectBusStmt = conn.prepareStatement(selectBusSql);
            selectBusStmt.setInt(1, busId);
            rsBus = selectBusStmt.executeQuery();

            if (!rsBus.next()) {
                throw new SeatBookingException("Associated bus not found.");
            }

            String currentReservedStr = rsBus.getString("reserved_seats");
            int availableSeats = rsBus.getInt("available_seats");

            List<Integer> reservedList = new ArrayList<>();
            if (currentReservedStr != null && !currentReservedStr.trim().isEmpty()) {
                for (String s : currentReservedStr.split(",")) {
                    reservedList.add(Integer.parseInt(s.trim()));
                }
            }

            reservedList.removeAll(seatsToCancel);
            String newReservedStr = reservedList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            int newAvailableSeats = availableSeats + seatsToCancel.size();

            String updateBusSql = "UPDATE buses SET reserved_seats = ?, available_seats = ? WHERE id = ?";
            updateBusStmt = conn.prepareStatement(updateBusSql);
            updateBusStmt.setString(1, newReservedStr);
            updateBusStmt.setInt(2, newAvailableSeats);
            updateBusStmt.setInt(3, busId);
            updateBusStmt.executeUpdate();

            String updateBookingSql = "UPDATE bookings SET status = 'CANCELLED' WHERE booking_id = ?";
            updateBookingStmt = conn.prepareStatement(updateBookingSql);
            updateBookingStmt.setInt(1, bookingId);
            updateBookingStmt.executeUpdate();

            conn.commit();

        } catch (SQLException | SeatBookingException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (rsBooking != null) rsBooking.close();
            if (rsBus != null) rsBus.close();
            if (selectBookingStmt != null) selectBookingStmt.close();
            if (selectBusStmt != null) selectBusStmt.close();
            if (updateBusStmt != null) updateBusStmt.close();
            if (updateBookingStmt != null) updateBookingStmt.close();
            if (conn != null) conn.close();
        }
    }

    /**
     * Admin view: Fetch all bookings.
     */
    public static List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings ORDER BY booking_id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractBooking(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * User-isolated view: Fetch bookings for a specific customer login.
     */
    public static List<Booking> getBookingsByUsername(String username) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE username = ? ORDER BY booking_id DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractBooking(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Booking> getBookingsByPhone(String phone) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE customer_phone = ? ORDER BY booking_id DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractBooking(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Filtered search for Admin: Search by Phone or Passenger Name.
     */
    public static List<Booking> searchBookingsAdmin(String query) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE customer_name LIKE ? OR customer_phone LIKE ? ORDER BY booking_id DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractBooking(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Booking getBookingById(int bookingId) {
        String sql = "SELECT * FROM bookings WHERE booking_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractBooking(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- Object Extraction Helpers ---
    private static Bus extractBus(ResultSet rs) throws SQLException {
        return new Bus(
                rs.getInt("id"),
                rs.getString("bus_number"),
                rs.getString("source"),
                rs.getString("destination"),
                rs.getString("departure_time"),
                rs.getInt("total_seats"),
                rs.getInt("available_seats"),
                rs.getDouble("fare"),
                rs.getString("bus_type"),
                rs.getString("reserved_seats")
        );
    }

    private static Booking extractBooking(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getInt("booking_id"),
                rs.getString("customer_name"),
                rs.getString("customer_phone"),
                rs.getInt("bus_id"),
                rs.getString("seat_numbers"),
                rs.getString("booking_date"),
                rs.getString("status"),
                rs.getDouble("total_amount")
        );
    }
}
