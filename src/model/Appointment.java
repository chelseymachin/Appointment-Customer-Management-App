package model;


import DAO.Query;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {
    Integer appointmentId;
    Customer customer;
    String title;
    String description;
    String location;
    String type;
    LocalDate date;
    LocalTime startTime;
    LocalTime endTime;
    User user;
    Contact contact;
    Integer customerId;
    Integer userId;
    Integer contactId;

    public Appointment() {}

    public Appointment(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

//    public Appointment(String appointmentId, Customer customer, String title, String description, String location, String type, String user, LocalDate date, LocalTime start, LocalTime end) {
//        this.appointmentId = appointmentId;
//        this.customer = customer;
//        this.title = title;
//        this.description = description;
//        this.location = location;
//        this.type = type;
//        this.user = user;
//        this.date = date;
//        this.startTime = start.toString();
//        this.endTime = end.toString();
//    }
//
//    public Appointment(String appointmentId, String customerId, String title, String description, String location, String type, String date, String start, String end, String userId, String contactId) {
//        this.appointmentId = appointmentId;
//        this.customerId = customerId;
//        this.title = title;
//        this.description = description;
//        this.location = location;
//        this.type = type;
//        this.date = LocalDate.parse(date.substring(0, 10));
//        this.startTime = start;
//        this.endTime = end;
//        this.userId = userId;
//        this.contactId = contactId;
//    }
//
//
//    public Appointment(String appointmentId, Customer customer, String title, String description, String location, String type, String user, String url, String date, String start, String end) {
//        this.appointmentId = appointmentId;
//        this.customer = customer;
//        this.title = title;
//        this.description = description;
//        this.location = location;
//        this.type = type;
//        this.user = user;
//        this.url = url;
//        this.date = LocalDate.parse(date);
//        this.startTime = start;
//        this.endTime = end;
//    }

    public Appointment(Integer appointmentId, Customer customer, String title, String description, String location, String type, User user, Contact contact, LocalDate date, LocalTime start, LocalTime end) {
        this.appointmentId = appointmentId;
        this.customer = customer;
        this.title = title;
        this.description = description;
        this.location = location;
        this.type = type;
        this.user = user;
        this.contact = contact;
        this.date = date;
        this.startTime = start;
        this.endTime = end;
    }

    public Appointment(Integer appointmentId, Integer customerId, String title, String description, String location, String type, Integer userId, Integer contactId, LocalDate date, LocalTime start, LocalTime end) {
        this.appointmentId = appointmentId;
        this.customer = Query.getCustomerById(customerId);
        this.customerId = customerId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.type = type;
        this.user = Query.getUserById(userId);
        this.userId = userId;
        this.contact = Query.getContactById(contactId);
        this.contactId = contactId;
        this.date = date;
        this.startTime = start;
        this.endTime = end;
    }

    // Getter & Setter Methods

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        this.customerId = customer.getCustomerId();
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime start) {
        this.startTime = start;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime end) {
        this.endTime = end;
    }

    public User getUser() {
        return user;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUser(User user) {
        this.user = user;
        this.userId = user.getUserId();
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
        this.contactId = contact.getContactID();
    }

    public Integer getContactId() {
        return contactId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setContactId(Integer contactId) {
        this.contactId = contactId;
    }
}
