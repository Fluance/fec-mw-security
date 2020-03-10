package net.fluance.security.ehprofile.service.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import net.fluance.security.core.model.jpa.Profile;
import net.fluance.security.core.model.jpa.Role;
import net.fluance.security.core.repository.jpa.IProfileRepository;
import net.fluance.security.core.repository.jpa.IRoleRepository;
import net.fluance.security.core.util.ProfileUtils;
import net.fluance.security.ehprofile.service.UserProfileRolesService;
import net.fluance.security.ehprofile.service.UserProfileValidatorService;

@Service
public class UserProfileRolesServiceImpl implements UserProfileRolesService {
	
	private static final Logger LOGGER = LogManager.getLogger(UserProfileRolesServiceImpl.class);
	

	private static final String SET_ROLES_LOG = "[setRoles]";

	@Value("${default.role.name}")
	private String defaultRoleName;
	
	@Autowired
	private IProfileRepository profileRepository;
	@Autowired
	private IRoleRepository roleRepository;
	@Autowired
	UserProfileValidatorService userProfileValidatorService;
	
	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfileRolesService#grantRoles(java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	@Transactional
	public boolean grantRoles(String username, String domainName, List<String> roles) throws Exception {
		if (roles == null || roles.isEmpty()) {
			return true;
		}
		// Ensures the list will be modifiable, because not all List classes support structural modifications (example: Arrays.ArrayList)
		roles = new ArrayList<>(roles);
		
		Profile profile = userProfileValidatorService.validateAndGetProfile(username, domainName, "[grant_roles]");
		
		if (!roles.contains(defaultRoleName) && !ProfileUtils.hasRole(profile, defaultRoleName)) {
			roles.add(defaultRoleName);
		}
		// We grant only the existing r
		List<Role> roleList = profile.getRoles();
		int initialSize = roleList.size();
		for (String roleToAdd : roles) {
			Role role = roleRepository.findByName(roleToAdd);
			if (role != null && !profile.getRoles().contains(role)) {
				roleList.add(role);
			}
		}
		if (roleList == null || roleList.isEmpty() || roleList.size() <= initialSize) {
			return false;
		}
		profile.setRoles(roleList);
		profileRepository.save(profile);
		List<String> addedRoleNames = ProfileUtils.rolesNames(roleList);
		List<String> currentRolesNames = ProfileUtils.rolesNames(profile.getRoles());
		boolean ok = CollectionUtils.isEqualCollection(addedRoleNames, currentRolesNames);
		return ok;
	}

	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfileRolesService#setRoles(java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	@Transactional
	public boolean setRoles(String username, String domainName, List<String> roles) throws Exception {
		return setRoles(username, domainName, roles, "");
	}

	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfileRolesService#revokeRoles(java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	@Transactional
	public boolean revokeRoles(String username, String domainName, List<String> roles) throws Exception {
		if ((roles == null || roles.isEmpty())) {
			return true;
		}
		
		Profile profile = userProfileValidatorService.validateAndGetProfile(username, domainName, "[revokeRoles]");
		
		if (roles != null && roles.contains(defaultRoleName)) {
			roles.remove(defaultRoleName);
		}
		for (String roleToRemove : roles) {
			Role role = roleRepository.findByName(roleToRemove);
			if (role != null && profile.getRoles().contains(role)) {
				profile.getRoles().remove(role);
			}
		}
		profile = profileRepository.save(profile);
		List<String> currentRoles = ProfileUtils.rolesNames(profile.getRoles());
		@SuppressWarnings("unchecked")
		List<String> intersection = new ArrayList<>(org.apache.commons.collections.CollectionUtils.intersection(currentRoles, roles));
		boolean ok = intersection == null || ((intersection != null) && (intersection.isEmpty() || (intersection.contains(defaultRoleName) && intersection.size() == 1)));
		return ok;
	}

	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfileRolesService#findUserRoles(java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional
	public List<String> findUserRoles(String localUserName, String fluanceKey) throws Exception {
		Profile profile = userProfileValidatorService.validateAndGetProfile(localUserName, fluanceKey, "[findUserRoles]");
		return findUserRoles(profile.getId());
	}
	
