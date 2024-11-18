import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;

public class DoctorAppointmentSystemDB {
    static final String JDBC_URL = "jdbc:mysql://localhost:3306/appointment_db";
    static final String DB_USER = "root"; // Replace with your username
    static final String DB_PASSWORD = ""; // Replace with your password

    static ArrayList<Doctor> doctors = new ArrayList<>();

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to the database successfully!");

            // Add doctors to the in-memory list (this could be fetched from DB as well)
            doctors.add(new Doctor(1, "Dr. Smith", "Cardiology"));
            doctors.add(new Doctor(2, "Dr. Jones", "Neurology"));

            int choice;
            do {
                System.out.println("\nDoctor Appointment System");
                System.out.println("1. Add Patient");
                System.out.println("2. Schedule Appointment");
                System.out.println("3. View Appointments");
                System.out.println("4. Exit");
                System.out.print("Enter your choice: ");
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        addPatient(conn, scanner);
                        break;
                    case 2:
                        scheduleAppointment(conn, scanner);
                        break;
                    case 3:
                        viewAppointments(conn);
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } while (choice != 4);

        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }

    // Method to add a patient
    public static void addPatient(Connection conn, Scanner scanner) {
        try {
            System.out.print("Enter patient ID: ");
            int patientId = scanner.nextInt();
            scanner.nextLine();
            System.out.print("Enter patient name: ");
            String name = scanner.nextLine();
            System.out.print("Enter symptoms: ");
            String symptoms = scanner.nextLine();

            String query = "INSERT INTO patients (patient_id, name, symptoms) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, patientId);
                stmt.setString(2, name);
                stmt.setString(3, symptoms);
                stmt.executeUpdate();
                System.out.println("Patient added successfully!");
            }
        } catch (SQLException e) {
            System.out.println("Error adding patient.");
            e.printStackTrace();
        }
    }

    // Method to schedule an appointment
    public static void scheduleAppointment(Connection conn, Scanner scanner) {
        try {
            System.out.print("Enter patient ID: ");
            int patientId = scanner.nextInt();
            scanner.nextLine();

            String patientQuery = "SELECT * FROM patients WHERE patient_id = ?";
            try (PreparedStatement patientStmt = conn.prepareStatement(patientQuery)) {
                patientStmt.setInt(1, patientId);
                ResultSet rs = patientStmt.executeQuery();
                if (!rs.next()) {
                    System.out.println("Patient not found.");
                    return;
                }
            }

            String doctorQuery = "SELECT * FROM doctors";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(doctorQuery)) {

                System.out.println("Available Doctors:");
                while (rs.next()) {
                    System.out.println("Doctor ID: " + rs.getInt("doctor_id") +
                                       ", Name: " + rs.getString("name") +
                                       ", Specialty: " + rs.getString("specialty"));
                }
            }

            System.out.print("Enter doctor ID: ");
            int doctorId = scanner.nextInt();
            scanner.nextLine();

            String date;
            System.out.print("Enter appointment date (YYYY-MM-DD): ");
            date = scanner.nextLine();

            String appointmentQuery = "INSERT INTO appointments (doctor_id, patient_id, appointment_date) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(appointmentQuery)) {
                stmt.setInt(1, doctorId);
                stmt.setInt(2, patientId);
                stmt.setDate(3, Date.valueOf(date));
                stmt.executeUpdate();
                System.out.println("Appointment scheduled successfully!");
            }
        } catch (SQLException e) {
            System.out.println("Error scheduling appointment.");
            e.printStackTrace();
        }
    }

    // Method to view appointments
    public static void viewAppointments(Connection conn) {
        try {
            String query = "SELECT a.appointment_id, d.name AS doctor_name, p.name AS patient_name, a.appointment_date " +
                           "FROM appointments a " +
                           "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                           "JOIN patients p ON a.patient_id = p.patient_id";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                if (!rs.isBeforeFirst()) {
                    System.out.println("No appointments found.");
                    return;
                }

                System.out.println("Appointments:");
                while (rs.next()) {
                    System.out.println("Appointment ID: " + rs.getInt("appointment_id") +
                                       ", Doctor: " + rs.getString("doctor_name") +
                                       ", Patient: " + rs.getString("patient_name") +
                                       ", Date: " + rs.getDate("appointment_date"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving appointments.");
            e.printStackTrace();
        }
    }

    // Doctor class to store doctor details
    static class Doctor {
        int doctorId;
        String name;
        String specialty;

        public Doctor(int doctorId, String name, String specialty) {
            this.doctorId = doctorId;
            this.name = name;
            this.specialty = specialty;
        }
    }
}
