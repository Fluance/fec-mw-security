package net.fluance.security.auth.service.oauth2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.fluance.app.data.model.identity.ThirdPartyUserReference;
import net.fluance.app.data.model.identity.User;
import net.fluance.app.security.auth.OAuth2AccessToken;
import net.fluance.app.web.util.exceptions.UnauthorizedException;
import net.fluance.commons.codec.Base64Utils;
import net.fluance.commons.json.JsonUtils;
import net.fluance.commons.net.HttpUtils;
import net.fluance.security.auth.config.helper.jwt.JWTValues;

@Service
public class OAuth2Service {
	
	private Logger logger = LogManager.getLogger();

	private static final String GRANT_TYPE = "grant_type";
	private static final String REFRESH_TOKEN = "refresh_token";
	
	private static final String VALIDATE_TOKEN_PREFIX = "[validate_token]";
	private static final String REFRESH_TOKEN_PREFIX = "[refresh_token]";
	private static final String REVOKE_TOKEN_PREFIX = "[revoke_token]";
	private static final String GENERATE_TOKEN_PREFIX = "[generate_token]";
	private static final String BUILD_TOKEN_PREFIX = "[build_token]";
	
	@Value("${oauth2.service.url.getToken}")
	private String urlOAuth2RequestTokent;
	@Value("${oauth2.service.url.AuthorizationHeader}")
	private String oAuth2ServerAuthorizationHeader;
	@Value("${oauth2.service.url.validateToken}")
	private String urlOAuth2ValidateToken;
	@Value("${oauth2.service.url.revokeToken}")
	private String urlOAuth2RevokeToken;
	@Value("${identity.domains.default}")
	private String defaultDomain;
	@Value("${config.accesstoken.validity.seconds}")
    private int accessTokenValiditySeconds;
    
    /**
     * Returns the {@link OAuth2AccessToken} get from the given assertion.<br>
     * The assertion can be the JSON representation of a OAUTH2 access token. The JSON can be base64 encoded or not<br>
     * If the assertion is not and OAUTH2 the assertion will be send to the low level endpoint to be managed as JWT token. 
     * 
     * @param assertion
     * @return
     * @throws Exception
     */
	public OAuth2AccessToken generateAuthorizationToken(String assertion) throws Exception{
		logger.info("{}Init...", GENERATE_TOKEN_PREFIX);
		logger.debug("{}{assertion: {}}", GENERATE_TOKEN_PREFIX, assertion);
		
		AssertionType assertionType = AssertionType.assertionType(assertion);
		logger.info("{}Assertion type is: {}", GENERATE_TOKEN_PREFIX, assertionType.getAssertionName());
		
		if(assertionType == AssertionType.OAUTH2){
			logger.info("{}Assertion type is OAUTH2", GENERATE_TOKEN_PREFIX);
			if(Base64.isBase64(assertion)){
				logger.debug("{}Assertion is base64 encoded", GENERATE_TOKEN_PREFIX);
				logger.info("{}Returning that OAuth2AccessToken instance", GENERATE_TOKEN_PREFIX);
				return buildTokenFromString(Base64Utils.base64UrlDecode(assertion), GENERATE_TOKEN_PREFIX);
			} else {
				logger.debug("{}Assertion is not base64 encoded", GENERATE_TOKEN_PREFIX);
				logger.info("{}Returning that OAuth2AccessToken instance", GENERATE_TOKEN_PREFIX);
				return buildTokenFromString(assertion, GENERATE_TOKEN_PREFIX);
			}
		} else {
			logger.info("{}Assertion is not OAUTH2", GENERATE_TOKEN_PREFIX);			
			String oAuth2SrvToken = getAuth2SrvToken(assertion, assertionType, GENERATE_TOKEN_PREFIX);
			
			return buildTokenFromString(oAuth2SrvToken, GENERATE_TOKEN_PREFIX);
		}
	}
	
