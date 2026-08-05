package com.mansilla_nazareno.feriadigital.feriadigital;

import com.mansilla_nazareno.feriadigital.feriadigital.models.*;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.*;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.CategoriaProducto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.TipoVenta;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.SolicitudParaFeriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.AdministradorDeFeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.FeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.StandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.ParticipacionRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.EdicionFeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.CategoriaProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.FerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.ProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun.SolicitudParaFerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@EnableScheduling
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
			EdicionFeriaRepository edicionFeriaRepository,
			StandRepository standRepository,
			CategoriaProductoRepository categoriaRepository,
			ProductoRepository productoRepository,
			ParticipacionRepository participacionRepository,
			SolicitudParaFerianteRepository solicitudParaFerianteRepository,
			PasswordEncoder passwordEncoder
	) {
		return (args) -> {
			if (usuarioRepository.findAll().isEmpty()) {

				// =========================================
				// 1. ADMIN Y USUARIOS PRINCIPALES
				// =========================================
				Usuario nazareno = new Usuario("Nazareno", "Guardia", "nazarenoguardia2004@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				nazareno.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
				nazareno.setEnabled(true);
				usuarioRepository.save(nazareno);

				AdministradorDeFeria admin1 = new AdministradorDeFeria();
				admin1.setUsuario(nazareno);
				administradorDeFeriaRepository.save(admin1);

				// =========================================
				// 2. CATEGORÍAS GLOBALES DE PRODUCTOS
				// =========================================
				CategoriaProducto catIndumentaria = new CategoriaProducto("Indumentaria", "Prendas de vestir y ropa de abrigo");
				CategoriaProducto catCalzado = new CategoriaProducto("Calzado", "Zapatos, zapatillas y calzado artesanal");
				CategoriaProducto catAccesorios = new CategoriaProducto("Accesorios y Joyería", "Bijouterie, accesorios y joyería fina");
				CategoriaProducto catMascotas = new CategoriaProducto("Mascotas", "Artículos, ropa y juguetes para animales");
				CategoriaProducto catGastronomia = new CategoriaProducto("Gastronomía & Comidas", "Platos preparados y alimentos artesanales");
				CategoriaProducto catPanaderia = new CategoriaProducto("Panificación & Repostería", "Pan casero, facturas y repostería artesanal");
				CategoriaProducto catBebidas = new CategoriaProducto("Bebidas Artesanales", "Jugos naturales, licores y cerveza artesanal");
				CategoriaProducto catHogar = new CategoriaProducto("Hogar & Decoración", "Velas, difusores y elementos decorativos");
				CategoriaProducto catArtesanias = new CategoriaProducto("Artesanías & Cerámica", "Trabajos en madera, cerámica y tejido");
				CategoriaProducto catVivero = new CategoriaProducto("Vivero & Jardín", "Plantas de interior, suculentas y macetas");
				CategoriaProducto catBelleza = new CategoriaProducto("Belleza & Cosmética", "Cosmética natural y cuidado personal");

				categoriaRepository.saveAll(List.of(
						catIndumentaria, catCalzado, catAccesorios, catMascotas, catGastronomia,
						catPanaderia, catBebidas, catHogar, catArtesanias, catVivero, catBelleza
				));

				// =========================================
				// 3. FERIAS Y SUS EDICIONES
				// =========================================
				Feria feriaDonBosco = new Feria("Feria Gimnasio Don Bosco", "Colegio Don Bosco, Alberdi 368", "Feria artesanal y comercial familiar", "/uploads/ferias/Don_Bosco.png", -53.789041, -67.700629);
				Feria feriaCantoAgua = new Feria("Feria Paseo Canto del Agua", "Av. Belgrano 500", "Paseo gastronómico y de emprendedores", "/uploads/ferias/Canto_Agua.png", -53.785123, -67.698456);
				feriaRepository.saveAll(List.of(feriaDonBosco, feriaCantoAgua));

				EdicionFeria edicionDonBoscoInvierno = new EdicionFeria();
				edicionDonBoscoInvierno.setFeria(feriaDonBosco);
				edicionDonBoscoInvierno.setNombreEdicion("Edición Invierno 2026");
				edicionDonBoscoInvierno.setFechaInicio(LocalDate.of(2026, 6, 20));
				edicionDonBoscoInvierno.setFechaFinal(LocalDate.of(2026, 6, 22));
				edicionDonBoscoInvierno.setHoraInicio(LocalTime.of(14, 0));
				edicionDonBoscoInvierno.setHoraFin(LocalTime.of(20, 0));
				edicionDonBoscoInvierno.setEstado("ACTIVA");
				edicionDonBoscoInvierno.setCapacidad(30);

				EdicionFeria edicionDonBoscoPrimavera = new EdicionFeria();
				edicionDonBoscoPrimavera.setFeria(feriaDonBosco);
				edicionDonBoscoPrimavera.setNombreEdicion("Edición Primavera 2026");
				edicionDonBoscoPrimavera.setFechaInicio(LocalDate.of(2026, 9, 18));
				edicionDonBoscoPrimavera.setFechaFinal(LocalDate.of(2026, 9, 20));
				edicionDonBoscoPrimavera.setHoraInicio(LocalTime.of(14, 0));
				edicionDonBoscoPrimavera.setHoraFin(LocalTime.of(21, 0));
				edicionDonBoscoPrimavera.setEstado("PROXIMA");
				edicionDonBoscoPrimavera.setCapacidad(30);

				EdicionFeria edicionCantoAguaJulio = new EdicionFeria();
				edicionCantoAguaJulio.setFeria(feriaCantoAgua);
				edicionCantoAguaJulio.setNombreEdicion("Edición Gastronómica & Artesanal Julio");
				edicionCantoAguaJulio.setFechaInicio(LocalDate.of(2026, 7, 10));
				edicionCantoAguaJulio.setFechaFinal(LocalDate.of(2026, 7, 12));
				edicionCantoAguaJulio.setHoraInicio(LocalTime.of(12, 0));
				edicionCantoAguaJulio.setHoraFin(LocalTime.of(22, 0));
				edicionCantoAguaJulio.setEstado("ACTIVA");
				edicionCantoAguaJulio.setCapacidad(20);

				edicionFeriaRepository.saveAll(List.of(edicionDonBoscoInvierno, edicionDonBoscoPrimavera, edicionCantoAguaJulio));

				// =========================================
				// 4. 10 FERIANTES REALISTAS CON STANDS Y PRODUCTOS CON IMÁGENES
				// =========================================

				// Feriante 1: Indumentaria Falco
				Usuario u1 = new Usuario("Francisco", "García", "francisco@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				u1.setTipoUsuario(TipoUsuario.FERIANTE); u1.setEnabled(true);
				usuarioRepository.save(u1);
				Stand s1 = new Stand("Indumentaria Falco", "Ropa urbana y prendas térmicas de abrigo", null);
				Feriante f1 = new Feriante("2964-555999", "falco@gmail.com", EstadoUsuario.ACTIVO);
				f1.setUsuario(u1); f1.setStand(s1); s1.setFeriante(f1);

				Producto p1 = new Producto(50000, "Pantalón térmico neopren de alto rendimiento", "Pantalón Térmico Neopren");
				p1.setCategoria(catIndumentaria); p1.setTipoVenta(TipoVenta.UNIDAD); p1.setUnidadMedida("un"); p1.setStand(s1);
				p1.setImagenUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&q=80");

				Producto p2 = new Producto(25000, "Camisa floreada manga corta de diseño exclusivo", "Camisa Floreada Manga Corta");
				p2.setCategoria(catIndumentaria); p2.setTipoVenta(TipoVenta.UNIDAD); p2.setUnidadMedida("un"); p2.setStand(s1);
				p2.setImagenUrl("https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=600&q=80");

				Producto p3 = new Producto(37000, "Zapatillas urbanas reforzadas para uso diario", "Zapatillas Urbanas Reforzadas");
				p3.setCategoria(catCalzado); p3.setTipoVenta(TipoVenta.UNIDAD); p3.setUnidadMedida("un"); p3.setStand(s1);
				p3.setImagenUrl("https://images.unsplash.com/photo-1549298916-b41d501d3772?w=600&q=80");

				s1.setProductos(List.of(p1, p2, p3));
				standRepository.save(s1);

				// Feriante 2: Mascotas Felices
				Usuario u2 = new Usuario("María", "González", "maria.perros@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				u2.setTipoUsuario(TipoUsuario.FERIANTE); u2.setEnabled(true);
				usuarioRepository.save(u2);
				Stand s2 = new Stand("Mascotas Felices", "Juguetes y accesorios para perros y gatos", "/uploads/stands/mascota.png");
				Feriante f2 = new Feriante("2964-444555", "mascotas@gmail.com", EstadoUsuario.ACTIVO);
				f2.setUsuario(u2); f2.setStand(s2); s2.setFeriante(f2);

				Producto p4 = new Producto(15000, "Pelota de caucho ultra resistente para entrenamiento canino", "Pelota de Caucho Resistente");
				p4.setCategoria(catMascotas); p4.setTipoVenta(TipoVenta.UNIDAD); p4.setUnidadMedida("un"); p4.setStand(s2);
				p4.setImagenUrl("https://images.unsplash.com/photo-1535294435445-d7249524ef2e?w=600&q=80");

				Producto p5 = new Producto(18000, "Collar de cuero genuino ajustable con herrajes reforzados", "Collar de Cuero Ajustables");
				p5.setCategoria(catMascotas); p5.setTipoVenta(TipoVenta.UNIDAD); p5.setUnidadMedida("un"); p5.setStand(s2);
				p5.setImagenUrl("https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=600&q=80");

				s2.setProductos(List.of(p4, p5));
				standRepository.save(s2);

				// Feriante 3: Panadería Artesanal Denis
				Usuario u3 = new Usuario("Denis", "Mansilla", "denis@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				u3.setTipoUsuario(TipoUsuario.FERIANTE); u3.setEnabled(true);
				usuarioRepository.save(u3);
				Stand s3 = new Stand("Denis Panadería Artesanal", "Panificados de masa madre y repostería", null);
				Feriante f3 = new Feriante("2964-333111", "panaderiadenis@gmail.com", EstadoUsuario.ACTIVO);
				f3.setUsuario(u3); f3.setStand(s3); s3.setFeriante(f3);

				Producto p6 = new Producto(6000, "Medialunas de manteca artesanales horneadas en el día x6", "Medialunas de Manteca x6");
				p6.setCategoria(catPanaderia); p6.setTipoVenta(TipoVenta.UNIDAD); p6.setUnidadMedida("media docena"); p6.setStand(s3);
				p6.setImagenUrl("https://th.bing.com/th/id/OIP.XiU7ARUTmGy05F7xI3mN-AHaHa?w=172&h=180&c=7&r=0&o=7&pid=1.7&rm=3");

				Producto p7 = new Producto(4500, "Pan de masa madre orgánico de fermentación lenta 800g", "Pan de Masa Madre 800g");
				p7.setCategoria(catPanaderia); p7.setTipoVenta(TipoVenta.UNIDAD); p7.setUnidadMedida("un"); p7.setStand(s3);
				p7.setImagenUrl("https://tse1.mm.bing.net/th/id/OIP.26uQa0FCdVJLO1Bfna8gsAHaHa?r=0&rs=1&pid=ImgDetMain&o=7&rm=3");

				s3.setProductos(List.of(p6, p7));
				standRepository.save(s3);

				// Feriante 4: Joyas y Plata Lucía
				Usuario u4 = new Usuario("Lucía", "Benítez", "lucia.joyas@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				u4.setTipoUsuario(TipoUsuario.FERIANTE); u4.setEnabled(true);
				usuarioRepository.save(u4);
				Stand s4 = new Stand("Lucía Joyas & Plata", "Accesorios y bijouterie artesanal en plata 925", null);
				Feriante f4 = new Feriante("2964-888111", "luciajoyas@gmail.com", EstadoUsuario.ACTIVO);
				f4.setUsuario(u4); f4.setStand(s4); s4.setFeriante(f4);

				Producto p8 = new Producto(28000, "Anillo de Plata 925 engarzado con piedra de fuego", "Anillo Plata 925 Piedra Fuego");
				p8.setCategoria(catAccesorios); p8.setTipoVenta(TipoVenta.UNIDAD); p8.setUnidadMedida("un"); p8.setStand(s4);
				p8.setImagenUrl("https://i.pinimg.com/736x/5b/7d/c6/5b7dc67388c60327fce817e30a3fac7e.jpg");

				s4.setProductos(List.of(p8));
				standRepository.save(s4);

				// Feriante 5: Cervecería Beagle
				Usuario u5 = new Usuario("Mateo", "Rossi", "mateo.cerveza@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				u5.setTipoUsuario(TipoUsuario.FERIANTE); u5.setEnabled(true);
				usuarioRepository.save(u5);
				Stand s5 = new Stand("Cerveza Artesanal Beagle", "Cerveza artesanal de alta calidad producida en la isla", null);
				Feriante f5 = new Feriante("2964-999222", "beaglecerveza@gmail.com", EstadoUsuario.ACTIVO);
				f5.setUsuario(u5); f5.setStand(s5); s5.setFeriante(f5);

				Producto p9 = new Producto(4000, "Cerveza artesanal IPA intensamente lupulada 500ml", "Cerveza IPA 500ml");
				p9.setCategoria(catBebidas); p9.setTipoVenta(TipoVenta.UNIDAD); p9.setUnidadMedida("un"); p9.setStand(s5);
				p9.setImagenUrl("https://th.bing.com/th/id/OIP.gCVw2aHKLJQG-a1KbPwGiAHaFO?w=246&h=180&c=7&r=0&o=7&pid=1.7&rm=3");

				Producto p11 = new Producto(4200, "Cerveza Stout negra estilo robusto con notas de café y cacao 500ml", "Cerveza Stout Robusta 500ml");
				p11.setCategoria(catBebidas); p11.setTipoVenta(TipoVenta.UNIDAD); p11.setUnidadMedida("un"); p11.setStand(s5);
				p11.setImagenUrl("https://jumboargentina.vtexassets.com/arquivos/ids/584204/Cerveza-Stout-Tsingtao-Lata-500-Ml-1-446785.jpg");

				s5.setProductos(List.of(p9, p11));
				standRepository.save(s5);

				// Feriante 6: Aromas del Sur
				Usuario u6 = new Usuario("Camila", "Vega", "camila.velas@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				u6.setTipoUsuario(TipoUsuario.FERIANTE); u6.setEnabled(true);
				usuarioRepository.save(u6);
				Stand s6 = new Stand("Aromas & Velas del Sur", "Velas aromáticas de cera de soja y difusores artesanales", null);
				Feriante f6 = new Feriante("2964-123456", "aromasdelsur@gmail.com", EstadoUsuario.ACTIVO);
				f6.setUsuario(u6); f6.setStand(s6); s6.setFeriante(f6);

				Producto p12 = new Producto(9500, "Vela artesanal de cera de soja aroma Lavanda & Vainilla", "Vela de Soja Lavanda & Vainilla");
				p12.setCategoria(catHogar); p12.setTipoVenta(TipoVenta.UNIDAD); p12.setUnidadMedida("un"); p12.setStand(s6);
				p12.setImagenUrl("https://th.bing.com/th/id/OIP.bO4VF8HIQ6C2SQvkW6JOqAHaHa?w=176&h=180&c=7&r=0&o=7&pid=1.7&rm=3");

				s6.setProductos(List.of(p12));
				standRepository.save(s6);

				// Feriante 7: Marroquinería Silva
				Usuario u7 = new Usuario("Joaquín", "Silva", "joaquin.cuero@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				u7.setTipoUsuario(TipoUsuario.FERIANTE); u7.setEnabled(true);
				usuarioRepository.save(u7);
				Stand s7 = new Stand("Marroquinería Silva", "Billeteras, mochilas y accesorios en 100% cuero vacuno", null);
				Feriante f7 = new Feriante("2964-654321", "marroquineriasilva@gmail.com", EstadoUsuario.ACTIVO);
				f7.setUsuario(u7); f7.setStand(s7); s7.setFeriante(f7);

				Producto p13 = new Producto(32000, "Billetera de cuero vacuno legítimo cosida artesanalmente a mano", "Billetera de Cuero Vacuno");
				p13.setCategoria(catAccesorios); p13.setTipoVenta(TipoVenta.UNIDAD); p13.setUnidadMedida("un"); p13.setStand(s7);
				p13.setImagenUrl("https://th.bing.com/th/id/OIP.VVcff3ICrMi2aIb3Cj70WwHaEU?w=291&h=180&c=7&r=0&o=7&pid=1.7&rm=3");

				s7.setProductos(List.of(p13));
				standRepository.save(s7);

				// Feriante 8: Vivero El Bosque
				Usuario u8 = new Usuario("Elena", "Morales", "elena.vivero@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				u8.setTipoUsuario(TipoUsuario.FERIANTE); u8.setEnabled(true);
				usuarioRepository.save(u8);
				Stand s8 = new Stand("Vivero El Bosque", "Plantas de interior, macetas pintadas a mano y suculentas", null);
				Feriante f8 = new Feriante("2964-789012", "viveroelbosque@gmail.com", EstadoUsuario.ACTIVO);
				f8.setUsuario(u8); f8.setStand(s8); s8.setFeriante(f8);

				Producto p14 = new Producto(7000, "Maceta artesanal de cerámica decorada con variedad de Suculentas", "Maceta Cerámica con Suculenta");
				p14.setCategoria(catVivero); p14.setTipoVenta(TipoVenta.UNIDAD); p14.setUnidadMedida("un"); p14.setStand(s8);
				p14.setImagenUrl("https://th.bing.com/th/id/OIP.Ir70NNae6e4pkJY61qYjIgHaHa?w=183&h=183&c=7&r=0&o=7&pid=1.7&rm=3");

				s8.setProductos(List.of(p14));
				standRepository.save(s8);

				// Feriante 9: Sublimaciones & Remeras
				Usuario u9 = new Usuario("Santiago", "Castro", "santiago.estampados@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				u9.setTipoUsuario(TipoUsuario.FERIANTE); u9.setEnabled(true);
				usuarioRepository.save(u9);
				Stand s9 = new Stand("Sublimaciones & Remeras Castro", "Estampados personalizados, tazas y remeras de algodón", null);
				Feriante f9 = new Feriante("2964-345678", "castrostampados@gmail.com", EstadoUsuario.ACTIVO);
				f9.setUsuario(u9); f9.setStand(s9); s9.setFeriante(f9);

				Producto p15 = new Producto(16000, "Remera 100% algodón peinado con estampado personalizado de alta calidad", "Remera Algodón Estampada");
				p15.setCategoria(catIndumentaria); p15.setTipoVenta(TipoVenta.UNIDAD); p15.setUnidadMedida("un"); p15.setStand(s9);
				p15.setImagenUrl("https://th.bing.com/th/id/R.df870e743281f1d502d64143b2f90571?rik=0hucXh16OnAh4g&pid=ImgRaw&r=0");

				s9.setProductos(List.of(p15));
				standRepository.save(s9);

				// Feriante 10: Chocolates Patagónicos
				Usuario u10 = new Usuario("Laura", "Navarro", "laura.chocolates@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				u10.setTipoUsuario(TipoUsuario.FERIANTE); u10.setEnabled(true);
				usuarioRepository.save(u10);
				Stand s10 = new Stand("Chocolates Patagónicos", "Chocolates artesanales en rama, bombones y alfajores regionales", null);
				Feriante f10 = new Feriante("2964-901234", "chocolatespatagonicos@gmail.com", EstadoUsuario.ACTIVO);
				f10.setUsuario(u10); f10.setStand(s10); s10.setFeriante(f10);

				Producto p16 = new Producto(12000, "Caja fina de bombones surtidos artesanalmente 250g", "Caja de Bombones Surtidos 250g");
				p16.setCategoria(catGastronomia); p16.setTipoVenta(TipoVenta.UNIDAD); p16.setUnidadMedida("un"); p16.setStand(s10);
				p16.setImagenUrl("https://cdn.palbincdn.com/users/39387/images/28a-1593685879.jpg");

				Producto p17 = new Producto(2500, "Alfajor de chocolate con abundante dulce de leche y corazón de calafate", "Alfajor de Chocolate y Calafate");
				p17.setCategoria(catGastronomia); p17.setTipoVenta(TipoVenta.UNIDAD); p17.setUnidadMedida("un"); p17.setStand(s10);
				p17.setImagenUrl("https://pastelbymariu.es/wp-content/uploads/2023/10/alfajor-2.jpg");

				s10.setProductos(List.of(p16, p17));
				standRepository.save(s10);


				// =========================================
				// 5. USUARIOS COMUNES CON SOLICITUDES PENDIENTES PARA SER FERIANTE
				// =========================================
				Usuario carlosGomez = new Usuario("Carlos", "Gómez", "carlos.gomez@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				carlosGomez.setEnabled(true);
				Usuario valentinaRios = new Usuario("Valentina", "Ríos", "valentina.rios@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				valentinaRios.setEnabled(true);
				Usuario estebanPeralta = new Usuario("Esteban", "Peralta", "esteban.peralta@gmail.com", passwordEncoder.encode("123"), EstadoUsuario.ACTIVO);
				estebanPeralta.setEnabled(true);

				usuarioRepository.saveAll(List.of(carlosGomez, valentinaRios, estebanPeralta));

				SolicitudParaFeriante sol1 = new SolicitudParaFeriante(carlosGomez, "Artesanías en Madera Gómez", "Venta de cuencos, tablas y accesorios artesanales tallados en lenga fueguina.", "2964-112233", "contacto@gomezmadera.com");
				SolicitudParaFeriante sol2 = new SolicitudParaFeriante(valentinaRios, "Sabores Fueguinos", "Mermeladas caseras con frutos regionales (calafate, ruibarbo) y conservas agridulces.", "2964-445566", "saboresfueguinos@gmail.com");
				SolicitudParaFeriante sol3 = new SolicitudParaFeriante(estebanPeralta, "Cerámicas del Fin del Mundo", "Tazas, platos y piezas de cerámica artesanal esmaltada hechas a mano.", "2964-778899", "contacto@ceramicasfdm.com");

				solicitudParaFerianteRepository.saveAll(List.of(sol1, sol2, sol3));


				// =========================================
				// 6. PARTICIPACIONES A EDICIONES DE FERIAS
				// =========================================

				// Edición Don Bosco Invierno
				Participacion part1 = new Participacion(edicionDonBoscoInvierno, s1, null, EstadoParticipacion.CONFIRMADO);
				part1.setEstadoPago(EstadoPago.PAGADO); part1.setMontoAbonado(15000.0);

				Participacion part2 = new Participacion(edicionDonBoscoInvierno, s2, null, EstadoParticipacion.CONFIRMADO);
				part2.setEstadoPago(EstadoPago.PAGADO); part2.setMontoAbonado(15000.0);

				Participacion part3 = new Participacion(edicionDonBoscoInvierno, s3, null, EstadoParticipacion.CONFIRMADO);
				part3.setEstadoPago(EstadoPago.SENADO); part3.setMontoAbonado(7500.0);

				Participacion part4 = new Participacion(edicionDonBoscoInvierno, s4, null, EstadoParticipacion.PENDIENTE);
				part4.setEstadoPago(EstadoPago.DEBE); part4.setMontoAbonado(0.0);

				Participacion part5 = new Participacion(edicionDonBoscoInvierno, s5, null, EstadoParticipacion.PENDIENTE);
				part5.setEstadoPago(EstadoPago.DEBE); part5.setMontoAbonado(0.0);

				Participacion part6 = new Participacion(edicionDonBoscoInvierno, s6, null, EstadoParticipacion.CONFIRMADO);
				part6.setEstadoPago(EstadoPago.PAGADO); part6.setMontoAbonado(15000.0);

				Participacion part7 = new Participacion(edicionDonBoscoInvierno, s7, null, EstadoParticipacion.RECHAZADO);
				part7.setNumeroStandPreferido(null); // Rechazo sin stand preferido registrado

				Participacion part8 = new Participacion(edicionDonBoscoInvierno, s8, null, EstadoParticipacion.EN_ESPERA);

				// Edición Canto del Agua
				Participacion part9 = new Participacion(edicionCantoAguaJulio, s3, null, EstadoParticipacion.PENDIENTE);
				Participacion part10 = new Participacion(edicionCantoAguaJulio, s5, null, EstadoParticipacion.CONFIRMADO);
				part10.setEstadoPago(EstadoPago.PAGADO); part10.setMontoAbonado(18000.0);

				Participacion part11 = new Participacion(edicionCantoAguaJulio, s10, null, EstadoParticipacion.PENDIENTE);

				participacionRepository.saveAll(List.of(
						part1, part2, part3, part4, part5, part6, part7, part8, part9, part10, part11
				));

				System.out.println("==========================================================================");
				System.out.println(" DATOS DE PRUEBA CREADOS CON IMÁGENES REALES EN PRODUCTOS");
				System.out.println("==========================================================================");
			}
		};
	}
}