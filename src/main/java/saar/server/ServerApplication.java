package saar.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;



@SpringBootApplication
public class ServerApplication {
//	@Bean
//	public SecurityWebFilterChain functionalValidationsSpringSecurityFilterChain(ServerHttpSecurity http) {
//		http.authorizeExchange()
//				.anyExchange()
//				.permitAll();
//		http.csrf().disable();
//		return http.build();
//	}


	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

}