	/**
	 * Parses a {@link DefaultOAuth2AccessToken} to a new instance of {@link OAuth2AccessToken}
	 * 
	 * @param defaultOAuth2AccessToken
	 * @return
	 * @throws Exception
	 */
	public OAuth2AccessToken parseToOAuth2AccessToken(DefaultOAuth2AccessToken defaultOAuth2AccessToken) {		
		OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken();
		
		oAuth2AccessToken.setAccessToken(defaultOAuth2AccessToken.getValue());
		oAuth2AccessToken.setTokenType(defaultOAuth2AccessToken.getTokenType());
		oAuth2AccessToken.setRefreshToken(defaultOAuth2AccessToken.getRefreshToken().getValue());
		if(defaultOAuth2AccessToken.getExpiration() != null) {
			oAuth2AccessToken.setExpirationDate(defaultOAuth2AccessToken.getExpiration().getTime());
		} else {
			oAuth2AccessToken.setExpirationDate(Date.from(LocalDateTime.now().plusSeconds(accessTokenValiditySeconds).atZone(ZoneId.systemDefault()).toInstant()).getTime());
			logger.warn("Expiration time was null, set to default current time plus {}seconds", accessTokenValiditySeconds);
		}
		
		return oAuth2AccessToken;
	}
	
	/**
	 * Refresh the token using the the given <b>refreshToken</b>
	 * 
	 * @param refreshToken
	 * @param clientAuthorization, can be null
	 * @return
	 * @throws Exception
	 */
	public OAuth2AccessToken refreshAuthorizationToken(String refreshToken, String clientAuthorization) throws Exception{		
		logger.info("{}Init refresh...", REFRESH_TOKEN_PREFIX);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair(GRANT_TYPE, REFRESH_TOKEN));
		params.add(new BasicNameValuePair(REFRESH_TOKEN, refreshToken));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
				
		entity.setContentType("application/x-www-form-urlencoded");
		
		logger.debug("{}Refresh request to: {}", REFRESH_TOKEN_PREFIX, urlOAuth2RequestTokent);
		HttpPost postRequest = HttpUtils.buildPost(new URI(urlOAuth2RequestTokent), null, entity);		
		
