/**
 * 
 */
package net.fluance.security.ehprofile.service.idm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.fluance.commons.lang.exception.GenericException;

/**
 *
 */
@Service
public class IdentityManagementService {
	@Value("${identity.domains.default}")
	private String defaultDomain;
	
	/* (non-Javadoc)
	 * @see net.fluance.security.auth.user.service.IdentityDomainService#availableIdDomains(java.lang.Object[])
	 */
	public List<? extends Object> availableIdDomains() throws GenericException, IOException {
		List<Object> availableDomains = new ArrayList<>();
		availableDomains.addAll(keycloakAvailableDomains());
		return availableDomains;
	}

	private Collection<? extends Object> keycloakAvailableDomains() {
		return Arrays.asList(defaultDomain);
	}
}
