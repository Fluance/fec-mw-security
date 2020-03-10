package net.fluance.security.core.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.fluance.app.data.model.identity.Email;
import net.fluance.app.data.model.identity.User;
import net.fluance.app.data.model.identity.UserClaims;
import net.fluance.app.data.model.identity.UserReference;
import net.fluance.app.security.service.IUserService;
import net.fluance.security.core.model.MyTeam;
import net.fluance.security.core.service.keycloak.FluanceKeycloakAdminClient;
import net.fluance.security.core.support.exception.NotFoundException;

@Service(value = "keycloakUserService")
public class KeycloakUserService implements IUserService {

	private static final Logger LOGGER = LogManager.getLogger(KeycloakUserService.class);
	private static final String GET_BY_EMAIL = "[UserByEmail]";
	private static final String GET_BY_USERNAME = "[UserByUsername]";
	
	public static final String USER_TYPE_KEY = "userType";
	private final static String UPDATE_PASSWORD = "UPDATE_PASSWORD";
	
	// User Representation Attributes
	private static final String MANAGER_ATTRIBUTE = "manager";
	private static final String DIRECT_REPORTS_ATTRIBUTE = "directReports";
	
	// Active Directory Fields
	private static final String COMMON_NAME_AD = "CN";
	private static final String SEPARATOR_DESCRIPTION_AD = ",";
	private static final String FIELD_VALUE_AD = "=";
	
	@Autowired
	FluanceKeycloakAdminClient keycloakService;
	
	@Override
	public boolean isExistingUser(String username, String domain) throws Exception {
		try {
			UserRepresentation user = this.getUserByUsername(username);
			return user != null;
		} catch(javax.ws.rs.NotFoundException e){
			throw new NotFoundException("The Realm is unreachable");
		}
	}

	/**
	 * Returns a {@link UserRepresentation} if the email passed as arguments matches to some user. Is the username is empty or null, only checks the email 
	 * @param email	the email to be checked
	 * @return	UserRepresentation
	 * @throws Exception
	 * @throws NotFoundException If the email is not found
	 */
	public UserRepresentation getUserByEmail(String email, String userName) throws Exception {

		return getUserByEmail(email, userName, "");
	}
	
	/**
	 * Returns a list of {@link UserRepresentation} if the email passed as arguments matches to some user. Is the username is empty or null, only checks the email 
	 * @param email	the email to be checked
	 * @return	UserRepresentation
	 * @throws Exception
	 * @throws NotFoundException If the email is not found
	 */
	public UserRepresentation getUsersByEmail(String email, String userName) throws Exception {

		return getUserByEmail(email, userName, "");
	}
	
