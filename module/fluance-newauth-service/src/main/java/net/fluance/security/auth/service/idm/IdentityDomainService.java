/**
 * 
 */
package net.fluance.security.auth.service.idm;

import java.util.List;

public interface IdentityDomainService {
	
	/**
	 * Returns the list all available ID domains
	 * @param params
	 * @return
	 * @throws Exception
	 */
	List<? extends Object> availableIdDomains(Object... params) throws Exception;

	/**
	 * Returns the list all available ID domains where the application has the right to read users informations
	 * @param params
	 * @return
	 * @throws Exception
	 */
	List<? extends Object> availableIdDomainsWithReadUserInfoPrevileges(Object... params) throws Exception;
}
