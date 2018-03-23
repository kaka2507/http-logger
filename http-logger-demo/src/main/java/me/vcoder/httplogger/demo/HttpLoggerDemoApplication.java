package me.vcoder.httplogger.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@ComponentScan("me.vcoder")
public class HttpLoggerDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(HttpLoggerDemoApplication.class, args);
	}

	@GetMapping(path = "/ping")
	public ResponseEntity ping() {
		return ResponseEntity.ok("pong");
	}
}
