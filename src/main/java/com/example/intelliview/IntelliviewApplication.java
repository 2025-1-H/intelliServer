package com.example.intelliview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class IntelliviewApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntelliviewApplication.class, args);
	}

}
