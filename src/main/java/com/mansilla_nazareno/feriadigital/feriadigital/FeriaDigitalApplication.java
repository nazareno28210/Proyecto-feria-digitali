package com.mansilla_nazareno.feriadigital.feriadigital;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.fair.TipoDeFeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.fair.EdicionFeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.fair.FeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.fair.AsignacionStandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.fair.StandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.fair.UbicacionRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.fair.ParticipacionRepository;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.*;
import com.mansilla_nazareno.feriadigital.feriadigital.models.product.CategoriaProducto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.product.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.product.TipoVenta;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.Participante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.ParticipantePersona;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.*;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.product.CategoriaProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.product.ProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.auth.UsuarioRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.participant.ParticipanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
			ParticipanteRepository participanteRepository,
			FeriaRepository feriaRepository,
			StandRepository standRepository,
			CategoriaProductoRepository categoriaRepository,
			ProductoRepository productoRepository,
			ParticipacionRepository participacionRepository,
			TipoDeFeriaRepository tipoDeFeriaRepository,
			UbicacionRepository ubicacionRepository,
			EdicionFeriaRepository edicionFeriaRepository,
			AsignacionStandRepository asignacionStandRepository,
			PasswordEncoder passwordEncoder
	) {
		return (args) -> {

				System.out.println("--- DATOS DE PRUEBA CARGADOS EXITOSAMENTE CON LA NUEVA ARQUITECTURA ---");

		};
	}
}