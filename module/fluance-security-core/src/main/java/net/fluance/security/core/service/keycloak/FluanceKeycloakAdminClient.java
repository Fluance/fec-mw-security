package net.fluance.security.core.service.keycloak;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.fluance.security.core.support.exception.NotFoundException;

@Service
public class FluanceKeycloakAdminClient {
	private static Logger LOGGER = LogManager.getLogger(FluanceKeycloakAdminClient.class);
	
	private static final String SEARCH_BY_EMAIL="[search][byEmail]";
	private static final String SEARCH_BY_USERNAME="[search][username]";
	private static final String SEARCH_BY_FIRSTNAME="[search][firstname]";
	private static final String SEARCH_BY_LASTNAME="[search][lastname]";
	private static final String KEYCLOACK_CONNECTION="[keycloackConection]";
	private static final String UPDATE_USER="[updateUser]";
	private static final String LOGOUT_USER="[logoutUser]";
	private static final String SET_REQUIRED_ACTION="[setRequiredAction]";
	private static final String REMOVE_USER_SESSION="[removeUserSession]";

	@Value("${keycloak.url}")
	private String keycloakUrl;
	@Value("${keycloak.client.token.clientid}")
	private String clientId;
	@Value("${keycloak.client.token.username}")
	private String username;
	@Value("${keycloak.client.token.password}")
	private String password;
	@Value("${keycloak.client.token.clientsecret}")
	private String clientSecret;
	@Value("${keycloak.client.token.granttype}")
	private String grantType;
	@Value("${keycloak.client.fluanceEhealthCockpit.realmName}")
	private String REALM;
	
	private final static String LOGIN_REALM = "master";
	private final static String MINIMUM_CHARACTERS_OR_DIGITS_REGEX = "[a-zA-Z0-9]{3,}";
	
	//Original default values
	private Integer defaultResultLimit = 50;
	private Integer defaultResultOffset = 0;
	
	/**
	 * Get all users which match with the email
	 * 
	 * @param email an email address
	 * @return A {@link List} of {@link UserRepresentation}
	 */
	public List<UserRepresentation> searchByEmail(String email, Boolean isEmailMustMatch, Integer limit, Integer offset) {
		LOGGER.info(SEARCH_BY_EMAIL+"Init search...");
		
		if (!isEmailMustMatch) {
			LOGGER.info(SEARCH_BY_EMAIL+"Checking Email...");			
		}
		
		LOGGER.debug(SEARCH_BY_EMAIL+"Email= "+email);	
		return search(null, email, null, null, isEmailMustMatch, limit, offset, SEARCH_BY_EMAIL);
	}

	/**
	 * Get all users which match with the username
	 * 
	 * @param username an keycloak username
	 * @return A {@link List} of {@link UserRepresentation}
	 */
	public List<UserRepresentation> searchByUsername(String username, Boolean isUsernameMustMatch, Integer limit, Integer offset) {
		LOGGER.info(SEARCH_BY_USERNAME+"Init search...");
		LOGGER.info(SEARCH_BY_USERNAME+"Checking username...");
		
		LOGGER.debug(SEARCH_BY_USERNAME+"Username= "+username);
		return search(username, null, null, null, isUsernameMustMatch, limit, offset, SEARCH_BY_USERNAME);
	}
	
	public List<UserRepresentation> searchByFirstName(String firstName, Boolean isUsernameMustMatch, Integer limit, Integer offset) {
		LOGGER.info(SEARCH_BY_FIRSTNAME+"Init search...");
		LOGGER.info(SEARCH_BY_FIRSTNAME+"Firstname= "+firstName);
		return search(null, null, firstName, null, isUsernameMustMatch, limit, offset, SEARCH_BY_FIRSTNAME);
	}
	
	public List<UserRepresentation> searchByLastName(String lastName, Boolean isUsernameMustMatch, Integer limit, Integer offset) {
		LOGGER.info(SEARCH_BY_LASTNAME+"Init search...");
		LOGGER.debug(SEARCH_BY_LASTNAME+"Lastname= "+lastName);
		return search(null, null, null, lastName, isUsernameMustMatch, limit, offset, SEARCH_BY_LASTNAME);
	}

