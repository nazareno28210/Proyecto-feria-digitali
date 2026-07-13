package com.mansilla_nazareno.feriadigital.feriadigital.repositories.product;
import com.mansilla_nazareno.feriadigital.feriadigital.models.product.ProductoImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoImagenRepository extends JpaRepository<ProductoImagen, Integer> {
}
