/**
 * 
 */
package net.fluance.security.auth.config.helper.jwt;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.fluance.app.data.model.identity.User;
import net.fluance.app.security.auth.AuthorizationStrategyEnum;
import net.fluance.app.security.service.IUserService;
import net.fluance.app.security.util.JwtHelper;
import net.fluance.app.security.util.exception.InvalidTokenException;
import net.fluance.commons.json.jwt.JWTUtils;
import net.fluance.commons.json.jwt.JWTUtils.JwtPart;
import net.fluance.commons.net.HttpUtils;
import net.fluance.security.auth.config.helper.CustomTokenGranter;
import net.fluance.security.auth.config.helper.jwt.exception.InvalidAssertionException;
import net.fluance.security.auth.config.helper.jwt.exception.InvalidAuthenticationTokenException;
import net.fluance.security.auth.config.helper.jwt.exception.InvalidSubjectIdException;
import net.fluance.security.core.model.jdbc.UserInfo;
import net.fluance.security.core.repository.jdbc.UserInfoRepository;
import net.fluance.security.core.service.UserIdentityService;

public class JWTTokenGranter extends CustomTokenGranter {

	private Logger LOGGER = LogManager.getLogger(JWTTokenGranter.class);
	private static final String OAUTH2AUTHENTICATION_LOG = "[OAuth2Authentication]";
	private static final String IS_ASSERTION_VALID_LOG = "[isAssertionValid]";
	private static final String IS_SIGNATURE_VALID_LOG = "[isSignatureValid]";
	private static final String SUBJECT_ID_LOG = "[subjectId]";
	private static final String IS_USER_EXISTS_LOG = "[isUserExists]";
		
	@Value("${oauth2.service.token.url}")
	protected String oAuth2TokenUrl;
	
	@Value("${oauth2.service.client.authorization-type}")
	protected String oAuth2ClientAuthorizationType;
	
	@Value("${oauth2.service.client.id}")
	protected String oAuth2ClientId;
	
	@Value("${oauth2.service.client.secret}")
	protected String oAuth2ClientSecret;
	
	@Value("${application.user.shared-password}")
	protected String applicationUserSharedPassword;
	
	@Value("${config.jwt-assertion.subject-id.allowed}")
	private String allowedSubjectIdClaimsConf;
	
	@Value("${config.jwt-assertion.user-info.allowed}")
	private String allowedUserInfoClaimsConf;
	
	@Value("${identity.domains.default}")
	private String defaultDomain;
	
	private List<String> allowedUserInfoClaims;
	private List<String> allowedSubjetIdClaims;
	private JWTAssertionService jwtAssertionService;
	private IUserService userService;
	
	UserInfoRepository userInfoRepository;
	
	private UserIdentityService userIdentityService;

