package org.archivision.pickbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PickbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(PickbotApplication.class, args);
	}

}
