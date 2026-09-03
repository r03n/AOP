package models;
public class Event {
    private int id;
    private String title;
    private String description; 
    private String date;
    private String time;
    private int capacity;
    private int organizerId;
    private String organizerName; 

    // Constructor used by Organizer and Participant
    public Event(int id, String title, String description, String date, String time, int capacity, int organizerId) {
        this.id = id; this.title = title; this.description = description; 
        this.date = date; this.time = time; this.capacity = capacity; this.organizerId = organizerId;
    }
    
    // Constructor used by Admin
    public Event(int id, String title, String description, String date, String time, int capacity, String organizerName) {
        this.id = id; this.title = title; this.description = description;
        this.date = date; this.time = time; this.capacity = capacity; this.organizerName = organizerName;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; } 
    public String getDate() { return date; }
    public String getTime() { return time; }
    public int getCapacity() { return capacity; }
    public String getOrganizerName() { return organizerName; }
    public int getOrganizerId() { return organizerId; }
}