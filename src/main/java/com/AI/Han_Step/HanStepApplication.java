package com.AI.Han_Step;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HanStepApplication {

	public static void main(String[] args) {
		SpringApplication.run(HanStepApplication.class, args);
	}

}
