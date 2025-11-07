package org.example.entities;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String name;
    private String password;
    private String hashPassword;
    private String userId;
    private List<Ticket> tickedBooked = new ArrayList<>();

    public User() {}

    public User(String name, String password, String hashPassword, String userId) {
        this.name = name;
        this.password = password;
        this.hashPassword = hashPassword;
        this.userId = userId;
        this.tickedBooked = new ArrayList<>();
    }

    // Getters & setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getHashPassword() { return hashPassword; }
    public void setHashPassword(String hashPassword) { this.hashPassword = hashPassword; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<Ticket> getTickedBooked() { return tickedBooked; }
    public void setTickedBooked(List<Ticket> tickedBooked) { this.tickedBooked = tickedBooked; }
}