	public JWTTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService,
			OAuth2RequestFactory requestFactory, String grantType) throws Exception {
		super(tokenServices, clientDetailsService, requestFactory, grantType);
	}

	protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
		Map<String, String> params = tokenRequest.getRequestParameters();

		String jwtAssertion = params.get("jwtAssertion");

		boolean isAssertionValid;
		try {
			LOGGER.info(OAUTH2AUTHENTICATION_LOG+"Init...");
			
			isAssertionValid = isAssertionValid(jwtAssertion, OAUTH2AUTHENTICATION_LOG);
			LOGGER.info(OAUTH2AUTHENTICATION_LOG+"Assertion valid: " + true);
			
			Authentication user = null;
			//Verify the validity of the JWT
			if (!isAssertionValid) {
				throw new InvalidAuthenticationTokenException("Invalid JWT assertion");
			}
			
			//Check the subjectId
			String subjectId = subjectId(jwtAssertion, OAUTH2AUTHENTICATION_LOG);
			LOGGER.info(OAUTH2AUTHENTICATION_LOG+"Subject Id: " + (subjectId != null));			
			if (subjectId == null) {
				throw new InvalidSubjectIdException("Invalid subject in JWT assertion: " + subjectId);
			}
						
			//Get the UserInfos and put them in the database
			String infos = "{}";
			
			AuthorizationStrategyEnum authorizationStrategy = JwtHelper.determineStrategy((String)jwtAssertion);
			if (authorizationStrategy == AuthorizationStrategyEnum.AUTHENTICATION_TRUSTED_PARTNER) {
				infos = getUserInfoFromIDP(jwtAssertion, OAUTH2AUTHENTICATION_LOG);
			} else {
				infos = getUserInfosFromJwt(jwtAssertion, OAUTH2AUTHENTICATION_LOG);
			}

			UserInfo userInfo = new UserInfo(subjectId, infos);
			userInfoRepository.insertOrUpdate(userInfo);
			LOGGER.info(OAUTH2AUTHENTICATION_LOG + "User info strored");
			LOGGER.debug("{}{}", OAUTH2AUTHENTICATION_LOG, infos);
			
			user = new JWTAuthenticationToken(jwtAssertion, subjectId);
			user.setAuthenticated(isAssertionValid && subjectId != null);
			LOGGER.info(OAUTH2AUTHENTICATION_LOG + "User Authenticated: " + (isAssertionValid && subjectId != null));
			
			OAuth2Authentication authentication = new OAuth2Authentication(tokenRequest.createOAuth2Request(client), user);			
			authentication.setAuthenticated(user.isAuthenticated());
			LOGGER.info(OAUTH2AUTHENTICATION_LOG + "User OAuth2Authentication: " + user.isAuthenticated());
			
			return authentication;
		} catch (IOException | InvalidAssertionException e) {
			LOGGER.warn(OAUTH2AUTHENTICATION_LOG + e.getMessage());
			LOGGER.debug(e);
			throw OAuth2Exception.create(OAuth2Exception.INVALID_TOKEN, e.getMessage());
		} catch (Exception e) {
			LOGGER.error(OAUTH2AUTHENTICATION_LOG + e.getMessage());
			LOGGER.debug(e);
			throw OAuth2Exception.create(OAuth2Exception.INVALID_TOKEN, e.getMessage());
		}
	}

	private String getUserInfosFromJwt(Object jwtAssertion, String logPrefix) throws InvalidAssertionException, JsonProcessingException, IOException {
		LOGGER.info(logPrefix + "Preparing User infos from JWT ...");
		if(jwtAssertion == null) {
			throw new InvalidAssertionException("Assertion cannot be null");
		}

		ObjectNode jwtPayload = JWTUtils.getPart((String) jwtAssertion, JwtPart.PAYLOAD);
		ObjectNode subjectIdJson = new ObjectMapper().createObjectNode();

		boolean atLeastOneSubjectIdClaim = false;
		for (String allowedSubjectIdClaim : allowedUserInfoClaims) {
			if (jwtPayload.has(allowedSubjectIdClaim) && jwtPayload.get(allowedSubjectIdClaim) != null) {
				if(jwtPayload.get(allowedSubjectIdClaim).isLong()){
					subjectIdJson.put(allowedSubjectIdClaim, jwtPayload.get(allowedSubjectIdClaim).textValue());
				} else if(jwtPayload.get(allowedSubjectIdClaim).isInt()){
					subjectIdJson.put(allowedSubjectIdClaim, jwtPayload.get(allowedSubjectIdClaim).intValue());
				} else if(jwtPayload.get(allowedSubjectIdClaim).isTextual()){
					subjectIdJson.put(allowedSubjectIdClaim, jwtPayload.get(allowedSubjectIdClaim).textValue());
				}
				atLeastOneSubjectIdClaim = true;
			}
		}

		if (!atLeastOneSubjectIdClaim) {
			return subjectIdJson.toString();
		}
		
		String issuer = (jwtPayload != null && jwtPayload.has(JWTUtils.ISSUER_KEY)
				&& jwtPayload.get(JWTUtils.ISSUER_KEY).isTextual()) ? jwtPayload.get(JWTUtils.ISSUER_KEY).textValue()
						: null;
		subjectIdJson.put(ISSUER_KEY, issuer);
		return subjectIdJson.toString();
	}

	private String getUserInfoFromIDP(Object jwtAssertion, String logPrefix) throws Exception, JsonProcessingException {
		LOGGER.info(logPrefix + "Preparing User infos from KEYCLOAK for trusted partner ...");
		ObjectNode jwtPayload = JWTUtils.getPart((String) jwtAssertion, JwtPart.PAYLOAD);
		User user = new User();
		user.setUsername(jwtPayload.get("username").textValue());
		user.setDomain(jwtPayload.get("domain").textValue());
		user = userIdentityService.getUserClaims(user);
		String infos = user.toJsonString();
		return infos;
	}

	@Override
	protected String subjectId(Object jwtAssertion) throws JsonProcessingException, IOException, InvalidAssertionException {
		return subjectId(jwtAssertion, "");
	}
	
	private String subjectId(Object jwtAssertion, String logPrefix) throws JsonProcessingException, IOException, InvalidAssertionException {
		logPrefix = logPrefix + SUBJECT_ID_LOG;
		
		LOGGER.info("{}Init...", logPrefix);
		
		if(jwtAssertion == null) {
			LOGGER.error("{}Assertion cannot be null", logPrefix);
			throw new InvalidAssertionException("Assertion cannot be null");
		}
		
		ObjectNode jwtPayload = JWTUtils.getPart((String) jwtAssertion, JwtPart.PAYLOAD);
		ObjectNode subjectIdJson = new ObjectMapper().createObjectNode();

		boolean atLeastOneSubjectIdClaim = false;
		for (String allowedSubjectIdClaim : allowedSubjetIdClaims) {
			if (jwtPayload.has(allowedSubjectIdClaim) && jwtPayload.get(allowedSubjectIdClaim) != null) {
				if(jwtPayload.get(allowedSubjectIdClaim).isLong()){
					subjectIdJson.put(allowedSubjectIdClaim, jwtPayload.get(allowedSubjectIdClaim).textValue());
				} else if(jwtPayload.get(allowedSubjectIdClaim).isInt()){
					subjectIdJson.put(allowedSubjectIdClaim, jwtPayload.get(allowedSubjectIdClaim).intValue());
				} else if(jwtPayload.get(allowedSubjectIdClaim).isTextual()){
					subjectIdJson.put(allowedSubjectIdClaim, jwtPayload.get(allowedSubjectIdClaim).textValue());
				}
				atLeastOneSubjectIdClaim = true;
			}
		}

		if (!atLeastOneSubjectIdClaim) {
			return null;
		}

		String issuer = (jwtPayload != null && jwtPayload.has(JWTUtils.ISSUER_KEY)
				&& jwtPayload.get(JWTUtils.ISSUER_KEY).isTextual()) ? jwtPayload.get(JWTUtils.ISSUER_KEY).textValue()
						: null;
		subjectIdJson.put(ISSUER_KEY, issuer);

		String subjectId = defaultDomain + "/" + issuer;
		AuthorizationStrategyEnum authorizationStrategy = JwtHelper.determineStrategy((String)jwtAssertion);
		if(authorizationStrategy == AuthorizationStrategyEnum.SINGLE_PATIENT){
			if(subjectIdJson.get("pid") !=null){
				subjectId = subjectId + "/" + subjectIdJson.get("pid").asLong();
				if(subjectIdJson.get("username") !=null){
					subjectId = subjectId + "/" + subjectIdJson.get("username").asText();
				}
			}
		} else if (authorizationStrategy == AuthorizationStrategyEnum.AUTHENTICATION_TRUSTED_PARTNER) {
			subjectId = subjectIdJson.get("domain").asText() + "/" + subjectIdJson.get("username").asText();
		}
		
		LOGGER.info("{}Subject_ID Ready: {}", logPrefix, subjectId);
		return subjectId;
	}

	@Override
	public org.slf4j.Logger getLogger() {
		return logger;
	}

	@Override
	public boolean isAssertionValid(Object assertion) throws Exception {
		return isAssertionValid(assertion, "");
	}
	
	public boolean isAssertionValid(Object assertion, String logPrefix) throws Exception {
		logPrefix = logPrefix + IS_ASSERTION_VALID_LOG;
		
		LOGGER.info("{}Init...", logPrefix);
		LOGGER.debug("{}{Assertion: {}}", logPrefix, assertion);
		
		if (!JWTUtils.isJwt((String) assertion)) {
			LOGGER.info("{}Bad Jwt assertion", logPrefix);
			return false;
		}
		
		boolean isAssertionValid = isSignatureValid(assertion, logPrefix);
		LOGGER.info("{}Checking signature", logPrefix);
		
		AuthorizationStrategyEnum authorizationStrategy = JwtHelper.determineStrategy((String)assertion);
		LOGGER.info("{}Authorization Strategy will be: {}", logPrefix, authorizationStrategy.getName());
		
		switch (authorizationStrategy) {
		case SINGLE_PATIENT:
			if(isAssertionValid) {
				boolean isPatientValid = jwtAssertionService.isPatientValid((String) assertion);
				LOGGER.info("{}Is patient valid: {}", logPrefix, isPatientValid);
				
				isAssertionValid = isAssertionValid && isPatientValid;
			}
		case AUTHENTICATION_TRUSTED_PARTNER:			
			boolean userExists = isUserExists((String) assertion, logPrefix);
			LOGGER.info("{}user exists: {}", logPrefix, userExists);
			
			isAssertionValid = isAssertionValid & userExists;
		default:
			LOGGER.info("{}Default", logPrefix);
		}
		
		LOGGER.info("{}Is assertion valid: {}", logPrefix, isAssertionValid);
		return isAssertionValid;
	}
	
	private boolean isSignatureValid(Object assertion, String logPrefix) throws JsonProcessingException, IOException, InvalidAssertionException {
		logPrefix = logPrefix + IS_SIGNATURE_VALID_LOG;
		
		LOGGER.info("{}Init...", logPrefix);
		
		ObjectNode jwtHeader = JWTUtils.getPart((String) assertion, JwtPart.HEADER);
		LOGGER.info("{}Checked the presence of JWT part : header", logPrefix);
		
				
		String signingAlgorithm = (jwtHeader != null && jwtHeader.has(JWTUtils.SIGNING_ALGORITHM_KEY)
				&& jwtHeader.get(JWTUtils.SIGNING_ALGORITHM_KEY).isTextual())
						? jwtHeader.get(JWTUtils.SIGNING_ALGORITHM_KEY).textValue() : null;
		LOGGER.info("{}Checked the presence of  JWT part : signature algorithm", logPrefix);
						
							
		ObjectNode jwtPayload = JWTUtils.getPart((String) assertion, JwtPart.PAYLOAD);
		String issuer = (jwtPayload != null && jwtPayload.has(JWTUtils.ISSUER_KEY)
				&& jwtPayload.get(JWTUtils.ISSUER_KEY).isTextual()) ? jwtPayload.get(JWTUtils.ISSUER_KEY).textValue()
						: null;
		LOGGER.info("{}Checked the presence of  JWT part : payload", logPrefix);				
		LOGGER.debug("{}{issuer : {}, signing_algorithm: {}}", logPrefix, issuer, signingAlgorithm);
		
		PublicKey issuerPublicKey = trustedPublicKeys.get(issuer);
		LOGGER.info("{}Getted issuer trusted public key", logPrefix);
		LOGGER.debug("{}{trusted_public_key: {}}", logPrefix, issuerPublicKey);

		boolean isAssertionValid;
		isAssertionValid = (issuerPublicKey != null) ? JWTUtils.verifySignature((String) assertion, signingAlgorithm, issuerPublicKey) : false;
		LOGGER.info("{}Signature verified: {}", logPrefix, isAssertionValid);
		
		return isAssertionValid;
	}
	
	private boolean isUserExists(String jwt, String logPrefix) throws JsonProcessingException, IOException {
		logPrefix = logPrefix + IS_USER_EXISTS_LOG;
		
		JsonNode jwtPayload = JWTUtils.getPart((String) jwt, JwtPart.PAYLOAD);
		if (jwtPayload == null || !jwtPayload.has(JWTUtils.ISSUER_KEY)) {
			LOGGER.error(logPrefix+"The element '" + JWTUtils.ISSUER_KEY + "' is not present in the JWT Token's payload: " + jwtPayload);
			throw new IllegalArgumentException("The element '" + JWTUtils.ISSUER_KEY + "' is not present in the JWT Token's payload: " + jwtPayload);
		}
		try {
			String username = null;
			String domain = null;
			username = jwtPayload.get(JwtHelper.USERNAME_KEY).textValue();
			String jwtDomain = (jwtPayload.has(JwtHelper.DOMAIN_KEY)) ? jwtPayload.get(JwtHelper.DOMAIN_KEY).textValue() : domain;
			if (jwtDomain == null) {
				domain = defaultDomain;
			} else {
				domain = jwtDomain;
			}
			if (username == null || username.isEmpty()) {
				throw new InvalidTokenException("User name " + username + " is not valid for JWT " + jwt);
			}
			
			LOGGER.info(logPrefix + "Calling user service");
			LOGGER.info("{}User service is: {}", logPrefix, userService.getClass().getCanonicalName());
			boolean userExists = userService.isExistingUser(username, domain);			
			LOGGER.info(logPrefix + "{user : " + domain + "/" + username + ", exists: " + userExists + "}");
			return userExists;
		} catch (Exception exc) {
			LOGGER.error("{}NOT ABLE to validate User existance: {}", logPrefix, exc.getMessage());
			LOGGER.debug(ExceptionUtils.getStackTrace(exc));
			return false;
		}	
	}

	/**
	 * @return the allowedSubjetIdClaims
	 */
	public List<String> getAllowedSubjetIdClaims() {
		return allowedSubjetIdClaims;
	}

	/**
	 * @param allowedSubjetIdClaims
	 *            the allowedSubjetIdClaims to set
	 */
	public void setAllowedSubjetIdClaims(List<String> allowedSubjetIdClaims) {
		this.allowedSubjetIdClaims = allowedSubjetIdClaims;
	}
	
	/**
	 * @return the allowedSubjetIdClaims
	 */
	public List<String> getAllowedUserInfoClaims() {
		return allowedUserInfoClaims;
	}

	/**
	 * @param allowedUserInfoClaims
	 *            the allowedSubjetIdClaims to set
	 */
	public void setAllowedUserInfoClaims(List<String> allowedUserInfoClaims) {
		this.allowedUserInfoClaims = allowedUserInfoClaims;
	}
	
	public void setAssertionService(JWTAssertionService jwtAssertionService){
		this.jwtAssertionService = jwtAssertionService;
	}
	
	public void setUserService(IUserService userService){
		this.userService = userService;
	}
	
	public void setUserInfoRepository(UserInfoRepository userInfoRepository) {
		this.userInfoRepository = userInfoRepository;
	}
	
	public void setUserIdentityService(UserIdentityService userIdentityService){
		this.userIdentityService = userIdentityService;
	}

	/**
	 * Send a real http requestusing the complete URL and the access token
	 * 
	 * @param fullUri
	 * @param token
	 *            accessToken
	 * @return CloseableHttpResponse
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws HttpException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	private CloseableHttpResponse sendRequest(String fullUri, String token) throws URISyntaxException,
			KeyManagementException, NoSuchAlgorithmException, KeyStoreException, HttpException, IOException {
		URI uri = HttpUtils.buildUri(fullUri);
		HttpGet get = HttpUtils.buildGet(uri, null);
		get.setHeader("Authorization", "Bearer " + token);
		CloseableHttpResponse response = HttpUtils.sendGet(get, true);
		return response;
	}
}
