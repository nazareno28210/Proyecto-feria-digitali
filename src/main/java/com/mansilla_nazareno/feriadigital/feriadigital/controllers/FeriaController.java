package com.mansilla_nazareno.feriadigital.feriadigital.controllers;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.FeriaDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.FerianteDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.StandDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.FeriaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class FeriaController {

    private final FeriaRepository feriaRepository;

    public FeriaController(FeriaRepository feriaRepository) {this.feriaRepository = feriaRepository;}

    @GetMapping("/ferias")
    public List<FeriaDTO> getFerias() {
        return feriaRepository.findAll()
                .stream()
                .map(feria -> new FeriaDTO(feria))
                .collect(Collectors.toList());
    }

    @GetMapping("/ferias/{id}")
    public FeriaDTO getFeria(@PathVariable Integer id) {
        return feriaRepository.findById(id)
                .map(FeriaDTO::new)
                .orElse(null);
    }




}
