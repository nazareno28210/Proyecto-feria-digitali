package com.mansilla_nazareno.feriadigital.feriadigital;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.User;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UserEstate;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UserType;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.FerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class FeriaDigitalApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeriaDigitalApplication.class, args);
	}
		@Bean
		public CommandLineRunner initData (UserRepository userRepository, FerianteRepository ferianteRepository) {
			return (args) -> {
				if (userRepository.findAll().isEmpty()) {
					User usuario1 = new User("Nazareno",
							"Guardia",
							"nazarenoguardia2004@gmail.com",
							"123",
							UserType.ADMINISTRADOR,
							UserEstate.ACTIVO
					);
					userRepository.save(usuario1);
				}

				if (ferianteRepository.findAll().isEmpty()) {
					User francisco = new User(
							"Francisco",
							"García",
							"francisco@mail.com",
							"12345",
							UserType.FERIANTE,
							UserEstate.ACTIVO
					);
					userRepository.save(francisco);

					Feriante ferianteFransico = new Feriante(
							"Emprendimiento Francisco",
							"Venta de artesanías de madera",
							"2964-555999",
							"contactofrancisco@gmail.com",
							LocalDate.now(),
							UserEstate.ACTIVO,
							francisco
					);
					ferianteRepository.save(ferianteFransico);
				}
			};
		}
}