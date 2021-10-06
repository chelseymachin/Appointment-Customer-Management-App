package model;


public class Appointment {
    String appointmentId;
    Customer customer;
    String title;
    String description;
    String location;
    String type;
    String url;
    String date;
    String start;
    String end;
    String user;

    public Appointment() {}

    public Appointment(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Appointment(String appointmentId, Customer customer, String title, String description, String location, String type, String user, String date, String start, String end) {
        this.appointmentId = appointmentId;
        this.customer = customer;
        this.title = title;
        this.description = description;
        this.location = location;
        this.type = type;
        this.user = user;
        this.date = date;
        this.start = start;
        this.end = end;
    }

    public Appointment(String appointmentId, Customer customer, String title, String description, String location, String type, String user, String url, String date, String start, String end) {
        this.appointmentId = appointmentId;
        this.customer = customer;
        this.title = title;
        this.description = description;
        this.location = location;
        this.type = type;
        this.user = user;
        this.url = url;
        this.date = date;
        this.start = start;
        this.end = end;
    }

    // Getter & Setter Methods

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