	private UserRepresentation getUserByEmail(String email, String username, String logPrefix) throws Exception {
		logPrefix = logPrefix + GET_BY_EMAIL;
		try {
			LOGGER.info("{}Init...", logPrefix);
			List<UserRepresentation> usersFiltered = new ArrayList<>();
			
			if(!StringUtils.isEmpty(email)){
				LOGGER.info("{}Searching by email", logPrefix);
				usersFiltered = keycloakService.searchByEmail(email, true, null, null);
			}
			else if(!StringUtils.isEmpty(username)){
				LOGGER.info("{}Searching by username", logPrefix);
				usersFiltered =  keycloakService.searchByUsername(username, true, null, null);
			}
			if(usersFiltered == null || usersFiltered.isEmpty()){
				LOGGER.error("{}No user found", logPrefix);
				throw new NotFoundException("User not found");
			}

			Optional<UserRepresentation> userFound = usersFiltered.stream().filter(user -> user.getEmail().equals(email) || user.getUsername().equals(username)).findFirst();
			
			if(userFound.isPresent()) {
				LOGGER.info("{}User found", logPrefix);
				LOGGER.info("{}{user: " + userFound.get().getUsername() +" }", logPrefix);
				return userFound.get();
			}
			else {
				LOGGER.error("{}No user present", logPrefix);
				throw new NotFoundException("User not found");	
			}
		} catch(javax.ws.rs.NotFoundException e){
			LOGGER.error("{}{}", logPrefix, e.getMessage());
			throw new NotFoundException("The Realm is unreachable");
		}
	}

	
	/**
	 * Get a {@link List} of {@link UserRepresentation} where one of their field values matches partially with the parameters given
	 * @param firstName
	 * @param lastName
	 * @param userNames 
	 * @return
	 * @throws Exception
	 */
	public List<UserRepresentation> byCriteria(String firstName, String lastName, List<String> userNames, Integer limit, Integer offset) throws Exception {
		try {
			List<UserRepresentation> usersFiltered = new ArrayList<>();
			if(!StringUtils.isEmpty(firstName)){
				usersFiltered = keycloakService.searchByFirstName(firstName, false, limit, offset);
			}
			if(!StringUtils.isEmpty(lastName)){
				usersFiltered.addAll(keycloakService.searchByLastName(lastName, false, limit, offset));
			}
			if(!CollectionUtils.isEmpty(userNames)){
				for (String userName : userNames){
					
					List<UserRepresentation> userRepresentation = keycloakService.searchByUsername(userName, false, limit, offset);
					if(!CollectionUtils.isEmpty(userRepresentation)) {
						usersFiltered.addAll(userRepresentation);
					}
				}
			}
			if(usersFiltered == null || usersFiltered.isEmpty()){
				throw new NotFoundException("User not found");
			}
			List<UserRepresentation> usersFilteredWithoutDuplicates = usersFiltered
				.stream()
				.collect(
					Collectors.collectingAndThen(
						Collectors.toCollection(
							() -> new TreeSet<>(Comparator.comparing(UserRepresentation::getId))
						),
						ArrayList<UserRepresentation>::new
					)
				);
			return usersFilteredWithoutDuplicates;			
		} catch(javax.ws.rs.NotFoundException e){
			throw new NotFoundException("The Realm is unreachable");
		}
	}

	/**
	 * Returns a {@link UserRepresentation} which matchs with the username
	 * @param username
	 * @return
	 * @throws Exception
	 * @throws NotFoundException If the username is not found
	 */
	public UserRepresentation getUserByUsername(String userName) throws NotFoundException {

		return getUserByUsername(userName, "");
	}
	
	private UserRepresentation getUserByUsername(String userName, String logPrefix) throws NotFoundException {
		logPrefix = logPrefix + GET_BY_USERNAME;
		UserRepresentation userFound = null;
		try {
			LOGGER.info("{}Init...", logPrefix);
			List<UserRepresentation> usersFiltered = keycloakService.searchByUsername(userName, true, null, null);
			
			if (usersFiltered == null || usersFiltered.isEmpty()) {
				LOGGER.error("{}No user found", logPrefix);
				throw new NotFoundException("User not found");
			}
			usersFiltered = usersFiltered.stream().filter(user -> user.getUsername().equals(userName)).collect(Collectors.toList());
			if(usersFiltered.size() > 0) {				
				userFound = usersFiltered.get(0);
				if(usersFiltered.size()  > 1l) {
					LOGGER.warn("{}More that one user found, returning first", logPrefix);
				}
			}

			if (userFound !=  null) {
				LOGGER.info("{}User found", logPrefix);
				LOGGER.info("{}{user: " + userFound.getUsername() +" }", logPrefix);
				return userFound;
			} else {
				LOGGER.error("{}No user present", logPrefix);
				throw new NotFoundException("User: " + userName + " Not found");
			}
		} catch (javax.ws.rs.NotFoundException e) {
			LOGGER.error("{}{}", logPrefix, e.getMessage());
			throw new NotFoundException("The Realm is unreachable");
		}
	}
	
	/**
	 * Updates the {@link UserRepresentation} given as argument
	 * @param username
	 * @param userType
	 * @return
	 * @throws NotFoundException
	 */
	public Boolean updateUserType(String userName, List<String> userType) throws NotFoundException {
		
		UserRepresentation user = this.getUserByUsername(userName);
		if(user == null){
			throw new NotFoundException("User: " + userName + " Not found");
		} else {
			user.getAttributes().put(USER_TYPE_KEY, userType);
			return keycloakService.updateUser(user);
		}
	}

