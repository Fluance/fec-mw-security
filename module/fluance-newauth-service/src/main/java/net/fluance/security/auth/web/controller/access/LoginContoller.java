package net.fluance.security.auth.web.controller.access;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiOperation;
import net.fluance.commons.codec.Base64Utils;
import net.fluance.security.auth.service.oauth2.OAuth2Service;

/**
 * This controller manages the login process and the goal of it is redirect to the given URL or default, if there is any error {@link ErrorController} will manage 
 */
@RestController
@RequestMapping("/login")
public class LoginContoller {
	private Logger logger = LogManager.getLogger(LoginContoller.class);
	private static final String INITIALIZE_PREFIX = "[login][initialize]";
	
	private static final String TOKEN_PARAM =  "token=";
	
	@Autowired
	@Qualifier("tokenServices")
	private DefaultTokenServices tokenServices;	
	
	@Autowired
	OAuth2Service oAuth2Service;

	@Value("${clients.fe.url}")
	private String uiUrl;
	
	@ApiOperation(value = "Finalize the login process redirecting to default or the given url, concats \"" + TOKEN_PARAM + " \" to the url with the Fluance OAuth2 access token if there is a valid authentication", tags = {"AUTH API"})
	@RequestMapping(path="/initialize", method = RequestMethod.GET)
	public void initialize(@RequestParam(required=false) String redirectUrl, HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws Exception {
		logger.info("{}Init...", INITIALIZE_PREFIX);
		if(redirectUrl == null) {
			redirectUrl = uiUrl;
			logger.debug("{}Redirecting to default ui url", INITIALIZE_PREFIX);
		}
		
		redirectUrl = doLogin(redirectUrl, request, authentication);
				
		logger.info("{}Redirecting to: {}", INITIALIZE_PREFIX, redirectUrl);
		response.sendRedirect(redirectUrl);
	}

	/**
	 * Do all logging stuff using the data of the request and of the authentication object
	 * 
	 * @param redirectUrl
	 * @param request
	 * @param authentication
	 * @return
	 */
	private String doLogin(String redirectUrl, HttpServletRequest request, Authentication authentication) {
		if(authentication != null && authentication instanceof OAuth2Authentication) {
			logger.info("{}Authentication is OAuth2Authentication", INITIALIZE_PREFIX);				
			OAuth2Authentication oauth2Authentication = (OAuth2Authentication)authentication;

			try {
				OAuth2AccessToken oauth2AccessToken = tokenServices.getAccessToken(oauth2Authentication);
				logger.debug("{}Token getted from Authorization server", INITIALIZE_PREFIX);

				net.fluance.app.security.auth.OAuth2AccessToken oAuth2AccessTokenFluance = oAuth2Service.parseToOAuth2AccessToken((DefaultOAuth2AccessToken) oauth2AccessToken);
							
				String encodedOAuth2AccessToken = Base64Utils.encode(new ObjectMapper().writeValueAsString(oAuth2AccessTokenFluance));				
				if(encodedOAuth2AccessToken != null) {	
					
					redirectUrl = compundRedirectUrl(redirectUrl, encodedOAuth2AccessToken);
					
					logger.info("{}Fluance OAuth2AccessToken added to redirect", INITIALIZE_PREFIX);
				}
			} catch (Exception e) {
				logger.error("{}TokenServices oauth2AccessToken error: {}", INITIALIZE_PREFIX, e.getMessage());
			}
		}
		return redirectUrl;
	}
	
	String compundRedirectUrl(String redirectUrl, String encodedOAuth2AccessToken) {
		String appendCharacter = "";
		if(redirectUrl.lastIndexOf('?') > 0) {
			appendCharacter = "&";
		} else {
			appendCharacter = "?";
		}
		
		return redirectUrl.concat(appendCharacter).concat(TOKEN_PARAM).concat(encodedOAuth2AccessToken);
	}
}
