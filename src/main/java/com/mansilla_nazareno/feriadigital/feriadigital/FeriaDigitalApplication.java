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
		public CommandLineRunner initData (UsuarioRepository usuarioRepository,
										   AdministradorDeFeriaRepository administradorDeFeriaRepository,
										   FerianteRepository ferianteRepository,
										   CategoriaProductoRepository categoriaProductoRepository,
										   ProductoRepository productoRepository,
										   StandRepository standRepository,
										   FeriaRepository feriaRepository) {
			return (args) -> {
				if (usuarioRepository.findAll().isEmpty()) {
					Usuario nazareno = new Usuario("Nazareno",
							"Guardia",
							"nazarenoguardia2004@gmail.com",
							passwordEncoder.encode("123"),
							EstadoUsuario.ACTIVO
					);
					usuarioRepository.save(nazareno);

					AdministradorDeFeria admin1 = new AdministradorDeFeria("464366433");
					admin1.setUsuario(nazareno);

					administradorDeFeriaRepository.save(admin1);


					Usuario francisco = new Usuario(
							"Francisco",
							"García",
							"francisco@mail.com",
							passwordEncoder.encode("123"),
							EstadoUsuario.ACTIVO
					);
					usuarioRepository.save(francisco);

					Feriante ferianteFransico = new Feriante(
							"Emprendimiento Francisco",
							"Venta de artesanías de madera",
							"2964-555999",
							"contactofrancisco@gmail.com",
							EstadoUsuario.ACTIVO

					);
					ferianteFransico.setUsuario(francisco);
					ferianteRepository.save(ferianteFransico);
					CategoriaProducto c1 = new CategoriaProducto("pantalon", "prenda de vestir");
					CategoriaProducto c2 = new CategoriaProducto("remera", "prenda de vestir");
					CategoriaProducto c3 = new CategoriaProducto("zapatilla", "calzado");
					CategoriaProducto c4 = new CategoriaProducto("pelota", "juguete para perro");

					Producto p1 =new Producto(10000,"pantalon para nieve de neopren","pantalon termico");
					Producto p2 =new Producto(20000,"camisa con estampado de flores","camisa de verano");
					Producto p3 =new Producto(20000,"color rojo con negro,de vestir","zapatillas rebook");
					Producto p4 =new Producto(20000,"pelota color rojo para perro","kongo");

					p1.setCategorias(List.of(c1));
					p2.setCategorias(List.of(c2));
					p3.setCategorias(List.of(c3));
					p4.setCategorias(List.of(c4));
					c1.setProducto(p1);
					c2.setProducto(p2);
					c3.setProducto(p3);
					c4.setProducto(p4);



					categoriaProductoRepository.save(c1);
					categoriaProductoRepository.save(c2);
					categoriaProductoRepository.save(c3);
					categoriaProductoRepository.save(c4);


					productoRepository.save(p1);
					productoRepository.save(p2);
					productoRepository.save(p3);
					productoRepository.save(p4);
					Stand stand1 = new Stand("Indumentaria Falco","vendemos ,ropa zapatillas ,sombreros");
					stand1.setProductos(List.of(p1,p2,p3));
					p1.setStand(stand1);
					p2.setStand(stand1);
					p3.setStand(stand1);
					standRepository.save(stand1);

					Feria feria = new Feria(
							"Feria de Tecnología",
							LocalDate.of(2025, 11, 1),    // fechaInicio
							LocalDate.of(2025, 11, 5),    // fechaFinal
							"Centro Cultural",            // lugar
							"Feria dedicada a la tecnología y gadgets", // descripcion
							"Activa",
							"Centro Cultural"
					);
					feria.setStands(List.of(stand1));

					feriaRepository.save(feria);


				}
			};
		}
}