/**
 * 
 */
package net.fluance.security.ehprofile.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.fluance.app.data.model.identity.AccessControl;
import net.fluance.app.data.model.identity.CompanyStaffId;
import net.fluance.app.data.model.identity.EhProfile;
import net.fluance.app.data.model.identity.GrantedCompany;
import net.fluance.app.data.model.identity.HospService;
import net.fluance.app.data.model.identity.Language;
import net.fluance.app.data.model.identity.PatientUnit;
import net.fluance.app.data.model.identity.User;
import net.fluance.app.security.service.IUserService;
import net.fluance.security.core.model.jdbc.Company;
import net.fluance.security.core.model.jpa.Profile;
import net.fluance.security.core.model.jpa.ProfileMetadata;
import net.fluance.security.core.model.jpa.UserCompany;
import net.fluance.security.core.model.jpa.UserCompanyIdentity;
import net.fluance.security.core.model.jpa.UserType;
import net.fluance.security.core.repository.jdbc.CompanyRepository;
import net.fluance.security.core.repository.jpa.IProfileMetadataRepository;
import net.fluance.security.core.repository.jpa.IProfileRepository;
import net.fluance.security.core.repository.jpa.IUserClientDataRepository;
import net.fluance.security.core.repository.jpa.IUserCompanyIdentityRepository;
import net.fluance.security.core.repository.jpa.IUserCompanyRepository;
import net.fluance.security.core.repository.jpa.IUserTypeRepository;
import net.fluance.security.core.service.UserIdentityService;
import net.fluance.security.core.support.exception.NotFoundException;
import net.fluance.security.core.util.CompanyUtils;
import net.fluance.security.ehprofile.service.UserProfileCompanyService;
import net.fluance.security.ehprofile.service.UserProfileRolesService;
import net.fluance.security.ehprofile.service.UserProfileService;
import net.fluance.security.ehprofile.service.UserProfileValidatorService;

/**
 *
 */
@Service
public class UserProfileServiceImpl implements UserProfileService {

	private static final Logger LOGGER = LogManager.getLogger(UserProfileServiceImpl.class);
	private static final String CREATE_LOG = "[create]";
	private static final String DELETE_LOG = "[delete]";
	
	@Value("${identity.domains.default}")
	private String defaultDomain;
	@Autowired
	private IProfileRepository profileRepository;
	@Autowired
	private IUserCompanyIdentityRepository userCompanyIdentityRepository;
	@Autowired
	private IUserCompanyRepository userCompanyRepository;
	@Autowired
	private IUserTypeRepository userTypeRepository;
	@Autowired
	private CompanyRepository companyRepository;
	@Autowired
	private IProfileMetadataRepository profileMetadataRepository;
	@Autowired	
	private IUserService keycloakUserService;
	@Autowired
	private UserIdentityService userIdentityService;
	@Autowired
	private UserProfileRolesService userProfileRolesService;
	@Autowired
	private UserProfileCompanyService userProfileCompanyService;
	@Autowired
	private UserProfileValidatorService userProfileValidatorService;
	@Autowired
	private IUserClientDataRepository userClientDataRepository;
	
