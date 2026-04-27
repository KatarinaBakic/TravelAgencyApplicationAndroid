package com.example.travelagencyapplication.model;

//user1 je osoba koja je pokrenula zahtev
// user2 je osoba kojoj je zahtev poslat.
public class Friendship {
    private long id;
    private String status;
    private int isDeleted;
    private User user1;
    private User user2;

    public Friendship() {}
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public User getUser1() { return user1; }
    public void setUser1(User user1) { this.user1 = user1; }
    public User getUser2() { return user2; }
    public void setUser2(User user2) { this.user2 = user2; }
}