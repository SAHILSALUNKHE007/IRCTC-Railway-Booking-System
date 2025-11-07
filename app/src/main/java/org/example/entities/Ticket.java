package org.example.entities;

import org.example.enums.TicketStatus;
import java.util.Date;

public class Ticket {
    private long prnno;
    private String userName;
    private String userId;
    private String source;
    private String destination;
    private Train train;
    private Date dateofTravel;
    private TicketStatus status;
    private int ticketNo;

    public Ticket() {}

    public Ticket(long prnno, Date dateofTravel, Train traino, String destination, String source, String userId, String userName,int ticketNo) {
        this.prnno = prnno;
        this.dateofTravel = dateofTravel;
        this.train = traino;
        this.destination = destination;
        this.source = source;
        this.userId = userId;
        this.userName = userName;
        this.status = TicketStatus.BOOKED;
        this.ticketNo=ticketNo;

    }

    // Getters & setters
    public long getPrnno() { return prnno; }
    public void setPrnno(long prnno) { this.prnno = prnno; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }



    public Date getDateofTravel() { return dateofTravel; }
    public void setDateofTravel(Date dateofTravel) { this.dateofTravel = dateofTravel; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public int getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(int ticketNo) {
        this.ticketNo = ticketNo;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }


}
