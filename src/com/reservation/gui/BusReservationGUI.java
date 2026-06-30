package com.reservation.gui;

import com.reservation.database.DatabaseManager;
import com.reservation.exception.CustomExceptions.InvalidInputException;
import com.reservation.exception.CustomExceptions.SeatBookingException;
import com.reservation.model.Booking;
import com.reservation.model.Bus;
import com.reservation.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bus Reservation GUI styled with FlatLaf.
 * Features a Top-Level CardLayout for Auth (Login, Register) and Main App.
 */
public class BusReservationGUI extends JFrame {
    // Top-Level Navigation
    private CardLayout topCardLayout;
    private JPanel topContainer;

    // Current State
    private User currentUser;

    // Login Panel Components
    private JTextField txtLoginUser;
    private JPasswordField txtLoginPass;

    // Register Panel Components
    private JTextField txtRegName;
    private JTextField txtRegPhone;
    private JTextField txtRegUser;
    private JPasswordField txtRegPass;
    private JPasswordField txtRegPassConfirm;

    // --- Main App Subcomponents ---
    private JPanel mainAppPanel;
    private JPanel sidebarPanel;
    private CardLayout contentCardLayout;
    private JPanel contentAreaPanel;

    // Sidebar navigation buttons
    private JButton btnSearchTab;
    private JButton btnBookTab;
    private JButton btnMyBookingsTab;
    private JButton btnAboutTab;
    private JButton btnLogoutTab;

    // Admin Sidebar navigation buttons
    private JButton btnAdminBusesTab;
    private JButton btnAdminBookingsTab;

    // --- Card: Search & View Buses (User View) ---
    private JTextField txtSearchSource;
    private JTextField txtSearchDest;
    private JTable tblBuses;
    private DefaultTableModel busTableModel;

    // --- Card: Seat Booking (User View) ---
    private Bus selectedBus;
    private JLabel lblBookBusNum;
    private JLabel lblBookRoute;
    private JLabel lblBookTime;
    private JLabel lblBookFare;
    private JLabel lblSelectedSeats;
    private JLabel lblTotalAmount;
    private JTextField txtPassengerName;
    private JTextField txtPassengerPhone;
    private JPanel pnlSeatsGrid;
    private List<Integer> selectedSeatsList = new ArrayList<>();
    private List<JToggleButton> seatButtons = new ArrayList<>();

    // --- Card: My Bookings (User View) ---
    private JTable tblUserBookings;
    private DefaultTableModel userBookingTableModel;

    // --- Card: Manage Buses (Admin View) ---
    private JTextField txtAdminBusNo;
    private JTextField txtAdminSource;
    private JTextField txtAdminDest;
    private JTextField txtAdminTime;
    private JTextField txtAdminFare;
    private JComboBox<String> cmbAdminType;
    private JTable tblAdminBuses;
    private DefaultTableModel adminBusesModel;

    // --- Card: Global Bookings (Admin View) ---
    private JTextField txtAdminSearchQuery;
    private JTable tblAdminBookings;
    private DefaultTableModel adminBookingsModel;

