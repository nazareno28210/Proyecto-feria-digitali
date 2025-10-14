package com.mansilla_nazareno.feriadigital.feriadigital.controllers;


import com.mansilla_nazareno.feriadigital.feriadigital.dtos.UserDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class UserController {

    private UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<UserDTO> getaccounts(){
        return userRepository.findAll().stream().map(user -> new UserDTO(user)).collect(toList());
    }
    @RequestMapping("/users/{id}")
    public UserDTO getUserDTO(@PathVariable Integer id){
        return userRepository.findById(id)
                .map(UserDTO::new)
                .orElse(null);

    }
}
