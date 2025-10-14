package com.mansilla_nazareno.feriadigital.feriadigital;

import com.mansilla_nazareno.feriadigital.feriadigital.models.User;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UserEstate;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UserType;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FeriaDigitalApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeriaDigitalApplication.class, args);
	}
		@Bean
		public CommandLineRunner initData (UserRepository userRepository){
			return (args) -> {
				if (userRepository.findAll().isEmpty()) {
					User usuario1 = new User("Nazareno",
							"Guardia",
							"nazarenoguardia2004@gmail.com",
							"123",
							UserType.ADMINISTRADOR,
							UserEstate.ACTIVO
					);
				}
			};
		}

}