package org.example.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.Ticket;
import org.example.entities.Train;
import org.example.entities.User;
import org.example.enums.TicketStatus;
import org.example.util.UserServiceUtil;

import  java.io.*;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class UserBookingService {

    private User user;
    private List<User> userList;
    private List<Train> trainList;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

    private static final String USER_PATH = "G:/IRCTC/app/src/main/java/org/example/localDB/user.json";
    private static final String TRAIN_PATH = "G:/IRCTC/app/src/main/java/org/example/localDB/train.json";

    public UserBookingService(User user) throws IOException {
        this.user = user;
        userList = objectMapper.readValue(new File(USER_PATH), new TypeReference<List<User>>() {});
        trainList = objectMapper.readValue(new File(TRAIN_PATH), new TypeReference<List<Train>>() {});
    }

    public  boolean login(){
      Optional<User> userfind= userList.stream()
              .filter(user1 -> {
                return  user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user1.getPassword(),user.getHashPassword());

              })
              .findFirst();
        if(userfind.isPresent()){
            this.user = userfind.get(); // important: replace in-memory user with the one in list
            return true;
        }
        return false;


    }

   public boolean signUp(User user1){
        try {
            userList.add(user1);
            saveToUserFile();
            return true;
        }catch (IOException io){
            return  false;
        }

    }

    public void fetchBooking() {
        List<Ticket> bookedTickets = userList.stream()
                .filter(u -> Objects.equals(u.getUserId(), user.getUserId()))
                .flatMap(u -> u.getTickedBooked().stream())
                .filter(t -> TicketStatus.BOOKED.equals(t.getStatus()))
                .toList();

        if (bookedTickets.isEmpty()) {
            System.out.println("❌ No booked tickets found!");
            return;
        }

        bookedTickets.forEach(ticket ->
                System.out.println("PRN: " + ticket.getPrnno() +
                        " | From: " + ticket.getSource() +
                        " | To: " + ticket.getDestination() +
                        " | Train: " + ticket.getTrain().getTrainId())
        );
    }

    public void fetchCancelTicket() {
        List<Ticket> canceledTickets = userList.stream()
                .filter(u -> Objects.equals(u.getUserId(), user.getUserId()))
                .flatMap(u -> u.getTickedBooked().stream())
                .filter(t -> TicketStatus.CANCEL.equals(t.getStatus()))
                .toList();

        if (canceledTickets.isEmpty()) {
            System.out.println("❌ No cancelled tickets found!");
            return;
        }

        canceledTickets.forEach(ticket ->
                System.out.println("PRN: " + ticket.getPrnno() +
                        " | From: " + ticket.getSource() +
                        " | To: " + ticket.getDestination() +
                        " | Train: " + ticket.getTrain().getTrainId())
        );
    }

    public void saveToUserFile() throws IOException {
        objectMapper.writeValue(new File(USER_PATH), userList);
    }
    public boolean cancelTicket(long ticketNo) throws IOException {
        Optional<Ticket> cancelTicket = userList.stream()
                .filter(u -> u.getUserId().equals(user.getUserId()))
                .flatMap(u -> u.getTickedBooked().stream())
                .filter(t -> t.getPrnno() == ticketNo)
                .findFirst();

        if (cancelTicket.isPresent()) {
            Ticket ticket = cancelTicket.get();
            ticket.setStatus(TicketStatus.CANCEL);

            Train train = ticket.getTrain();
            List<String> stations = train.getStations();
            List<List<Integer>> seats = train.getSeats();

            int startIndex = stations.indexOf(ticket.getSource());
            int endIndex = stations.indexOf(ticket.getDestination());
            int seatIndex = ticket.getTicketNo() - 1; // 1-based to 0-based

            for (int seg = startIndex; seg < endIndex; seg++) {
                seats.get(seg).set(seatIndex, 0); // free seat
            }

            saveToTrainFile();
            saveToUserFile();
            return true;
        }
        return false;
    }



    public boolean bookTicket(String Source, String Destination) throws IOException {
        List<Train> trains = trainList.stream()
                .filter(train -> train.getStations() != null)
                .filter(train -> train.getStations().contains(Source) && train.getStations().contains(Destination))
                .toList();

        if (trains.isEmpty()) {
            System.out.println("❌ No train available for this route.");
            return false;
        }

        boolean booked = false;
        Ticket bookedTicket = null;

        for (Train train : trains) {
            List<String> stations = train.getStations();
            List<List<Integer>> seats = train.getSeats();

            int startIndex = stations.indexOf(Source);
            int endIndex = stations.indexOf(Destination);

            if (startIndex == -1 || endIndex == -1 || endIndex <= startIndex) {
                System.out.println("⚠️ Invalid source/destination for Train ID " + train.getTrainId());
                continue;
            }

            List<Integer> availableSeats = new ArrayList<>();
            for (int seat = 0; seat < seats.get(0).size(); seat++) {
                boolean isAvailable = true;
                for (int seg = startIndex; seg < endIndex; seg++) {  // Corrected: < endIndex
                    if (seats.get(seg).get(seat) == 1) {
                        isAvailable = false;
                        break;
                    }
                }
                if (isAvailable) availableSeats.add(seat + 1);
            }

            System.out.println("\n🚆 Train ID: " + train.getTrainId());
            System.out.println("Route: " + Source + " → " + Destination);
            System.out.println("Available Seats: " + availableSeats);

            if (availableSeats.isEmpty()) {
                System.out.println("❌ No seats available in this train.\n");
                continue;
            }

            Scanner sc = new Scanner(System.in);
            System.out.print("Enter seat number to book (or 0 to skip): ");
            int seatToBook = sc.nextInt();

            if (seatToBook > 0 && availableSeats.contains(seatToBook)) {
                int seatIndex = seatToBook - 1;
                for (int seg = startIndex; seg < endIndex; seg++) {  
                    seats.get(seg).set(seatIndex, 1);
                }
                System.out.println("✅ Seat " + seatToBook + " successfully booked in Train " + train.getTrainId());
                saveToTrainFile();

                bookedTicket = new Ticket(
                        1000000000L + (long)(new Random().nextDouble() * 9000000000L),  // 10-digit PRN
                        new Date(),
                        train,
                        Destination,
                        Source,
                        user.getUserId(),
                        user.getName(),
                        seatToBook
                );
                bookedTicket.setStatus(TicketStatus.BOOKED);

                booked = true;
                break;
            } else {
                System.out.println("❌ Invalid seat selection. Please choose from available seats.");
            }
        }

        if (booked) {
            Ticket finalBookedTicket = bookedTicket;
            userList.stream()
                    .filter(u -> u.getUserId().equals(user.getUserId()))
                    .findFirst()
                    .ifPresent(u -> {
                        if (u.getTickedBooked() == null) u.setTickedBooked(new ArrayList<>());
                        u.getTickedBooked().add(finalBookedTicket);
                    });
            saveToUserFile();
        }

        return booked;
    }
    private void saveToTrainFile() throws IOException {
        objectMapper.writeValue(new File(TRAIN_PATH), trainList);
    }


}