	// ---------------------------- PROFILE CRUD ---------
	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.IUserProfileService#create(net.fluance.app.data.model.identity.EhProfile)
	 */
	@Override
	@Transactional
	public boolean create(EhProfile ehProfile) throws Exception {
		LOGGER.info(CREATE_LOG+"Init...");
		
		validateDataForCreation(ehProfile, CREATE_LOG);
		
		LOGGER.debug(CREATE_LOG+"Testing user type...");
		testUserTypeforCreation(ehProfile, CREATE_LOG);
		LOGGER.info(CREATE_LOG+"User type correct");

		String userName = ehProfile.getUsername();
		if(!StringUtils.isEmpty(ehProfile.getUsername()) && ehProfile.getUsername() != null 
				&& !ehProfile.getUsername().equals(ehProfile.getUsername().toLowerCase()) ) {
			LOGGER.warn(CREATE_LOG+"User name will be transformed to lowercase!!!");
			userName = ehProfile.getUsername().toLowerCase();
		}	
		
		boolean profileExistsKeycloak = existsUserInKeycloak(userName, ehProfile.getDomain(), CREATE_LOG);
		LOGGER.info(CREATE_LOG+"User exists in keycloack: " + profileExistsKeycloak);
		
		if (!profileExistsKeycloak) {
			LOGGER.error(CREATE_LOG+"User '" + userName + "' not found in domain '" + ehProfile.getDomain() + "'");
			throw new NotFoundException("User '" + userName + "' not found in domain '" + ehProfile.getDomain() + "'");
		}
		
		saveUserProfile(userName, ehProfile.getDomain(), ehProfile.getUsertype(), CREATE_LOG);		
		LOGGER.info(CREATE_LOG+"User info saved");
				
		// Save Language
		saveLanguage(userName, ehProfile.getDomain(), ehProfile.getLanguage(), CREATE_LOG);
		LOGGER.info(CREATE_LOG+"User language saved");
		
		//Add grants, no adding is possible
		boolean grantsCorrect = setGrantsForCreation(ehProfile, userName);

		LOGGER.info("{}Creation complete: {}", CREATE_LOG, grantsCorrect);
		return grantsCorrect;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.IUserProfileService#delete(java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional
	public boolean delete(String username, String domainName) throws Exception {
		LOGGER.info(DELETE_LOG + "Init...");
		LOGGER.debug("{username: " + username + "}");
		
		Profile profile = userProfileValidatorService.validateAndGetProfile(username, domainName, "[delete]");
		
		if (profile != null) {
			LOGGER.debug(DELETE_LOG + "deleting...");
			LOGGER.debug("{id" + profile.getId()  + ", username: " + profile.getUsername() + "}");
			profileRepository.delete(profile.getId());
			userClientDataRepository.deleteByProfileId(profile.getId());
		}
		
		//Test deleted
		try {
			userProfileValidatorService.validateAndGetProfile(username, domainName, "[delete_test]");
		}catch (NotFoundException notFoundException) {
			profile = null;
			LOGGER.debug(DELETE_LOG + "User deleted on DB");	
		}
		
		LOGGER.info(DELETE_LOG + "Deleted: " + (profile == null));
		return profile == null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.IUserProfileService#find(java.lang.String, java.lang.String)
	 */
	@Override
	public EhProfile find(String userName, String domainName) throws Exception {

		Profile fluanceProfile = userProfileValidatorService.validateAndGetProfile(userName, domainName, "");
		
		AccessControl grants = new AccessControl();
		List<String> userRoles = userProfileRolesService.findUserRoles(userName, domainName);
		grants.setRoles(userRoles);
		List<GrantedCompany> grantedCompanies = new ArrayList<>();
		List<UserCompany> userCompanies = userCompanyRepository.findByUsernameAndDomainName(userName, domainName);
		List<UserCompanyIdentity> userCompanyIdentities = userCompanyIdentityRepository.findByProfileId(fluanceProfile.getId());
		for (UserCompany userCompany : userCompanies) {
			Company company = companyRepository.findOne(userCompany.getCompanyId());
			GrantedCompany grantedCompany = new GrantedCompany();
			grantedCompany.setId(userCompany.getCompanyId());
			grantedCompany.setCode(company.getCode());
			List<PatientUnit> companyUnits = CompanyUtils.patientUnits(userCompany.getUnitsAndServices());
			List<HospService> companyServices = CompanyUtils.hospServices(userCompany.getUnitsAndServices());
			grantedCompany.setPatientunits(companyUnits);
			grantedCompany.setHospservices(companyServices);
			List<UserCompanyIdentity> companyIds = CompanyUtils.companyIdentityByCompanyId(userCompany.getCompanyId(), userCompanyIdentities);
			if (companyIds != null) {
				for (UserCompanyIdentity companyIdentity : companyIds) {
					CompanyStaffId companyStaffId = new CompanyStaffId();
					companyStaffId.setProviderId(companyIdentity.getProviderId());
					companyStaffId.setStaffId(companyIdentity.getStaffId());
					grantedCompany.getStaffIds().add(companyStaffId);
				}
			}
			grantedCompanies.add(grantedCompany);
		}
		grants.setGrantedCompanies(grantedCompanies);
		net.fluance.app.data.model.identity.UserType userType = net.fluance.app.data.model.identity.UserType.valueOf(fluanceProfile.getUserType().getName().toUpperCase());
		EhProfile ehProfile = new EhProfile(fluanceProfile.getId().toString(), domainName, fluanceProfile.getUsername().toLowerCase(), fluanceProfile.getLanguage(), userType, grants);
		return ehProfile;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.IUserProfileService#findByPStaffIds(java.lang.String, java.lang.Long, java.lang.Long)
	 */
	@Override
	@Transactional
	public List<EhProfile> findByPStaffIds(String staffId, Long cliniqueId, Long providerId) throws Exception {
		
		List<EhProfile> ehProfiles = new ArrayList<>();
		List<Profile> fluanceProfiles = profileRepository.findByStaffIds(staffId, cliniqueId, providerId);
		
		if (fluanceProfiles != null && !fluanceProfiles.isEmpty()) {
			for (Profile fluanceProfile : fluanceProfiles) {
				try {
					ehProfiles.add(find(fluanceProfile.getUsername(), fluanceProfile.getDomainName()));
				}catch (NotFoundException e) {
					//Don't find a profile don't interupt the code execution, because another user can match with the search
					LOGGER.warn("No profile found for user " + fluanceProfile.getUsername() + " in domain " + fluanceProfile.getDomainName());
				}
			}
		} 
		return ehProfiles;		
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.IUserProfileService#exists(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean exists(String userName, String domainName){
		
		Profile fluanceProfile = null;
		try {
			fluanceProfile = userProfileValidatorService.validateAndGetProfile(userName, domainName, "");
		} catch (NotFoundException e) {
			//If the user is not found the expected result will be false
		}
		
		return fluanceProfile != null ? true : false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.IUserProfileService#searchProfilesBeginningWith(java.lang.String, java.lang.String)
	 */
	@Override
	public List<User> searchProfilesBeginningWith(
			String username, 
			String domainName
	) {

		List<Profile> 
		matchingProfiles = new ArrayList<>();
		
		List<User> 
		matchingUsers = new ArrayList<>();

		// Look for user profiles whose "username" begins with the method input parameter
		matchingProfiles = profileRepository.findByUsernameBeginningWith(
			username, 
			domainName
		);
		
		// Let's loop through the found user profiles...
		for (Profile matchingProfile : matchingProfiles) {
			User user = null;
	
			user = userIdentityService.getUserWithInfos(
				new User(
					matchingProfile.getUsername(), 
					matchingProfile.getDomainName(), 
					null, 
					null
				)
			);

			if (user != null) 
				matchingUsers.add(user);
			
		}
		
		return matchingUsers;
	}

	// ------------ LANGUAGE ------------ 
	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.IUserProfileService#updateLanguage(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional
	public void updateLanguage(String username, String domainName, String language) throws Exception {		
		saveLanguage(username, domainName, language, "");
	}

	// ----------- METADATA ------------
	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.IUserProfileService#readMetadata(java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional
	public net.fluance.app.data.model.identity.ProfileMetadata readMetadata(String username, String domain) throws NotFoundException {
		Profile fluanceProfile = userProfileValidatorService.validateAndGetProfile(username, domain, "[readMetadata]");
		
		ProfileMetadata profileMetadataDb = profileMetadataRepository.findOne(fluanceProfile.getId());
		
		net.fluance.app.data.model.identity.ProfileMetadata profileMetadata = new net.fluance.app.data.model.identity.ProfileMetadata();
		
		if(profileMetadataDb != null){
			profileMetadata.setProfileId(profileMetadataDb.getProfileId()); 
			profileMetadata.setGender(profileMetadataDb.getGender());
			profileMetadata.setBirthDate(profileMetadataDb.getBirthDate());
			profileMetadata.setTitle(profileMetadataDb.getTitle());
			profileMetadata.setSpeciality(profileMetadataDb.getSpeciality());
			profileMetadata.setGoogleToken(profileMetadataDb.getGoogleToken()); 
			profileMetadata.setLinkedInToken(profileMetadataDb.getLinkedInToken());
			profileMetadata.setEmail(profileMetadataDb.getEmail());
			profileMetadata.setExternalPhoneNumberOne(profileMetadataDb.getExternalPhoneNumberOne());
			profileMetadata.setExternalPhoneNumbertwo(profileMetadataDb.getExternalPhoneNumbertwo());
			profileMetadata.setLatitude(profileMetadataDb.getLatitude());
			profileMetadata.setLongitude(profileMetadataDb.getLongitude());
			profileMetadata.setPictureUri(profileMetadataDb.getPictureUri());
			profileMetadata.setEmployeeClinicId(profileMetadataDb.getEmployeeClinicId());
			profileMetadata.setPreferredPhoneNumber(profileMetadataDb.getPreferredPhoneNumber());
			profileMetadata.setSupportContactName(profileMetadataDb.getSupportContactName());
			profileMetadata.setSupportContactPhoneNumber(profileMetadataDb.getSupportContactPhoneNumber());
			profileMetadata.setIban(profileMetadataDb.getIban());
			profileMetadata.setLastLocalizationAt(profileMetadataDb.getLastLocalizationAt());
			profileMetadata.setLastActivityAt(profileMetadataDb.getLastActivityAt());				
		} 
		return profileMetadata;
	}

	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.IUserProfileService#saveMetadata(java.lang.String, java.lang.String, net.fluance.app.data.model.identity.ProfileMetadata)
	 */
	@Override
	@Transactional
	public void saveMetadata(String username, String domain, net.fluance.app.data.model.identity.ProfileMetadata profileMetadata) throws Exception {		
		Profile profile = userProfileValidatorService.validateAndGetProfile(username, domain, "[saveMetada]");
		
		ProfileMetadata metadata = new ProfileMetadata();
		metadata.setProfileId(profile.getId()); 
		metadata.setGender(profileMetadata.getGender()); 
		metadata.setBirthDate(profileMetadata.getBirthDate()); 
		metadata.setTitle(profileMetadata.getTitle());
		metadata.setSpeciality(profileMetadata.getSpeciality()); 
		metadata.setGoogleToken(profileMetadata.getGoogleToken()); 
		metadata.setLinkedInToken(profileMetadata.getLinkedInToken()); 
		metadata.setEmail(profileMetadata.getEmail()); 
		metadata.setExternalPhoneNumberOne(profileMetadata.getExternalPhoneNumberOne()); 
		metadata.setExternalPhoneNumbertwo(profileMetadata.getExternalPhoneNumbertwo()); 
		metadata.setLatitude(profileMetadata.getLatitude()); 
		metadata.setLongitude(profileMetadata.getLongitude());
		metadata.setPictureUri(profileMetadata.getPictureUri());
		metadata.setEmployeeClinicId(profileMetadata.getEmployeeClinicId()); 
		metadata.setPreferredPhoneNumber(profileMetadata.getPreferredPhoneNumber());
		metadata.setSupportContactName(profileMetadata.getSupportContactName()); 
		metadata.setSupportContactPhoneNumber(profileMetadata.getSupportContactPhoneNumber());
		metadata.setIban(profileMetadata.getIban());
		metadata.setLastLocalizationAt(profileMetadata.getLastLocalizationAt());
		metadata.setLastActivityAt(profileMetadata.getLastActivityAt());
		
		profileMetadataRepository.save(metadata);

	}
	
	//--------------Creation Utils---------------------
	/**
	 * Validates the data for the {@link EhProfile} for creation method
	 * 
	 * @param ehProfile
	 * @param logPrefix
	 */
	private void validateDataForCreation(EhProfile ehProfile, String logPrefix) {
		if (null == ehProfile.getDomain()) {
			LOGGER.debug("{}Setting default domain: {}", logPrefix ,defaultDomain);
			ehProfile.setDomain(defaultDomain);
		}
		
		if (null == ehProfile.getUsername()) {
			LOGGER.error("{}username is missing in the profile data", logPrefix);
			LOGGER.error(CREATE_LOG+"username is missing in the profile data");
			throw new IllegalArgumentException("username is missing in the profile data");
		}
	}
	
	/**
	 * Validates the user type for the creation method
	 * 
	 * @param ehProfile
	 * @param logPrefix
	 */
	private void testUserTypeforCreation(EhProfile ehProfile, String logPrefix) {
		LOGGER.debug("{}User type: {}", logPrefix, ehProfile.getUsertype());
		UserType userType = userTypeRepository.findByName(ehProfile.getUsertype());
		if (userType == null) {
			LOGGER.error("{}User Type is missing in the profile data or Invalid User Type provided", CREATE_LOG);
			throw new IllegalArgumentException("User Type is missing in the profile data or Invalid User Type provided");
		}
	}
	
	/**
	 * Tests if the givne user/domain exists in keycloak
	 * 
	 * @param userName
	 * @param domain
	 * @param logPrefix
	 * @return
	 * @throws Exception
	 */
	private boolean existsUserInKeycloak(String userName, String domain, String logPrefix) throws Exception {
		LOGGER.debug("{}Testing user exists in keycloak...", logPrefix);
		boolean profileExistsKeycloak = false;
		try {
			profileExistsKeycloak = keycloakUserService.isExistingUser(userName, domain);		
		} catch (NotFoundException notFoundException) {
			LOGGER.debug("{}User don't exist in keycloak", logPrefix);
			profileExistsKeycloak = false;
		}
		
		return profileExistsKeycloak;
	}
	
	/**
	 * Saves the user profile for fist time, test the result of the save operation
	 * 
	 * @param userName
	 * @param domain
	 * @param userType
	 * @param logPrefix
	 */
	@Transactional
	private void saveUserProfile(String userName, String domain, String userType, String logPrefix) {
		LOGGER.debug("{}Saving user info...", logPrefix);
		// Save user basic info
		int savedUserNb = profileRepository.saveByUsernameDomainNameAndUsertype(userName, domain, userType);				
		if (1 < savedUserNb) {
			LOGGER.error("{}Could not save the user profile", logPrefix);
			throw new IllegalStateException("Could not save the user profile");
		}
		if (1 > savedUserNb) {
			LOGGER.error("{}Save caused inconsistent state. Affected (inserted) rows: {}", logPrefix, savedUserNb);
			throw new IllegalStateException("Save caused inconsistent state. Affected (inserted) rows: " + savedUserNb);
		}
	}
	
	/**
	 * Addss the grants for the user for copanies and roles.
	 * 
	 * @param ehProfile
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	private boolean setGrantsForCreation(EhProfile ehProfile, String userName) throws Exception {
		// Save grants
		AccessControl grants = ehProfile.getGrants();
		boolean companyGrantsSaved = false;
		if ((grants.getGrantedCompanies() != null) && (grants.getGrantedCompanies().size() > 0)) {
			LOGGER.debug("{}Saving user company grants...", CREATE_LOG);
			// When being creating a profile, ensure the grant will not conflict
			// with any other
			companyGrantsSaved = userProfileCompanyService.setCompanies(userName, ehProfile.getDomain(), grants.getGrantedCompanies(), CREATE_LOG);
			LOGGER.info("{}User grants company saved: {}", CREATE_LOG, companyGrantsSaved);
		} else {
			LOGGER.warn("{}User don't have company grants", CREATE_LOG);
		}
		
		boolean rolesGranted = false;
		if (grants.getRoles() != null) {
			LOGGER.debug("{}Saving roles...", CREATE_LOG);
			rolesGranted = userProfileRolesService.setRoles(userName, ehProfile.getDomain(), grants.getRoles());
			LOGGER.info("{}User roles saved: {}", CREATE_LOG, rolesGranted);
		} else {
			LOGGER.warn("{}User don't have roles", CREATE_LOG);
		}
		
		boolean grantsCorrect = companyGrantsSaved && rolesGranted;
		return grantsCorrect;
	}
		
	// --------------------- language utils --------------------
	/**
	 * Test the language on {@link Language} and save it for the profile
	 * 
	 * @param username
	 * @param domainName
	 * @param language
	 * @param logPrefix
	 * @throws Exception
	 */
	@Transactional
	public void saveLanguage(String username, String domainName, String language, String logPrefix) throws Exception {
		logPrefix = logPrefix + "[saveLanguage]";
		
		LOGGER.debug("{}Saving user language...", logPrefix);
		String validLanguage = Language.permissiveValueOf(language);
		if (validLanguage == null || language.isEmpty()) {
			LOGGER.error("{}Unknown language: {}", logPrefix, language);			
			throw new IllegalArgumentException("Unknown language: " + language);
		}
		
		Profile profile = userProfileValidatorService.validateAndGetProfile(username, domainName, "");		
		profile.setLanguage(validLanguage);
		
		profileRepository.save(profile);
	}
}
