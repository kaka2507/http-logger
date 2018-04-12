package me.vcoder.httplogger.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetricsFilter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@Controller
@ComponentScan("me.vcoder")
public class HttpLoggerDemoApplication {
	private final static Logger LOGGER = LoggerFactory.getLogger(HttpLoggerDemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(HttpLoggerDemoApplication.class, args);
	}

	@GetMapping(path = "/ping")
	public ResponseEntity ping() {
		LOGGER.info("controller is running");
		return ResponseEntity.ok("pong");
	}

	@GetMapping(path = "/")
	public String index() {
		LOGGER.info("controller is running");
		return "index";
	}
}
