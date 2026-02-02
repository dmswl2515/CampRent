package com.rental.camprent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CampRentApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampRentApplication.class, args);
	}

}
