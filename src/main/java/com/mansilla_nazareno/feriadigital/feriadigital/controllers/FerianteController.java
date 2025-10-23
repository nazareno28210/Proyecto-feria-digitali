package com.mansilla_nazareno.feriadigital.feriadigital.controllers;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.FerianteDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.FerianteRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class FerianteController {

    private FerianteRepository ferianteRepository;

    public FerianteController (FerianteRepository ferianteRepository){this.ferianteRepository=ferianteRepository;}

    @GetMapping("/feriantes")
    public List<FerianteDTO>getFeriantes(){
        return ferianteRepository.findAll()
                .stream()
                .map(feriante -> new FerianteDTO(feriante))
                .toList();
    }

    @GetMapping("/feriantes/{id}")
    public FerianteDTO getFerianteDTO(@PathVariable Integer id){
        return ferianteRepository.findById(id)
                .map(FerianteDTO::new)
                .orElse(null);
    }
}