		if(clientAuthorization != null && !clientAuthorization.isEmpty()){
			logger.debug("{}With client authorization", REFRESH_TOKEN_PREFIX);
			postRequest.setHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, clientAuthorization));
		} else {
			logger.debug("{}Without client authorization", REFRESH_TOKEN_PREFIX);
			postRequest.setHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, oAuth2ServerAuthorizationHeader));
		}
		
		logger.info("{}Sending request...", REFRESH_TOKEN_PREFIX);
		CloseableHttpResponse response = HttpUtils.send(postRequest, true);
		logger.info("{}Request result: {}", REFRESH_TOKEN_PREFIX, response.getStatusLine().getStatusCode());
		
		if(response.getStatusLine().getStatusCode() == HttpStatus.BAD_REQUEST.value()) {
			String message = getMessageFromResponseEntity(response.getEntity(), "Not valid acces token", REVOKE_TOKEN_PREFIX);
			logger.warn("{}{}", REFRESH_TOKEN_PREFIX, message);
			throw new UnauthorizedException(message);
		} else if(response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {			
			String message = getMessageFromResponseEntity(response.getEntity(), "Impossible refresh token", REVOKE_TOKEN_PREFIX);
			logger.warn("{}{}", REFRESH_TOKEN_PREFIX, message);
			throw new IllegalArgumentException(message);
		}
		
		String token = EntityUtils.toString(response.getEntity());
		logger.debug("{}New token from request: {}", REFRESH_TOKEN_PREFIX, token);
				
	    return buildTokenFromString(token, REFRESH_TOKEN_PREFIX);
	}
	
	/**
	 * Revoke an OAuth2 access token by calling the "low-level" OAuth2 endpoint.
	 * 
	 * @param token
	 * @return
	 * @throws Exception
	 */
	public boolean revokeAccessToken(String token) throws Exception{
		logger.info("{}Init revoke token...", REVOKE_TOKEN_PREFIX);
		
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("token", token));
		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
		entity.setContentType("application/x-www-form-urlencoded");
		
		logger.debug("{}Revoke Token request to: {}", REVOKE_TOKEN_PREFIX, urlOAuth2RevokeToken);
		HttpPost postRequest = HttpUtils.buildPost(URI.create(urlOAuth2RevokeToken), null, entity);
		
		postRequest.setHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, oAuth2ServerAuthorizationHeader));
		
		logger.info("{}Sending request...", REVOKE_TOKEN_PREFIX);
		CloseableHttpResponse response = HttpUtils.send(postRequest, true);
		logger.debug("{}Request result: {}", REVOKE_TOKEN_PREFIX, response.getStatusLine().getStatusCode());
		
		if(response.getStatusLine().getStatusCode() == HttpStatus.BAD_REQUEST.value()) {
			String message = getMessageFromResponseEntity(response.getEntity(), "Not valid acces token", REVOKE_TOKEN_PREFIX);
			logger.warn("{}{}", REVOKE_TOKEN_PREFIX, message);
			throw new UnauthorizedException(message);
		} else if(response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {			
			String message = getMessageFromResponseEntity(response.getEntity(), "Impossible revoke token", REVOKE_TOKEN_PREFIX);
			logger.warn("{}{}", REVOKE_TOKEN_PREFIX, message);
			throw new IllegalArgumentException(message);
		}

		boolean revoked = HttpStatus.OK.value() == response.getStatusLine().getStatusCode();
		
		logger.info("{}Result: {}", REVOKE_TOKEN_PREFIX, revoked);
		return revoked;
	}
	
	/**
	 * This Method uses the OAuth endpoint to validate an access Token.<br>
	 * If the endpoint returns a 400 error an {@link UnauthorizedException} will be throw.<br>
	 * If the endpoint returns an status different than 200 an {@link IllegalArgumentException} will be throw.
	 * 
	 * @param accessToken
	 * @return net.fluance.app.data.model.identity.User corresponding to the access token
	 * @throws Exception
	 */
	public User validateAccessTokent(String accessToken) throws Exception{
		logger.info("{}Init validate token... ", VALIDATE_TOKEN_PREFIX);
		logger.debug("{}Validating the Token: {}", VALIDATE_TOKEN_PREFIX, accessToken);
		
		logger.debug("{}Sending validation request to: {}", VALIDATE_TOKEN_PREFIX, urlOAuth2ValidateToken);
		
		logger.info("{}Sending request...", VALIDATE_TOKEN_PREFIX);
		CloseableHttpResponse response = sendRequest(urlOAuth2ValidateToken+"?token=" + accessToken, oAuth2ServerAuthorizationHeader);
		logger.info("{}Request result: {}", VALIDATE_TOKEN_PREFIX, response.getStatusLine().getStatusCode());
		
		String message = "";		
		if(response.getStatusLine().getStatusCode() == HttpStatus.BAD_REQUEST.value()) {
			message = getMessageFromResponseEntity(response.getEntity(), "Not valid acces token", VALIDATE_TOKEN_PREFIX);
			logger.warn("{}{}", VALIDATE_TOKEN_PREFIX, message);
			throw new UnauthorizedException(message);
		} else if(response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
			message = getMessageFromResponseEntity(response.getEntity(), "Impossible to validate token", VALIDATE_TOKEN_PREFIX);
			logger.warn("{}{}", VALIDATE_TOKEN_PREFIX, message);
			throw new IllegalArgumentException(message);
		}
		
		String entity = "";
		ObjectMapper mapper = new ObjectMapper();
		if(response.getEntity() != null) {
			entity = EntityUtils.toString(response.getEntity());
		}		
		logger.debug("{}Response from request: {}", VALIDATE_TOKEN_PREFIX, entity);
	    JsonNode json = mapper.readTree(entity);
	    
	    return newUserInstance(accessToken, json, VALIDATE_TOKEN_PREFIX);
	}
	
	/**
	 * Get the token with call to the OAuth endpoint defined at the property oauth2.service.url.getToken 
	 * 
	 * @param assertion
	 * @param assertionType
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws HttpException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws KeyManagementException
	 * @throws URISyntaxException
	 */
	private String getAuth2SrvToken(String assertion, AssertionType assertionType, String logPrefix) throws UnsupportedEncodingException, HttpException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, URISyntaxException {
		logger.info("{}Getting token from auth server...", logPrefix);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair(assertionType.getKeyOAuth2(), assertion));
		
		String grantType = assertionType.getAssertionName().toLowerCase();
		
		params.add(new BasicNameValuePair(GRANT_TYPE, grantType));
		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
		
		entity.setContentType("application/x-www-form-urlencoded");
		
		HttpPost postRequest = HttpUtils.buildPost(new URI(urlOAuth2RequestTokent), null, entity);
		
		postRequest.setHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, oAuth2ServerAuthorizationHeader));
		
		logger.debug("{}{url: {}}", logPrefix, urlOAuth2RequestTokent);
		logger.info("{}Sending request...", logPrefix);
		CloseableHttpResponse response = HttpUtils.send(postRequest, true);
		logger.info("{}Request result: {}", logPrefix, response.getStatusLine().getStatusCode());
		
		String message = "";		
		if(response.getStatusLine().getStatusCode() == HttpStatus.BAD_REQUEST.value()) {
			message = getMessageFromResponseEntity(response.getEntity(), "Bad request to auth server", VALIDATE_TOKEN_PREFIX);
			logger.warn("{}{}", logPrefix, message);
			throw new UnauthorizedException(message);
		} else if(response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
			message = getMessageFromResponseEntity(response.getEntity(), "Internal problem in the auth server", VALIDATE_TOKEN_PREFIX);
			logger.warn("{}{}", logPrefix, message);
			throw new IllegalArgumentException(message);
		}
		
		logger.info("{}Token getted from server", logPrefix);
		
		return EntityUtils.toString(response.getEntity());
	}
	
	/**
	 * Return and instance of {@link OAuth2AccessToken} with the given JSON representation of the rerun object.
	 * 
	 * @param oAuth2AccessTokenString
	 * @param logPrefix, prefix to concatenate to the logging 
	 * @return
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private OAuth2AccessToken buildTokenFromString(String oAuth2AccessTokenString, String logPrefix) throws JsonParseException, IOException {
		logPrefix = logPrefix + BUILD_TOKEN_PREFIX;
		
		logger.info("{}Checking JSON Compatibility", logPrefix);
		logger.debug( "{}{token:{}, class: OAuth2AccessToken.class}", logPrefix, oAuth2AccessTokenString);

		if (JsonUtils.checkJsonCompatibility(oAuth2AccessTokenString, OAuth2AccessToken.class)){
			logger.info("{}Passed JSON Compatibility", logPrefix);
			OAuth2AccessToken oauth2accessToken = new ObjectMapper().readValue(oAuth2AccessTokenString, OAuth2AccessToken.class);
			logger.info("{}Returning OAuth2AccessToken instance", logPrefix);
			return oauth2accessToken;
		} else{

			logger.debug("{}Not passed JSON Compatibility", logPrefix);
			
			logger.debug("{}Parsing to DefaultOAuth2AccessToken object", logPrefix);
			DefaultOAuth2AccessToken defaultOAuth2Token = new ObjectMapper().readValue(oAuth2AccessTokenString, DefaultOAuth2AccessToken.class);
			
			if(defaultOAuth2Token !=null && defaultOAuth2Token.getValue() != null){
				logger.debug("{}Parsing to DefaultOAuth2AccessToken object successful", logPrefix);
				
				OAuth2AccessToken oauth2accessToken = parseToOAuth2AccessToken(defaultOAuth2Token);
				
				logger.info("{}Creating and returning OAuth2AccessToken instance", logPrefix);
				return oauth2accessToken;
			} else {
				logger.error("{}Parsing to DefaultOAuth2AccessToken object fail", logPrefix);
				throw new UnauthorizedException("Assertion Not valid.");
			}
		}
	}
	
	/**
	 * Create an instance of {@link User} using the data from the JSON object.
	 * It calls other methods to extract specific data  
	 * 
	 * @param accessToken
	 * @param json JSON object with the data of the access token
	 * @return
	 */
	private User newUserInstance(String accessToken, JsonNode json, String logPrefix) {
		if(json.get("user_name") != null){
	    	String userId = json.get("user_name").asText();
	    	String[] userIdSplitted = userId.split("/");
	    	String domain = userIdSplitted.length > 1 ? userIdSplitted[0] : defaultDomain;
	    	String username = userIdSplitted.length > 1 ? userIdSplitted[1] : userIdSplitted[0];
	    	Long pid = null;
	    	if (userIdSplitted.length > 2) {
	    		pid = Long.valueOf(userIdSplitted[2]);
	    	}
	    	User user = new User(username, domain, accessToken, pid);
	    	String evitaUserName = "";
	    	if (userIdSplitted.length > 3) {
	    		evitaUserName = userIdSplitted[3];
	    		user.setEvitaUserName(evitaUserName);
	    	}	    		    	
	    	logger.debug("{}user{User_name: " + user.getUsername() + ", domain: " + user.getDomain() +"}", logPrefix);
	    	
	    	logger.info("{}Access Token is Valid", logPrefix);
	    	
	    	setThirdPartyUser(user, json, logPrefix);
	    	
	    	return user;
	    } else {
	    	logger.warn("{}Cannot validate AccessToken: {}", logPrefix, accessToken);
	    	logger.warn("{}Oauth2 Server response: {}", logPrefix, json.asText());
	    	return null;
	    }
	}
	
	/**
	 * Set the thirdPartyUser attribute to the given {@link User} only if on the JSON object the username is present.
	 * 
	 * @param user
	 * @param json
	 * @param logPrefix
	 */
	private void setThirdPartyUser(User user, JsonNode json, String logPrefix) {
		String thirdPartyUsername = null;
    	String thirdPartyFirstName = null;
    	String thirdPartyLastName = null;
    	String thirdPartyEmail = null;
    	
    	if(json.get(JWTValues.ACTUAL_USERNAME) != null) {
    		thirdPartyUsername = json.get(JWTValues.ACTUAL_USERNAME).asText();
    	}
    	
    	if(json.get(JWTValues.ACTUAL_FIRSTNAME) != null) {
    		thirdPartyFirstName = json.get(JWTValues.ACTUAL_FIRSTNAME).asText();
    	}
    	
    	if(json.get(JWTValues.ACTUAL_LASTNAME) != null) {
    		thirdPartyLastName = json.get(JWTValues.ACTUAL_LASTNAME).asText();
    	}
    	
    	if(json.get(JWTValues.ACTUAL_EMAIL) != null) {
    		thirdPartyEmail = json.get(JWTValues.ACTUAL_EMAIL).asText();
    	}
    	
    	if(StringUtils.isNotEmpty(thirdPartyUsername)) {
    		ThirdPartyUserReference thirdPartyUser = new ThirdPartyUserReference(); 
    		thirdPartyUser.setActualUserName(thirdPartyUsername);
    		thirdPartyUser.setActualFirstName(thirdPartyFirstName);
    		thirdPartyUser.setActualLastName(thirdPartyLastName);
    		thirdPartyUser.setActualEmail(thirdPartyEmail);
    		
    		user.setThirdPartyUser(thirdPartyUser);
    		logger.info("{}Added third party user", logPrefix);
    	}
	}
	
	/**
	 * Manages the content of the response from the auth server in order to get the message or the error_description
	 * 
	 * @param entity
	 * @param defaultMessage
	 * @param logPrefix
	 * @return
	 */
	private String getMessageFromResponseEntity(HttpEntity entity, String defaultMessage, String logPrefix) {
		try {		
			String responseEntity = "";
			ObjectMapper mapper = new ObjectMapper();
			if(entity != null) {
				responseEntity = EntityUtils.toString(entity);
				
				if(responseEntity.length() > 0) {
					logger.warn("{}{}", logPrefix, responseEntity);
					JsonNode json = mapper.readTree(responseEntity);
					if(json.get("error_description") != null) {
						defaultMessage = json.get("error_description").asText();
					} else if(json.get("message") != null) {
						defaultMessage = json.get("message").asText();
					} else {
						logger.warn("{}{}", logPrefix, "Respose don't contain am expected value, default error message set");
					}
				} else {
					logger.warn("{}{}", logPrefix, "Response is empty, default error message set");
				}
			} else {
				logger.warn("{}{}", logPrefix, "Respose is null, default error message set");
			}
		} catch (ParseException exception) {
			logger.warn("{}{}", logPrefix, "Response is not a correct JSON, default error message set");
			logger.warn("{}{}", logPrefix, exception.getMessage());
		} catch (IOException exception) {
			logger.warn("{}{}", logPrefix, "Response is not a JSON default, error message set");
			logger.warn("{}{}", logPrefix, exception.getMessage());
		} catch (Exception exception) {
			logger.warn("{}{}", logPrefix, "Errors parsing response, default message set");
			logger.warn("{}{}", logPrefix, exception.getMessage());
		}
		
		return defaultMessage;
	}
	
	/**
	 * Send a real http request using the complete URL and the access token
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
	protected CloseableHttpResponse sendRequest(String fullUri, String authorizationHeader) throws URISyntaxException,
			KeyManagementException, NoSuchAlgorithmException, KeyStoreException, HttpException, IOException {
		URI uri = new URI(fullUri);
		HttpGet get = HttpUtils.buildGet(uri, null);
		get.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
		CloseableHttpResponse response = HttpUtils.sendGet(get, true);
		return response;
	}
}
