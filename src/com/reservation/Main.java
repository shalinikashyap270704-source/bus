package com.reservation;

import com.reservation.database.DatabaseManager;
import com.reservation.gui.BusReservationGUI;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

/**
 * Main Entry Point for the Bus Ticket Reservation System.
 */
public class Main {
    public static void main(String[] args) {
        // Initialize the modern FlatLaf dark look and feel
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("FlatDarkLaf is not supported. Reverting to system default look and feel.");
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Initialize the database and create/seed tables if they don't exist
        DatabaseManager.initializeDatabase();

        // Boot up the Swing GUI
        SwingUtilities.invokeLater(() -> {
            try {
                BusReservationGUI frame = new BusReservationGUI();
                frame.setVisible(true);
            } catch (Exception e) {
                System.err.println("Failed to start the GUI application.");
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                        "An unexpected error occurred while starting the application:\n" + e.getMessage(), 
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
