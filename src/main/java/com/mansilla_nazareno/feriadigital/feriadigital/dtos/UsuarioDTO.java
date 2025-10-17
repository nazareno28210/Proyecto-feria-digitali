package com.mansilla_nazareno.feriadigital.feriadigital.dtos;

import com.mansilla_nazareno.feriadigital.feriadigital.models.User;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UserEstate;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UserType;

import java.time.LocalDate;

public class UserDTO {
    private int id;
    private  String nombre;
    private  String apellido;
    private  String email;
    private  String contrasena;
    private UserEstate userEstate;
    private LocalDate DayRegistrer;
    private UserType userType;

    public UserDTO(){}
    public UserDTO(User user) {
        this.id= user.getId();
        this.nombre =user.getNombre();
        this.apellido = user.getApellido();
        this.email = user.getEmail();
        this.contrasena = user.getContrasena();
        this.userEstate = user.getUserEstate();
        this.DayRegistrer = user.getFechaRegistro();
        this.userType=user.getUserType();
    }

}
