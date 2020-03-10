/**
 * 
 */
package net.fluance.security.auth.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;

import net.fluance.app.security.service.IUserService;
import net.fluance.security.auth.config.helper.CustomAuthenticationKeyGenerator;
import net.fluance.security.auth.config.helper.CustomJdbcTokenStore;
import net.fluance.security.auth.config.helper.CustomTokenService;
import net.fluance.security.auth.config.helper.FluanceAuthorizationCodeTokenGranter;
import net.fluance.security.auth.config.helper.FluanceSessionAthenticationStrategy;
import net.fluance.security.auth.config.helper.jwt.JWTAssertionService;
import net.fluance.security.auth.config.helper.jwt.JWTTokenGranter;
import net.fluance.security.auth.service.FluanceUserDetailsService;
import net.fluance.security.core.repository.jdbc.UserInfoRepository;
import net.fluance.security.core.repository.jdbc.UserSessionDataRepository;
import net.fluance.security.core.service.UserIdentityService;


@Configuration
@EnableAuthorizationServer
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationServerConfig.class);
	
	private static final String SET_TOKEN_STORE = "[init][TokenStoreBeanInit]";
	private static final String SET_TOKEN_GRANTER = "[init][CustomTokenGranter]";
	private static final String SET_TOKEN_SERVICE = "[init][TokenServiceBeanInit]";
	private static final String CONFIG = "[init][config]";

	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
    @Value("${config.jwt-assertion.subject-id.allowed}")
    private String allowedSubjectIdClaimsConf;
    @Value("${config.jwt-assertion.user-info.allowed}")
    private String allowedUserInfoClaimsConf;
    @Value("${config.accesstoken.validity.seconds}")
    private int accessTokenValiditySeconds;
    @Value("${oauth2.server.clientId}")
    private String oAuth2ServerClientId;
    @Value("${oauth2.server.clientSecret}")
    private String oAuth2ServerClientSecret;
    @Value("${config.refreshtoken.validity.seconds}")
    private int refreshTokenValiditySeconds;
    
    @Autowired
    private JWTAssertionService jwtAssertionService;
    
    @Autowired
	private IUserService keycloakUserService;
    
    @Autowired
    private UserInfoRepository userInfoRepository;
    
    @Autowired
    private UserSessionDataRepository userSessionDataRepository;
    
    @Autowired
    private UserIdentityService userIdentityService;
    
    @Autowired
    private FluanceUserDetailsService fluanceUserDetailsService;
    
    @Autowired
	FluanceSessionAthenticationStrategy fluanceSessionAthenticationStrategy;
    
	/**
	 * Defines the security constraints on the token endpoints /oauth/token_key
	 * and /oauth/check_token Client credentials are required to access the
	 * endpoints
	 *
	 * @param oauthServer
	 * @throws Exception
	 */
	@Override
	public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
		oauthServer.tokenKeyAccess("isAnonymous() || hasRole('ROLE_TRUSTED_CLIENT')") // permitAll()
				.checkTokenAccess("hasRole('TRUSTED_CLIENT')"); // isAuthenticated()
	}

    /**
     * Token granters configuration. To add a custom grant type, the corresponding granter must be added to the list here
     * @param endpoints
     * @return
     * @throws Exception 
     */
	private TokenGranter tokenGranter(final AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		LOGGER.info("{}Adding custom granters", SET_TOKEN_GRANTER);
		List<TokenGranter> granters = new ArrayList<TokenGranter>();
		
		JWTTokenGranter jwtTokenGranter = new JWTTokenGranter(endpoints.getTokenServices(), endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory(), "jwt");
		setJwtAllowedSubject(jwtTokenGranter);
		setJwtAllowedUserInfo(jwtTokenGranter);	
		
		FluanceAuthorizationCodeTokenGranter authorizationCodeTokenGranter = new FluanceAuthorizationCodeTokenGranter(endpoints.getTokenServices(), authorizationCodeServices(), endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory());		
		authorizationCodeTokenGranter.setUserSessionDataRepository(userSessionDataRepository);
		
		granters.add(jwtTokenGranter);
		LOGGER.info("{}Token granter add to granters {}", SET_TOKEN_GRANTER, jwtTokenGranter.getClass().getCanonicalName());		
		
		granters.add(authorizationCodeTokenGranter);
		LOGGER.info("{}Token granter add to granters {}", SET_TOKEN_GRANTER, authorizationCodeTokenGranter.getClass().getCanonicalName());
		
		granters.addAll(Arrays.asList(endpoints.getTokenGranter()));
		
		LOGGER.info("{}Returning CompositeTokenGranter", SET_TOKEN_GRANTER);
		return new CompositeTokenGranter(granters);
	}
	
	@Bean
	public AuthenticationKeyGenerator authenticationKeyGenerator() {
		return new CustomAuthenticationKeyGenerator();
	}
	
	@Bean
	public TokenStore tokenStore() {
		LOGGER.info("{}Init...", SET_TOKEN_STORE);
		CustomJdbcTokenStore customJdbcTokenStore = new CustomJdbcTokenStore(dataSource);
		customJdbcTokenStore.setAuthenticationKeyGenerator(authenticationKeyGenerator());
		
		LOGGER.info("{}Token store is instance of {}", SET_TOKEN_STORE, customJdbcTokenStore.getClass().getCanonicalName());
		LOGGER.info("{}Return", SET_TOKEN_STORE);
		return customJdbcTokenStore;
	}

	@Bean
	public CustomTokenService tokenServices() {
		LOGGER.info("{}Init...", SET_TOKEN_SERVICE);
		CustomTokenService customTokenService = new CustomTokenService();
		customTokenService.setTokenStore(tokenStore());
		customTokenService.setSupportRefreshToken(true);
		
		LOGGER.info("{}Token service is instance of {}", SET_TOKEN_SERVICE, customTokenService.getClass().getCanonicalName());
		
		LOGGER.info("{}Return", SET_TOKEN_SERVICE);
	    return customTokenService;
	}
	
	/**
	 * Defines the authorization and token endpoints and the token services
	 *
	 * @param endpoints
	 * @throws Exception
	 */
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		CustomTokenService customTokenService = tokenServices();
		customTokenService.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
		customTokenService.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);		
		
		endpoints.tokenServices(customTokenService);
		fluanceSessionAthenticationStrategy.setTokenServices(customTokenService);
		LOGGER.info("{}Added Token Services", CONFIG);
		
		endpoints.accessTokenConverter(new DefaultAccessTokenConverter());
		LOGGER.info("{}Added Token Converter", CONFIG);
		
		LOGGER.info("{}Setting Token Granter", SET_TOKEN_GRANTER);
		endpoints.authenticationManager(authenticationManager).tokenGranter(tokenGranter(endpoints)).userDetailsService(fluanceUserDetailsService);
		LOGGER.info("{}Added Token Granter", CONFIG);
		
		// Configuring the default validity period for acces and refresh tokens
        //endpoints.authenticationManager(authenticationManager);
		
        endpoints.tokenStore(tokenStore());
        LOGGER.info("{}Added token store", CONFIG);
                
        endpoints.authorizationCodeServices(authorizationCodeServices());
        LOGGER.info("{}Added Authorization Code Services", CONFIG);
	}


	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.jdbc(dataSource);
	}	
	
    /**
     * 
     * @param jwtTokenGranter
     */
	private void setJwtAllowedSubject(JWTTokenGranter jwtTokenGranter) {
		List<String> allowedSubjetIdClaims = new ArrayList<>();
		if (allowedSubjectIdClaimsConf != null && !allowedSubjectIdClaimsConf.isEmpty()) {
			StringTokenizer strTok = new StringTokenizer(allowedSubjectIdClaimsConf, ",");
			while (strTok.hasMoreTokens()) {
				allowedSubjetIdClaims.add(strTok.nextToken());
			}
		}
		jwtTokenGranter.setAllowedSubjetIdClaims(allowedSubjetIdClaims);
		jwtTokenGranter.setAssertionService(jwtAssertionService);
		jwtTokenGranter.setUserService(keycloakUserService);
		jwtTokenGranter.setUserInfoRepository(userInfoRepository);
	}
	  /**
     * 
     * @param jwtTokenGranter
     */
	private void setJwtAllowedUserInfo(JWTTokenGranter jwtTokenGranter) {
		List<String> allowedUserInfoClaims = new ArrayList<>();
		if (allowedUserInfoClaimsConf != null && !allowedUserInfoClaimsConf.isEmpty()) {
			StringTokenizer strTok = new StringTokenizer(allowedUserInfoClaimsConf, ",");
			while (strTok.hasMoreTokens()) {
				allowedUserInfoClaims.add(strTok.nextToken());
			}
		}
		jwtTokenGranter.setAllowedUserInfoClaims(allowedUserInfoClaims);
		jwtTokenGranter.setAssertionService(jwtAssertionService);
		jwtTokenGranter.setUserService(keycloakUserService);
		jwtTokenGranter.setUserIdentityService(userIdentityService);
	}
	
	@Bean
    protected AuthorizationCodeServices authorizationCodeServices() {
        return new JdbcAuthorizationCodeServices(dataSource);
    }
	
}
