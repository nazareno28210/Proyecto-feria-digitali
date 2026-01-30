package com.mansilla_nazareno.feriadigital.feriadigital.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;


    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }


    public Map<String, String> subirImagen(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

            Map<String, String> resultado = new HashMap<>();
            resultado.put("url", uploadResult.get("secure_url").toString());
            resultado.put("public_id", uploadResult.get("public_id").toString());

            return resultado;

        } catch (IOException e) {throw new RuntimeException("Error al subir imagen a Cloudinary", e);}
    }
    public void borrarImagen(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Error al borrar imagen de Cloudinary", e);
        }
    }


    public Map<String, String> reemplazarImagen(
            MultipartFile nuevaImagen,
            String publicIdViejo
    ) {
        // 1. Borrar imagen anterior
        if (publicIdViejo != null && !publicIdViejo.isEmpty()) {
            borrarImagen(publicIdViejo);
        }

        // 2. Subir nueva imagen
        return subirImagen(nuevaImagen);
    }


}
