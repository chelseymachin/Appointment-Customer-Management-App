package model;

public class Contact {
    public Integer contactID;
    public String name;
    public String email;


    public Contact(Integer contactId, String name, String email) {
        this.contactID = contactId;
        this.name = name;
        this.email = email;
    }

    public Contact(Integer contactId, String name) {
        this.contactID = contactId;
        this.name = name;
    }

    public Integer getContactID() {
        return contactID;
    }

    public void setContactID(Integer contactID) {
        this.contactID = contactID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override public String toString() {
        return (Integer.toString(contactID) + " - " + name);
    }
}