	/**
	 * Gets the {@link UserRepresentation} which the username and email match with the arguments.<br />
	 * If <b>isEmailOrUsernameMustMatch</b> is <b>TRUE</b>, the parameters must <b>completely match</b> the values of the retrieved user {@link UserRepresentation}. Else the parameters must be contained in <b>part of the values</b> of the recovered {@link UserRepresentation}<br/>
	 * @param username
	 * @param email
	 * @param isEmailOrUsernameMustMatch
	 * @param complementary
	 * @param limit
	 * @param offset
	 * @return
	 */
	private List<UserRepresentation> search(String username, String email, String firstName, String lastName, Boolean isEmailOrUsernameMustMatch, Integer limit, Integer offset, final String logPrefix) {		
		Keycloak keycloak = null;
		
		try {
			keycloak = getKeycloakConnection();
			LOGGER.info(logPrefix+"Connection to Keycloak initialized");
			
			UsersResource usersResource = getUserResource(keycloak, logPrefix);
			LOGGER.debug(logPrefix+"UsersResource ready");
			
			LOGGER.info(logPrefix+"Searching...");
			LOGGER.info("{}{username: {}, firstName: {}, lastName: {}, email: {} }", logPrefix, username, firstName, lastName, email);
			
			limit = (limit == null) ? defaultResultLimit : limit;
			offset = (offset == null) ? defaultResultOffset : offset;
			Integer firstResult = offset * limit;
			
			List<UserRepresentation> searchResult = usersResource.search(username, firstName, lastName, email, firstResult, limit);
			
			if (!isEmailOrUsernameMustMatch) {
				LOGGER.info(logPrefix+"Returning result");
				LOGGER.info(logPrefix+"{usersFound: " + searchResult.size() + "}");
				return searchResult;
			} else {
				LOGGER.info(logPrefix+"Email or Username must match");
				return getListOfMatchingEmailAndUsernameUserPresentations(searchResult, username, email, firstName, lastName, logPrefix);
			}			
		}finally {
			this.closeKeycloakConnection(keycloak);
		}
	}

	/**
	 * Check the username or the email of the users contained in the {@link List} of {@link UserRepresentation}. One of must be null. 
	 * @param users
	 * @param username
	 * @param email
	 * @param complementary
	 * @return
	 */
	private List<UserRepresentation> getListOfMatchingEmailAndUsernameUserPresentations(List<UserRepresentation> users,
			String username, String email, String firstName, String lastName, final String logPrefix) {
		List<UserRepresentation> matchingUsers = new ArrayList<>();
		for (UserRepresentation userRepresentation : users) {
			if (!StringUtils.isEmpty(email)) {
				if (userRepresentation.getEmail().equals(email)) {
					matchingUsers.add(userRepresentation);
				}
			} else if (!StringUtils.isEmpty(username)) {
				if (userRepresentation.getUsername().equals(username)) {
					matchingUsers.add(userRepresentation);
				}
			} else if (!StringUtils.isEmpty(firstName)) {
				if (userRepresentation.getFirstName().equals(firstName)) {
					matchingUsers.add(userRepresentation);
				}
			} else if (!StringUtils.isEmpty(lastName)) {
				if (userRepresentation.getLastName().equals(lastName)) {
					matchingUsers.add(userRepresentation);
				}
			}			
		}
		
		LOGGER.info(logPrefix+"Returning result");
		LOGGER.info(logPrefix+"{usersFound: " + matchingUsers.size() + "}");
		return matchingUsers;
	}

	private UsersResource getUserResource(Keycloak keycloak, final String logPrefix) {
		LOGGER.info(logPrefix+"Getting users resources");
		return keycloak.realm(REALM).users();
	}

	private Keycloak getKeycloakConnection() {
		LOGGER.info(KEYCLOACK_CONNECTION + "Getting Keycloak connection...");
		LOGGER.debug(KEYCLOACK_CONNECTION + "{realm: " + REALM + ", keycloakUrl: " + keycloakUrl + ", clientID: " + clientId + ", grantType: " + grantType + "}");
		return KeycloakBuilder
				.builder()
				.serverUrl(keycloakUrl)
				.grantType(grantType)
				.realm(REALM)
				.clientId(clientId)
				.clientSecret(clientSecret)
				.resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
				.build();
	}
	
