package net.fluance.security.auth.web.controller.usersession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import net.fluance.app.data.model.identity.User;
import net.fluance.app.data.model.identity.UserProfile;
import net.fluance.app.data.model.identity.UserType;
import net.fluance.app.security.service.UserProfileLoader;
import net.fluance.app.web.util.exceptions.ForbiddenException;
import net.fluance.app.web.util.exceptions.InternalServerErrorException;
import net.fluance.app.web.util.exceptions.UnauthorizedException;
import net.fluance.security.auth.service.oauth2.OAuth2Service;
import net.fluance.security.auth.web.controller.AbstractAuthRestController;
import net.fluance.security.core.model.jdbc.UserSessionData;
import net.fluance.security.core.repository.jdbc.UserSessionDataRepository;
import net.fluance.security.core.service.KeycloakUserService;
import net.fluance.security.core.support.exception.NotFoundException;

@RestController
@ConditionalOnProperty(value = "fluance.endpoints.revokeSessionController", havingValue="true")
@RequestMapping("/sessions")
public class RevokeSessionController extends AbstractAuthRestController{

	private Logger logger = LogManager.getLogger(RevokeSessionController.class);
	
	@Autowired
	private KeycloakUserService keycloakUserService;

	@Autowired
	private UserSessionDataRepository userSessionDataRepository;
	
	@Autowired
	private OAuth2Service oAuth2Service;
	
	@Autowired
	private UserProfileLoader userProfileLoader;
	
	/**
	 * Logout all the sessions of the user associated to the accessToken
	 * @param accessToken
	 * @param request
	 * @param response
	 * @throws Exception	If the UserType is <em>Shared</em>, the method returns a {@link UnauthorizedException} and it cannot disable any session
	 */
	@ApiOperation(value = "Logout from multiple devices", responseContainer = "list", tags = "User Sessions API")
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public void revokeUserSessions(@RequestParam String accessToken, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			User userHeader = getUserHeader(request);
			
			User user = oAuth2Service.validateAccessTokent(accessToken);				
			getLogger().info("Validation of (to revoke) access token " + accessToken + " returned user " + ((user != null) ? user.getUsername() : null));		
			if(user == null){
				throw new UnauthorizedException("Token Not valid");
			}
					
			getLogger().debug("Auth User: " + userHeader.getUsername() + "/" + userHeader.getDomain());
			getLogger().debug("Revoke User: " + user.getUsername() + "/" + user.getDomain());
			
			validateUserForLogout(accessToken, userHeader, user);
				
			removeKeycloakSession(accessToken);
			
			revokeAccessToken(accessToken);
			getLogger().info("Access token revoked");
			
			userSessionDataRepository.delete(accessToken);
			getLogger().info("User session removed");
			
		} catch(Exception e) {
			handleException(e);
		}
	}

	/**
	 * Tries to remove the keycloak session if its possible. It usses the session index store in {@link UserSessionData}
	 * 
	 * @param accessToken
	 */
	private void removeKeycloakSession(String accessToken) {
		UserSessionData userSessionData = userSessionDataRepository.findOne(accessToken);
		
		if(userSessionData != null && userSessionData.getIssuer() != null && userSessionData.getIssuer().equalsIgnoreCase("keycloak")
				&& userSessionData.getSessionIndex() != null) {
			// Remove Keycloak Session
			logger.info("Removing user's session from keycloak : " + userSessionData.getSessionIndex());
			
			try {
				keycloakUserService.removeUserSession(userSessionData.getSessionIndex());
			} catch(Exception e) {
				logger.warn("Error removing keycloak session: {}", e.getMessage());
			}			
		}
	}

	/**
	 * Validate the data for the user on the request and the user owner of the token
	 * 
	 * @param accessToken
	 * @param userHeader
	 * @param user
	 * @throws NotFoundException
	 */
	private void validateUserForLogout(String accessToken, User userHeader, User user) throws NotFoundException {
		if(!(user.getUsername().equals(userHeader.getUsername()) &&  user.getDomain().equals(userHeader.getDomain()))){
			throw new UnauthorizedException("User Not Authorized to Revoke This Session");
		}
	
		UserProfile userProfile = userProfileLoader.loadProfile(user.getUsername(), user.getDomain(), accessToken);
		
		if(userProfile == null){
			throw new NotFoundException("The User Profile could not be found");
		}
		
		if(userProfile.getProfile() == null){
			throw new NotFoundException("The Profile could not be found");
		}		
		
		if(userProfile.getProfile().getUsertype() == null || userProfile.getProfile().getUsertype().isEmpty()){
			throw new InternalServerErrorException("UserType's of the User profile is null");
		}
		
		if(userProfile.getProfile().getUsertype().equals(UserType.SHARED.getValue())){
			throw new ForbiddenException("User Type is not allowed to revoke tokens");
		}
	}
	
	/**
	 * From a {@link Request}, gets the {@link User} associated with the Auth Token if it exists
	 * @param request
	 * @return
	 * @throws Exception
	 */
	private User getUserHeader(HttpServletRequest request) throws Exception {
		BearerTokenExtractor bearerTokenExtractor = new BearerTokenExtractor();
		Authentication authentication = bearerTokenExtractor.extract(request);
		if (authentication != null && authentication.getPrincipal() != null){
			String accessToken = authentication.getPrincipal().toString();
			User userConnected = oAuth2Service.validateAccessTokent(accessToken);		
			if(userConnected == null){
				throw new UnauthorizedException("Token Not valid");
			}
			return userConnected;
		} else {
			throw new UnauthorizedException("Cannot Extract Access token");
		}
	}

	/**
	 * Revokes the access token and logs the result
	 * 
	 * @param accessToken
	 * @throws Exception
	 */
	private void revokeAccessToken(String accessToken) throws Exception {
		if (oAuth2Service.revokeAccessToken(accessToken)) {
			logger.info("OAuth2 access token " + accessToken + " revoked");
		} else {
			logger.warn("OAuth2 access token " + accessToken + " not revoked");
		}
	}

	@Override
	public Logger getLogger() {
		return logger;
	}
}
