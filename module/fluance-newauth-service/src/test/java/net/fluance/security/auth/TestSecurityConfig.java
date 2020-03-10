package net.fluance.security.auth;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public class TestSecurityConfig extends net.fluance.security.auth.config.SecurityConfig {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().anyRequest().permitAll();
		http.csrf().disable();
	}
}
