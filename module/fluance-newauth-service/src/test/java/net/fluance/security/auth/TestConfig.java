/**
 * 
 */
package net.fluance.security.auth;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import net.fluance.app.security.service.xacml.BalanaXacmlPDP;
import net.fluance.app.security.service.xacml.XacmlPDP;
import net.fluance.app.security.util.JwtHelper;
import net.fluance.app.test.mock.AuthorizationServerMock;


@Configuration
@ComponentScan(basePackages = {"net.fluance.app.security", "net.fluance.app.test", "net.fluance.security.user"})
@EnableAutoConfiguration
@PropertySource({"classpath:webapps/conf/auth/ldap.properties","classpath:webapps/conf/security.properties"})
public class TestConfig {

	@Bean
	public JwtHelper jwtHelper() {
		return new JwtHelper();
	}
	
	@Bean
	public AuthorizationServerMock authorizationServerMock() {
		return new AuthorizationServerMock();
	}
	
	@Bean
	public XacmlPDP xacmlPdp() {
		return new BalanaXacmlPDP();
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfig() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
}