    public BusReservationGUI() {
        setTitle("LogiTrack Bus Ticket Reservation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null); // Center on screen
        setMinimumSize(new Dimension(950, 600));

        // Top Level Container uses CardLayout to separate Login/Register from Dashboard
        topCardLayout = new CardLayout();
        topContainer = new JPanel(topCardLayout);

        topContainer.add(createLoginPanel(), "LOGIN_PANEL");
        topContainer.add(createRegisterPanel(), "REGISTER_PANEL");

        // The Dashboard panel is created but only populated and shown after successful login
        mainAppPanel = new JPanel(new BorderLayout());
        topContainer.add(mainAppPanel, "MAIN_APP_PANEL");

        add(topContainer);
        topCardLayout.show(topContainer, "LOGIN_PANEL");
    }

    // ==========================================
    // 1. LOGIN & REGISTRATION PANELS
    // ==========================================

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(25, 25, 25));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 50), 1),
                new EmptyBorder(30, 40, 30, 40)
        ));
        card.setBackground(new Color(33, 33, 33));
        card.setPreferredSize(new Dimension(420, 380));
        card.setMaximumSize(new Dimension(420, 380));

        JLabel lblTitle = new JLabel("LogiTrack Login");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(78, 161, 243));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Access user bookings and administrator dashboard");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.GRAY);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(lblSub);
        card.add(Box.createRigidArea(new Dimension(0, 30)));

        // Inputs Panel
        JPanel form = new JPanel(new GridLayout(4, 1, 5, 5));
        form.setOpaque(false);

        form.add(new JLabel("Username:"));
        txtLoginUser = new JTextField(15);
        form.add(txtLoginUser);

        form.add(new JLabel("Password:"));
        txtLoginPass = new JPasswordField(15);
        form.add(txtLoginPass);

        card.add(form);
        card.add(Box.createRigidArea(new Dimension(0, 25)));

        // Buttons Panel
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        btnPanel.setOpaque(false);

        JButton btnLogin = new JButton("Sign In");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(78, 161, 243));
        btnLogin.setForeground(Color.BLACK);
        btnLogin.addActionListener(e -> handleLogin());
        btnPanel.add(btnLogin);

        JButton btnGoRegister = new JButton("Register");
        btnGoRegister.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnGoRegister.addActionListener(e -> {
            clearRegisterFields();
            topCardLayout.show(topContainer, "REGISTER_PANEL");
        });
        btnPanel.add(btnGoRegister);

        card.add(btnPanel);

        panel.add(card);
        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(25, 25, 25));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 50), 1),
                new EmptyBorder(25, 40, 25, 40)
        ));
        card.setBackground(new Color(33, 33, 33));
        card.setPreferredSize(new Dimension(450, 500));
        card.setMaximumSize(new Dimension(450, 500));

        JLabel lblTitle = new JLabel("Create Account");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(46, 204, 113));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Register to search routes and book bus tickets");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.GRAY);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(lblSub);
        card.add(Box.createRigidArea(new Dimension(0, 25)));

        // Form Fields
        JPanel form = new JPanel(new GridLayout(10, 1, 3, 3));
        form.setOpaque(false);

        form.add(new JLabel("Full Name:"));
        txtRegName = new JTextField();
        form.add(txtRegName);

        form.add(new JLabel("Contact Number (10 digits):"));
        txtRegPhone = new JTextField();
        form.add(txtRegPhone);

        form.add(new JLabel("Username:"));
        txtRegUser = new JTextField();
        form.add(txtRegUser);

        form.add(new JLabel("Password:"));
        txtRegPass = new JPasswordField();
        form.add(txtRegPass);

        form.add(new JLabel("Confirm Password:"));
        txtRegPassConfirm = new JPasswordField();
        form.add(txtRegPassConfirm);

        card.add(form);
        card.add(Box.createRigidArea(new Dimension(0, 25)));

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        btnPanel.setOpaque(false);

        JButton btnRegisterSubmit = new JButton("Sign Up");
        btnRegisterSubmit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegisterSubmit.setBackground(new Color(46, 204, 113));
        btnRegisterSubmit.setForeground(Color.BLACK);
        btnRegisterSubmit.addActionListener(e -> handleRegistration());
        btnPanel.add(btnRegisterSubmit);

        JButton btnBackLogin = new JButton("← Back to Login");
        btnBackLogin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnBackLogin.addActionListener(e -> topCardLayout.show(topContainer, "LOGIN_PANEL"));
        btnPanel.add(btnBackLogin);

        card.add(btnPanel);

        panel.add(card);
        return panel;
    }

    private void handleLogin() {
        String username = txtLoginUser.getText().trim();
        String password = new String(txtLoginPass.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            User user = DatabaseManager.authenticateUser(username, password);
            if (user != null) {
                this.currentUser = user;
                initializeDashboardPanel(); // dynamic components load
                topCardLayout.show(topContainer, "MAIN_APP_PANEL");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database connection error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void handleRegistration() {
        try {
            String name = txtRegName.getText().trim();
            String phone = txtRegPhone.getText().trim();
            String username = txtRegUser.getText().trim();
            String pass = new String(txtRegPass.getPassword()).trim();
            String passConfirm = new String(txtRegPassConfirm.getPassword()).trim();

            // Validations
            if (name.isEmpty() || phone.isEmpty() || username.isEmpty() || pass.isEmpty()) {
                throw new InvalidInputException("All fields are mandatory.");
            }
            if (!phone.matches("^[0-9]{10}$")) {
                throw new InvalidInputException("Please enter a valid 10-digit phone number.");
            }
            if (username.length() < 3) {
                throw new InvalidInputException("Username must be at least 3 characters long.");
            }
            if (pass.length() < 4) {
                throw new InvalidInputException("Password must be at least 4 characters long.");
            }
            if (!pass.equals(passConfirm)) {
                throw new InvalidInputException("Passwords do not match.");
            }

            DatabaseManager.registerUser(username, pass, name, phone);
            JOptionPane.showMessageDialog(this, "Registration successful! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            topCardLayout.show(topContainer, "LOGIN_PANEL");
            
            // Pre-fill username for convenience
            txtLoginUser.setText(username);
            txtLoginPass.setText("");

        } catch (InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "SQL Exception", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void clearRegisterFields() {
        txtRegName.setText("");
        txtRegPhone.setText("");
        txtRegUser.setText("");
        txtRegPass.setText("");
        txtRegPassConfirm.setText("");
    }

    // ==========================================
    // 2. MAIN APPLICATION DASHBOARD SETUP
    // ==========================================

    private void initializeDashboardPanel() {
        mainAppPanel.removeAll();

        // Left Panel (Sidebar)
        sidebarPanel = createSidebarPanel();
        mainAppPanel.add(sidebarPanel, BorderLayout.WEST);

        // Right Panel (Content CardLayout)
        contentCardLayout = new CardLayout();
        contentAreaPanel = new JPanel(contentCardLayout);
        contentAreaPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Add Role-Based Cards
        if (currentUser.isAdmin()) {
            contentAreaPanel.add(createAdminBusesPanel(), "ADMIN_BUSES");
            contentAreaPanel.add(createAdminBookingsPanel(), "ADMIN_HISTORY");
        } else {
            contentAreaPanel.add(createSearchBusesPanel(), "SEARCH_BUSES");
            contentAreaPanel.add(createSeatBookingPanel(), "SEAT_BOOKING");
            contentAreaPanel.add(createUserBookingsPanel(), "USER_HISTORY");
        }
        contentAreaPanel.add(createAboutPanel(), "ABOUT");

        mainAppPanel.add(contentAreaPanel, BorderLayout.CENTER);

        // Initialize state view
        if (currentUser.isAdmin()) {
            refreshAdminBusesTable();
            refreshAdminBookingsTable(DatabaseManager.getAllBookings());
            switchTab("ADMIN_BUSES", btnAdminBusesTab);
        } else {
            selectedBus = null;
            refreshBusTable(DatabaseManager.getAllBuses());
            refreshUserBookingsTable();
            switchTab("SEARCH_BUSES", btnSearchTab);
        }

        mainAppPanel.revalidate();
        mainAppPanel.repaint();
    }

    private JPanel createSidebarPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(240, getHeight()));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(new EmptyBorder(25, 15, 25, 15));

        // Header Logo
        JLabel lblLogo = new JLabel("LogiTrack Bus");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogo.setForeground(new Color(78, 161, 243));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblUserRole = new JLabel(currentUser.isAdmin() ? "ADMIN PORTAL" : "CUSTOMER PORTAL");
        lblUserRole.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblUserRole.setForeground(currentUser.isAdmin() ? new Color(231, 76, 60) : new Color(46, 204, 113));
        lblUserRole.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblUserDetail = new JLabel("Hello, " + currentUser.getName());
        lblUserDetail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUserDetail.setForeground(Color.LIGHT_GRAY);
        lblUserDetail.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(lblLogo);
        panel.add(lblUserRole);
        panel.add(lblUserDetail);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Standard User Buttons
        btnSearchTab = createNavButton("Search & View Buses");
        btnBookTab = createNavButton("Reserve Seats");
        btnMyBookingsTab = createNavButton("My Tickets");

        // Admin Buttons
        btnAdminBusesTab = createNavButton("Manage Buses");
        btnAdminBookingsTab = createNavButton("Global Booking Log");

        // Shared Buttons
        btnAboutTab = createNavButton("About System");
        btnLogoutTab = createNavButton("Log Out");

        if (currentUser.isAdmin()) {
            panel.add(btnAdminBusesTab);
            panel.add(Box.createRigidArea(new Dimension(0, 15)));
            panel.add(btnAdminBookingsTab);
        } else {
            panel.add(btnSearchTab);
            panel.add(Box.createRigidArea(new Dimension(0, 15)));
            panel.add(btnBookTab);
            panel.add(Box.createRigidArea(new Dimension(0, 15)));
            panel.add(btnMyBookingsTab);
        }
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(btnAboutTab);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(btnLogoutTab);

        panel.add(Box.createVerticalGlue());

        // Footer version info
        JLabel lblVersion = new JLabel("Logged in as " + currentUser.getUsername());
        lblVersion.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblVersion.setForeground(Color.GRAY);
        lblVersion.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblVersion);

        // Navigation Actions
        btnSearchTab.addActionListener(e -> switchTab("SEARCH_BUSES", btnSearchTab));
        btnBookTab.addActionListener(e -> {
            if (selectedBus == null) {
                JOptionPane.showMessageDialog(this, "Select a bus from 'Search & View Buses' first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            } else {
                switchTab("SEAT_BOOKING", btnBookTab);
            }
        });
        btnMyBookingsTab.addActionListener(e -> {
            refreshUserBookingsTable();
            switchTab("USER_HISTORY", btnMyBookingsTab);
        });

        btnAdminBusesTab.addActionListener(e -> {
            refreshAdminBusesTable();
            switchTab("ADMIN_BUSES", btnAdminBusesTab);
        });
        btnAdminBookingsTab.addActionListener(e -> {
            refreshAdminBookingsTable(DatabaseManager.getAllBookings());
            switchTab("ADMIN_HISTORY", btnAdminBookingsTab);
        });

        btnAboutTab.addActionListener(e -> switchTab("ABOUT", btnAboutTab));
        
        btnLogoutTab.addActionListener(e -> {
            currentUser = null;
            txtLoginUser.setText("");
            txtLoginPass.setText("");
            topCardLayout.show(topContainer, "LOGIN_PANEL");
        });

        return panel;
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(210, 40));
        button.setPreferredSize(new Dimension(210, 40));
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void switchTab(String cardName, JButton activeBtn) {
        contentCardLayout.show(contentAreaPanel, cardName);

        JButton[] buttons = {btnSearchTab, btnBookTab, btnMyBookingsTab, btnAdminBusesTab, btnAdminBookingsTab, btnAboutTab};
        for (JButton btn : buttons) {
            if (btn == activeBtn) {
                btn.setBackground(new Color(78, 161, 243));
                btn.setForeground(Color.BLACK);
            } else {
                btn.setBackground(null);
                btn.setForeground(null);
            }
        }
    }

    // ==========================================
    // 3. USER PAGES (SEARCH, BOOKING, HISTORY)
    // ==========================================

    private JPanel createSearchBusesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("Available Bus Routes");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel lblDesc = new JLabel("Specify route filters, select a bus, and book seats.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(Color.GRAY);
        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblDesc, BorderLayout.SOUTH);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Filter Form
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));

        filterPanel.add(new JLabel("Source:"));
        txtSearchSource = new JTextField(12);
        filterPanel.add(txtSearchSource);

        filterPanel.add(new JLabel("Destination:"));
        txtSearchDest = new JTextField(12);
        filterPanel.add(txtSearchDest);

        JButton btnSearch = new JButton("Find Buses");
        btnSearch.setBackground(new Color(78, 161, 243));
        btnSearch.setForeground(Color.BLACK);
        filterPanel.add(btnSearch);

        JButton btnClear = new JButton("Clear");
        filterPanel.add(btnClear);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.add(filterPanel, BorderLayout.NORTH);

        // Buses Table
        String[] columns = {"ID", "Bus Number", "Source", "Destination", "Departure Time", "Available Seats", "Fare", "Bus Type"};
        busTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblBuses = new JTable(busTableModel);
        tblBuses.setRowHeight(35);
        tblBuses.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblBuses.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblBuses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tblBuses.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblBuses.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tblBuses.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tblBuses.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        tblBuses.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(tblBuses);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnProceed = new JButton("Proceed to Book Seats →");
        btnProceed.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnProceed.setBackground(new Color(46, 204, 113));
        btnProceed.setForeground(Color.BLACK);
        btnProceed.setPreferredSize(new Dimension(230, 40));
        actionPanel.add(btnProceed);
        panel.add(actionPanel, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> {
            String src = txtSearchSource.getText().trim();
            String dest = txtSearchDest.getText().trim();
            refreshBusTable(DatabaseManager.searchBuses(src, dest));
        });

        btnClear.addActionListener(e -> {
            txtSearchSource.setText("");
            txtSearchDest.setText("");
            refreshBusTable(DatabaseManager.getAllBuses());
        });

        btnProceed.addActionListener(e -> proceedToSeatMap());
        
        tblBuses.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) proceedToSeatMap();
            }
        });

        return panel;
    }

    private void proceedToSeatMap() {
        int selectedRow = tblBuses.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a bus from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int busId = (int) tblBuses.getValueAt(selectedRow, 0);
        Bus bus = DatabaseManager.getBusById(busId);
        if (bus != null) {
            this.selectedBus = bus;
            loadSeatBookingPanel();
            switchTab("SEAT_BOOKING", btnBookTab);
        }
    }

    private void refreshBusTable(List<Bus> buses) {
        busTableModel.setRowCount(0);
        for (Bus bus : buses) {
            busTableModel.addRow(new Object[]{
                    bus.getId(),
                    bus.getBusNumber(),
                    bus.getSource(),
                    bus.getDestination(),
                    bus.getDepartureTime(),
                    bus.getAvailableSeats(),
                    "₹ " + String.format("%.2f", bus.getFare()),
                    bus.getBusType()
            });
        }
    }

    private JPanel createSeatBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));

        JPanel mainContent = new JPanel(new GridLayout(1, 2, 20, 0));

        // LEFT: Interactive Seating Map
        JPanel seatLayoutContainer = new JPanel(new BorderLayout(0, 10));
        JPanel seatHeader = new JPanel(new GridLayout(1, 2));
        JLabel lblFront = new JLabel("← FRONT (Driver Side)");
        lblFront.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFront.setForeground(Color.GRAY);
        seatHeader.add(lblFront);

        JPanel pnlLegend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlLegend.add(createLegendItem("Available", new Color(46, 204, 113)));
        pnlLegend.add(createLegendItem("Selected", new Color(52, 152, 219)));
        pnlLegend.add(createLegendItem("Booked", new Color(231, 76, 60)));
        seatHeader.add(pnlLegend);
        seatLayoutContainer.add(seatHeader, BorderLayout.NORTH);

        pnlSeatsGrid = new JPanel(new GridLayout(8, 5, 8, 8));
        pnlSeatsGrid.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));
        seatLayoutContainer.add(pnlSeatsGrid, BorderLayout.CENTER);
        mainContent.add(seatLayoutContainer);

        // RIGHT: Booking Form Panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1), "Reservation Ticket Form"),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel infoGrid = new JPanel(new GridLayout(6, 2, 5, 12));
        infoGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoGrid.add(new JLabel("Bus Number:"));
        lblBookBusNum = new JLabel("-");
        lblBookBusNum.setFont(new Font("Segoe UI", Font.BOLD, 13));
        infoGrid.add(lblBookBusNum);

        infoGrid.add(new JLabel("Route:"));
        lblBookRoute = new JLabel("-");
        lblBookRoute.setFont(new Font("Segoe UI", Font.BOLD, 13));
        infoGrid.add(lblBookRoute);

        infoGrid.add(new JLabel("Departure Time:"));
        lblBookTime = new JLabel("-");
        lblBookTime.setFont(new Font("Segoe UI", Font.BOLD, 13));
        infoGrid.add(lblBookTime);

        infoGrid.add(new JLabel("Fare (per seat):"));
        lblBookFare = new JLabel("-");
        lblBookFare.setFont(new Font("Segoe UI", Font.BOLD, 13));
        infoGrid.add(lblBookFare);

        infoGrid.add(new JLabel("Selected Seats:"));
        lblSelectedSeats = new JLabel("None");
        lblSelectedSeats.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSelectedSeats.setForeground(new Color(52, 152, 219));
        infoGrid.add(lblSelectedSeats);

        infoGrid.add(new JLabel("Total Price:"));
        lblTotalAmount = new JLabel("₹ 0.00");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalAmount.setForeground(new Color(46, 204, 113));
        infoGrid.add(lblTotalAmount);

        detailsPanel.add(infoGrid);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Passenger Form Inputs (Pre-filled with logged-in user profile details)
        JPanel formInput = new JPanel(new GridLayout(2, 2, 5, 12));
        formInput.setAlignmentX(Component.LEFT_ALIGNMENT);

        formInput.add(new JLabel("Passenger Name:"));
        txtPassengerName = new JTextField(15);
        formInput.add(txtPassengerName);

        formInput.add(new JLabel("Contact Number:"));
        txtPassengerPhone = new JTextField(15);
        formInput.add(txtPassengerPhone);

        detailsPanel.add(formInput);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Actions
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnCancelBook = new JButton("← Back");
        btnCancelBook.addActionListener(e -> {
            selectedSeatsList.clear();
            switchTab("SEARCH_BUSES", btnSearchTab);
        });
        actionRow.add(btnCancelBook);

        JButton btnConfirmBook = new JButton("Confirm Reservation ✓");
        btnConfirmBook.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConfirmBook.setBackground(new Color(46, 204, 113));
        btnConfirmBook.setForeground(Color.BLACK);
        btnConfirmBook.setPreferredSize(new Dimension(190, 40));
        btnConfirmBook.addActionListener(e -> handleUserBooking());
        actionRow.add(btnConfirmBook);

        detailsPanel.add(actionRow);
        mainContent.add(detailsPanel);

        panel.add(mainContent, BorderLayout.CENTER);

        JPanel bookingHeader = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Seat Reservation Map");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        bookingHeader.add(title, BorderLayout.WEST);
        panel.add(bookingHeader, BorderLayout.NORTH);

        return panel;
    }

    private void loadSeatBookingPanel() {
        if (selectedBus == null) return;

        selectedSeatsList.clear();
        seatButtons.clear();
        pnlSeatsGrid.removeAll();

        lblBookBusNum.setText(selectedBus.getBusNumber());
        lblBookRoute.setText(selectedBus.getSource() + " to " + selectedBus.getDestination());
        lblBookTime.setText(selectedBus.getDepartureTime());
        lblBookFare.setText("₹ " + String.format("%.2f", selectedBus.getFare()));
        lblSelectedSeats.setText("None");
        lblTotalAmount.setText("₹ 0.00");

        // Pre-fill fields with current user profile
        txtPassengerName.setText(currentUser.getName());
        txtPassengerPhone.setText(currentUser.getPhone());

        List<Integer> bookedSeats = selectedBus.getReservedSeatsList();

        int seatCounter = 1;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 5; c++) {
                if (c == 2) {
                    JLabel lblAisle = new JLabel("Aisle", JLabel.CENTER);
                    lblAisle.setFont(new Font("Segoe UI", Font.ITALIC, 10));
                    lblAisle.setForeground(Color.GRAY);
                    pnlSeatsGrid.add(lblAisle);
                } else {
                    int seatNumber = seatCounter++;
                    JToggleButton btnSeat = new JToggleButton(String.valueOf(seatNumber));
                    btnSeat.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    btnSeat.setFocusPainted(false);
                    btnSeat.setCursor(new Cursor(Cursor.HAND_CURSOR));

                    if (bookedSeats.contains(seatNumber)) {
                        btnSeat.setBackground(new Color(231, 76, 60));
                        btnSeat.setForeground(Color.WHITE);
                        btnSeat.setEnabled(false);
                    } else {
                        btnSeat.setBackground(new Color(46, 204, 113));
                        btnSeat.setForeground(Color.BLACK);
                        btnSeat.addActionListener(e -> {
                            if (btnSeat.isSelected()) {
                                btnSeat.setBackground(new Color(52, 152, 219));
                                btnSeat.setForeground(Color.WHITE);
                                selectedSeatsList.add(seatNumber);
                            } else {
                                btnSeat.setBackground(new Color(46, 204, 113));
                                btnSeat.setForeground(Color.BLACK);
                                selectedSeatsList.remove(Integer.valueOf(seatNumber));
                            }
                            updateSelectedSeatsUI();
                        });
                    }
                    seatButtons.add(btnSeat);
                    pnlSeatsGrid.add(btnSeat);
                }
            }
        }
        pnlSeatsGrid.revalidate();
        pnlSeatsGrid.repaint();
    }

    private void handleUserBooking() {
        try {
            String name = txtPassengerName.getText().trim();
            String phone = txtPassengerPhone.getText().trim();

            if (selectedSeatsList.isEmpty()) {
                throw new InvalidInputException("Please select at least one seat.");
            }
            if (name.isEmpty()) {
                throw new InvalidInputException("Passenger Name is required.");
            }
            if (!phone.matches("^[0-9]{10}$")) {
                throw new InvalidInputException("Invalid 10-digit phone number.");
            }

            double total = selectedSeatsList.size() * selectedBus.getFare();
            int confirmChoice = JOptionPane.showConfirmDialog(this,
                    "Reserve seat(s) [" + lblSelectedSeats.getText() + "] for " + name + "?\nTotal Amount: ₹ " + String.format("%.2f", total),
                    "Confirm Booking", JOptionPane.YES_NO_OPTION);

            if (confirmChoice != JOptionPane.YES_OPTION) return;

            // Reserve seats linked to current user
            int bookingId = DatabaseManager.reserveSeats(
                    selectedBus.getId(),
                    name,
                    phone,
                    selectedSeatsList,
                    total,
                    currentUser.getUsername()
            );

            if (bookingId != -1) {
                showTicketReceiptDialog(bookingId, name, phone, lblSelectedSeats.getText(), total);
                
                selectedBus = DatabaseManager.getBusById(selectedBus.getId());
                refreshBusTable(DatabaseManager.getAllBuses());
                refreshUserBookingsTable();
                
                switchTab("USER_HISTORY", btnMyBookingsTab);
            }

        } catch (InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (SeatBookingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Booking Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "SQL Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createUserBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("My Booked Tickets");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel lblDesc = new JLabel("View active tickets, print receipts, or cancel your reservation.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(Color.GRAY);
        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblDesc, BorderLayout.SOUTH);
        panel.add(headerPanel, BorderLayout.NORTH);

        String[] cols = {"Booking ID", "Passenger Name", "Contact", "Bus No", "Seat(s)", "Total Amount", "Booking Date", "Status"};
        userBookingTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblUserBookings = new JTable(userBookingTableModel);
        tblUserBookings.setRowHeight(35);
        tblUserBookings.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblUserBookings.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblUserBookings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tblUserBookings.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblUserBookings.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tblUserBookings.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tblUserBookings.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tblUserBookings.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        tblUserBookings.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        tblUserBookings.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(tblUserBookings);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        JButton btnPrintReceipt = new JButton("View Ticket Receipt");
        actionPanel.add(btnPrintReceipt);

        JButton btnCancelTicket = new JButton("Cancel Selected Ticket");
        btnCancelTicket.setBackground(new Color(231, 76, 60));
        btnCancelTicket.setForeground(Color.WHITE);
        btnCancelTicket.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancelTicket.setPreferredSize(new Dimension(200, 40));
        actionPanel.add(btnCancelTicket);

        panel.add(actionPanel, BorderLayout.SOUTH);

        btnPrintReceipt.addActionListener(e -> {
            int selectedRow = tblUserBookings.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a booking first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int bookingId = (int) tblUserBookings.getValueAt(selectedRow, 0);
            Booking booking = DatabaseManager.getBookingById(bookingId);
            if (booking != null) {
                Bus bus = DatabaseManager.getBusById(booking.getBusId());
                showReceiptForBooking(booking, bus);
            }
        });

        btnCancelTicket.addActionListener(e -> handleUserCancellation());

        return panel;
    }

    private void refreshUserBookingsTable() {
        if (currentUser == null) return;
        userBookingTableModel.setRowCount(0);
        List<Booking> bookings = DatabaseManager.getBookingsByUsername(currentUser.getUsername());
        for (Booking b : bookings) {
            userBookingTableModel.addRow(new Object[]{
                    b.getBookingId(),
                    b.getCustomerName(),
                    b.getCustomerPhone(),
                    resolveBusNumber(b.getBusId()),
                    b.getSeatNumbers(),
                    "₹ " + String.format("%.2f", b.getTotalAmount()),
                    b.getBookingDate(),
                    b.getStatus()
            });
        }
    }

    private void handleUserCancellation() {
        int selectedRow = tblUserBookings.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a booking to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = (int) tblUserBookings.getValueAt(selectedRow, 0);
        String status = (String) tblUserBookings.getValueAt(selectedRow, 7);

        if ("CANCELLED".equals(status)) {
            JOptionPane.showMessageDialog(this, "This ticket is already cancelled.", "Invalid Action", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to cancel Ticket ID: " + bookingId + "?", 
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) return;

        try {
            DatabaseManager.cancelBooking(bookingId);
            JOptionPane.showMessageDialog(this, "Booking cancelled successfully.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            
            refreshUserBookingsTable();
            refreshBusTable(DatabaseManager.getAllBuses());
            
            if (selectedBus != null) {
                selectedBus = DatabaseManager.getBusById(selectedBus.getId());
                loadSeatBookingPanel();
            }
        } catch (SQLException | SeatBookingException ex) {
            JOptionPane.showMessageDialog(this, "Error cancelling booking: " + ex.getMessage(), "Cancellation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // 4. ADMIN PORTAL PAGES (MANAGE BUSES, GLOBAL BOOKINGS)
    // ==========================================

    private JPanel createAdminBusesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("Manage Buses (Admin Panel)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel lblDesc = new JLabel("Add new bus routes or delete schedules from the grid database.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(Color.GRAY);
        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblDesc, BorderLayout.SOUTH);
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel mainGrid = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Left Side: Add Bus Form
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 8, 12));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Bus Route"));
        formPanel.setPreferredSize(new Dimension(320, 380));

        formPanel.add(new JLabel("Bus Number:"));
        txtAdminBusNo = new JTextField();
        formPanel.add(txtAdminBusNo);

        formPanel.add(new JLabel("Source:"));
        txtAdminSource = new JTextField();
        formPanel.add(txtAdminSource);

        formPanel.add(new JLabel("Destination:"));
        txtAdminDest = new JTextField();
        formPanel.add(txtAdminDest);

        formPanel.add(new JLabel("Departure Time:"));
        txtAdminTime = new JTextField();
        txtAdminTime.setToolTipText("e.g. 08:30 AM, 09:00 PM");
        formPanel.add(txtAdminTime);

        formPanel.add(new JLabel("Fare (₹):"));
        txtAdminFare = new JTextField();
        formPanel.add(txtAdminFare);

        formPanel.add(new JLabel("Bus Type:"));
        cmbAdminType = new JComboBox<>(new String[]{"AC Seater", "AC Sleeper", "Non-AC Seater", "Non-AC Sleeper", "AC Sleeper Multi-Axle"});
        formPanel.add(cmbAdminType);

        JButton btnAddBus = new JButton("Add Bus Route ✓");
        btnAddBus.setBackground(new Color(46, 204, 113));
        btnAddBus.setForeground(Color.BLACK);
        btnAddBus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(new JLabel()); // space keeper
        formPanel.add(btnAddBus);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.2;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 15);
        mainGrid.add(formPanel, gbc);

        // Right Side: Buses Table View
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBorder(BorderFactory.createTitledBorder("Current Active Routes"));

        String[] cols = {"ID", "Bus Number", "Source", "Destination", "Time", "Seats Left", "Fare", "Bus Type"};
        adminBusesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblAdminBuses = new JTable(adminBusesModel);
        tblAdminBuses.setRowHeight(35);
        tblAdminBuses.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblAdminBuses.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblAdminBuses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(tblAdminBuses);
        tableContainer.add(scroll, BorderLayout.CENTER);

        JButton btnDeleteBus = new JButton("Delete Selected Bus Route");
        btnDeleteBus.setBackground(new Color(231, 76, 60));
        btnDeleteBus.setForeground(Color.WHITE);
        btnDeleteBus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDeleteBus.setPreferredSize(new Dimension(220, 38));
        tableContainer.add(btnDeleteBus, BorderLayout.SOUTH);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainGrid.add(tableContainer, gbc);

        panel.add(mainGrid, BorderLayout.CENTER);

        // Actions Listeners
        btnAddBus.addActionListener(e -> handleAddBus());
        btnDeleteBus.addActionListener(e -> handleDeleteBus());

        return panel;
    }

    private void handleAddBus() {
        try {
            String busNo = txtAdminBusNo.getText().trim();
            String src = txtAdminSource.getText().trim();
            String dest = txtAdminDest.getText().trim();
            String time = txtAdminTime.getText().trim();
            String fareStr = txtAdminFare.getText().trim();
            String type = (String) cmbAdminType.getSelectedItem();

            if (busNo.isEmpty() || src.isEmpty() || dest.isEmpty() || time.isEmpty() || fareStr.isEmpty()) {
                throw new InvalidInputException("All fields in the Add Bus Form must be populated.");
            }

            double fare;
            try {
                fare = Double.parseDouble(fareStr);
                if (fare <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                throw new InvalidInputException("Fare must be a positive decimal number.");
            }

            // Fixed total capacity of 32 for layout visualization
            DatabaseManager.addBus(busNo, src, dest, time, 32, fare, type);
            JOptionPane.showMessageDialog(this, "Bus Route '" + busNo + "' added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Clear inputs
            txtAdminBusNo.setText("");
            txtAdminSource.setText("");
            txtAdminDest.setText("");
            txtAdminTime.setText("");
            txtAdminFare.setText("");

            refreshAdminBusesTable();
        } catch (InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteBus() {
        int row = tblAdminBuses.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a bus route from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int busId = (int) tblAdminBuses.getValueAt(row, 0);
        String busNo = (String) tblAdminBuses.getValueAt(row, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Bus Route '" + busNo + "'?\nThis will automatically CANCEL all active reservations for this bus!",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) return;

        try {
            DatabaseManager.deleteBus(busId);
            JOptionPane.showMessageDialog(this, "Bus Route '" + busNo + "' and all associated bookings were processed.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            refreshAdminBusesTable();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAdminBusesTable() {
        adminBusesModel.setRowCount(0);
        List<Bus> buses = DatabaseManager.getAllBuses();
        for (Bus bus : buses) {
            adminBusesModel.addRow(new Object[]{
                    bus.getId(),
                    bus.getBusNumber(),
                    bus.getSource(),
                    bus.getDestination(),
                    bus.getDepartureTime(),
                    bus.getAvailableSeats(),
                    "₹ " + String.format("%.2f", bus.getFare()),
                    bus.getBusType()
            });
        }
    }

    private JPanel createAdminBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("Global Booked Log");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel lblDesc = new JLabel("Global view of all passenger tickets. Admins can lookup passengers and cancel bookings.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(Color.GRAY);
        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblDesc, BorderLayout.SOUTH);
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Lookup Passenger"));

        searchPanel.add(new JLabel("Search Name/Phone:"));
        txtAdminSearchQuery = new JTextField(18);
        searchPanel.add(txtAdminSearchQuery);

        JButton btnSearch = new JButton("Find Tickets");
        btnSearch.setBackground(new Color(78, 161, 243));
        btnSearch.setForeground(Color.BLACK);
        searchPanel.add(btnSearch);

        JButton btnReset = new JButton("Reset");
        searchPanel.add(btnReset);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // Global Table
        String[] cols = {"Booking ID", "Passenger Name", "Phone No", "Bus No", "Seat(s)", "Total Paid", "Date Booked", "Status"};
        adminBookingsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblAdminBookings = new JTable(adminBookingsModel);
        tblAdminBookings.setRowHeight(35);
        tblAdminBookings.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblAdminBookings.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblAdminBookings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tblAdminBookings.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblAdminBookings.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tblAdminBookings.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tblAdminBookings.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tblAdminBookings.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        tblAdminBookings.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        tblAdminBookings.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(tblAdminBookings);
        centerPanel.add(scroll, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        JButton btnViewTicket = new JButton("View Invoice Details");
        actionPanel.add(btnViewTicket);

        JButton btnCancelBooking = new JButton("Cancel Selected Booking");
        btnCancelBooking.setBackground(new Color(231, 76, 60));
        btnCancelBooking.setForeground(Color.WHITE);
        btnCancelBooking.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancelBooking.setPreferredSize(new Dimension(200, 40));
        actionPanel.add(btnCancelBooking);

        panel.add(actionPanel, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> {
            String query = txtAdminSearchQuery.getText().trim();
            if (query.isEmpty()) {
                refreshAdminBookingsTable(DatabaseManager.getAllBookings());
            } else {
                refreshAdminBookingsTable(DatabaseManager.searchBookingsAdmin(query));
            }
        });

        btnReset.addActionListener(e -> {
            txtAdminSearchQuery.setText("");
            refreshAdminBookingsTable(DatabaseManager.getAllBookings());
        });

        btnViewTicket.addActionListener(e -> {
            int row = tblAdminBookings.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a booking row first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int bookingId = (int) tblAdminBookings.getValueAt(row, 0);
            Booking booking = DatabaseManager.getBookingById(bookingId);
            if (booking != null) {
                Bus bus = DatabaseManager.getBusById(booking.getBusId());
                showReceiptForBooking(booking, bus);
            }
        });

        btnCancelBooking.addActionListener(e -> handleAdminCancellation());

        return panel;
    }

    private void refreshAdminBookingsTable(List<Booking> bookings) {
        adminBookingsModel.setRowCount(0);
        for (Booking b : bookings) {
            adminBookingsModel.addRow(new Object[]{
                    b.getBookingId(),
                    b.getCustomerName(),
                    b.getCustomerPhone(),
                    resolveBusNumber(b.getBusId()),
                    b.getSeatNumbers(),
                    "₹ " + String.format("%.2f", b.getTotalAmount()),
                    b.getBookingDate(),
                    b.getStatus()
            });
        }
    }

    private void handleAdminCancellation() {
        int row = tblAdminBookings.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a booking row to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = (int) tblAdminBookings.getValueAt(row, 0);
        String status = (String) tblAdminBookings.getValueAt(row, 8 - 1); // 7th index is status

        if ("CANCELLED".equals(status)) {
            JOptionPane.showMessageDialog(this, "This booking has already been cancelled.", "Invalid Action", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel passenger Booking ID: " + bookingId + "?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) return;

        try {
            DatabaseManager.cancelBooking(bookingId);
            JOptionPane.showMessageDialog(this, "Passenger ticket cancelled successfully.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh
            if (txtAdminSearchQuery.getText().trim().isEmpty()) {
                refreshAdminBookingsTable(DatabaseManager.getAllBookings());
            } else {
                refreshAdminBookingsTable(DatabaseManager.searchBookingsAdmin(txtAdminSearchQuery.getText().trim()));
            }
        } catch (SQLException | SeatBookingException ex) {
            JOptionPane.showMessageDialog(this, "Error cancelling: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // 5. HELPER METHODS AND DIALOGS
    // ==========================================

    private JPanel createLegendItem(String labelText, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JPanel indicator = new JPanel();
        indicator.setPreferredSize(new Dimension(15, 15));
        indicator.setBackground(color);
        indicator.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        p.add(indicator);
        JLabel l = new JLabel(labelText);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        p.add(l);
        return p;
    }

    private void updateSelectedSeatsUI() {
        if (selectedSeatsList.isEmpty()) {
            lblSelectedSeats.setText("None");
            lblTotalAmount.setText("₹ 0.00");
        } else {
            selectedSeatsList.sort(Integer::compareTo);
            String seatsStr = selectedSeatsList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            lblSelectedSeats.setText(seatsStr);

            double total = selectedSeatsList.size() * selectedBus.getFare();
            lblTotalAmount.setText("₹ " + String.format("%.2f", total));
        }
    }

    private void showTicketReceiptDialog(int bookingId, String name, String phone, String seatsStr, double total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=========================================\n");
        receipt.append("         BUS TICKET BOOKING RECEIPT      \n");
        receipt.append("=========================================\n\n");
        receipt.append("Booking ID     : ").append(bookingId).append("\n");
        receipt.append("Passenger Name : ").append(name).append("\n");
        receipt.append("Phone Number   : ").append(phone).append("\n\n");
        receipt.append("Bus Number     : ").append(selectedBus.getBusNumber()).append("\n");
        receipt.append("Route          : ").append(selectedBus.getSource()).append(" -> ").append(selectedBus.getDestination()).append("\n");
        receipt.append("Departure Time : ").append(selectedBus.getDepartureTime()).append("\n");
        receipt.append("Seats Reserved : ").append(seatsStr).append("\n");
        receipt.append("Fare per Seat  : ₹ ").append(String.format("%.2f", selectedBus.getFare())).append("\n");
        receipt.append("Total Paid     : ₹ ").append(String.format("%.2f", total)).append("\n\n");
        receipt.append("Booking Time   : ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        receipt.append("=========================================\n");
        receipt.append("        Thank you for traveling with us! \n");
        receipt.append("=========================================\n");

        JTextArea textArea = new JTextArea(receipt.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(380, 360));

        JOptionPane.showMessageDialog(this, scrollPane, "Booking Confirmed! - Ticket Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showReceiptForBooking(Booking booking, Bus bus) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=========================================\n");
        receipt.append("         BUS TICKET BOOKING RECEIPT      \n");
        receipt.append("=========================================\n\n");
        receipt.append("Booking ID     : ").append(booking.getBookingId()).append("\n");
        receipt.append("Passenger Name : ").append(booking.getCustomerName()).append("\n");
        receipt.append("Phone Number   : ").append(booking.getCustomerPhone()).append("\n\n");
        receipt.append("Bus Number     : ").append(bus != null ? bus.getBusNumber() : "Unknown").append("\n");
        receipt.append("Route          : ").append(bus != null ? (bus.getSource() + " -> " + bus.getDestination()) : "Unknown").append("\n");
        receipt.append("Departure Time : ").append(bus != null ? bus.getDepartureTime() : "Unknown").append("\n");
        receipt.append("Seats Reserved : ").append(booking.getSeatNumbers()).append("\n");
        receipt.append("Total Amount   : ₹ ").append(String.format("%.2f", booking.getTotalAmount())).append("\n");
        receipt.append("Booking Date   : ").append(booking.getBookingDate()).append("\n");
        receipt.append("Ticket Status  : ").append(booking.getStatus()).append("\n");
        receipt.append("=========================================\n");

        JTextArea textArea = new JTextArea(receipt.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(380, 360));

        JOptionPane.showMessageDialog(this, scrollPane, "Ticket Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private String resolveBusNumber(int busId) {
        Bus bus = DatabaseManager.getBusById(busId);
        return bus != null ? bus.getBusNumber() : "ID: " + busId;
    }

    private JPanel createAboutPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel lblTitle = new JLabel("LogiTrack Bus Ticket System");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(78, 161, 243));
        panel.add(lblTitle, gbc);

        JLabel lblSub = new JLabel("Academic OOP Project Demonstration");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(lblSub, gbc);

        panel.add(Box.createRigidArea(new Dimension(0, 15)), gbc);

        String descText = "<html><center>"
                + "This application is a complete, modular Bus Ticket Reservation System designed with standard OOP concepts.<br><br>"
                + "<b>Key Features Covered:</b><br>"
                + "• <b>Role-Based Access:</b> Distinct panels for Customers (search/book) and Administrators (add/delete buses, view logs).<br>"
                + "• <b>Encapsulation:</b> Private fields and getters/setters in Bus, Booking, and User model classes.<br>"
                + "• <b>Inheritance:</b> Custom Exceptions (SeatBookingException & InvalidInputException) inheriting from standard Exception.<br>"
                + "• <b>Swing GUI:</b> Modern theme integration using FlatLaf and interactive seat booking grid layout.<br>"
                + "• <b>JDBC SQLite:</b> Auto-initialization, parameterized queries, and ACID transactional reservations.<br>"
                + "• <b>Input Validation:</b> Verification of passenger inputs with graceful exceptions."
                + "</center></html>";

        JLabel lblDesc = new JLabel(descText, JLabel.CENTER);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setPreferredSize(new Dimension(500, 220));
        panel.add(lblDesc, gbc);

        return panel;
    }
}
