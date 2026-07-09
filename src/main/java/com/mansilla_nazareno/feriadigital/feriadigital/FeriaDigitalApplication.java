package com.mansilla_nazareno.feriadigital.feriadigital;

import com.mansilla_nazareno.feriadigital.feriadigital.models.*;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.*;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.CategoriaProducto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.TipoVenta;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Participante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.ParticipantePersona;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.*;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.CategoriaProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.ProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.UsuarioRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.ParticipanteRepository;
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