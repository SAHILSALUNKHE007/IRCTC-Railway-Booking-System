package org.example.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.Ticket;
import org.example.entities.Train;
import org.example.entities.User;
import org.example.enums.TicketStatus;
import org.example.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserBookingService {

    private User user;
    private List<User> userList;
    private List<Train> trainList;

    
    private static final Object USER_LOCK = new Object();
    private static final Object TRAIN_LOCK = new Object();

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

    private static final String USER_PATH =
            "G:/IRCTC/app/src/main/java/org/example/localDB/user.json";
    private static final String TRAIN_PATH =
            "G:/IRCTC/app/src/main/java/org/example/localDB/train.json";

    public UserBookingService(User user) throws IOException {
        this.user = user;
        synchronized (USER_LOCK) {
            userList = objectMapper.readValue(
                    new File(USER_PATH),
                    new TypeReference<List<User>>() {});
        }
        synchronized (TRAIN_LOCK) {
            trainList = objectMapper.readValue(
                    new File(TRAIN_PATH),
                    new TypeReference<List<Train>>() {});
        }
    }

   
    public boolean login() {
        synchronized (USER_LOCK) {
            Optional<User> foundUser = userList.stream()
                    .filter(u -> u.getName().equals(user.getName())
                            && UserServiceUtil.checkPassword(
                                    u.getPassword(),
                                    user.getHashPassword()))
                    .findFirst();

            if (foundUser.isPresent()) {
                this.user = foundUser.get();
                return true;
            }
            return false;
        }
    }

    
    public boolean signUp(User newUser) {
        synchronized (USER_LOCK) {
            try {
                userList.add(newUser);
                saveToUserFile();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

   
    public void fetchBooking() {
        synchronized (USER_LOCK) {
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
                    System.out.println("PRN: " + ticket.getPrnno()
                            + " | From: " + ticket.getSource()
                            + " | To: " + ticket.getDestination()
                            + " | Train: " + ticket.getTrain().getTrainId())
            );
        }
    }

   
    public void fetchCancelTicket() {
        synchronized (USER_LOCK) {
            List<Ticket> cancelledTickets = userList.stream()
                    .filter(u -> Objects.equals(u.getUserId(), user.getUserId()))
                    .flatMap(u -> u.getTickedBooked().stream())
                    .filter(t -> TicketStatus.CANCEL.equals(t.getStatus()))
                    .toList();

            if (cancelledTickets.isEmpty()) {
                System.out.println("❌ No cancelled tickets found!");
                return;
            }

            cancelledTickets.forEach(ticket ->
                    System.out.println("PRN: " + ticket.getPrnno()
                            + " | From: " + ticket.getSource()
                            + " | To: " + ticket.getDestination()
                            + " | Train: " + ticket.getTrain().getTrainId())
            );
        }
    }

  
    public boolean bookTicket(String source, String destination) throws IOException {

        synchronized (TRAIN_LOCK) {
            synchronized (USER_LOCK) {

                for (Train train : trainList) {

                    if (!train.getStations().contains(source)
                            || !train.getStations().contains(destination)) {
                        continue;
                    }

                    int start = train.getStations().indexOf(source);
                    int end = train.getStations().indexOf(destination);

                    if (end <= start) continue;

                    for (int seat = 0; seat < train.getSeats().get(0).size(); seat++) {

                        boolean available = true;
                        for (int seg = start; seg < end; seg++) {
                            if (train.getSeats().get(seg).get(seat) == 1) {
                                available = false;
                                break;
                            }
                        }

                        if (available) {
                            // Mark seat booked
                            for (int seg = start; seg < end; seg++) {
                                train.getSeats().get(seg).set(seat, 1);
                            }

                            Ticket ticket = new Ticket(
                                    1000000000L + new Random().nextInt(900000000),
                                    new Date(),
                                    train,
                                    destination,
                                    source,
                                    user.getUserId(),
                                    user.getName(),
                                    seat + 1
                            );
                            ticket.setStatus(TicketStatus.BOOKED);

                            userList.stream()
                                    .filter(u -> u.getUserId().equals(user.getUserId()))
                                    .findFirst()
                                    .ifPresent(u -> {
                                        if (u.getTickedBooked() == null)
                                            u.setTickedBooked(new ArrayList<>());
                                        u.getTickedBooked().add(ticket);
                                    });

                            saveToTrainFile();
                            saveToUserFile();

                            System.out.println("✅ Ticket booked successfully");
                            return true;
                        }
                    }
                }
                System.out.println("❌ No seats available");
                return false;
            }
        }
    }

   
    public boolean cancelTicket(long ticketNo) throws IOException {

        synchronized (TRAIN_LOCK) {
            synchronized (USER_LOCK) {

                Optional<Ticket> cancelTicket = userList.stream()
                        .filter(u -> u.getUserId().equals(user.getUserId()))
                        .flatMap(u -> u.getTickedBooked().stream())
                        .filter(t -> t.getPrnno() == ticketNo)
                        .findFirst();

                if (cancelTicket.isPresent()) {

                    Ticket ticket = cancelTicket.get();
                    ticket.setStatus(TicketStatus.CANCEL);

                    Train train = ticket.getTrain();
                    int start = train.getStations().indexOf(ticket.getSource());
                    int end = train.getStations().indexOf(ticket.getDestination());
                    int seatIndex = ticket.getTicketNo() - 1;

                    for (int seg = start; seg < end; seg++) {
                        train.getSeats().get(seg).set(seatIndex, 0);
                    }

                    saveToTrainFile();
                    saveToUserFile();
                    return true;
                }
                return false;
            }
        }
    }

 
    private void saveToUserFile() throws IOException {
        objectMapper.writeValue(new File(USER_PATH), userList);
    }

    private void saveToTrainFile() throws IOException {
        objectMapper.writeValue(new File(TRAIN_PATH), trainList);
    }
}
