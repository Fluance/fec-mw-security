package net.fluance.security.auth.config.helper;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.fluance.app.data.model.identity.Email;
import net.fluance.app.data.model.identity.Telephon;
import net.fluance.app.data.model.identity.User;
import net.fluance.security.core.model.jdbc.UserInfo;
import net.fluance.security.core.model.jdbc.UserSessionData;
import net.fluance.security.core.repository.jdbc.UserInfoRepository;
import net.fluance.security.core.repository.jdbc.UserSessionDataRepository;

/**
 * Authentication strategy that will be use for the filter after a keycloak login.
 * The filter is defined in {@link SecurityConfig}
 * 
 */
@Component
public final class FluanceSessionAthenticationStrategy implements SessionAuthenticationStrategy {
	private static final Logger LOGGER = LoggerFactory.getLogger(FluanceSessionAthenticationStrategy.class);
	private static final String PREFIX = "[onAuthentication]";

	@Autowired
	UserInfoRepository userInfoRepository;

	private DefaultTokenServices tokenServices;
	
	@Autowired
	private UserSessionDataRepository userSessionDataRepository;

	public void onAuthentication(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
		try {
			saveUserInfo(authentication);
			LOGGER.debug("{}User info saved", PREFIX);
			saveUserSessionData(request, authentication);
			LOGGER.debug("{}User session data saved", PREFIX);
		} catch (Exception e) {
			LOGGER.warn("{}Error on authentication: {}", PREFIX, e.getMessage());
		}
	}
	
	/**
	 * Saves the user info
	 *
	 * @param authentication
	 * @throws JsonProcessingException
	 */
	private void saveUserInfo(Authentication authentication) throws JsonProcessingException {
		String subjectId = (String) authentication.getPrincipal();
		String userInfos = buildUserInfos(authentication);
		UserInfo userInfo = new UserInfo(subjectId, userInfos);
		userInfoRepository.insertOrUpdate(userInfo);
	}

	/**
	 * Match the data that comes from Keycloak to the data for Users.
	 * 
	 * @param authentication
	 * @return String representation of a JSON containing the user data
	 * @throws JsonProcessingException
	 */
	private String buildUserInfos(Authentication authentication) throws JsonProcessingException {
		User user = new User();
		
		if(authentication != null && authentication instanceof OAuth2Authentication) {	
			OAuth2Authentication oAuth2Authentication = (OAuth2Authentication)authentication;
			if(oAuth2Authentication.getUserAuthentication() != null && oAuth2Authentication.getUserAuthentication().getDetails() != null) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) oAuth2Authentication.getUserAuthentication().getDetails();
				
				if(map.containsKey("family_name")){
					user.setLastName(map.get("family_name").toString());
				}
				if(map.containsKey("given_name")){
					user.setFirstName(map.get("given_name").toString());
				}
				if(map.containsKey("email")){
					user.setEmail(map.get("email").toString());
					user.setEmails(Arrays.asList(new Email("", map.get("email").toString())));
				}
				if(map.containsKey("phone")){
					user.setTelephons(Arrays.asList(new Telephon("", map.get("phone").toString())));
				}
				if(map.containsKey("title")){
					user.setTitle(map.get("title").toString());
				}
				if(map.containsKey("company")){
					user.setCompany(map.get("company").toString());
				}
				if(map.containsKey("directReports")){
					user.setIsManager(true);
				}
				if(map.containsKey("manager")){
					user.setManagerUsername(map.get("manager").toString());
				}
			}
		}
		
		return user.toJsonString();
	}

	/**
	 * Save the session data for the user. Is specially important the keycloak session id because it will be use in the future for a proper logout process.
	 * 
	 * @param authentication
	 */
	private void saveUserSessionData(HttpServletRequest request, Authentication authentication) {
		if(authentication != null && authentication instanceof OAuth2Authentication) {				
			OAuth2Authentication oAuth2Authentication = (OAuth2Authentication)authentication;
			OAuth2AccessToken oAuth2AccessToken = tokenServices.createAccessToken(oAuth2Authentication);
			
			OAuth2AuthenticationDetails oAuth2AuthenticationDetails = (OAuth2AuthenticationDetails) oAuth2Authentication.getDetails();
			
			UserSessionData userSessionData = new UserSessionData(oAuth2AccessToken.getValue(), "Keycloak", (String) oAuth2Authentication.getUserAuthentication().getPrincipal(),
					oAuth2AuthenticationDetails.getSessionId(), request.getHeader("user-agent"), request.getRemoteAddr(),
					new Date(), oAuth2AccessToken.getExpiration());
			
			userSessionDataRepository.insertOrUpdate(userSessionData);
			LOGGER.debug("{}User session id: {}", PREFIX,oAuth2AccessToken.getValue());
			LOGGER.debug("{}Keycloak session id: {}", PREFIX, oAuth2AuthenticationDetails.getSessionId());
			LOGGER.debug("{}User-agent: {}", PREFIX, request.getHeader("user-agent"));
			LOGGER.debug("{}Remote addr: {}", PREFIX, request.getRemoteAddr());
		}
	}

	/**
	 * Set the instance of the DefaultTokenServices to be use, to avoid link it from spring context
	 * 
	 * @param tokenServices
	 */
	public void setTokenServices(DefaultTokenServices tokenServices) {
		this.tokenServices = tokenServices;
	}

}