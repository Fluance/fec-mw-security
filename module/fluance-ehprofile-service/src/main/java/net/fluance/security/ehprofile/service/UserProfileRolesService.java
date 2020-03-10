package net.fluance.security.ehprofile.service;

import java.util.List;

import net.fluance.app.data.model.identity.ProfileMetadata;

public interface UserProfileRolesService {
	/**
	 * Adds the roles to the profiles
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param roles
	 * @return
	 * @throws Exception
	 */
	boolean grantRoles(String username, String domainName, List<String> roles) throws Exception;
	
	/**
	 * Overwrites the roles for a <b>profile</b> with the given ones
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param roles
	 * @return
	 * @throws Exception
	 */
	boolean setRoles(String username, String domainName, List<String> roles) throws Exception;
	
	/**
	 * Revokes the roles for the <b>profile</b>
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param roles
	 * @return
	 * @throws Exception
	 */
	boolean revokeRoles(String username, String domainName, List<String> roles) throws Exception;
	
	/**
	 * Find all the roles for the given username
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param localUserName
	 * @param fluanceKey
	 * @return {@link ProfileMetadata}
	 * @throws Exception
	 */
	List<String> findUserRoles(String localUserName, String fluanceKey) throws Exception;
	
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
	//boolean setRoles(String username, String domain, List<String> rolesToAdd, String logPrefix) throws Exception;		
		
}
