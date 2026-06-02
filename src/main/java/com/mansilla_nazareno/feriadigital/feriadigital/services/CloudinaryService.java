package com.mansilla_nazareno.feriadigital.feriadigital.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service // Indica a Spring que esta clase es un componente de servicio (Capa de Lógica de Negocio)
public class CloudinaryService {

    // Dependencia inyectada para interactuar con la API de Cloudinary
    private final Cloudinary cloudinary;

    // Inyección de dependencias por constructor (Buena práctica en Spring Boot)
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

     //Sube un archivo de imagen a Cloudinary y retorna sus datos de acceso.
    public Map<String, String> subirImagen(MultipartFile file) {
        try {
            // Convierte el MultipartFile a un arreglo de bytes y lo sube a Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

            // Estructura de datos para devolver solo la información necesaria al frontend o base de datos
            Map<String, String> resultado = new HashMap<>();
            resultado.put("url", uploadResult.get("secure_url").toString()); // URL segura (HTTPS) para mostrar la imagen
            resultado.put("public_id", uploadResult.get("public_id").toString()); // ID único necesario para futuras eliminaciones

            return resultado;

        } catch (IOException e) {
            // Captura errores de lectura del archivo y lanza una excepción no comprobada
            throw new RuntimeException("Error al subir imagen a Cloudinary", e);
        }
    }

     // Elimina una imagen existente en Cloudinary utilizando su identificador público.

    public void borrarImagen(String publicId) {
        try {
            // Llama al método destroy de la API para eliminar el recurso en la nube
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            // Captura errores de red o de la API al intentar borrar
            throw new RuntimeException("Error al borrar imagen de Cloudinary", e);
        }
    }

    // Reemplaza una imagen antigua por una nueva.

    public Map<String, String> reemplazarImagen(
            MultipartFile nuevaImagen,
            String publicIdViejo
    ) {
        // 1. Verifica que el ID de la imagen vieja sea válido antes de intentar borrarla
        if (publicIdViejo != null && !publicIdViejo.isEmpty()) {
            borrarImagen(publicIdViejo);
        }

        // 2. Sube la nueva imagen y retorna el mapa con la nueva URL y el nuevo ID
        return subirImagen(nuevaImagen);
    }
}