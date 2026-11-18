package com.mdd.booktor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BooktorApplication {

	public static void main(String[] args) {
		SpringApplication.run(BooktorApplication.class, args);
	}

}
