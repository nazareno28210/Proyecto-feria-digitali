package com.mansilla_nazareno.feriadigital.feriadigital;

import com.mansilla_nazareno.feriadigital.feriadigital.models.*;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.AdministradorDeFeria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.CategoriaProducto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.AdministradorDeFeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.FeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.StandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.CategoriaProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.FerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.UsuarioRepository;
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
			StandRepository standRepository,
			CategoriaProductoRepository categoriaRepository // 🟢 Agregamos esto
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
				francisco.setTipoUsuario(TipoUsuario.FERIANTE);
				usuarioRepository.save(nazareno);
				usuarioRepository.save(denis);
				usuarioRepository.save(francisco);
				usuarioRepository.save(maria);

				AdministradorDeFeria admin1 = new AdministradorDeFeria();
				admin1.setUsuario(nazareno);
				administradorDeFeriaRepository.save(admin1);

				// =========================================
				// 2. CATEGORÍAS GLOBALES (Creamos las que usaremos)
				// =========================================
				CategoriaProducto catIndumentaria = new CategoriaProducto("Indumentaria", "Ropa y telas");
				CategoriaProducto catCalzado = new CategoriaProducto("Calzado", "Zapatos y zapatillas");
				CategoriaProducto catAccesorios = new CategoriaProducto("Accesorios", "Complementos varios");
				CategoriaProducto catMascotas = new CategoriaProducto("Mascotas", "Todo para animales");

				categoriaRepository.saveAll(List.of(catIndumentaria, catCalzado, catAccesorios, catMascotas));

				// =========================================
				// 3. FERIA
				// =========================================
				Feria feria = new Feria("Feria Gimnasio Don Bosco", LocalDate.of(2025, 12, 24), LocalDate.of(2025, 12, 27), "Colegio Don Bosco, Alberdi 368", "Feria artesanal y comercial", "ACTIVA", "/uploads/ferias/Don_Bosco.png");
				feriaRepository.save(feria);

				// =========================================
				// 4. STAND 1 E INDUMENTARIA
				// =========================================
				Stand stand1 = new Stand("Indumentaria Falco", "Ropa deportiva y urbana", null);
				stand1.setFeria(feria);

				Feriante feriante1 = new Feriante("Indumentaria Francisco", "Venta de ropa", "2964-555999", "falco@gmail.com", EstadoUsuario.ACTIVO);
				feriante1.setUsuario(francisco);
				feriante1.setStand(stand1);
				stand1.setFeriante(feriante1);

				// --- Productos Stand 1 ---
				Producto p1 = new Producto(50000, "Pantalón térmico neopren", "Pantalón Invierno");
				p1.setCategoria(catIndumentaria); // 🟢 Ahora es setCategoria

				Producto p2 = new Producto(25000, "Camisa floreada manga corta", "Camisa Verano");
				p2.setCategoria(catIndumentaria);

				Producto p3 = new Producto(17000, "Zapatillas urbanas", "Zapatillas Rebook");
				p3.setCategoria(catCalzado);

				Producto p4 = new Producto(20000, "Remera estampada Messi", "Remera 10");
				p4.setCategoria(catIndumentaria);

				Producto p5 = new Producto(18000, "Bufanda de lana tejida", "Bufanda Artesanal");
				p5.setCategoria(catAccesorios);

				List<Producto> productosStand1 = List.of(p1, p2, p3, p4, p5);
				productosStand1.forEach(p -> p.setStand(stand1));
				stand1.setProductos(productosStand1);

				// =========================================
				// 5. STAND 2 Y MASCOTAS
				// =========================================
				Stand stand2 = new Stand("Mascotas Felices", "Todo para tu perro y gato", "/uploads/stands/mascota.png");
				stand2.setFeria(feria);

				Feriante feriante2 = new Feriante("Emprendimiento Mascotas", "Accesorios para mascotas", "2964-444555", "mascotas@gmail.com", EstadoUsuario.ACTIVO);
				feriante2.setUsuario(maria);
				feriante2.setStand(stand2);
				stand2.setFeriante(feriante2);

				// --- Productos Stand 2 ---
				Producto p10 = new Producto(15000, "Pelota de caucho resistente", "Juguete perro");
				p10.setCategoria(catMascotas);

				Producto p11 = new Producto(12000, "Collar ajustable reflectivo", "Collar perro");
				p11.setCategoria(catMascotas);

				List<Producto> productosStand2 = List.of(p10, p11);
				productosStand2.forEach(p -> p.setStand(stand2));
				stand2.setProductos(productosStand2);

				// Guardado final
				feria.setStands(List.of(stand1, stand2));
				feriaRepository.save(feria);

				System.out.println("--- DATOS DE PRUEBA CARGADOS EXITOSAMENTE ---");
			}
		};
	}}