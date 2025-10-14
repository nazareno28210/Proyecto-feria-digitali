package com.mansilla_nazareno.feriadigital.feriadigital.models;

import jakarta.persistence.Entity;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@Entity
public class User {
    @Id
    private int id;
    private  String firstName;
    private  String LastName;
    private  String email;
    private  String password;
    private UserEstate userEstate;
    private LocalDate DayRegistrer;
    private  UserType userType;

    public User() {}
    public User(String firstName, String lastName, String email, String password,UserType userType,UserEstate userEstate) {
        this.firstName = firstName;
        this.LastName = lastName;
        this.email = email;
        this.password = password;
        this.DayRegistrer = LocalDate.now();
        this.userEstate=userEstate;
        this.userType=userType;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDayRegistrer() {
        return DayRegistrer;
    }

    public UserEstate getUserEstate() {
        return userEstate;
    }

    public void setUserEstate(UserEstate userEstate) {
        this.userEstate = userEstate;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}
