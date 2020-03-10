package net.fluance.security.ehprofile.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableTransactionManagement
@ComponentScan(basePackages = {"net.fluance.security.core", "net.fluance.security.ehprofile"})
@EnableJpaRepositories(basePackages = {"net.fluance.security.core", "net.fluance.security.ehprofile"})
@EntityScan(basePackages = {"net.fluance.security.core", "net.fluance.security.ehprofile"})
@SpringBootApplication(scanBasePackages={"net.fluance.security.ehprofile"})
public class Application extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
