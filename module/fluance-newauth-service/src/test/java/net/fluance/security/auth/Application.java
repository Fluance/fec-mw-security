package net.fluance.security.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import net.fluance.security.auth.config.SecurityConfig;

//@EnableTransactionManagement
//@EntityScan(basePackages = {"net.fluance.security.core", "net.fluance.security.auth"})
@SpringBootApplication(scanBasePackages={"net.fluance.security"})
public class Application extends net.fluance.security.auth.app.Application {

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
