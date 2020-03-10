package net.fluance.security.ehprofile.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import net.fluance.security.ehprofile.config.SecurityConfig;

@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"net.fluance.security.core", "net.fluance.security.ehprofile"})
@EntityScan(basePackages = {"net.fluance.security.core", "net.fluance.security.ehprofile"})
@SpringBootApplication(scanBasePackages={"net.fluance.security.ehprofile"})
public class Application extends net.fluance.security.ehprofile.app.Application {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Bean
	public SecurityConfig securityConfig(){
		return new TestSecurityConfig();
	}
	
}
