package com.mansilla_nazareno.feriadigital.feriadigital;

import com.mansilla_nazareno.feriadigital.feriadigital.models.*;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.AdministradorDeFeria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.CategoriaProducto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.TipoVenta; // 🟢 Importado
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.AdministradorDeFeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.FeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.StandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.CategoriaProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.FerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.ProductoRepository;
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
			CategoriaProductoRepository categoriaRepository,
			ProductoRepository productoRepository
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
				usuarioRepository.saveAll(List.of(nazareno, denis, francisco, maria));

				AdministradorDeFeria admin1 = new AdministradorDeFeria();
				admin1.setUsuario(nazareno);
				administradorDeFeriaRepository.save(admin1);

				// =========================================
				// 2. CATEGORÍAS GLOBALES
				// =========================================
				CategoriaProducto catIndumentaria = new CategoriaProducto("Indumentaria", "Prendas de vestir para todas las edades, ropa de diseño, uniformes y tejidos artesanales listos para usar");
				CategoriaProducto catBlancoTextil = new CategoriaProducto("Blanco y Textil", "Artículos de tela para el hogar como sábanas, manteles, cortinas y toallas, además de rollos de tela y retazos para confección");
				CategoriaProducto catCalzado = new CategoriaProducto("Calzado", "Zapatos, zapatillas, sandalias, botas y calzado artesanal o industrial");
				CategoriaProducto catAccesorios = new CategoriaProducto("Accesorios", "Complementos de moda, joyería, marroquinería, bufandas, cinturones y carteras");
				CategoriaProducto catMascotas = new CategoriaProducto("Mascotas", "Alimentos, accesorios, juguetes y ropa para mascotas y animales domésticos");


				CategoriaProducto catGastronomia = new CategoriaProducto("Gastronomía", "Comidas preparadas, minutas y platos regionales");
				CategoriaProducto catPanaderia = new CategoriaProducto("Panificación", "Pan casero, prepizzas, facturas y productos de repostería");
				CategoriaProducto catBebidas = new CategoriaProducto("Bebidas", "Jugos naturales, licuados, cervezas artesanales y conservas");

				CategoriaProducto catHogar = new CategoriaProducto("Hogar y Decoración", "Artículos de decoración, velas aromáticas y textiles para el hogar");
				CategoriaProducto catArtesanias = new CategoriaProducto("Artesanías", "Trabajos en madera, cerámica, tejido a mano y cuero");
				CategoriaProducto catVivero = new CategoriaProducto("Vivero y Jardín", "Plantas de interior, plantines, macetas decoradas y abonos");

				CategoriaProducto catBelleza = new CategoriaProducto("Belleza y Salud", "Cosmética natural, jabones artesanales y aceites esenciales");
				CategoriaProducto catJuguetes = new CategoriaProducto("Juguetes", "Juegos de ingenio, muñecos de tela y juguetes de madera");
				CategoriaProducto catLibreria = new CategoriaProducto("Librería y Arte", "Cuadernos artesanales, láminas decorativas y artículos de papelería");

				categoriaRepository.saveAll(List.of(catIndumentaria, catCalzado, catAccesorios, catMascotas,
						catGastronomia, catPanaderia, catBebidas,
						catHogar, catArtesanias, catVivero,
						catBelleza, catJuguetes, catLibreria,catBlancoTextil));

				// =========================================
				// 3. FERIA
				// =========================================
				Feria feria = new Feria("Feria Gimnasio Don Bosco", LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 22), "Colegio Don Bosco, Alberdi 368", "Feria artesanal y comercial", "Activa", "/uploads/ferias/Don_Bosco.png", -53.78904155240556,  -67.70062989474968);
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
				p1.setCategoria(catIndumentaria);
				p1.setTipoVenta(TipoVenta.UNIDAD);
				p1.setUnidadMedida("un");

				Producto p2 = new Producto(25000, "Camisa floreada manga corta", "Camisa Verano");
				p2.setCategoria(catIndumentaria);
				p2.setTipoVenta(TipoVenta.UNIDAD);
				p2.setUnidadMedida("un");

				Producto p3 = new Producto(17000, "Zapatillas urbanas", "Zapatillas Rebook");
				p3.setCategoria(catCalzado);
				p3.setTipoVenta(TipoVenta.UNIDAD);
				p3.setUnidadMedida("un");

				Producto p4 = new Producto(20000, "Remera estampada Messi", "Remera 10");
				p4.setCategoria(catIndumentaria);
				p4.setTipoVenta(TipoVenta.UNIDAD);
				p4.setUnidadMedida("un");

				Producto p5 = new Producto(18000, "Bufanda de lana tejida", "Bufanda Artesanal");
				p5.setCategoria(catAccesorios);
				p5.setTipoVenta(TipoVenta.UNIDAD);
				p5.setUnidadMedida("un");

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
				p10.setTipoVenta(TipoVenta.UNIDAD);
				p10.setUnidadMedida("un");

				Producto p11 = new Producto(12000, "Collar ajustable reflectivo", "Collar perro");
				p11.setCategoria(catMascotas);
				p11.setTipoVenta(TipoVenta.UNIDAD);
				p11.setUnidadMedida("un");

				List<Producto> productosStand2 = List.of(p10, p11);
				productosStand2.forEach(p -> p.setStand(stand2));
				stand2.setProductos(productosStand2);

				// Guardado final
				feria.setStands(List.of(stand1, stand2));
				feriaRepository.save(feria);

				System.out.println("--- DATOS DE PRUEBA CARGADOS EXITOSAMENTE ---");
			}
		};
	}
}