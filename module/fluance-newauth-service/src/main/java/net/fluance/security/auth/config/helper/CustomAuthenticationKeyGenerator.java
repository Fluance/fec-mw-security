package net.fluance.security.auth.config.helper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.fluance.app.data.model.identity.ThirdPartyUserReference;
import net.fluance.commons.json.jwt.JWTUtils;
import net.fluance.commons.json.jwt.JWTUtils.JwtPart;
import net.fluance.security.auth.config.helper.jwt.JWTAuthenticationToken;
import net.fluance.security.auth.config.helper.jwt.JWTValues;
import net.fluance.security.auth.service.util.ThirdPartyUserUtil;

/**
 * The class overwrites the DefaultAuthenticationKeyGenerator to take care of the possible added
 * values that must be added map that generates the Authentication id.
 * 
 * This id is use as PK on the oauth tables
 */
public class CustomAuthenticationKeyGenerator extends DefaultAuthenticationKeyGenerator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationKeyGenerator.class);
	
	private static final String EXTRACT_KEY = "[extractKey]";
	
	private static final String CLIENT_ID = "client_id";

	private static final String SCOPE = "scope";

	private static final String USERNAME = "username";
	
	private static final String ACTUAL_USER_ACCOUNT = "actualUserAccount";

	@Override
	public String extractKey(OAuth2Authentication authentication) {
		Map<String, String> values = new LinkedHashMap<String, String>();
		OAuth2Request authorizationRequest = authentication.getOAuth2Request();
				
		if (!authentication.isClientOnly()) {
			LOGGER.info("{}User Authentication is client only", EXTRACT_KEY);
			values.put(USERNAME, authentication.getName());
		}
				
		values.put(CLIENT_ID, authorizationRequest.getClientId());
		LOGGER.info("{}client_id set", EXTRACT_KEY);
		LOGGER.debug("{}{client_id: {}}", EXTRACT_KEY, authorizationRequest.getClientId());
		
		if (authorizationRequest.getScope() != null) {
			String formatedParametersList = OAuth2Utils.formatParameterList(new TreeSet<String>(authorizationRequest.getScope()));			
			values.put(SCOPE, formatedParametersList);
			LOGGER.info("{}Other parameters set", EXTRACT_KEY);
			LOGGER.debug("{}{}", EXTRACT_KEY, formatedParametersList);
		}
		
		if(authentication.getUserAuthentication() instanceof JWTAuthenticationToken) {
			
			LOGGER.info("{}User Authentication instanceof JWTAuthenticationToken", EXTRACT_KEY);
			
			JWTAuthenticationToken jwtAuthenticationToken = (JWTAuthenticationToken) authentication.getUserAuthentication();
			
			if( jwtAuthenticationToken.getDetails() != null){
				String sessionId = jwtAuthenticationToken.getDetails().toString();
				LOGGER.info("{}Authentication id will be generated for the corresponding Session", EXTRACT_KEY);
				LOGGER.debug("{}{session: {}}", EXTRACT_KEY, sessionId);
				values.put("sessionIndex", sessionId);
			}
			
			LOGGER.debug("{}{JWTAssertion: {}}", EXTRACT_KEY, jwtAuthenticationToken.getJwtAssertion());
			
			try {
				ObjectNode jwtPayload = JWTUtils.getPart(jwtAuthenticationToken.getJwtAssertion(), JwtPart.PAYLOAD);
				
				JsonNode thirdPartyUserNode = jwtPayload.get(JWTValues.THIRD_PARTY_FIELD);			
				ThirdPartyUserReference thirdPartyUser = ThirdPartyUserUtil.parseFromJson(thirdPartyUserNode);	
				
				if(thirdPartyUser != null && thirdPartyUser.getActualUserName() != null) {
					values.put(ACTUAL_USER_ACCOUNT, thirdPartyUser.getActualUserName());
					LOGGER.info("{}Authentication id will be generated with actual user account", EXTRACT_KEY);
				}	
			} catch (IOException e) {
				LOGGER.warn("{}JWT assertion bad format", EXTRACT_KEY);
			}
			
		} else if(authentication.getUserAuthentication() instanceof OAuth2Authentication && (OAuth2AuthenticationDetails) authentication.getUserAuthentication().getDetails() != null){			
			LOGGER.info("{}User Authentication instanceof OAuth2Authentication", EXTRACT_KEY);
			
			String jwtToken = ((OAuth2AuthenticationDetails) authentication.getUserAuthentication().getDetails()).getTokenValue();		
			LOGGER.debug("{}{jwtToken: {}}", EXTRACT_KEY, jwtToken);
			
			try {
				JsonNode jwtPayload = JWTUtils.getPart(jwtToken, JwtPart.PAYLOAD);
				
				String sessionId = jwtPayload.get("session_state").asText();
				LOGGER.info("{}Authentication id will be generated for the corresponding KEYCLOAK Session", EXTRACT_KEY);
				LOGGER.debug("{}{session: {}}", EXTRACT_KEY, sessionId);
				values.put("sessionIndex", sessionId);
				
				JsonNode thirdPartyUserNode = jwtPayload.get(JWTValues.THIRD_PARTY_FIELD);			
				ThirdPartyUserReference thirdPartyUser = ThirdPartyUserUtil.parseFromJson(thirdPartyUserNode);	
				
				if(thirdPartyUser != null && thirdPartyUser.getActualUserName() != null) {
					values.put(ACTUAL_USER_ACCOUNT, thirdPartyUser.getActualUserName());
					LOGGER.info("{}Authentication id will be generated with actual user account", EXTRACT_KEY);
				}				
			} catch (IOException e){
				LOGGER.warn("{}JWT token bad format", EXTRACT_KEY);
			}
		}
		
		String key = generateKey(values);
		
		LOGGER.info("{}Authentication_id generated", EXTRACT_KEY);
		LOGGER.debug("{}{Authentication_id: {}}", EXTRACT_KEY, key);
		
		return key;
	}
}
