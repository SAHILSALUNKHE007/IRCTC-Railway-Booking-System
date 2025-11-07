package org.example;

import org.example.entities.User;
import org.example.services.UserBookingService;
import org.example.util.UserServiceUtil;

import java.io.IOException;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("===== 🚆 Welcome to Railway Booking System =====");

        try {
            System.out.print("Enter Username: ");
            String name = sc.nextLine();

            System.out.print("Enter Password: ");
            String password = sc.nextLine();

            // Create user object
            User currentUser = new User();
            currentUser.setName(name);
            currentUser.setPassword(password);
           // actual password
            currentUser.setHashPassword( UserServiceUtil.hashPassword(password)); // assuming hashed or same

            // Initialize booking service
            UserBookingService service = new UserBookingService(currentUser);

            boolean loggedIn = service.login();

            if (!loggedIn) {
                System.out.println("User not found! Do you want to sign up? (yes/no): ");
                String choice = sc.nextLine();

                if (choice.equalsIgnoreCase("yes")) {
                    boolean signedUp = service.signUp(currentUser);
                    if (signedUp)
                        System.out.println("✅ Signup successful! Please login again.");
                    else
                        System.out.println("❌ Signup failed!");
                } else {
                    System.out.println("Exiting system...");
                    return;
                }
            } else {
                System.out.println("✅ Login successful!");
            }

            // Menu loop
            int option;
            do {
                System.out.println("\n===== MENU =====");
                System.out.println("1️⃣  Book Ticket");
                System.out.println("2️⃣  View Booked Tickets");
                System.out.println("3️⃣  View Cancelled Tickets");
                System.out.println("4️⃣  Cancel Ticket");
                System.out.println("5️⃣  Exit");
                System.out.print("Enter choice: ");
                option = sc.nextInt();
                sc.nextLine(); // consume newline

                switch (option) {
                    case 1 -> {
                        System.out.print("Enter Source Station: ");
                        String source = sc.nextLine();

                        System.out.print("Enter Destination Station: ");
                        String destination = sc.nextLine();

                        boolean booked = service.bookTicket(source, destination);
                        if (booked)
                            System.out.println("🎟️ Ticket booked successfully!");
                        else
                            System.out.println("❌ Ticket booking failed.");
                    }
                    case 2 -> service.fetchBooking();

                    case 3 -> service.fetchCancelTicket();

                    case 4 -> {
                        System.out.print("Enter PRN number of ticket to cancel: ");
                        long prn = sc.nextLong();
                        sc.nextLine();

                        boolean cancelled = service.cancelTicket(prn);
                        if (cancelled)
                            System.out.println("🛑 Ticket cancelled successfully!");
                        else
                            System.out.println("❌ Ticket not found or already cancelled.");
                    }

                    case 5 -> System.out.println("👋 Exiting... Thank you for using the system!");
                    default -> System.out.println("⚠️ Invalid choice! Try again.");
                }

            } while (option != 5);

        } catch (IOException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }
}