	@Override
	public boolean isScimProtocolSupported() {
		return false;
	}

	@Override
	public String scimProfile(String username, String domainName) throws Exception {
		return null;
	}

	@Override
	public Map<UserClaims, Object> userClaims(String username, String domain, Map<UserClaims, String> claims) throws Exception {
		return null;
	}
	
	@Override
	public User getUserInfos(User user) {
		try {
			UserRepresentation userRepresentation = this.getUserByUsername(user.getUsername());
			if (userRepresentation != null) {
				user.setLastName(userRepresentation.getLastName());
				user.setFirstName(userRepresentation.getFirstName());
				user.setEmail(userRepresentation.getEmail());
				user.setEmails(Arrays.asList(new Email("", userRepresentation.getEmail())));
				if (userRepresentation.getAttributes() != null && userRepresentation.getAttributes().containsKey("company")){
					user.setCompany(userRepresentation.getAttributes().get("company").get(0));
				}
				if (userRepresentation.getAttributes() != null && userRepresentation.getAttributes().containsKey("title")){
					user.setTitle(userRepresentation.getAttributes().get("title").get(0));
				}
				if (userRepresentation.getAttributes() != null && userRepresentation.getAttributes().containsKey("department")){
					user.setDepartment(userRepresentation.getAttributes().get("department").get(0));
				}
				if (userRepresentation.getAttributes() != null && userRepresentation.getAttributes().containsKey(DIRECT_REPORTS_ATTRIBUTE)){
					user.setIsManager(true);
				}
				if (userRepresentation.getAttributes() != null && userRepresentation.getAttributes().containsKey(MANAGER_ATTRIBUTE)){
					user.setManagerUsername(userRepresentation.getAttributes().get(MANAGER_ATTRIBUTE).get(0));
				}
				
			}
		} catch (NotFoundException e) {
			LOGGER.error("Cannot set userInfos - User not found", e);
		}
		return user;
	}

	@Override
	public Map<UserClaims, Object> userClaims(String username, String domain, Map<UserClaims, String> claims, String encoding) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Reset the password of the username. If the password is empty, a new ramdom is generated.
	 * @param username The complete username
	 * @param newPassword
	 * @return
	 * @throws NotFoundException
	 */
	public Boolean resetPassword(String userName) throws NotFoundException {		

		UserRepresentation user = this.getUserByUsername(userName);
		if(user == null){
			throw new NotFoundException("User: " + userName + " Not found");
		} else {
			return keycloakService.setRequiredAction(user, UPDATE_PASSWORD);
		}
	}
	
	/**
	 * Given an existing username, returns the Profile Photograph 
	 * @param userName
	 * @return
	 * @throws NotFoundException	If the username does not exist or the picture is not available
	 * @throws IOException
	 */
	public byte[] getThumbnailPhoto(String userName) throws NotFoundException, IOException {	
		
		UserRepresentation user = this.getUserByUsername(userName);
		List<String> result = user.getAttributes().get("thumbnailPhoto");
		if (result != null && !StringUtils.isEmpty(result.get(0))) {
			return Base64.getDecoder().decode(new String(result.get(0)).getBytes(StandardCharsets.UTF_8));
		}
		else {
			throw new NotFoundException("Image has not been found for the username");
		}
	}
	
	/**
	 * Removes the session of a User given its KeyCloak Session ID
	 * @param sessionId
	 * @return
	 * @throws Exception
	 */
	public boolean removeUserSession(String sessionId) throws Exception {
		return keycloakService.removeUserSession(sessionId);
	}
	
