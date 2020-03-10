package net.fluance.security.ehprofile.service;

import java.util.List;

public interface UserProfilePatientUnitService {
	/**
	 * Grants a patient unit for the profiles
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @param patientUnit
	 * @return
	 * @throws Exception 
	 */
	boolean grantPatientUnit(String username, String domainName, Integer companyId, String patientUnit) throws Exception;
	
	/**
	 * Add the given patient units to the <b>profile</b>
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @param patientUnits
	 * @return
	 * @throws Exception 
	 */
	boolean grantPatientUnits(String username, String domainName, Integer companyId, List<String> patientUnits) throws Exception;
	
	/**
	 * Overwrites all the patient units for the <b>profile</b> with the given patient units
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @param patientUnits
	 * @return
	 * @throws Exception
	 */
	boolean setPatientUnits(String username, String domainName, Integer companyId, List<String> patientUnits) throws Exception;	
	
	/**
	 * Revokes a patient unit for the <b>profile</b>
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @param patientUnit
	 * @return
	 * @throws Exception 
	 */
	boolean revokePatientUnit(String username, String domainName, Integer companyId, String patientUnit) throws Exception;
	
	/**
	 * Revokes the patient unit for the <b>profile</b>
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @param patientUnits
	 * @return
	 * @throws Exception 
	 */
	boolean revokePatientUnits(String username, String domainName, Integer companyId, List<String> patientUnits) throws Exception;
	
}
