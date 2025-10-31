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

				// Relacionar productos con categorías
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

				p1.setStand(stand1); p2.setStand(stand1); p3.setStand(stand1);
				p4.setStand(stand1); p5.setStand(stand1); p6.setStand(stand1);
				p7.setStand(stand1); p8.setStand(stand1); p9.setStand(stand1);

				// ------------------- Feria -------------------
				Feria feria = new Feria(
						"Feria Gimnasio Don Bosco",
						LocalDate.of(2025, 10, 24),
						LocalDate.of(2025, 10, 27),
						"Colegio Don Bosco,Alberdi 368",
						"Feria De Rio Grande TDF ",
						"ACTIVA",
						"Centro Cultural"
				);



				feria.setStands(List.of(stand1));
				stand1.setFeria(feria); // si la relación es bidireccional

				Feriante ferianteFrancisco = new Feriante(
						"Indumentaria Francisco",
						"Vendemos Pantalones,Remeras,zapatillas",
						"2964-555999",
						"contactofrancisco@gmail.com",
						EstadoUsuario.ACTIVO
				);
				ferianteFrancisco.setStand(stand1);
				stand1.setFeriante(ferianteFrancisco);

				ferianteFrancisco.setUsuario(francisco);

				usuarioRepository.save(francisco);

				// ------------------- Categorías adicionales para stand2 -------------------
				CategoriaProducto c10 = new CategoriaProducto("juguete", "juguete para perros");
				CategoriaProducto c11 = new CategoriaProducto("collar", "accesorio para perros");
				CategoriaProducto c12 = new CategoriaProducto("ropa", "prenda para perros");
				CategoriaProducto c13 = new CategoriaProducto("comida", "alimento para perros");
				CategoriaProducto c14 = new CategoriaProducto("cama", "cama para perros");
				CategoriaProducto c15 = new CategoriaProducto("accesorio", "accesorio para perros");
				CategoriaProducto c16 = new CategoriaProducto("correa", "accesorio para perros");
				CategoriaProducto c17 = new CategoriaProducto("hueso", "snack para perros");
				CategoriaProducto c18 = new CategoriaProducto("arnes", "accesorio para perros");
				CategoriaProducto c19 = new CategoriaProducto("chaqueta", "ropa para perros");

// ------------------- Productos stand2 (10 productos) -------------------
				Producto p10 = new Producto(15000, "pelota de caucho resistente para perros", "pelota perro");
				Producto p11 = new Producto(12000, "collar de cuero ajustable", "collar perro");
				Producto p12 = new Producto(25000, "abrigo impermeable para perros medianos", "abrigo perro");
				Producto p13 = new Producto(8000, "snacks naturales de carne", "snacks perro");
				Producto p14 = new Producto(30000, "cama acolchada para perros grandes", "cama perro");
				Producto p15 = new Producto(10000, "juguete mordedor de cuerda", "mordedor perro");
				Producto p16 = new Producto(7000, "correa resistente de nylon", "correa perro");
				Producto p17 = new Producto(18000, "arnés ajustable para paseo", "arnes perro");
				Producto p18 = new Producto(12000, "huesos de carne deshidratada", "hueso perro");
				Producto p19 = new Producto(22000, "chaqueta polar para perros pequeños", "chaqueta perro");

// ------------------- Relacionar productos con categorías -------------------
				p10.setCategorias(List.of(c10)); c10.setProducto(p10);
				p11.setCategorias(List.of(c11)); c11.setProducto(p11);
				p12.setCategorias(List.of(c12)); c12.setProducto(p12);
				p13.setCategorias(List.of(c13)); c13.setProducto(p13);
				p14.setCategorias(List.of(c14)); c14.setProducto(p14);
				p15.setCategorias(List.of(c15)); c15.setProducto(p15);
				p16.setCategorias(List.of(c16)); c16.setProducto(p16);
				p17.setCategorias(List.of(c18)); c18.setProducto(p17);
				p18.setCategorias(List.of(c17)); c17.setProducto(p18);
				p19.setCategorias(List.of(c19)); c19.setProducto(p19);

// ------------------- Stand -------------------
				Stand stand2 = new Stand("Mascotas Felices", "Todo para tu perro: juguetes, ropa y accesorios");
				stand2.setProductos(List.of(p10, p11, p12, p13, p14, p15, p16, p17, p18, p19));

// Relacionar productos con el stand
				p10.setStand(stand2); p11.setStand(stand2); p12.setStand(stand2);
				p13.setStand(stand2); p14.setStand(stand2); p15.setStand(stand2);
				p16.setStand(stand2); p17.setStand(stand2); p18.setStand(stand2); p19.setStand(stand2);

// ------------------- Feriante -------------------
				Usuario ferianteUsuario2 = new Usuario(
						"María",
						"González",
						"maria.perros@gmail.com",
						passwordEncoder.encode("123"),
						EstadoUsuario.ACTIVO
				);

				Feriante feriante2 = new Feriante(
						"Emprendimiento Mascotas Felices",
						"Venta de productos para perros: juguetes, ropa, accesorios y comida",
						"2964-444555",
						"contacto@mascotasfelices.com",
						EstadoUsuario.ACTIVO
				);
				feriante2.setUsuario(ferianteUsuario2);
				feriante2.setStand(stand2);
				stand2.setFeriante(feriante2);

				usuarioRepository.save(ferianteUsuario2);

// ------------------- Agregar el stand2 a la feria existente -------------------
				feria.setStands(List.of(stand1, stand2));
				stand2.setFeria(feria);




				// ------------------- Guardado final -------------------
				// Con cascade = ALL en Stand -> Producto y Producto -> CategoriaProducto
				// basta con guardar la feria
				feriaRepository.save(feria);

			}
		};
	}
}
