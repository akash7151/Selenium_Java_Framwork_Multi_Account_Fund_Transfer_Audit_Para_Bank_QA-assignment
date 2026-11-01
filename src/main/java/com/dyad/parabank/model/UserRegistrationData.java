package com.dyad.parabank.model;

public class UserRegistrationData {

    private final String firstName;
    private final String lastName;
    private final String address;
    private final String city;
    private final String state;
    private final String zipCode;
    private final String phone;
    private final String ssn;
    private final String username;
    private final String password;

    public UserRegistrationData(String firstName, String lastName, String address, String city,
                                String state, String zipCode, String phone, String ssn,
                                String username, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.phone = phone;
        this.ssn = ssn;
        this.username = username;
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getPhone() {
        return phone;
    }

    public String getSsn() {
        return ssn;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
