package com.mansilla_nazareno.feriadigital.feriadigital;

import com.mansilla_nazareno.feriadigital.feriadigital.models.*;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class FeriaDigitalApplication {

	@Autowired
	private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(FeriaDigitalApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(
			UsuarioRepository usuarioRepository,
			AdministradorDeFeriaRepository administradorDeFeriaRepository,
			FerianteRepository ferianteRepository,
			FeriaRepository feriaRepository,
			StandRepository standRepository
	) {
		return (args) -> {
			if (usuarioRepository.findAll().isEmpty()) {

				// ------------------- Usuarios -------------------
				Usuario nazareno = new Usuario(
						"Nazareno",
						"Guardia",
						"nazarenoguardia2004@gmail.com",
						passwordEncoder.encode("123"),
						EstadoUsuario.ACTIVO
				);



				AdministradorDeFeria admin1 = new AdministradorDeFeria();
				admin1.setUsuario(nazareno);
				usuarioRepository.save(nazareno);
				administradorDeFeriaRepository.save(admin1);

				Usuario francisco = new Usuario(
						"Francisco",
						"García",
						"francisco@gmail.com",
						passwordEncoder.encode("123"),
						EstadoUsuario.ACTIVO
				);


				// ------------------- Categorías -------------------
				CategoriaProducto c1 = new CategoriaProducto("pantalon", "prenda de vestir");
				CategoriaProducto c2 = new CategoriaProducto("remera", "prenda de vestir");
				CategoriaProducto c3 = new CategoriaProducto("zapatilla", "calzado");
				CategoriaProducto c4 = new CategoriaProducto("pelota", "juguete para perro");

				// ------------------- Productos -------------------
				Producto p1 = new Producto(10000, "pantalon para nieve de neopren", "pantalon termico");
				Producto p2 = new Producto(20000, "camisa con estampado de flores", "camisa de verano");
				Producto p3 = new Producto(20000, "color rojo con negro,de vestir", "zapatillas rebook");
				Producto p4 = new Producto(20000, "pelota color rojo para perro", "kongo");

				// Relacionar productos con categorías
				p1.setCategorias(List.of(c1));
				c1.setProducto(p1);

				p2.setCategorias(List.of(c2));
				c2.setProducto(p2);

				p3.setCategorias(List.of(c3));
				c3.setProducto(p3);

				p4.setCategorias(List.of(c4));
				c4.setProducto(p4);

				// ------------------- Stand -------------------
				Stand stand1 = new Stand("Indumentaria Falco", "vendemos ropa, zapatillas, sombreros");
				stand1.setProductos(List.of(p1, p2, p3));
				p1.setStand(stand1);
				p2.setStand(stand1);
				p3.setStand(stand1);

				// ------------------- Feria -------------------
				Feria feria = new Feria(
						"Feria de Tecnología",
						LocalDate.of(2025, 11, 1),
						LocalDate.of(2025, 11, 5),
						"Centro Cultural",
						"Feria dedicada a la tecnología y gadgets",
						"ACTIVA",
						"Centro Cultural"
				);

				feria.setStands(List.of(stand1));
				stand1.setFeria(feria); // si la relación es bidireccional

				Feriante ferianteFrancisco = new Feriante(
						"Emprendimiento Francisco",
						"Venta de artesanías de madera",
						"2964-555999",
						"contactofrancisco@gmail.com",
						EstadoUsuario.ACTIVO
				);
				ferianteFrancisco.setStand(stand1);
				stand1.setFeriante(ferianteFrancisco);

				ferianteFrancisco.setUsuario(francisco);

				usuarioRepository.save(francisco);





				// ------------------- Guardado final -------------------
				// Con cascade = ALL en Stand -> Producto y Producto -> CategoriaProducto
				// basta con guardar la feria
				feriaRepository.save(feria);
			}
		};
	}
}
