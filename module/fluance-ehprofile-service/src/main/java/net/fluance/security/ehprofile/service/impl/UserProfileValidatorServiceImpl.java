package net.fluance.security.ehprofile.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.fluance.security.core.model.jpa.Profile;
import net.fluance.security.core.repository.jpa.IProfileRepository;
import net.fluance.security.core.support.exception.NotFoundException;
import net.fluance.security.ehprofile.service.UserProfileValidatorService;

@Service
public class UserProfileValidatorServiceImpl implements UserProfileValidatorService {
	private static final Logger LOGGER = LogManager.getLogger(UserProfileValidatorServiceImpl.class);	
	
	@Value("${identity.domains.default}")
	private String defaultDomain;
	@Autowired
	private IProfileRepository profileRepository;
	
	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfileValidatorService#validateAndGetProfile(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Profile validateAndGetProfile(String username, String domain, String logPrefix) throws NotFoundException {
		if(username == null) {
			LOGGER.warn("{}Username is mandatory", logPrefix);
			throw new IllegalArgumentException("Username is mandatory\"");
		}
		
		if (domain == null) {
			LOGGER.debug("{}Set default domain: {}", logPrefix, defaultDomain);
			domain = defaultDomain;
		}
		
		Profile profile = getFirstProfile(username, domain);
		
		if (profile == null) {
			LOGGER.error("{}No profile found for user " + username + " in domain " + domain, logPrefix);
			throw new NotFoundException("No profile found for user " + username + " in domain " + domain);
		}
		
		return profile;
	}
	
	/**
	 * Get the profiles from DB and gets the first one if there is more than one<br>
	 * A warning will be write on the logs if there is more than one
	 * 
	 * @param userName
	 * @param domain
	 * @return
	 */
	private Profile getFirstProfile(String userName, String domain) {
		List<Profile> userProfiles = profileRepository.findProfilesByUsernameAndDomainName(userName, domain);
		
		Profile userProfile = null;
		
		if(userProfiles.size() > 0) {
			userProfile =userProfiles.get(0);
			if(userProfiles.size() > 1) {
				LOGGER.warn("More than one user for this user name: {}", userName);
			}
		}
		
		return userProfile;
	}

}
