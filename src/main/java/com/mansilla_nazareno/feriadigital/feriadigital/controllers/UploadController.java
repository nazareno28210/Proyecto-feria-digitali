package com.mansilla_nazareno.feriadigital.feriadigital.controllers;

import com.mansilla_nazareno.feriadigital.feriadigital.services.CloudinaryService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private final CloudinaryService cloudinaryService;

    public UploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping
    public Map<String, String> subir(@RequestParam("imagen") MultipartFile imagen) {
        return cloudinaryService.subirImagen(imagen);
    }
}

