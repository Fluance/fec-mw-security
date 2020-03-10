package net.fluance.security.ehprofile.service.impl;

import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.fluance.app.data.model.identity.CompanyStaffId;
import net.fluance.app.data.model.identity.EhProfile;
import net.fluance.app.data.model.identity.GrantedCompany;
import net.fluance.security.core.model.jpa.Profile;
import net.fluance.security.core.model.jpa.UserCompany;
import net.fluance.security.core.repository.jpa.IUserCompanyIdentityRepository;
import net.fluance.security.core.repository.jpa.IUserCompanyRepository;
import net.fluance.security.core.util.CompanyUtils;
import net.fluance.security.ehprofile.service.UserProfileCompanyService;
import net.fluance.security.ehprofile.service.UserProfileService;
import net.fluance.security.ehprofile.service.UserProfileValidatorService;

@Service
public class UserProfileCompanyServiceImpl implements UserProfileCompanyService {

	private static final Logger LOGGER = LogManager.getLogger(UserProfileCompanyServiceImpl.class);
	private static final String SET_COMPANIES_LOG = "[setCompanies]";
	private static final String GRANT_COMPANIES_LOG = "[grantCompanies]";
	private static final String GRANT_COMPANY_LOG = "[grantCompany]";
	
	@Autowired
	private IUserCompanyIdentityRepository userCompanyIdentityRepository;
	@Autowired
	private IUserCompanyRepository userCompanyRepository;
	@Autowired
	private UserProfileService userProfileService;
	@Autowired
	private UserProfileValidatorService userProfileValidatorService;
	
	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfileCompanyService#grantCompany(java.lang.String, java.lang.String, java.lang.Integer, java.util.List, java.util.List, java.util.List)
	 */
	@Override
	@Transactional
	public boolean grantCompany(String username, String domainName, Integer companyId, List<String> patientUnits, List<String> hospServices, List<CompanyStaffId> staffIds) throws Exception {
		return grantCompany(username, domainName, companyId, patientUnits, hospServices, staffIds, "");
	}

	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfileCompanyService#grantCompanies(java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	@Transactional
	public boolean grantCompanies(String username, String domainName, List<GrantedCompany> companies) throws Exception {
		return grantCompanies(username, domainName, companies, "");
	}

	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfileCompanyService#setCompanies(java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	@Transactional
	public boolean setCompanies(String username, String domainName, List<GrantedCompany> companies) throws Exception {
		return setCompanies(username, domainName, companies, "");
	}

	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfileCompanyService#revokeCompany(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	@Transactional
	public boolean revokeCompany(String username, String domainName, Integer companyId) {
		if (companyId != null) {
			userCompanyIdentityRepository.deleteByUsernameAndDomainNameAndCompanyId(username, domainName, companyId);
			userCompanyRepository.deleteByUsernameAndDomainNameAndCompanyId(username, domainName, companyId);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfileCompanyService#revokeCompanies(java.lang.String, java.lang.String, java.lang.Integer, java.util.List, java.util.List)
	 */
	@Override
	@Transactional
	public boolean revokeCompanies(String username, String domainName, Integer companyId, List<String> units, List<String> services) {
		return revokeCompany(username, domainName, companyId);
	}

	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfileCompanyService#hasAccessToCompany(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional
	public boolean hasAccessToCompany(String username, String domainName, String companyCode) throws Exception {
		EhProfile ehProfile = userProfileService.find(username, domainName);
		if (ehProfile == null) throw new net.fluance.app.web.util.exceptions.NotFoundException();
		
		if (ehProfile.getGrants().getGrantedCompanies() != null && !ehProfile.getGrants().getGrantedCompanies().isEmpty()) {
			for (GrantedCompany company : ehProfile.getGrants().getGrantedCompanies()) {
				if (company.getCode().equals(companyCode)) {
					return true;
				}
			}
		}
		return false; 
	}
	
	// -------------- Company utils -----------
	/**
	 * Grants the companies for the profile
	 * 
	 * @param username
	 * @param domainName
	 * @param companies
	 * @param logPrefix
	 * @return
	 * @throws Exception
	 */
	@Transactional
	private boolean grantCompanies(String username, String domainName, List<GrantedCompany> companies, String logPrefix) throws Exception {
		logPrefix = logPrefix + GRANT_COMPANIES_LOG;
		
		LOGGER.info(logPrefix+"Granting companies...");
		
		if (companies == null || companies.isEmpty()) {
			LOGGER.warn(logPrefix+"No companies set");
			return true;
		}
		
		boolean allGranted = false;
		int grantedNb = 0;
		Iterator<GrantedCompany> companiesIter = companies.iterator();
		while (!allGranted && companiesIter.hasNext()) {
			GrantedCompany company = companiesIter.next();
			
			boolean granted = grantCompany(username, domainName, company, logPrefix);
			LOGGER.debug(logPrefix+"{Company: " + company.getId() + ", granted: "+ granted + "}");
			
			grantedNb += (granted) ? 1 : 0;
		}
		
		allGranted = (grantedNb == companies.size());
		
		LOGGER.info(logPrefix+"All companies granted: " + allGranted);
		return allGranted;
	}
	
	/**
	 * Grants a company for the profile 
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @param units
	 * @param services
	 * @param staffIds
	 * @param logPrefix
	 * @return
	 * @throws Exception
	 */
	@Transactional
	private boolean grantCompany(String username, String domainName, Integer companyId, List<String> units, List<String> services, List<CompanyStaffId> staffIds, String logPrefix) throws Exception {
		logPrefix = logPrefix + GRANT_COMPANY_LOG;
		
		LOGGER.info(logPrefix+"Granting company...");
		LOGGER.debug(logPrefix+"{company: " + companyId + ", username: " + username + "}");
		if (companyId == null) {
			LOGGER.error(logPrefix+"company ID is mandatory for granting");
			throw new IllegalArgumentException("company ID is mandatory for granting");
		}
		
		boolean companySaved = saveUserCompany(username, domainName, companyId, units, services);
		
		LOGGER.info(logPrefix+"Saving company for user: " + companySaved);
		
		if (companySaved) {
			if (staffIds != null) {
				boolean staffIdSave = false;
				LOGGER.info(logPrefix+"Saving staffids for company...");
				for (CompanyStaffId staffId : staffIds) {
					LOGGER.debug(logPrefix+"{company: " + companyId + ", username: " + username + ", staffId: " +  staffId + "}");
					staffIdSave = saveCompanyStaffId(username, domainName, companyId, staffId);
					LOGGER.info(logPrefix+"Staffid saved: " + staffIdSave);
				}
			}
		}
		
		return true;
	}

	/**
	 * 
	 * @param username
	 * @param domain
	 * @param company
	 * @param logPrefix
	 * @return
	 * @throws Exception
	 */
	@Transactional
	private boolean grantCompany(String username, String domain, GrantedCompany company, String logPrefix) throws Exception {
		return grantCompany(username, domain, company.getId(), CompanyUtils.patientUnitsNames(company.getPatientunits()), CompanyUtils.hospServicesNames(company.getHospservices()), company.getStaffIds(), logPrefix);
	}

	/**
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @param staffId
	 * @return
	 */
	@Transactional
	private boolean saveCompanyStaffId(String username, String domainName, Integer companyId, CompanyStaffId staffId) {
		if (companyId == null) {
			throw new IllegalArgumentException("company ID is mandatory for granting");
		}
		if (staffId.getProviderId() != null || staffId.getStaffId() != null) {
			userCompanyIdentityRepository.saveByUsernameAndDomainName(username, domainName, companyId, staffId.getProviderId(), staffId.getStaffId());
		}
		// If Wrong data is given, then DB will raise exception
		return true;
	}

	/**
	 * Saves the user for the company, the method generate the {@link UserCompany} to be store.
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @param units
	 * @param services
	 * @return
	 * @throws Exception
	 */
	@Transactional
	private boolean saveUserCompany(String username, String domainName, Integer companyId, List<String> units, List<String> services) throws Exception {
		if (companyId == null) {
			throw new IllegalArgumentException("company ID is mandatory for granting");
		}
		
		Profile profile = userProfileValidatorService.validateAndGetProfile(username, domainName, "");
		
		UserCompany userCompany = userCompany(profile.getId(), companyId, units, services);
		userCompany = userCompanyRepository.save(userCompany);
		return userCompany != null;// == 1;
	}

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
	@Override
	@Transactional
	public boolean setCompanies(String username, String domainName, List<GrantedCompany> companies, String logPrefix) throws Exception{	
		logPrefix = logPrefix + SET_COMPANIES_LOG;
		LOGGER.info(logPrefix + "Init..");
		
		if (companies == null || companies.isEmpty()) {
			return true;
		}
		
		LOGGER.info(logPrefix + "Deleting previous company identities");
		userCompanyIdentityRepository.deleteByUsernameAndDomainName(username, domainName);
		
		LOGGER.info(logPrefix + "Deleting previous companies");
		userCompanyRepository.deleteByUsernameAndDomainName(username, domainName);
		
		boolean allGranted = grantCompanies(username, domainName, companies, logPrefix);
		
		LOGGER.info(logPrefix + "All the company set granted: " + allGranted);
		
		return allGranted;
	}

	/**
	 * Creates a {@link UserCompany} object mixing the all the data.<br>
	 * The mix of the patientUnits and hospServices will result ins a JSON Object.<br>
	 *
	 * @param profileId
	 * @param companyId
	 * @param patientUnits
	 * @param hospServices
	 * @return
	 */
	private UserCompany userCompany(Integer profileId, Integer companyId, List<String> patientUnits, List<String> hospServices) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode unitsAndServices = mapper.createObjectNode();
		ArrayNode patientUnitsArrayNode = mapper.createArrayNode();
		ArrayNode hospServicesArrayNode = mapper.createArrayNode();
		if (patientUnits != null) {
			for (String patientUnit : patientUnits) {
				patientUnitsArrayNode.add(patientUnit);
			}
		}
		if (hospServices != null) {
			for (String hospService : hospServices) {
				hospServicesArrayNode.add(hospService);
			}
		}
		((ObjectNode) unitsAndServices).set(CompanyUtils.PATIENTUNIT_JSON_PROPERTY, patientUnitsArrayNode);
		((ObjectNode) unitsAndServices).set(CompanyUtils.HOSPSERVICE_JSON_PROPERTY, hospServicesArrayNode);
		UserCompany userCompany = new UserCompany(companyId, profileId, unitsAndServices);
		return userCompany;
	}

}
