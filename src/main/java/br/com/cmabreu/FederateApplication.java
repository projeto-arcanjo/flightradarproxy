package br.com.cmabreu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("br.com.cmabreu")
public class FederateApplication {

	public static void main(String[] args) {
		SpringApplication.run( FederateApplication.class, args );
	}

}
