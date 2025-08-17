package com.bjpractice.game_core;

import com.bjpractice.game_core.config.properties.KafkaTopics;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(KafkaTopics.class)
public class GameCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(GameCoreApplication.class, args);
	}


}
