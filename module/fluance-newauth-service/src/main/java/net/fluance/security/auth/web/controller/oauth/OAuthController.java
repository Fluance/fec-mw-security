package net.fluance.security.auth.web.controller.oauth;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import net.fluance.app.web.support.payload.response.GenericResponsePayload;

/**
 * This controller adds to the api the endpoint to revoke the access tokens. This endpoint is not included on the default api for
 * the OAuth built in server in Spring
 * 
 */
@RestController
public class OAuthController {
	
	private Logger logger = LogManager.getLogger(OAuthController.class);
	
	private static final String REVOKE_TOKEN_PREFIX = "[revoke_token]";

	@Autowired
	@Qualifier("tokenServices")
	private DefaultTokenServices tokenServices;
	
	@ApiOperation(value = "Revokes the token completing the operations for the built in ouauht server in Spring", tags = {"OAUTH API", "AUTH API"})
	@RequestMapping(value = "/oauth/revoke", method = {RequestMethod.POST})
	public ResponseEntity<?> revoke(@RequestParam String token, HttpServletRequest request) {		
		
		ResponseEntity<?> responseEntity;
		
		// Response must be different if the token to revoke exists or not
		// => Checking existence of the token
		// (Minor) Impact: token is read at least twice
		logger.info(REVOKE_TOKEN_PREFIX+"Reading access token...");		
		OAuth2AccessToken existingToken = tokenServices.readAccessToken(token);
		logger.info(REVOKE_TOKEN_PREFIX+"Access token read");
		
		if(existingToken == null) {
			logger.info(REVOKE_TOKEN_PREFIX+"Access is null");
			responseEntity =  new ResponseEntity<>(new GenericResponsePayload("Unknown access token: " + token), HttpStatus.BAD_REQUEST);
		}
		
		boolean revoked = false;
		
		try {
			logger.info(REVOKE_TOKEN_PREFIX+"Revoking access token...");
			revoked = tokenServices.revokeToken(token);
			logger.info(REVOKE_TOKEN_PREFIX+"Access token revoke status: " + revoked);
		} catch(Exception exc) {
			logger.error(REVOKE_TOKEN_PREFIX+"Error revoking access token");
			responseEntity = new ResponseEntity<>(new GenericResponsePayload("Failed to revoke token " + token + ". Error is " + exc.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		if(revoked) {
			responseEntity =  new ResponseEntity<>(new GenericResponsePayload("Token " + token + " successfully revoked"), HttpStatus.OK);
		} else {
			responseEntity =  new ResponseEntity<>(new GenericResponsePayload("Token " + token + " not revoked due to an unknown error"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return responseEntity;
	}

}
