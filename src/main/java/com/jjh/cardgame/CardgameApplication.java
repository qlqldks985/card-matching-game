package com.jjh.cardgame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CardgameApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardgameApplication.class, args);
	}

}
