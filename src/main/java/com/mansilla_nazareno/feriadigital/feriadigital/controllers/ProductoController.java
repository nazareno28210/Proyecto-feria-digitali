package com.mansilla_nazareno.feriadigital.feriadigital.controllers;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.ProductoDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.ProductoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductoController {
    private ProductoRepository productoRepository;

    public ProductoController(ProductoRepository productoRepository){this.productoRepository=productoRepository;}

    @GetMapping("/productos")
    public List<ProductoDTO> getProductos(){
        return productoRepository.findAll()
                .stream()
                .map(producto -> new ProductoDTO(producto))
                .toList();
    }

    @GetMapping("/productos/{id}")
    public ProductoDTO getProductoDTO(@PathVariable Integer id){
        return productoRepository.findById(id)
                .map(ProductoDTO::new)
                .orElse(null);
    }


}
