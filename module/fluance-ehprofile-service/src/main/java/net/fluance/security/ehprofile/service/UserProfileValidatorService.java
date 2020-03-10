package net.fluance.security.ehprofile.service;

import net.fluance.security.core.model.jpa.Profile;
import net.fluance.security.core.support.exception.NotFoundException;

public interface UserProfileValidatorService {
	/**
	 * Gets the profile using the default domain if domain is null it will be set to default
	 * 
	 * @param username
	 * @param domain
	 * @param logPrefix
	 * @return
	 * @throws NotFoundException
	 */
	Profile validateAndGetProfile(String username, String domain, String logPrefix) throws NotFoundException;
}
