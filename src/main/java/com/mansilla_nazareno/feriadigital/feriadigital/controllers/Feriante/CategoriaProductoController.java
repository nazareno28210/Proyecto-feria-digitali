package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Feriante;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante.CategoriaProductoDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.CategoriaProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaProductoController {

    @Autowired
    private CategoriaProductoRepository categoriaRepository;

    // Endpoint que usa tu JS para llenar los selects
    @GetMapping
    public List<CategoriaProductoDTO> getCategorias() {
        return categoriaRepository.findAll()
                .stream()
                .map(CategoriaProductoDTO::new)
                .toList();
    }
}