package model;


import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {
    String appointmentId;
    Customer customer;
    String title;
    String description;
    String location;
    String type;
    String url;
    LocalDate date;
    String startTime;
    String endTime;
    String user;
    String customerId;
    String userId;
    String contactId;

    public Appointment() {}

    public Appointment(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Appointment(String appointmentId, Customer customer, String title, String description, String location, String type, String user, LocalDate date, LocalTime start, LocalTime end) {
        this.appointmentId = appointmentId;
        this.customer = customer;
        this.title = title;
        this.description = description;
        this.location = location;
        this.type = type;
        this.user = user;
        this.date = date;
        this.startTime = start.toString();
        this.endTime = end.toString();
    }

    public Appointment(String appointmentId, String customerId, String title, String description, String location, String type, String date, String start, String end, String userId, String contactId) {
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.type = type;
        this.date = LocalDate.parse(date.substring(0, 10));
        this.startTime = start;
        this.endTime = end;
        this.userId = userId;
        this.contactId = contactId;
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
        this.date = LocalDate.parse(date);
        this.startTime = start;
        this.endTime = end;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime.substring(11, 16);
    }

    public void setStartTime(String start) {
        this.startTime = start;
    }

    public String getEndTime() {
        return endTime.substring(11, 16);
    }

    public void setEndTime(String end) {
        this.endTime = end;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }
}
