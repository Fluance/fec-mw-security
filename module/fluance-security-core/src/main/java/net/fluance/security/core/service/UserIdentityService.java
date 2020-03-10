package net.fluance.security.core.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.fluance.app.data.model.identity.Address;
import net.fluance.app.data.model.identity.User;
import net.fluance.app.data.model.identity.UserClaims;
import net.fluance.app.data.model.identity.UserReference;
import net.fluance.commons.json.JsonUtils;
import net.fluance.commons.net.HttpUtils;
import net.fluance.security.core.model.jdbc.UserInfo;
import net.fluance.security.core.support.exception.NotFoundException;

@Service
public class UserIdentityService {

	@Autowired
	private KeycloakUserService keycloakUserService;

	@Value("${oauth2.service.url.getUserInfos}")
	private String urlUserInfoRequest;
	@Value("${oauth2.service.url.AuthorizationHeader}")
	private String oAuth2ServerAuthorizationHeader;

	private Logger logger = LogManager.getLogger();

	@SuppressWarnings({"incomplete-switch"})
	public User getUserClaims(User user) throws Exception {
		return keycloakUserService.getUserInfos(user);
	}
	/**
	 * 
	 * @param user
	 * @return
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws HttpException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public User getUserWithInfos(User user)
	{
		UserInfo userInfos = null;
		try {
			userInfos = requestUserInfos(user);
		} catch (KeyManagementException e) {
			logger.warn("Key management exception occured while trying getUserWithInfos with user '" + user + "'");
		} catch (NoSuchAlgorithmException e) {
			logger.warn("No such algorithm exception occured while trying getUserWithInfos with user '" + user + "'");
		} catch (KeyStoreException e) {
			logger.warn("Key store exception occured while trying getUserWithInfos with user '" + user + "'");
		} catch (HttpException e) {
			logger.warn("Http exception occured while trying getUserWithInfos with user '" + user + "'");
		} catch (IOException e) {
			logger.warn("IO exception occured while trying getUserWithInfos with user '" + user + "'");
		} catch (URISyntaxException e) {
			logger.warn("URI syntax exception occured while trying getUserWithInfos with user '" + user + "'");
		}
		
		try {
			if (userInfos != null && JsonUtils.checkJsonCompatibility(userInfos.getUserInfo(), User.class)){
				try {
					user = setUserInfos(user, userInfos);
				} catch (JsonParseException e) {
					logger.warn("JSON parse exception occured while trying setUserInfos with userInfos '" + userInfos.getUserInfo() + "'");
				} catch (JsonMappingException e) {
					logger.warn("JSON mapping exception occured while trying setUserInfos with userInfos '" + userInfos.getUserInfo() + "'");
				} catch (IOException e) {
					logger.warn("IO exception occured while trying setUserInfos with userInfos '" + userInfos.getUserInfo() + "'");
				}
			}
			else{
				try {
					userInfos = this.getUserInfosFromIdp(
							user.getUsername(), 
							user.getDomain()
					);
					logger.info("User Info loaded from IDP : " + userInfos.getUserInfo());
					user = setUserInfos(user, userInfos);
					return user;
				} catch (Exception e) {
					logger.warn("Unable to initialize the user infos of : " + user.getDomain() + "/" + user.getUsername());
					logger.error("", e);
				}
				return user;
			}
		} catch (IOException e) {
			logger.warn("IO exception occured while trying JsonUtils.checkJsonCompatibility with userInfos '" + userInfos.getUserInfo() + "'");
		}
		
		return user;
	}
	
	private User setUserInfos(User user, UserInfo userInfos) throws IOException, JsonParseException, JsonMappingException {
		logger.info("User Info loaded \"Authenticated User\" : " + userInfos.getUserInfo());
		User userWithInfo = new ObjectMapper().readValue(userInfos.getUserInfo(), User.class);
		logger.debug("====> DEBUG MANAGER : " + userWithInfo.getManagerUsername());
		userWithInfo.setUserIdentityFromUser(user);
		
		String managerUsername = keycloakUserService.cleanUsername(userWithInfo.getManagerUsername());
		if(managerUsername != null ){
			try {
				UserReference manager = keycloakUserService.getUserReference(managerUsername);
				userWithInfo.setManager(manager);
			} catch (Exception e) {
				logger.warn("No Manager found with the username : " + userWithInfo.getManagerUsername(), e);
			}
		}
		
		try {
			UserRepresentation userRepresentation = keycloakUserService.getUserByUsername(user.getUsername());
			if (userRepresentation.getAttributes() != null && userRepresentation.getAttributes().containsKey("department")){
				userWithInfo.setDepartment(userRepresentation.getAttributes().get("department").get(0));
			}
		} catch (NotFoundException e) {
			logger.warn("No User information found on Keycloak by username : " + userWithInfo.getManagerUsername(), e);
		}
		
		return userWithInfo;
	}
	
	/**
	 * 
	 * @param user
	 * @return
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws HttpException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private UserInfo requestUserInfos(User user) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, HttpException, IOException, URISyntaxException {
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);

		params.add(new BasicNameValuePair("username", user.getUsername()));
		params.add(new BasicNameValuePair("domain", user.getDomain()));

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
		entity.setContentType("application/json");
		Header authorizationHeader = new BasicHeader("Authorization", oAuth2ServerAuthorizationHeader);
		HttpGet getRequest = HttpUtils.buildGet(new URI(urlUserInfoRequest + "?username=" + user.getUsername() + "&domain=" + user.getDomain()), Arrays.asList(authorizationHeader));
		CloseableHttpResponse response = HttpUtils.send(getRequest, true);

		if (response.getStatusLine().getStatusCode() == 200) {
			String test = EntityUtils.toString(response.getEntity(), "UTF-8");
			try{
				return new ObjectMapper().readValue(test, UserInfo.class);
			} catch (JsonParseException e){
				return null;
			}
		} else {
			return null;
		}
	}
	/**
	 * 
	 * @param userClaims
	 * @return
	 */
	private Address buildAddressFromClaims(Map<UserClaims, Object> userClaims) {
		Address address = null;

		String addressLine = (String) ((userClaims.containsKey(UserClaims.ADDRESS_LINE)) ? userClaims.get(UserClaims.ADDRESS_LINE) : null);
		String postalCode = (String) ((userClaims.containsKey(UserClaims.POSTAL_CODE)) ? userClaims.get(UserClaims.POSTAL_CODE) : null);
		String locality = (String) ((userClaims.containsKey(UserClaims.LOCALITY)) ? userClaims.get(UserClaims.LOCALITY) : null);
		String state = (String) ((userClaims.containsKey(UserClaims.STATE_OR_PROVINCE)) ? userClaims.get(UserClaims.STATE_OR_PROVINCE) : null);
		String country = (String) ((userClaims.containsKey(UserClaims.COUNTRY)) ? userClaims.get(UserClaims.COUNTRY) : null);

		if(addressLine!=null || postalCode!=null || locality!=null || state!=null || country!=null) {
			address = new Address(addressLine, postalCode, locality, state, country);
		}
		return address;
	}
	
	/**
	 * 
	 * @param username
	 * @param domain
	 * @return
	 * @throws Exception
	 * @throws JsonProcessingException
	 */
	public UserInfo getUserInfosFromIdp(String userName, String domain) throws Exception, JsonProcessingException {

		User user = new User(userName, domain, null, null);
		user = this.getUserClaims(user);
		String infos = user.toJsonString();
		String id = domain + "/" + userName;
		return new UserInfo(id, infos);
	}
}
