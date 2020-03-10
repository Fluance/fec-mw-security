package net.fluance.security.auth.web.controller.oauth2;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import net.fluance.app.data.model.identity.User;
import net.fluance.app.security.auth.OAuth2AccessToken;
import net.fluance.app.web.support.payload.response.GenericResponsePayload;
import net.fluance.security.auth.service.oauth2.OAuth2Service;
import net.fluance.security.auth.web.controller.AbstractAuthRestController;
import net.fluance.security.core.repository.jdbc.UserSessionDataRepository;

@RestController
@RequestMapping("/oauth2")
public class OAuth2Controller extends AbstractAuthRestController {

	private static Logger LOGGER = LogManager.getLogger(OAuth2Controller.class);

	@Autowired
	private OAuth2Service oAuth2Service;
	
	@Autowired
	private UserSessionDataRepository userSessionDataRepository;

	@ApiOperation(
			value = "Get OAuth2 Access Token", 
			response = GenericResponsePayload.class, tags = "OAuth2 API"
	)
	@RequestMapping(
			value = "/token", 
			method = RequestMethod.POST, 
			consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, 
						 MediaType.APPLICATION_JSON_VALUE }, 
			produces = { MediaType.APPLICATION_JSON_VALUE }
	)
	public ResponseEntity<?> token(
			@RequestBody String requestBody, 
			HttpServletRequest request, 
			HttpServletResponse response
	) {		
		try {
			OAuth2AccessToken oAuth2AccessToken = oAuth2Service.generateAuthorizationToken(requestBody);
			
			LOGGER.info("[oauth2][getToken]Storing token into session database");			
			userSessionDataRepository.insertUserAgent(oAuth2AccessToken.getAccessToken(), request.getHeader("user-agent"), request.getRemoteAddr());
			
			return new ResponseEntity<>(
					oAuth2AccessToken, 
					HttpStatus.OK
			);
		} catch (Exception e) {
			return handleException(e);
		}
	}

	@ApiOperation(value = "Validate OAuth2 Access Token and return the user", response = GenericResponsePayload.class, tags = "OAuth2 API")
	@RequestMapping(value = "/validate", method = RequestMethod.GET)
	public ResponseEntity<?> validate(@RequestParam(required=false) String accessToken, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			if (accessToken == null) {
				BearerTokenExtractor bearerTokenExtractor = new BearerTokenExtractor();			
				Authentication authentication = bearerTokenExtractor.extract(request);
				if(authentication!=null && authentication.getPrincipal()!=null) {
					accessToken = authentication.getPrincipal().toString();
				}
			}
			
			if(accessToken != null) {
				User user = oAuth2Service.validateAccessTokent(accessToken);

				if (user != null) {
					return new ResponseEntity<User>(user, HttpStatus.OK);
				} else {
					return new ResponseEntity<>("Access token not valid", HttpStatus.UNAUTHORIZED);
				}
			} else {
				return new ResponseEntity<>("Not access token provided", HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception e) {
			return handleException(e);
		}
	}

	@ApiOperation(value = "Revoke OAuth2 Access Token", response = GenericResponsePayload.class, tags = "OAuth2 API")
	@RequestMapping(value = "/revoke", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE })
	public ResponseEntity<?> revoke(@RequestParam String token, HttpServletRequest request, HttpServletResponse response)
			throws InvalidKeyException, XMLStreamException, IOException, GeneralSecurityException, NumberFormatException, URISyntaxException {
		
		boolean revoked = false;
		GenericResponsePayload responseBody = new GenericResponsePayload();
		String msg = null;
		HttpStatus status = null;
		
		try {
			revoked = oAuth2Service.revokeAccessToken(token);
			
			if (revoked) {
				msg = "Successfully revoked token " + token;
				status = HttpStatus.OK;
			} else {
				msg = "Token " + token + " not revoked due to an unknown error";
				status = HttpStatus.BAD_REQUEST;
			}
			LOGGER.debug("Token revoked : " + revoked);
			
			responseBody.setMessage(msg);
			return new ResponseEntity<>(responseBody, status);
		} catch (Exception exc) {
			return handleException(exc);
		}
	}

	@ApiOperation(value = "Refresh OAuth2 Access Token", response = GenericResponsePayload.class, tags = "OAuth2 API", notes="Authorization parameter is only useful for third party clients. Value should be : Basic encodedbase64(client_id:client_secret)")
	@RequestMapping(value = "/refresh", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> refresh(@RequestParam String token, @RequestParam(required = false, name="Authorization") String clientAuthorization, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {			
			OAuth2AccessToken oAuth2AccessTokenString = oAuth2Service.refreshAuthorizationToken(token, clientAuthorization);
			
			return new ResponseEntity<>(oAuth2AccessTokenString, HttpStatus.OK);
		} catch (Exception e) {
			return handleException(e);
		}
	}

	@Override
	public Logger getLogger() {
		return LOGGER;
	}
}