	private void closeKeycloakConnection(Keycloak keycloak){
		if(keycloak != null) {
			LOGGER.info(KEYCLOACK_CONNECTION + "Clossing connection");
			keycloak.close();
		}
	}

	/**
	 * Sets the a User Action and sends an email to the user
	 * @param userId
	 * @return
	 * @throws NotFoundException
	 */
	public boolean setRequiredAction(UserRepresentation userRepresentation, String action) throws NotFoundException {
		LOGGER.info(SET_REQUIRED_ACTION+"Init...");
		LOGGER.debug(SET_REQUIRED_ACTION+"User id: " + userRepresentation.getId());
		LOGGER.debug(SET_REQUIRED_ACTION+"Action: " + action);
		Keycloak keycloak = null;
		try {
			keycloak = this.getKeycloakConnection();
			UsersResource usersResource = getUserResource(keycloak, SET_REQUIRED_ACTION);
			UserResource user = usersResource.get(userRepresentation.getId());
			if (user == null) {
				throw new NotFoundException("User not found");
			}
			userRepresentation.getRequiredActions().add(action);
			this.updateUser(userRepresentation);
			List<String> actions = new ArrayList<>();
			actions.add(action);
			user.executeActionsEmail(actions);
			LOGGER.info(SET_REQUIRED_ACTION+"Required action setted");
			return true;
		} catch (javax.ws.rs.NotFoundException e) {
			throw new NotFoundException("The Realm is unreachable");
		} finally {
			closeKeycloakConnection(keycloak);
		}
	}
	
	/**
	 * Update a KeyCloak user with the info contained in the User Representation Argument
	 * @param userRepresentation
	 * @return
	 * @throws NotFoundException
	 */
	public boolean updateUser(UserRepresentation userRepresentation) throws NotFoundException {
		LOGGER.info(UPDATE_USER+"Init...");
		LOGGER.debug(UPDATE_USER+"User id: " + userRepresentation.getId());
		Keycloak keycloak = null;
		try {
			keycloak = this.getKeycloakConnection();
			
			UsersResource usersResource = getUserResource(keycloak, UPDATE_USER);		
			
			UserResource user = usersResource.get(userRepresentation.getId());
			if(user == null) {
				throw new NotFoundException("User not found");
			}
			user.update(userRepresentation);
			LOGGER.info(UPDATE_USER+"Updated");
			return true;
		} catch(javax.ws.rs.NotFoundException e){
			throw new NotFoundException("The Realm is unreachable");
		}
		finally {
			closeKeycloakConnection(keycloak);
		}
	}

	/**
	 * Close all the active sessions of a KeyCloak User Id
	 * @param userId
	 * @return
	 * @throws NotFoundException
	 */
	public boolean userLogout(String userId) throws NotFoundException {
		LOGGER.info(LOGOUT_USER+"Init...");
		LOGGER.debug(LOGOUT_USER+"User id: " + userId);
		Keycloak keycloak = null;
		try {
			keycloak = this.getKeycloakConnection();
			UsersResource usersResource = getUserResource(keycloak, LOGOUT_USER);
			UserResource user = usersResource.get(userId);
			if(user == null) {
				throw new NotFoundException("User not found");
			}
			user.logout();
			LOGGER.info(LOGOUT_USER+"Loged out");
			return true;
		} catch(javax.ws.rs.NotFoundException e){
			throw new NotFoundException("The Realm is unreachable");
		}
		finally {
			closeKeycloakConnection(keycloak);
		}
	}

	public boolean removeUserSession(String sessionId) throws NotFoundException {
		LOGGER.info(REMOVE_USER_SESSION+"Init...");
		LOGGER.debug(REMOVE_USER_SESSION+"Session Id: " + sessionId);
		Keycloak keycloak = null;
		try {
			keycloak = this.getKeycloakConnection();
			keycloak.realm(REALM).deleteSession(sessionId);
			
			LOGGER.info(REMOVE_USER_SESSION+"Session removed");
			return true;
		} catch(javax.ws.rs.NotFoundException e){
			throw new NotFoundException("The Realm is unreachable");
		}
		finally {
			closeKeycloakConnection(keycloak);
		}		
	}
}
