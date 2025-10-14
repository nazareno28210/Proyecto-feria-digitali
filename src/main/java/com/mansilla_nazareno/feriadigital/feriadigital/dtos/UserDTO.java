package com.mansilla_nazareno.feriadigital.feriadigital.dtos;

import com.mansilla_nazareno.feriadigital.feriadigital.models.User;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UserEstate;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UserType;

import java.time.LocalDate;

public class UserDTO {
    private int id;
    private  String firstName;
    private  String lastName;
    private  String email;
    private  String password;
    private UserEstate userEstate;
    private LocalDate DayRegistrer;
    private UserType userType;

    public UserDTO(){}
    public UserDTO(User user) {
        this.id= user.getId();
        this.firstName =user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.userEstate = user.getUserEstate();
        this.DayRegistrer = user.getDayRegistrer();
        this.userType=user.getUserType();
    }

}
