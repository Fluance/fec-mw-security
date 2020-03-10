package net.fluance.security.auth.web.controller.access;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import net.fluance.app.data.model.identity.User;
import net.fluance.app.web.util.exceptions.UnauthorizedException;
import net.fluance.security.auth.service.oauth2.OAuth2Service;
import net.fluance.security.core.model.jdbc.UserSessionData;
import net.fluance.security.core.repository.jdbc.UserSessionDataRepository;

/**
 * This controller manages the logout process and the goal of it is do a secure logout and redirect at the end, if there is any error {@link ErrorController} will manage 
 */
@RestController
@RequestMapping("/logout")
public class LogoutController {

	private Logger logger = LogManager.getLogger(LogoutController.class);
	
	private static final String INITIALIZE_PREFIX = "[logout][initialize]";
	
	private static final String REDIRECT_URL_PARAMETER = "url_redirect";
	
	@Autowired
	private UserSessionDataRepository userSessionDataRepository;
	@Autowired
	private OAuth2Service oAuth2Service;

	@Value("${keycloak.client.logoutUrl}")
	private String keycloakLogoutUri;
	@Value("${keycloak.client.oidc.logoutUrl}")
	private String oidcClientLogoutUrl;
	@Value("${auth.logout}")
	private String authLogoutUrl;

	@ApiOperation(value = "Terminate a SSO session. This revokes the associated access token. Complets the auth API and cancels the Keycloak session if it's need ", tags = {"AUTH API"})
	@RequestMapping(value = "/initialize", method = RequestMethod.GET)
	public void logout(@RequestParam String accessToken, @RequestParam String redirectUrl, HttpServletRequest request, HttpServletResponse response) throws InvalidKeyException, XMLStreamException, IOException, GeneralSecurityException {
		
		String finalRedirectUrl = authLogoutUrl;
		
		try {
			//Get the user
			User user = oAuth2Service.validateAccessTokent(accessToken);
			logger.warn(INITIALIZE_PREFIX + "Validation of (to revoke) access token " + accessToken + " returned user " + ((user != null) ? user.getUsername() : null));			
			if(user == null){
				throw new UnauthorizedException("Token Not valid");
			}
			
			// Revoke Fluance Oauth2 Access token
			logger.info(INITIALIZE_PREFIX + "Revoking OAuth2 access token...");
			logger.debug(INITIALIZE_PREFIX + "OAuth2 access token: " + accessToken);
			revokeAccessToken(accessToken,  INITIALIZE_PREFIX);
			
			//set redirect url
			if(redirectUrl != null) {
				logger.info(INITIALIZE_PREFIX+"Seting custom redirect");
				finalRedirectUrl = finalRedirectUrl + "?" + REDIRECT_URL_PARAMETER + "="+redirectUrl;
			}
			
			finalRedirectUrl = redirectToKeycloacLogout(accessToken, finalRedirectUrl, user);
			
			//delete access token from DB
			logger.info(INITIALIZE_PREFIX + "Deleting access token from user session data...");
			userSessionDataRepository.delete(accessToken);
			
			//deleting cookies
			deleteCookies(request, response);
			
			//Cleaning security context
			clearSecurityConext();

		} catch (Exception exc) {
			logger.warn("Error in the logut process: {}", exc.getMessage());
			logger.warn(ExceptionUtils.getStackTrace(exc));
		} finally {
			logger.info(INITIALIZE_PREFIX + "Redirectiing...");
			logger.debug(INITIALIZE_PREFIX + "redirecting to: " + finalRedirectUrl);
			response.sendRedirect(finalRedirectUrl);
		}
	}

	/**
	 * Clears the current security context 
	 */
	private void clearSecurityConext() {
		logger.info(INITIALIZE_PREFIX + "Deleting authentication from context...");
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if(authentication != null && authentication instanceof OAuth2Authentication) {
			logger.info(INITIALIZE_PREFIX + "Authentication is OAuth2Authentication");	
							
			OAuth2Authentication auth2Authentication = (OAuth2Authentication)authentication;
			
			logger.debug(INITIALIZE_PREFIX + "OAuth2Authentication implementation: " + authentication.getClass().getName());
			
			auth2Authentication.getAuthorities().stream().forEach(r -> logger.debug(INITIALIZE_PREFIX + "Authority: " + r.getAuthority()));
			
			logger.info(INITIALIZE_PREFIX + "Erasing credentials...");
			auth2Authentication.eraseCredentials();
			
			logger.info(INITIALIZE_PREFIX + "Set setAuthenticated to false...");
			auth2Authentication.setAuthenticated(false);
			
			logger.info(INITIALIZE_PREFIX + "Deleted authentication from context");
		} else {
			logger.warn(INITIALIZE_PREFIX + "Authentication from context is unexpected instance: " + authentication.getClass().getName());
		}
	}

	/**
	 * Delete the request cookies and set empty to the response
	 * 
	 * @param request
	 * @param response
	 */
	private void deleteCookies(HttpServletRequest request, HttpServletResponse response) {
		logger.info(INITIALIZE_PREFIX + "Deleting cookies...");
		Cookie[] cookies = request.getCookies();
		if(cookies != null && cookies.length > 0) {
			logger.debug(INITIALIZE_PREFIX + "Cookies != null and length=" + cookies.length);
			for (Cookie cookie : cookies) {
				logger.debug(INITIALIZE_PREFIX + "Cookie values: MaxAge=" + cookie.getMaxAge() + ", value=" + cookie.getValue() + ", Path=" + cookie.getPath());
				cookie.setMaxAge(0);
				cookie.setValue(null);
				cookie.setPath("/");
				response.addCookie(cookie);
			}
		} else {
			logger.debug(INITIALIZE_PREFIX + "Cookies are empty");
		}
	}

	/**
	 * Set the redirection to keycloak if it's need
	 * 
	 * @param accessToken
	 * @param finalRedirectUrl
	 * @param user
	 * @return
	 */
	private String redirectToKeycloacLogout(String accessToken, String finalRedirectUrl, User user) {
		//Get the session datas
		UserSessionData userSessionData = (user != null) ? userSessionDataRepository.findOne(accessToken) : null;
		String issuer = (userSessionData != null) ? userSessionData.getIssuer() : null;
		
		if(userSessionData != null){				
			if(issuer != null && issuer.equalsIgnoreCase("Keycloak")){
				logger.info(INITIALIZE_PREFIX + "Will redirect to Keycloak LOGOUT");					
				//redirectUrl = keycloakLogoutUri + URLEncoder.encode(oidcClientLogoutUrl + redirectUrl, StandardCharsets.UTF_8.toString());
				finalRedirectUrl = keycloakLogoutUri + finalRedirectUrl;		
			} else {
				logger.warn(INITIALIZE_PREFIX + "Not expected isuser");					
			}
		} else {
			logger.debug(INITIALIZE_PREFIX + "No user session data");
		}
		return finalRedirectUrl;
	}

	/**
	 * Revoke the access token and logs the result
	 * 
	 * @param accessToken
	 * @param logPrefix
	 * @throws Exception
	 */
	private void revokeAccessToken(String accessToken,  String logPrefix) throws Exception {
		if (oAuth2Service.revokeAccessToken(accessToken)) {
			logger.info(logPrefix + "OAuth2 access token revoked");
			logger.debug(logPrefix + "Access token: " + accessToken);
		} else {
			logger.warn(logPrefix + "OAuth2 access token not revoked");
			logger.warn(logPrefix + "Access token: " + accessToken);
		}
	}
}
