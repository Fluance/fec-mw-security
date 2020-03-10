package net.fluance.security.ehprofile.service;

import java.util.List;

public interface UserProfileHospServiceService {
	/**
	 * Adds one hospservice for the <b>profile</b>
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @param hospService
	 * @return
	 * @throws Exception 
	 */
	boolean grantHospService(String username, String domainName, Integer companyId, String hospService) throws Exception;
	
	/**
	 * Adds the given list of hopservices for the profiles
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @param hospService
	 * @return true if all hospservices in hospServices have been successfully granted. Otherwise, false.
	 * @throws Exception 
	 */
	boolean grantHospServices(String username, String domainName, Integer companyId, List<String> hospServices) throws Exception;
	
	/**
	 * overwrites the hospservices for the <b>profile</b>
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @param hospService
	 * @return true if all hospservices in hospServices have been successfully granted. Otherwise, false.
	 * @throws Exception 
	 */
	boolean setHospServices(String username, String domainName, Integer companyId, List<String> hospServices) throws Exception;
	
	/**
	 * Revokes a hospservice for the <b>profile</b>
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @param hospService
	 * @return
	 * @throws Exception 
	 */
	boolean revokeHospService(String username, String domainName, Integer companyId, String hospService) throws Exception;
	
	/**
	 * Revokes the hospservices for the profiles
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @param hospServices
	 * @return
	 * @throws Exception 
	 */
	boolean revokeHospServices(String username, String domainName, Integer companyId, List<String> hospServices) throws Exception;
}
