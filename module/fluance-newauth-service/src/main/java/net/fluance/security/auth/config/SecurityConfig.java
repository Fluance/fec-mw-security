package net.fluance.security.auth.config;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.CompositeFilter;

import net.fluance.security.auth.config.helper.FluanceSessionAthenticationStrategy;
import net.fluance.security.auth.config.helper.OIDCPrincipalExtractor;
import net.fluance.security.auth.service.FluanceUserDetailsService;
import net.fluance.security.auth.web.controller.access.CustomLogoutSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableOAuth2Client
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	OAuth2ClientContext oauth2ClientContext;
	
	@Autowired
	OAuth2RestOperations restTemplate;

	@Value("${application.user.shared-password}")
	private String applicationUserSharedPassword;

	@Value("${identity.domains.default}")
	private String defaultDomain;

	@Value("${clients.fe.url}")
	private String uiUrl;

	@Autowired
	private CustomLogoutSuccessHandler customLogoutSuccessHandler;

	@Autowired
	private FluanceUserDetailsService fluanceUserDetailsService;

	@Autowired
	FluanceSessionAthenticationStrategy fluanceSessionAthenticationStrategy;

	@Override
	@Autowired
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser("fluance").password(applicationUserSharedPassword).roles("USER");

		// Add to service shared with Authorization Server (AuthorizationServerConfig.java) to ensure comunication to ../oauth/..
		fluanceUserDetailsService.addService(auth.getDefaultUserDetailsService());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/oauth2/**", "/logout/**", "/jwt/**", "/v2/api-docs", "/swagger-resources/**",
						"/swagger-ui.html", "/idps/**", "/sessions/**")
				.permitAll().and().logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessHandler(customLogoutSuccessHandler).permitAll();

		http.authorizeRequests().anyRequest().authenticated();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER);
		http.httpBasic();
		http.exceptionHandling().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login/keycloak"));
		http.csrf().disable();
		http.addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
	}

	@Bean
	public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(filter);
		registration.setOrder(-100);
		return registration;
	}

	@Bean
	@ConfigurationProperties("keycloak")
	public ClientResources keycloak() {
		return new ClientResources();
	}
	
	@Bean
	public OAuth2RestOperations restTemplate() {		
		return new OAuth2RestTemplate(keycloak().getClient(), oauth2ClientContext);
	}

	private Filter ssoFilter() {
		CompositeFilter filter = new CompositeFilter();
		List<Filter> filters = new ArrayList<>();
		filters.add(ssoFilter(keycloak(), "/login/keycloak"));
		filter.setFilters(filters);
		return filter;
	}

	private Filter ssoFilter(ClientResources client, String path) {
		OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(path);
		filter.setRestTemplate(restTemplate);

		UserInfoTokenServices tokenServices = new UserInfoTokenServices(client.getResource().getUserInfoUri(), client.getClient().getClientId());
		tokenServices.setPrincipalExtractor(new OIDCPrincipalExtractor(this.defaultDomain));
		tokenServices.setRestTemplate(restTemplate);
		filter.setTokenServices(tokenServices);

		filter.setSessionAuthenticationStrategy(fluanceSessionAthenticationStrategy);

		return filter;
	}

	class ClientResources {
		@NestedConfigurationProperty
		private AuthorizationCodeResourceDetails client = new AuthorizationCodeResourceDetails();

		@NestedConfigurationProperty
		private ResourceServerProperties resource = new ResourceServerProperties();

		public AuthorizationCodeResourceDetails getClient() {
			return client;
		}

		public ResourceServerProperties getResource() {
			return resource;
		}
	}
}
