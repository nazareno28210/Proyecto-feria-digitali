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
				Usuario denis = new Usuario(
						"denis",
						"mansilla",
						"denis@gmail.com",
						passwordEncoder.encode("123"),
						EstadoUsuario.ACTIVO
				);



				AdministradorDeFeria admin1 = new AdministradorDeFeria();
				admin1.setUsuario(nazareno);
				usuarioRepository.save(nazareno);
				usuarioRepository.save(denis);
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
				CategoriaProducto c5 = new CategoriaProducto("bufanda", "prenda de abrigo");
				CategoriaProducto c6 = new CategoriaProducto("campera", "prenda de abrigo");
				CategoriaProducto c7 = new CategoriaProducto("guantes", "accesorio invierno");
				CategoriaProducto c8 = new CategoriaProducto("gorro", "accesorio invierno");
				CategoriaProducto c9 = new CategoriaProducto("chaleco", "prenda outdoor");

// ------------------- Productos -------------------
				Producto p1 = new Producto(50000, "pantalon para nieve de neopren", "pantalon termico");
				Producto p2 = new Producto(25000, "camisa con estampado de flores", "camisa de verano");
				Producto p3 = new Producto(17000, "zapatillas con logo de supere", "zapatillas rebook");
				Producto p4 = new Producto(20000, "Remera negra con estampado de la cara de Messi", "Remera Messi");
				Producto p5 = new Producto(18000, "bufanda tejida a mano con lana argentina", "bufanda lana");
				Producto p6 = new Producto(65000, "campera impermeable para invierno", "campera de invierno");
				Producto p7 = new Producto(8000, "guantes de cuero sintético", "guantes invierno");
				Producto p8 = new Producto(14000, "gorro de lana con pompon", "gorro invierno");
				Producto p9 = new Producto(27000, "chaleco con bolsillos en los costados", "chaleco");

// ------------------- Relacionar productos con categorías -------------------
				p1.setCategorias(List.of(c1)); c1.setProducto(p1);
				p2.setCategorias(List.of(c2)); c2.setProducto(p2);
				p3.setCategorias(List.of(c3)); c3.setProducto(p3);
				p4.setCategorias(List.of(c2)); c4.setProducto(p4); // remera también
				p5.setCategorias(List.of(c5)); c5.setProducto(p5);
				p6.setCategorias(List.of(c6)); c6.setProducto(p6);
				p7.setCategorias(List.of(c7)); c7.setProducto(p7);
				p8.setCategorias(List.of(c8)); c8.setProducto(p8);
				p9.setCategorias(List.of(c9)); c9.setProducto(p9);

// ------------------- Stand -------------------
				Stand stand1 = new Stand("Indumentaria Falco", "vendemos ropa, zapatillas, sombreros");
				stand1.setProductos(List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9));

// Relacionar productos con el stand
				p1.setStand(stand1); p2.setStand(stand1); p3.setStand(stand1);
				p4.setStand(stand1); p5.setStand(stand1); p6.setStand(stand1);
				p7.setStand(stand1); p8.setStand(stand1); p9.setStand(stand1);

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
				stand1.setFeria(feria);

// ------------------- Feriante -------------------
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

// ------------------- Guardado final -------------------
				feriaRepository.save(feria);





				// ------------------- Guardado final -------------------
				// Con cascade = ALL en Stand -> Producto y Producto -> CategoriaProducto
				// basta con guardar la feria

			}
		};
	}
}
