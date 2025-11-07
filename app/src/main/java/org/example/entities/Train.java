package org.example.entities;

import java.util.List;
import java.util.Map;

public class Train {
    private int trainId;
    private List<String> stations;
    private List<List<Integer>> seats;
    private Map<String, String> arival;

    public Train() {}

    // Getters & setters
    public int getTrainId() { return trainId; }
    public void setTrainId(int trainId) { this.trainId = trainId; }

    public List<String> getStations() { return stations; }
    public void setStations(List<String> stations) { this.stations = stations; }

    public List<List<Integer>> getSeats() { return seats; }
    public void setSeats(List<List<Integer>> seats) { this.seats = seats; }

    public Map<String, String> getArival() { return arival; }
    public void setArival(Map<String, String> arival) { this.arival = arival; }
}
