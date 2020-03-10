package net.fluance.security.auth.config.helper;

import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import com.fasterxml.jackson.databind.JsonNode;

import net.fluance.commons.json.jwt.JWTUtils;
import net.fluance.commons.json.jwt.JWTUtils.JwtPart;
import net.fluance.security.core.model.jdbc.UserSessionData;
import net.fluance.security.core.repository.jdbc.UserSessionDataRepository;


public class FluanceAuthorizationCodeTokenGranter extends AuthorizationCodeTokenGranter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FluanceAuthorizationCodeTokenGranter.class);
	private static final String GET_ACCESS_TOKEN = "[getAccessToken]";
	
	private UserSessionDataRepository userSessionDataRepository;

	public FluanceAuthorizationCodeTokenGranter(AuthorizationServerTokenServices tokenServices, AuthorizationCodeServices authorizationCodeServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
		super(tokenServices, authorizationCodeServices, clientDetailsService, requestFactory);
	}
	
	@Override
	protected OAuth2AccessToken getAccessToken(ClientDetails client, TokenRequest tokenRequest) {
		LOGGER.info("{}Getting oAuth2Authentication...", GET_ACCESS_TOKEN);
		OAuth2Authentication oAuth2Authentication = getOAuth2Authentication(client, tokenRequest);
		LOGGER.info("{}oAuth2Authentication get", GET_ACCESS_TOKEN);
		
		LOGGER.info("{}Getting oAuth2AccessToken...", GET_ACCESS_TOKEN);
		OAuth2AccessToken oAuth2AccessToken = this.getTokenServices().createAccessToken(oAuth2Authentication);
		LOGGER.info("{}oAuth2AccessToken get", GET_ACCESS_TOKEN);
		
		try {
			String jwtToken = ((OAuth2AuthenticationDetails) oAuth2Authentication.getUserAuthentication().getDetails()).getTokenValue();
			JsonNode jwtPayload = JWTUtils.getPart(jwtToken, JwtPart.PAYLOAD);
			String sessionId = jwtPayload.get("session_state").asText();
			String principal = (String) oAuth2Authentication.getUserAuthentication().getPrincipal();
			UserSessionData userSessionData = new UserSessionData(oAuth2AccessToken.getValue(), "Keycloak", principal, sessionId, "", "", new Date(), oAuth2AccessToken.getExpiration());
			
			LOGGER.info("{}Saving User session Data", GET_ACCESS_TOKEN);
			LOGGER.debug("{}{}", GET_ACCESS_TOKEN, userSessionData);
			userSessionDataRepository.insertOrUpdate(userSessionData);
		} catch (IOException e) {
			LOGGER.error("Enable to extract user Session ID from keycloak Token", e);
		}
		return oAuth2AccessToken;
	}

	public void setUserSessionDataRepository(UserSessionDataRepository userSessionDataRepository) {
		this.userSessionDataRepository = userSessionDataRepository;
	}
}
