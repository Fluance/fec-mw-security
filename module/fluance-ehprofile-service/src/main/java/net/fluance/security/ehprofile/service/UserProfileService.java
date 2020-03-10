/**
 * 
 */
package net.fluance.security.ehprofile.service;

import java.util.List;

import net.fluance.app.data.model.identity.EhProfile;
import net.fluance.app.data.model.identity.ProfileMetadata;
import net.fluance.app.data.model.identity.User;
import net.fluance.security.core.support.exception.NotFoundException;


/**
 *
 */
public interface UserProfileService {

	// ---------------------------- PROFILE CRUD ---------
	/**
	 * Profile creation
	 * 
	 * @param ehProfile  {@link EhProfile}}
	 * @return
	 * @throws Exception
	 */
	public boolean create(EhProfile ehProfile) throws Exception;
	
	/**
	 * Deletes a user <b>profile</b>
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @return 
	 * @throws Exception 
	 */
	boolean delete(String username, String domainName) throws Exception;
	
	/**
	 * finds the <b>profile</b> by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @return 
	 * @throws Exception 
	 */
	EhProfile find(String username, String domainName) throws Exception;
	
	/**
	 * List of profiles
	 * 
	 * @param staffId
	 * @param cliniqueId
	 * @param providerId
	 * @return
	 * @throws Exception
	 */
	List<EhProfile> findByPStaffIds(String staffId, Long cliniqueId, Long providerId) throws Exception;
	
	/**
	 * Checks if the userName in this domain exists in the Security Database
	 * 
	 * @param userName
	 * @param domainName
	 * @return
	 * @throws Exception
	 */
	 boolean exists(String userName, String domainName) throws Exception;

	/**
	 * Search profiles whose username starts with the given username
	 * 
	 * @param username
	 * @param domain
	 * @return
	 */
	List<User> searchProfilesBeginningWith(String username, String domain);
	
	// ------------ LANGUAGE ------------ 
	/**
	 * Updates the language for the <b>profile</b>
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param language
	 * @throws Exception
	 */
	void updateLanguage(String username, String domainName, String language) throws Exception;
	
	// ----------- METADATA ------------
	/**
	 * Returns the metadata for the <b>profile</b>
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domain
	 * @return
	 * @throws NotFoundException
	 */
	ProfileMetadata readMetadata(String username, String domain) throws NotFoundException;
	
	/**
	 * Saves all the metadata for the <b>profile</b>
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domain
	 * @param profileMetadata  {@link ProfileMetadata}
	 * @throws Exception
	 */
	void saveMetadata(String username, String domain, ProfileMetadata profileMetadata) throws Exception;
}
