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

				// =========================================
				// 1. USUARIOS Y ADMIN
				// =========================================
				Usuario nazareno = new Usuario("Nazareno", "Guardia", "nazarenoguardia2004@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				Usuario denis = new Usuario("Denis", "Mansilla", "denis@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				Usuario francisco = new Usuario("Francisco", "García", "francisco@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				Usuario maria = new Usuario("María", "González", "maria.perros@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				nazareno.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
				usuarioRepository.save(nazareno);
				usuarioRepository.save(denis);
				usuarioRepository.save(francisco);
				usuarioRepository.save(maria);

				AdministradorDeFeria admin1 = new AdministradorDeFeria();
				admin1.setUsuario(nazareno);
				administradorDeFeriaRepository.save(admin1);

				// =========================================
				// 2. FERIA
				// =========================================
				Feria feria = new Feria(
						"Feria Gimnasio Don Bosco",
						LocalDate.of(2025, 12, 24),
						LocalDate.of(2025, 12, 27),
						"Colegio Don Bosco, Alberdi 368",
						"Feria artesanal y comercial",
						"ACTIVA",
						"Centro Cultural",
						"/uploads/ferias/Don_Bosco.png"
				);
				// Guardamos la feria primero para tener su ID si hace falta,
				// aunque con CascadeType.ALL en los stands se guardaría al final también.
				feriaRepository.save(feria);

				// =========================================
				// 3. STAND 1: INDUMENTARIA FALCO
				// =========================================
				Stand stand1 = new Stand("Indumentaria Falco", "Ropa deportiva y urbana para todos",
						"/uploads/stands/Indumentaria.png");
				stand1.setFeria(feria);

				Feriante feriante1 = new Feriante("Indumentaria Francisco", "Venta de ropa y calzado", "2964-555999", "falco@gmail.com", EstadoUsuario.ACTIVO);
				feriante1.setUsuario(francisco);
				feriante1.setStand(stand1);
				stand1.setFeriante(feriante1);

				// --- Productos Stand 1 (Con categorías generales) ---
				Producto p1 = new Producto(50000, "Pantalón térmico neopren", "Pantalón Invierno");
				CategoriaProducto cp1 = new CategoriaProducto("Indumentaria", "Ropa de abrigo");
				p1.setCategorias(List.of(cp1));
				cp1.setProducto(p1);

				Producto p2 = new Producto(25000, "Camisa floreada manga corta", "Camisa Verano");
				CategoriaProducto cp2 = new CategoriaProducto("Indumentaria", "Ropa de verano");
				p2.setCategorias(List.of(cp2));
				cp2.setProducto(p2);

				Producto p3 = new Producto(17000, "Zapatillas urbanas", "Zapatillas Rebook");
				CategoriaProducto cp3 = new CategoriaProducto("Calzado", "Calzado deportivo/urbano");
				p3.setCategorias(List.of(cp3));
				cp3.setProducto(p3);

				Producto p4 = new Producto(20000, "Remera estampada Messi", "Remera 10");
				CategoriaProducto cp4 = new CategoriaProducto("Indumentaria", "Remeras temáticas");
				p4.setCategorias(List.of(cp4));
				cp4.setProducto(p4);

				Producto p5 = new Producto(18000, "Bufanda de lana tejida", "Bufanda Artesanal");
				CategoriaProducto cp5 = new CategoriaProducto("Accesorios", "Accesorios de invierno");
				p5.setCategorias(List.of(cp5));
				cp5.setProducto(p5);

				Producto p6 = new Producto(65000, "Campera impermeable térmica", "Campera Invierno");
				CategoriaProducto cp6 = new CategoriaProducto("Indumentaria", "Ropa de abrigo pesada");
				p6.setCategorias(List.of(cp6));
				cp6.setProducto(p6);

				Producto p7 = new Producto(8000, "Guantes de cuero sintético", "Guantes");
				CategoriaProducto cp7 = new CategoriaProducto("Accesorios", "Accesorios de manos");
				p7.setCategorias(List.of(cp7));
				cp7.setProducto(p7);

				Producto p8 = new Producto(14000, "Gorro de lana con pompón", "Gorro Invierno");
				CategoriaProducto cp8 = new CategoriaProducto("Accesorios", "Accesorios de cabeza");
				p8.setCategorias(List.of(cp8));
				cp8.setProducto(p8);

				Producto p9 = new Producto(27000, "Chaleco polar con bolsillos", "Chaleco Outdoor");
				CategoriaProducto cp9 = new CategoriaProducto("Indumentaria", "Ropa outdoor");
				p9.setCategorias(List.of(cp9));
				cp9.setProducto(p9);

				// Asignar productos al stand 1
				List<Producto> productosStand1 = List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9);
				productosStand1.forEach(p -> p.setStand(stand1));
				stand1.setProductos(productosStand1);

				// =========================================
				// 4. STAND 2: MASCOTAS FELICES
				// =========================================
				Stand stand2 = new Stand("Mascotas Felices", "Todo para tu perro y gato",
						"/uploads/stands/mascota.png");
				stand2.setFeria(feria);

				Feriante feriante2 = new Feriante("Emprendimiento Mascotas", "Accesorios y alimento para mascotas", "2964-444555", "mascotas@gmail.com", EstadoUsuario.ACTIVO);
				feriante2.setUsuario(maria);
				feriante2.setStand(stand2);
				stand2.setFeriante(feriante2);

				// --- Productos Stand 2 (Categoría general "Mascotas" + subcategoría opcional) ---
				Producto p10 = new Producto(15000, "Pelota de caucho resistente", "Juguete perro");
				CategoriaProducto cp10a = new CategoriaProducto("Mascotas", "Artículos generales para mascotas");
				CategoriaProducto cp10b = new CategoriaProducto("Juguetes", "Diversión para animales");
				p10.setCategorias(List.of(cp10a, cp10b));
				cp10a.setProducto(p10);
				cp10b.setProducto(p10);

				Producto p11 = new Producto(12000, "Collar ajustable reflectivo", "Collar perro");
				CategoriaProducto cp11 = new CategoriaProducto("Mascotas", "Accesorios de paseo");
				p11.setCategorias(List.of(cp11));
				cp11.setProducto(p11);

				Producto p12 = new Producto(25000, "Capa de lluvia para perros", "Ropa perro");
				CategoriaProducto cp12 = new CategoriaProducto("Mascotas", "Indumentaria animal");
				p12.setCategorias(List.of(cp12));
				cp12.setProducto(p12);

				Producto p13 = new Producto(8000, "Snacks sabor carne 500gr", "Premios perro");
				CategoriaProducto cp13 = new CategoriaProducto("Mascotas", "Alimento y premios");
				p13.setCategorias(List.of(cp13));
				cp13.setProducto(p13);

				Producto p14 = new Producto(30000, "Cama acolchada grande", "Cama mascota");
				CategoriaProducto cp14 = new CategoriaProducto("Mascotas", "Descanso y hogar");
				p14.setCategorias(List.of(cp14));
				cp14.setProducto(p14);

				Producto p15 = new Producto(10000, "Juguete soga mordedor", "Mordedor");
				CategoriaProducto cp15a = new CategoriaProducto("Mascotas", "Artículos generales");
				CategoriaProducto cp15b = new CategoriaProducto("Juguetes", "Juguetes interactivos");
				p15.setCategorias(List.of(cp15a, cp15b));
				cp15a.setProducto(p15);
				cp15b.setProducto(p15);

				Producto p16 = new Producto(7000, "Correa de paseo 2mts", "Correa nylon");
				CategoriaProducto cp16 = new CategoriaProducto("Mascotas", "Accesorios de paseo");
				p16.setCategorias(List.of(cp16));
				cp16.setProducto(p16);

				Producto p17 = new Producto(18000, "Arnés pechera regulable", "Arnés seguridad");
				CategoriaProducto cp17 = new CategoriaProducto("Mascotas", "Accesorios de paseo");
				p17.setCategorias(List.of(cp17));
				cp17.setProducto(p17);

				Producto p18 = new Producto(12000, "Hueso de carnaza natural", "Hueso snack");
				CategoriaProducto cp18 = new CategoriaProducto("Mascotas", "Alimento y premios");
				p18.setCategorias(List.of(cp18));
				cp18.setProducto(p18);

				Producto p19 = new Producto(22000, "Buzo polar para gatos/perros", "Abrigo mascota");
				CategoriaProducto cp19 = new CategoriaProducto("Mascotas", "Indumentaria animal");
				p19.setCategorias(List.of(cp19));
				cp19.setProducto(p19);

				// Asignar productos al stand 2
				List<Producto> productosStand2 = List.of(p10, p11, p12, p13, p14, p15, p16, p17, p18, p19);
				productosStand2.forEach(p -> p.setStand(stand2));
				stand2.setProductos(productosStand2);

				// =========================================
				// 5. GUARDADO FINAL (Cascada desde Feria)
				// =========================================
				feria.setStands(List.of(stand1, stand2));
				feriaRepository.save(feria);

				System.out.println("--- DATOS DE PRUEBA CARGADOS EXITOSAMENTE ---");
			}
		};
	}
}