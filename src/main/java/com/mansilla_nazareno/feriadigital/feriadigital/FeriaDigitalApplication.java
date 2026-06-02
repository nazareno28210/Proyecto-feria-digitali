package com.mansilla_nazareno.feriadigital.feriadigital;

import com.mansilla_nazareno.feriadigital.feriadigital.models.*;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.*;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.CategoriaProducto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.TipoVenta;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.AdministradorDeFeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.FeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.StandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.ParticipacionRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.EdicionFeriaRepository; // 🟢 Importado
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.CategoriaProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.FerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.ProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@SpringBootApplication
public class FeriaDigitalApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeriaDigitalApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(
			UsuarioRepository usuarioRepository,
			AdministradorDeFeriaRepository administradorDeFeriaRepository,
			FerianteRepository ferianteRepository,
			FeriaRepository feriaRepository,
			EdicionFeriaRepository edicionFeriaRepository, // 🟢 Inyectado para inicializar eventos
			StandRepository standRepository,
			CategoriaProductoRepository categoriaRepository,
			ProductoRepository productoRepository,
			ParticipacionRepository participacionRepository,
			PasswordEncoder passwordEncoder
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
				CategoriaProducto catIndumentaria = new CategoriaProducto("Indumentaria", "Prendas de vestir para todas las edades");
				CategoriaProducto catBlancoTextil = new CategoriaProducto("Blanco y Textil", "Artículos de tela para el hogar");
				CategoriaProducto catCalzado = new CategoriaProducto("Calzado", "Zapatos y zapatillas");
				CategoriaProducto catAccesorios = new CategoriaProducto("Accesorios", "Complementos de moda");
				CategoriaProducto catMascotas = new CategoriaProducto("Mascotas", "Alimentos y juguetes para animales");
				CategoriaProducto catGastronomia = new CategoriaProducto("Gastronomía", "Comidas preparadas");
				CategoriaProducto catPanaderia = new CategoriaProducto("Panificación", "Pan casero y repostería");
				CategoriaProducto catBebidas = new CategoriaProducto("Bebidas", "Jugos y cervezas artesanales");
				CategoriaProducto catHogar = new CategoriaProducto("Hogar y Decoración", "Artículos de decoración y velas");
				CategoriaProducto catArtesanias = new CategoriaProducto("Artesanías", "Trabajos en madera y cerámica");
				CategoriaProducto catVivero = new CategoriaProducto("Vivero y Jardín", "Plantas de interior y macetas");
				CategoriaProducto catBelleza = new CategoriaProducto("Belleza y Salud", "Cosmética natural");
				CategoriaProducto catJuguetes = new CategoriaProducto("Juguetes", "Juegos de madera y tela");
				CategoriaProducto catLibreria = new CategoriaProducto("Librería y Arte", "Cuadernos artesanales");

				categoriaRepository.saveAll(List.of(catIndumentaria, catCalzado, catAccesorios, catMascotas,
						catGastronomia, catPanaderia, catBebidas, catHogar, catArtesanias, catVivero,
						catBelleza, catJuguetes, catLibreria, catBlancoTextil));

				// =========================================
				// 3. FERIA (PLANTILLA BASE)
				// =========================================
				// 🟢 CORREGIDO: Constructor de plantilla pura (7 parámetros)
				Feria feriaBase = new Feria("Feria Gimnasio Don Bosco", "Colegio Don Bosco, Alberdi 368", "Feria artesanal y comercial", "/uploads/ferias/Don_Bosco.png", -53.78904155240556, -67.70062989474968, 25);
				feriaRepository.save(feriaBase);

				// =========================================
				// 3.B EDICIÓN CRONOLÓGICA (EL EVENTO REAL)
				// =========================================
				// 🟢 NUEVO: Guardamos el fin de semana del evento en la tabla de ediciones
				EdicionFeria edicionFebrero = new EdicionFeria();
				edicionFebrero.setFeria(feriaBase);
				edicionFebrero.setNombreEdicion("Edición Febrero Semana 3 2026");
				edicionFebrero.setFechaInicio(LocalDate.of(2026, 6, 20));
				edicionFebrero.setFechaFinal(LocalDate.of(2026, 6, 22));
				edicionFebrero.setHoraInicio(LocalTime.of(14, 0));
				edicionFebrero.setHoraFin(LocalTime.of(20, 0));
				edicionFebrero.setEstado("ACTIVA");
				edicionFeriaRepository.save(edicionFebrero);

				// =========================================
				// 4. STAND 1 E INDUMENTARIA
				// =========================================
				Stand stand1 = new Stand("Indumentaria Falco", "Ropa deportiva y urbana", null);

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

				List<Producto> productosStand1 = List.of(p1, p2, p3);
				productosStand1.forEach(p -> p.setStand(stand1));
				stand1.setProductos(productosStand1);

				standRepository.save(stand1);

				// =========================================
				// 5. STAND 2 Y MASCOTAS
				// =========================================
				Stand stand2 = new Stand("Mascotas Felices", "Todo para tu perro y gato", "/uploads/stands/mascota.png");

				Feriante feriante2 = new Feriante("Emprendimiento Mascotas", "Accesorios para mascotas", "2964-444555", "mascotas@gmail.com", EstadoUsuario.ACTIVO);
				feriante2.setUsuario(maria);
				feriante2.setStand(stand2);
				stand2.setFeriante(feriante2);

				// --- Productos Stand 2 ---
				Producto p10 = new Producto(15000, "Pelota de caucho resistente", "Juguete perro");
				p10.setCategoria(catMascotas);
				p10.setTipoVenta(TipoVenta.UNIDAD);
				p10.setUnidadMedida("un");

				List<Producto> productosStand2 = List.of(p10);
				productosStand2.forEach(p -> p.setStand(stand2));
				stand2.setProductos(productosStand2);

				standRepository.save(stand2);

				// =========================================
				// 6. VINCULAR CON PARTICIPACIONES A LA EDICIÓN
				// =========================================
				// 🟢 CORREGIDO: Asociamos las postulaciones directas a edicionFebrero usando .setEdicion()
				Participacion part1 = new Participacion();
				part1.setEdicion(edicionFebrero);
				part1.setStand(stand1);
				part1.setEstado(EstadoParticipacion.CONFIRMADO);
				participacionRepository.save(part1);

				Participacion part2 = new Participacion();
				part2.setEdicion(edicionFebrero);
				part2.setStand(stand2);
				part2.setEstado(EstadoParticipacion.CONFIRMADO);
				participacionRepository.save(part2);

				System.out.println("--- DATOS DE PRUEBA CARGADOS EXITOSAMENTE CON LA NUEVA ARQUITECTURA ---");
			}
		};
	}
}