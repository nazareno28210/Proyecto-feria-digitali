package com.mansilla_nazareno.feriadigital.feriadigital.controllers;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.StandDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.StandRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StandController {
    private StandRepository standRepository;

    public StandController(StandRepository standRepository){this.standRepository= standRepository;}

    @GetMapping("/stands")
    public List<StandDTO> getStands(){
        return standRepository.findAll()
                .stream()
                .map(stand-> new StandDTO(stand))
                .toList();
    }
    @GetMapping("/stands/{id}")
    public StandDTO getStandDTO(@PathVariable Integer id){
        return standRepository.findById(id)
                .map(StandDTO::new)
                .orElse(null);

    }
}
