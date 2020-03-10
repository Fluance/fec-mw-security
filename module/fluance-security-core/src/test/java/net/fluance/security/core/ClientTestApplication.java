package net.fluance.security.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EnableJpaRepositories("net.fluance.security.core")
@EntityScan("net.fluance.security.core")
@SpringBootApplication(scanBasePackages={"net.fluance"})
public class ClientTestApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(ClientTestApplication.class, args);
	}
	
}