	// ----------------- Roles utils --------------------	
	/**
	 * Implements the expected functionality to saveRoles, it is overwrite the roles for the profiles
	 * 
	 * @param username
	 * @param domain
	 * @param rolesToAdd
	 * @param logPrefix
	 * @return
	 * @throws Exception
	 */
	private boolean setRoles(String username, String domain, List<String> rolesToAdd, String logPrefix) throws Exception {		
		logPrefix = logPrefix + SET_ROLES_LOG;
		LOGGER.info(logPrefix+"Init set roles...");		
		
		return saveRoles(userProfileValidatorService.validateAndGetProfile(username, domain, logPrefix), rolesToAdd, logPrefix);
	}

	/**
	 * Saves the roles for the given profile
	 * 
	 * @param username
	 * @param domain
	 * @param rolesToAdd
	 * @param logPrefix
	 * @return
	 * @throws Exception
	 */
	@Transactional
	private boolean saveRoles(Profile profile, List<String> rolesToAdd, String logPrefix) throws Exception {
		if (rolesToAdd == null || rolesToAdd.isEmpty()) {
			LOGGER.warn(logPrefix+"No roles set");
			return true;
		}		
		
		profile.getRoles().clear();
		profile = profileRepository.save(profile);
		LOGGER.info(logPrefix+"All roles clear in DB");
		
		List<Role> roles = validateRoles(rolesToAdd, logPrefix);		
		roles = addDefaultRole(roles, logPrefix);
		
		//Gets a profile with the current status of it
		Profile cleanProfile = profileRepository.findByUsernameAndDomainName(profile.getUsername(), profile.getDomainName());
		cleanProfile.setRoles(roles);		
		cleanProfile = profileRepository.save(cleanProfile);
		LOGGER.info(logPrefix+"New roles save in DB");
		
		boolean areCorrect = testSavedRoles(cleanProfile, roles, logPrefix);
		LOGGER.info(logPrefix+"Saved roles are the expected roles: " + areCorrect);
		
		return areCorrect;
	}
	
	/**
	 * Adds the default roles if need
	 * 
	 * @param rolesToAdd
	 * @param logPrefix
	 * @return
	 */
	private List<Role> addDefaultRole(List<Role> roles, String logPrefix){
		// User must always have default role
		Role defaultRole = roleRepository.findByName(defaultRoleName);
		if (defaultRole != null &&
			roles.stream().filter(role -> defaultRole.getName().equals(role.getName())).findAny().orElse(null) == null) {
			
			LOGGER.info(logPrefix+"Default role added");
			LOGGER.debug(logPrefix+"Default role: " + defaultRole);
			roles.add(defaultRole);
		}
		
		return roles;
	}
	
	/**
	 * Validate if all the roles exists, only return the existing roles
	 * 
	 * @param rolesToAdd
	 * @param logPrefix
	 * @return
	 */
	private List<Role> validateRoles(List<String> rolesToAdd, String logPrefix){
		List<Role> roles = new ArrayList<>();
		
		for (String roleToAdd : rolesToAdd) {
			Role role = roleRepository.findByName(roleToAdd);
			if (role != null) {
				roles.add(role);
			}
		}
		
		return roles;
	}
	
	/**
	 * Test that the gicen profile have the same roles that the fiven roles list
	 * 
	 * @param profile
	 * @param roles
	 * @param logPrefix
	 * @return
	 */
	private boolean testSavedRoles(Profile profile, List<Role> roles, String logPrefix) {
		LOGGER.info(logPrefix+"Testing saved roles...");
		List<String> addedRoleNames = ProfileUtils.rolesNames(roles);
		List<String> currentRolesNames = ProfileUtils.rolesNames(profile.getRoles());
		boolean areEqual = CollectionUtils.isEqualCollection(addedRoleNames, currentRolesNames);
		
		return areEqual;
	}

	/**
	 * 
	 * @param profileId
	 * @return
	 * @throws NumberFormatException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	@Transactional
	private List<String> findUserRoles(Integer profileId) throws NumberFormatException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, URISyntaxException, HttpException, IOException, ParserConfigurationException,
			SAXException, XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
		List<String> userRoles = new ArrayList<>();
		List<Role> roles = roleRepository.getByProfileId(profileId);
		if (roles != null && !roles.isEmpty()) {
			for (Role role : roles) {
				userRoles.add(role.getName());
			}
		}
		return userRoles;
	}

}