	/**
	 * Given a KeyCloak userName, returns a {@link MyTeam} instance with the existing data associated to it
	 * @param userName
	 * @return
	 * @throws NotFoundException If the userName does not exist, a 404 NotFoundError is generated
	 */
	public MyTeam getMyTeam(String userName) throws NotFoundException {
		MyTeam myTeam = new MyTeam();
		
		UserRepresentation me = this.getUserByUsername(userName);
		myTeam.setMe(me);
		Map<String, List<String>> attributes = me.getAttributes();
		if(attributes == null || attributes.isEmpty()) {
			return myTeam;
		}
		if(attributes.containsKey(DIRECT_REPORTS_ATTRIBUTE)) {
			List<UserRepresentation> subordinates = getUsers(userName, attributes.get(DIRECT_REPORTS_ATTRIBUTE));
			myTeam.setSubordinates(subordinates);		
		}
		if (attributes.containsKey(MANAGER_ATTRIBUTE)) {
			List<String> managerAttribute = attributes.get(MANAGER_ATTRIBUTE);

			if(managerAttribute != null && !managerAttribute.isEmpty() && !((String) managerAttribute.get(0)).equals(userName)) {
				String managerUserName = cleanUsername(managerAttribute.get(0));
				try{
					LOGGER.info("Getting informations about the manager : " + managerUserName);
					UserRepresentation manager = this.getUserByUsername(managerUserName);
					myTeam.setManager(manager);
					if(manager != null) {
						Map<String, List<String>> managerAttributes = manager.getAttributes();
						if(managerAttributes == null || managerAttributes.isEmpty() || !managerAttributes.containsKey(DIRECT_REPORTS_ATTRIBUTE)){
							return myTeam;
						}
						List<UserRepresentation> colleagues = getUsers(userName, managerAttributes.get(DIRECT_REPORTS_ATTRIBUTE));
						myTeam.setColleagues(colleagues);
					}
				} catch (NotFoundException e){
					LOGGER.warn("Manager of " + userName + " NOT FOUND");
				}
			}
		}
		return myTeam;
	}
	
	/**
	 * Returns a {@link List} of {@link UserRepresentation} with the KeyCloak existing users given in the descrptionAD list excluding the username
	 * @param userName The UserRepresentation not allowed in the response
	 * @param descriptionAD	A list with the description users in the next format inherited from <b>Active Directory</b>: <pre>CN=[\w]+,((OU=[\w]+),*)*,((DC=[\w]+),*)*</pre> as <b>CN=fooCN, OU=fooOU_1, OU=fooOU_2, OU=fooOU_3, OU=fooOU_4, DC=fooDC_1, DC=fooDC_1</b>
	 * @return
	 */
	private List<UserRepresentation> getUsers(String userName, List<String> descriptionAD) {
		if(descriptionAD == null || descriptionAD.isEmpty()){
			return null;
		}

		List<UserRepresentation> users = new ArrayList<>();
		for(String userDescAD : descriptionAD) {
			String nick = cleanUsername(userDescAD);
			if(!StringUtils.isEmpty(nick) && !nick.equals(userName)){
				try {
					users.add(getUserByUsername(nick));
				} catch (NotFoundException e) {
					LOGGER.error("The user " + nick + " does not exist in the KeyCloak Server");
				}
			}
		}
		return users;
	}
	
	@Override
	public UserReference getUserReference(String username) throws NotFoundException {
		UserRepresentation managerRepresentation = this.getUserByUsername(username);
		UserReference manager = new UserReference(username, managerRepresentation.getFirstName(), managerRepresentation.getLastName());
		manager.setEmail(managerRepresentation.getEmail());
		return manager;
	}

	public String cleanUsername(String ldapEntry) {
		LOGGER.info("Cleaning ldapEntry : " + ldapEntry);
		if (ldapEntry == null) {
			return null;
		} else {
			try{
				String[] splitedProperties = Arrays.stream(ldapEntry.split(SEPARATOR_DESCRIPTION_AD)).map(String::trim).toArray(String[]::new);
				Map<String, String> mapProperties = Arrays.asList(splitedProperties).stream().map(str -> str.split(FIELD_VALUE_AD)).collect(Collectors.toMap(propertie -> propertie[0], propertie -> propertie[1], (oldValue, newValue) -> oldValue + SEPARATOR_DESCRIPTION_AD + newValue));
				return mapProperties.get(COMMON_NAME_AD).toLowerCase();
			} catch (Exception e){
				LOGGER.warn("Cannot extract username from Ldap Entry : " + ldapEntry, e);
				return ldapEntry;
			}
		}
	}
}
