package net.fluance.security.ehprofile.service;

import java.util.List;

import net.fluance.app.data.model.identity.CompanyStaffId;
import net.fluance.app.data.model.identity.GrantedCompany;

public interface UserProfileCompanyService {
	/**
	 * Grant a company to the <b>profile</b>
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @param patientUnits
	 * @param hospServices
	 * @param staffIds
	 * @return
	 * @throws Exception
	 */
	boolean grantCompany(String username, String domainName, Integer companyId, List<String> patientUnits, List<String> hospServices, List<CompanyStaffId> staffIds) throws Exception;
	
	/**
	 * Adds the given companies to the <b>profile</b>
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param companies
	 * @return
	 * @throws Exception
	 */
	boolean grantCompanies(String username, String domainName, List<GrantedCompany> companies) throws Exception;
	
	/**
	 * Overwrites the companies for the <b>profile</b> with the given list of companies
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param companies
	 * @return
	 * @throws Exception
	 */
	boolean setCompanies(String username, String domainName, List<GrantedCompany> companies) throws Exception;

	/**
	 * Revoke the access to the <b>profile</b> to a company
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @return
	 */
	boolean revokeCompany(String username, String domainName, Integer companyId);
	
	/**
	 * Revoke the access for a <b>profile</b> to the companies
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @param units
	 * @param services
	 * @return
	 */
	boolean revokeCompanies(String username, String domainName, Integer companyId, List<String> units,
			List<String> services);
	
	/**
	 * Tests if a <b>profile</b> has access to the given company
	 * <br><b>profile</b> is represented by username and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param companyCode
	 * @return
	 * @throws Exception
	 */
	boolean hasAccessToCompany(String username, String domainName, String companyCode) throws Exception;

	/**
	 * Set the companies for the gicen user and domain
	 * 
	 * @param username
	 * @param domainName
	 * @param companies
	 * @param logPrefix
	 * @return
	 * @throws Exception
	 */
	boolean setCompanies(String username, String domainName, List<GrantedCompany> companies, String logPrefix) throws Exception;	
		
}
