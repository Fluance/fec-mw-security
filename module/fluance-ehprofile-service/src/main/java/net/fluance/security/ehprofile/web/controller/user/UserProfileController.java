package net.fluance.security.ehprofile.web.controller.user;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import io.swagger.annotations.ApiOperation;
import net.fluance.app.data.exception.DataException;
import net.fluance.app.data.model.identity.EhProfile;
import net.fluance.app.data.model.identity.GrantedCompany;
import net.fluance.app.data.model.identity.HospService;
import net.fluance.app.data.model.identity.PatientUnit;
import net.fluance.app.data.model.identity.ProfileMetadata;
import net.fluance.app.data.model.identity.User;
import net.fluance.app.data.model.identity.UserProfile;
import net.fluance.app.data.util.db.PostgreSQLUtils;
import net.fluance.app.web.servlet.controller.AbstractRestController;
import net.fluance.app.web.support.payload.response.GenericResponsePayload;
import net.fluance.security.core.service.UserIdentityService;
import net.fluance.security.core.support.exception.NotFoundException;
import net.fluance.security.ehprofile.service.UserProfileCompanyService;
import net.fluance.security.ehprofile.service.UserProfileHospServiceService;
import net.fluance.security.ehprofile.service.UserProfilePatientUnitService;
import net.fluance.security.ehprofile.service.UserProfileRolesService;
import net.fluance.security.ehprofile.service.UserProfileService;
import net.fluance.security.ehprofile.support.payload.request.userprofile.CompanyPayload;
import net.fluance.security.ehprofile.support.payload.request.userprofile.GrantCompanyRequestPayload;
import net.fluance.security.ehprofile.support.payload.request.userprofile.GrantHospserviceRequestPayload;
import net.fluance.security.ehprofile.support.payload.request.userprofile.GrantPatientunitRequestPayload;
import net.fluance.security.ehprofile.support.payload.request.userprofile.GrantRoleRequestPayload;
import net.fluance.security.ehprofile.support.payload.request.userprofile.RevokeCompanyRequestPayload;
import net.fluance.security.ehprofile.support.payload.response.userprofile.LightUserProfile;

/**
 *
 */
@RestController
public class UserProfileController extends AbstractRestController {

	private static Logger LOGGER = LogManager.getLogger(UserProfileController.class);
	@Autowired
	private UserProfileService userProfileService;
	@Autowired
	private UserProfileCompanyService userProfileCompanyService;
	@Autowired
	private UserProfileHospServiceService userProfileHospServiceService;
	@Autowired
	private UserProfilePatientUnitService userProfilePatientUnitService;
	@Autowired
	private UserProfileRolesService userProfileRolesService;
	@Autowired
	private UserIdentityService userIdentityService;

	@Value("${identity.domains.default}")
	private String defaultDomain;

	@ApiOperation(value = "Read user profile", response = UserProfile.class, tags = "User profile API")
	@RequestMapping(value = "/profile/{username}/{domain}", method = RequestMethod.GET)
	public ResponseEntity<?> readProfile(@PathVariable String username, @PathVariable String domain, HttpServletRequest request, HttpServletResponse response) {
		try {
			if (username == null || username.isEmpty()) {
				throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing username.");
			}
			
			if (domain == null || domain.isEmpty()) {
				LOGGER.warn("Cannot find user " + username + " in domain " + domain);
				throw new NotFoundException("Cannot find user " + username + " in domain " + domain);
			}

			EhProfile profile = userProfileService.find(username, domain);
			if (profile == null) {
				throw new NotFoundException("User profile not found.");
			}

			User user = userIdentityService.getUserWithInfos(new User(username, domain, null, null));
			ProfileMetadata profileMetadata = this.userProfileService.readMetadata(profile.getUsername(), profile.getDomain());
			UserProfile userProfile = new UserProfile(profile, user, profileMetadata);

			return new ResponseEntity<>(userProfile, HttpStatus.OK);
		} catch (Exception exc) {
			return handleException(exc);
		}

	}

	@ApiOperation(value = "Read MY User profile", response = UserProfile.class, tags = "User profile API")
	@RequestMapping(value = "/profile/my", method = RequestMethod.GET)
	public ResponseEntity<?> readMyProfile(HttpServletRequest request, HttpServletResponse response) {
		try {
			User user = (User) request.getAttribute(User.USER_KEY);
			if (user == null) {
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to retrieve User");
			}
			
			String username = user.getUsername();
			String domain = user.getDomain();
			
			if (domain == null || domain.isEmpty()) {
				LOGGER.warn("Cannot find user " + username + " in domain " + domain);
				throw new NotFoundException("Cannot find user " + username + " in domain " + domain);
			}
			
			EhProfile ehProfile = userProfileService.find(username, domain);
			
			if (ehProfile == null) {
				throw new NotFoundException("User profile not found");
			} else {
				user = userIdentityService.getUserWithInfos(user);
				ProfileMetadata profileMetadata = this.userProfileService.readMetadata(ehProfile.getUsername(), ehProfile.getDomain());
				UserProfile userProfile = new UserProfile(ehProfile, user, profileMetadata);
				return new ResponseEntity<>(userProfile, HttpStatus.OK);
			}
		} catch (Exception exc) {
			return handleException(exc);
		}

	}

	@ApiOperation(value = "Update my User profile Metadata", response = ProfileMetadata.class, tags = "User profile API")
	@RequestMapping(value = "/profile/my", method = RequestMethod.POST)
	public ResponseEntity<?> updateMyProfileMetadata(HttpServletRequest request, HttpServletResponse response, @RequestBody ProfileMetadata profileMetadata) {
		try {
			User user = (User) request.getAttribute(User.USER_KEY);
			if (user == null) {
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to retrieve User");
			}				
			String username = user.getUsername();
			String domain = user.getDomain();
			
			if (domain == null || domain.isEmpty()) {
				LOGGER.warn("Domain : " + domain + " not valid");
				throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Domain : " + domain + " not valid");
			}
			this.userProfileService.saveMetadata(username, domain, profileMetadata);
			return new ResponseEntity<>(new GenericResponsePayload("Profile updated"), HttpStatus.CREATED);
		} catch (Exception exc) {
			return handleException(exc);
		}

	}

	@ApiOperation(value = "Retrieve users profiles beginning with 'username' parameter value", response = GenericResponsePayload.class, tags = "User profile API")
	@RequestMapping(value = "/profile")
	public ResponseEntity<?> searchProfilesBeginningWith(@RequestParam String username, @RequestParam String domain, HttpServletRequest request, HttpServletResponse response) {
		// Check that "username" parameter length is at least 3 and tell it to the web-service consumer...
		if (username.length() < 3) {
			return new ResponseEntity<>(new GenericResponsePayload("username parameter must contain at least 3 characters."), HttpStatus.BAD_REQUEST);
		}

		List<User> matchingUsers = userProfileService.searchProfilesBeginningWith(username, domain);

		List<LightUserProfile> retrievedProfiles = new ArrayList<LightUserProfile>();

		matchingUsers.stream().forEach(matchingUser -> {
			retrievedProfiles.add(new LightUserProfile().withUserName(matchingUser.getUsername()).withFirstName(matchingUser.getFirstName()).withLastName(matchingUser.getLastName()).withDepartment(matchingUser.getDepartment()));
		});

		return new ResponseEntity<>(retrievedProfiles, HttpStatus.ACCEPTED);
	}

	@ApiOperation(value = "Create user profile", response = GenericResponsePayload.class, tags = "User profile API")
	@RequestMapping(value = "/profile", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createProfile(@RequestBody EhProfile payload, HttpServletRequest request, HttpServletResponse response) {

		GenericResponsePayload grp = new GenericResponsePayload();
		HttpStatus status = HttpStatus.ACCEPTED;
		try {

			if (userProfileService.create(payload)) {
				status = HttpStatus.OK;
				grp.setMessage("Successfully created the new profile");
			} else {
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error");
			}
		} catch (Exception exc) {
			return handleException(exc);
		}
		return new ResponseEntity<>(grp, status);
	}

	@ApiOperation(value = "Delete user profile", response = GenericResponsePayload.class, tags = "User profile API")
	@RequestMapping(value = "/profile/{username}/{domain}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteProfile(@PathVariable("username") String username, @PathVariable("domain") String domain, HttpServletRequest request, HttpServletResponse response) {
		HttpStatus status = HttpStatus.ACCEPTED;
		GenericResponsePayload responsePayload = new GenericResponsePayload();

		try {
			if (userProfileService.delete(username, domain)) {
				status = HttpStatus.OK;
				responsePayload.setMessage("User profile " + domain + "/" + username + " successfully deleted");
			} else {
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error : User profile not deleted.");
			}
		} catch (Exception exc) {
			return handleException(exc);
		}
		return new ResponseEntity<>(responsePayload, status);
	}

	@ApiOperation(value = "Set user language in the profile.", response = GenericResponsePayload.class, tags = "User profile API")
	@RequestMapping(value = "/profile/{username}/{domain}/{language}", method = RequestMethod.POST)
	public ResponseEntity<?> setLanguage(@PathVariable String username, @PathVariable String domain, @PathVariable String language, HttpServletRequest request, HttpServletResponse response) {

		GenericResponsePayload grp = new GenericResponsePayload();
		HttpStatus status = HttpStatus.ACCEPTED;

		try {
			userProfileService.updateLanguage(username, domain, language);
			String msg = "Successfully set " + language + " for " + domain + "/" + username;
			LOGGER.info(msg);
			status = HttpStatus.OK;
			grp.setMessage(msg);
		} catch (Exception exc) {
			return handleException(exc);
		}
		return new ResponseEntity<>(grp, status);
	}

	@ApiOperation(value = "Set user companies via the profile. All companies not in the list to set will be revoked", response = GenericResponsePayload.class, tags = "User profile API")
	@RequestMapping(value = "/profile/companies/set", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> setCompanies(@RequestBody GrantCompanyRequestPayload payload, HttpServletRequest request, HttpServletResponse response) {

		GenericResponsePayload grp = new GenericResponsePayload();
		HttpStatus status = HttpStatus.ACCEPTED;

		try {
			List<GrantedCompany> grantedCompanies = grantedCompaniesFromPayload(payload.getCompanies());

			if (userProfileCompanyService.setCompanies(payload.getUsername(), payload.getDomainName(), grantedCompanies)) {
				String msg = "Successfully set " + payload.getCompanies() + " for " + payload.getDomainName() + "/" + payload.getUsername();
				LOGGER.info(msg);
				status = HttpStatus.OK;
				grp.setMessage(msg);
			} else {
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error. Could not set " + payload.getCompanies() + " for " + payload.getDomainName() + "/" + payload.getUsername());
			}
		} catch (Exception exc) {
			return handleException(exc);
		}
		return new ResponseEntity<>(grp, status);
	}

	@ApiOperation(value = "Grant companies to a user. This adds the granted companies to the already granted ones.", response = GenericResponsePayload.class, tags = "User profile API")
	@RequestMapping(value = "/profile/companies/grant", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> grantCompanies(@RequestBody GrantCompanyRequestPayload payload, HttpServletRequest request, HttpServletResponse response) {

		GenericResponsePayload grp = new GenericResponsePayload();
		HttpStatus status = HttpStatus.ACCEPTED;

		try {
			boolean allGranted = (payload.getCompanies() == null || payload.getCompanies().isEmpty()) ? true : false;

			int grantedNb = 0;
			if (payload.getCompanies() != null) {
				Iterator<CompanyPayload> companiesIter = payload.getCompanies().iterator();
				while (!allGranted && companiesIter.hasNext()) {
					CompanyPayload company = companiesIter.next();

					boolean granted = userProfileCompanyService.grantCompany(payload.getUsername(), payload.getDomainName(), company.getCompanyId(), company.getPatientUnits(), company.getHospServices(), company.getStaffIds());
					grantedNb += (granted) ? 1 : 0;
				}
			}

			allGranted = (payload.getCompanies() == null) ? true : (grantedNb == payload.getCompanies().size());

			if (allGranted) {
				String msg = "Successfully granted " + payload.getCompanies() + " to " + payload.getDomainName() + "/" + payload.getUsername();
				LOGGER.info(msg);
				status = HttpStatus.OK;
				grp.setMessage(msg);
			} else {
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error. Could not grant " + payload.getCompanies() + " to " + payload.getDomainName() + "/" + payload.getUsername());
			}
		} catch (Exception exc) {
			return handleException(exc);
		}
		return new ResponseEntity<>(grp, status);
	}

	@ApiOperation(value = "Grant hospservices to a user. This adds the granted hospservices to the already granted ones.", response = GenericResponsePayload.class, tags = "User profile API")
	@RequestMapping(value = "/profile/hospservices/grant", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> grantHospServices(@RequestBody GrantHospserviceRequestPayload payload, HttpServletRequest request, HttpServletResponse response) {

		GenericResponsePayload grp = new GenericResponsePayload();
		HttpStatus status = HttpStatus.ACCEPTED;

		try {
			if (userProfileHospServiceService.grantHospServices(payload.getUsername(), payload.getDomainName(), payload.getCompanyId(), payload.getHospServices())) {
				String msg = "Successfully granted '" + payload.getHospServices() + "' to " + payload.getDomainName() + "/" + payload.getUsername();
				LOGGER.info(msg);
				status = HttpStatus.OK;
				grp.setMessage(msg);
			} else {
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error. Could not grant '" + payload.getHospServices() + "' to " + payload.getDomainName() + "/" + payload.getUsername());
			}
		} catch (Exception exc) {
			return handleException(exc);
		}
		return new ResponseEntity<>(grp, status);
	}

	@ApiOperation(value = "Set user hospservices via the profile. All hospservices not in the list to set will be revoked", response = GenericResponsePayload.class, tags = "User profile API")
	@RequestMapping(value = "/profile/hospservices/set", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> setHospServices(@RequestBody GrantHospserviceRequestPayload payload, HttpServletRequest request, HttpServletResponse response) {

		GenericResponsePayload grp = new GenericResponsePayload();
		HttpStatus status = HttpStatus.ACCEPTED;

		try {
			boolean allGranted = false;

			List<HospService> hospServices = new ArrayList<>();
			if (payload.getHospServices() != null) {
				for (String service : payload.getHospServices()) {
					HospService hospService = new HospService(service);
					hospServices.add(hospService);
				}
				allGranted = userProfileHospServiceService.setHospServices(payload.getUsername(), payload.getDomainName(), payload.getCompanyId(), payload.getHospServices());
			}

			if (allGranted) {
				String msg = "Successfully set '" + payload.getHospServices() + "' for " + payload.getDomainName() + "/" + payload.getUsername();
				LOGGER.info(msg);
				status = HttpStatus.OK;
				grp.setMessage(msg);
			} else {
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error. Could not set '" + payload.getHospServices() + "' for " + payload.getDomainName() + "/" + payload.getUsername());
			}
		} catch (Exception exc) {
			return handleException(exc);
		}
		return new ResponseEntity<>(grp, status);
	}

	@ApiOperation(value = "Grant patientunits to a user. This adds the patientunits roles to the already granted ones.", response = GenericResponsePayload.class, tags = "User profile API")
	@RequestMapping(value = "/profile/patientunits/grant", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> grantPatientUnits(@RequestBody GrantPatientunitRequestPayload payload, HttpServletRequest request, HttpServletResponse response) {

		GenericResponsePayload grp = new GenericResponsePayload();
		HttpStatus status = HttpStatus.ACCEPTED;

		try {
			if (userProfilePatientUnitService.grantPatientUnits(payload.getUsername(), payload.getDomainName(), payload.getCompanyId(), payload.getPatientUnits())) {
				String msg = "Successfully granted " + payload.getPatientUnits() + " to " + payload.getDomainName() + "/" + payload.getUsername();
				LOGGER.info(msg);
				status = HttpStatus.OK;
				grp.setMessage(msg);
			} else {
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error. Could not grant " + payload.getPatientUnits() + " to " + payload.getDomainName() + "/" + payload.getUsername());
			}
		} catch (Exception exc) {
			return handleException(exc);
		}
		return new ResponseEntity<>(grp, status);
	}

	@ApiOperation(value = "Set user patientunits via the profile. All patientunits not in the list to set will be revoked", response = GenericResponsePayload.class, tags = "User profile API")
	@RequestMapping(value = "/profile/patientunits/set", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> setPatientUnits(@RequestBody GrantPatientunitRequestPayload payload, HttpServletRequest request, HttpServletResponse response) {

		GenericResponsePayload grp = new GenericResponsePayload();
		HttpStatus status = HttpStatus.ACCEPTED;

		try {
			boolean allGranted = false;

			List<PatientUnit> patientUnits = new ArrayList<>();
			if (payload.getPatientUnits() != null) {
				for (String unit : payload.getPatientUnits()) {
					PatientUnit patientUnit = new PatientUnit(unit);
					patientUnits.add(patientUnit);
				}
				allGranted = userProfilePatientUnitService.setPatientUnits(payload.getUsername(), payload.getDomainName(), payload.getCompanyId(), payload.getPatientUnits());
			}

			if (allGranted) {
				String msg = "Successfully set " + payload.getPatientUnits() + " for " + payload.getDomainName() + "/" + payload.getUsername();
				LOGGER.info(msg);
				status = HttpStatus.OK;
				grp.setMessage(msg);
			} else {
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error. Could not set " + payload.getPatientUnits() + " for " + payload.getDomainName() + "/" + payload.getUsername());
			}
		} catch (Exception exc) {
			return handleException(exc);
		}
		return new ResponseEntity<>(grp, status);
	}

	@ApiOperation(value = "Read user roles.", response = GenericResponsePayload.class, tags = "User profile API")
	@RequestMapping(value = "/profile/{username}/{domain}/roles", method = RequestMethod.GET)
	public ResponseEntity<?> getUserRoles(@PathVariable String username, @PathVariable String domain, HttpServletRequest request, HttpServletResponse response) {
		try {
			if (username == null || username.isEmpty()) {
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Missing username.");
			}
			// If no domain is provided, then
			if (domain == null || domain.isEmpty()) {
				domain = defaultDomain;
			}
			
			List<String> userRoles = userProfileRolesService.findUserRoles(username, domain);
			if (userRoles == null) {
				throw new NotFoundException("User profile not found.");
			} else {
				return new ResponseEntity<>(userRoles, HttpStatus.OK);
			}
		} catch (Exception exc) {
			return handleException(exc);
		}
	}

	@ApiOperation(value = "Set user roles via the profile. All roles not in the list to set will be revoked", response = GenericResponsePayload.class, tags = "User profile API")
	@RequestMapping(value = "/profile/roles/set", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> setRoles(@RequestBody GrantRoleRequestPayload payload, HttpServletRequest request, HttpServletResponse response) {

		GenericResponsePayload grp = new GenericResponsePayload();
		HttpStatus status = HttpStatus.ACCEPTED;

		try {
			if (userProfileRolesService.setRoles(payload.getUsername(), payload.getDomainName(), payload.getRoles())) {
				String msg = "Successfully set " + payload.getRoles() + payload.getDomainName() + "/" + payload.getUsername() + "'s roles";
				LOGGER.info(msg);
				status = HttpStatus.OK;
				grp.setMessage(msg);
			} else {
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error. Could not set " + payload.getRoles() + payload.getDomainName() + "/" + payload.getUsername() + "'s roles");
			}
		} catch (Exception exc) {
			LOGGER.error(ExceptionUtils.getStackTrace(exc));
		}
		return new ResponseEntity<>(grp, status);
	}

	@ApiOperation(value = "Grant roles to a user. This adds the granted roles to the already granted ones. All patientunits not in the list to set will be revoked", response = GenericResponsePayload.class, tags = "User profile API")
	@RequestMapping(value = "/profile/roles/grant", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> grantRoles(@RequestBody GrantRoleRequestPayload payload, HttpServletRequest request, HttpServletResponse response) {

		GenericResponsePayload grp = new GenericResponsePayload();
		HttpStatus status = HttpStatus.ACCEPTED;

		try {
			if (userProfileRolesService.grantRoles(payload.getUsername(), payload.getDomainName(), payload.getRoles())) {
				String msg = "Successfully granted " + payload.getRoles() + " to " + payload.getDomainName() + "/" + payload.getUsername();
				LOGGER.info(msg);
				status = HttpStatus.OK;
				grp.setMessage(msg);
			} else {
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error. Could not grant " + payload.getRoles() + " to " + payload.getDomainName() + "/" + payload.getUsername());
			}
		} catch (Exception exc) {
			return handleException(exc);
		}
		return new ResponseEntity<>(grp, status);
	}

	@ApiOperation(value = "Revoke user companies.", response = GenericResponsePayload.class, tags = "User profile API")
	@RequestMapping(value = "/profile/companies/revoke", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> revokeCompanies(@RequestBody RevokeCompanyRequestPayload payload, HttpServletRequest request, HttpServletResponse response) {

		GenericResponsePayload grp = new GenericResponsePayload();
		HttpStatus status = HttpStatus.ACCEPTED;

		try {
			boolean allRevoked = (payload.getCompanies() == null || payload.getCompanies().isEmpty()) ? true : false;
			int revokedNb = 0;
			if (payload.getCompanies() != null) {
				Iterator<Integer> companiesIter = payload.getCompanies().iterator();
				while (!allRevoked && companiesIter.hasNext()) {
					Integer companyId = companiesIter.next();
					boolean revoked = userProfileCompanyService.revokeCompany(payload.getUsername(), payload.getDomainName(), companyId);
					revokedNb += (revoked) ? 1 : 0;
				}
			}
			allRevoked = (payload.getCompanies() == null) ? true : (revokedNb == payload.getCompanies().size());

			if (allRevoked) {
				String msg = "Successfully revoked " + payload.getCompanies() + " for " + payload.getDomainName() + "/" + payload.getUsername();
				LOGGER.info(msg);
				status = HttpStatus.OK;
				grp.setMessage(msg);
			} else {
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error. Could not revoke " + payload.getCompanies() + " for " + payload.getDomainName() + "/" + payload.getUsername());
			}
		} catch (Exception exc) {
			return handleException(exc);
		}
		return new ResponseEntity<>(grp, status);
	}

	@ApiOperation(value = "Revoke user hospservices.", response = GenericResponsePayload.class, tags = "User profile API")
	@RequestMapping(value = "/profile/hospservices/revoke", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> revokeHospServices(@RequestBody GrantHospserviceRequestPayload payload, HttpServletRequest request, HttpServletResponse response) {

		GenericResponsePayload grp = new GenericResponsePayload();
		HttpStatus status = HttpStatus.ACCEPTED;

		try {
			if (userProfileHospServiceService.revokeHospServices(payload.getUsername(), payload.getDomainName(), payload.getCompanyId(), payload.getHospServices())) {
				String msg = "Successfully revoked '" + payload.getHospServices() + "' from " + payload.getDomainName() + "/" + payload.getUsername();
				LOGGER.info(msg);
				status = HttpStatus.OK;
				grp.setMessage(msg);
			} else {
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error. Could not revoke '" + payload.getHospServices() + "' to " + payload.getDomainName() + "/" + payload.getUsername());
			}
		} catch (Exception exc) {
			return handleException(exc);
		}
		return new ResponseEntity<>(grp, status);
	}

	@ApiOperation(value = "Revoke user patientunits.", response = GenericResponsePayload.class, tags = "User profile API")
	@RequestMapping(value = "/profile/patientunits/revoke", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> revokePatientUnits(@RequestBody GrantPatientunitRequestPayload payload, HttpServletRequest request, HttpServletResponse response) {

		GenericResponsePayload grp = new GenericResponsePayload();
		HttpStatus status = HttpStatus.ACCEPTED;

		try {
			if (userProfilePatientUnitService.revokePatientUnits(payload.getUsername(), payload.getDomainName(), payload.getCompanyId(), payload.getPatientUnits())) {
				String msg = "Successfully revoked '" + payload.getPatientUnits() + "' from " + payload.getDomainName() + "/" + payload.getUsername();
				LOGGER.info(msg);
				status = HttpStatus.OK;
				grp.setMessage(msg);
			} else {
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error. Could not revoke '" + payload.getPatientUnits() + "' to " + payload.getDomainName() + "/" + payload.getUsername());
			}
		} catch (Exception exc) {
			return handleException(exc);
		}
		return new ResponseEntity<>(grp, status);
	}

	@ApiOperation(value = "Revoke user roles.", response = GenericResponsePayload.class, tags = "User profile API")
	@RequestMapping(value = "/profile/roles/revoke", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> revokeRoles(@RequestBody GrantRoleRequestPayload payload, HttpServletRequest request, HttpServletResponse response) {
		GenericResponsePayload grp = new GenericResponsePayload();
		HttpStatus status = HttpStatus.ACCEPTED;

		try {
			if (userProfileRolesService.revokeRoles(payload.getUsername(), payload.getDomainName(), payload.getRoles())) {
				String msg = "Successfully revoked " + payload.getRoles() + " from " + payload.getDomainName() + "/" + payload.getUsername();
				LOGGER.info(msg);
				status = HttpStatus.OK;
				grp.setMessage(msg);
			} else {
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error. Could not revoke " + payload.getRoles() + " from " + payload.getDomainName() + "/" + payload.getUsername());
			}
		} catch (Exception exc) {
			return handleException(exc);
		}
		return new ResponseEntity<>(grp, status);
	}

	@ApiOperation(value = "Check if user has access to a company", response = Boolean.class, tags = "User profile API", notes = "Use this end-point to check if a company is granted to a User or Not.")
	@RequestMapping(value = "/user/{username}/{domain}/{companycode}/exists", method = RequestMethod.GET)
	public ResponseEntity<?> checkCompany(@PathVariable String username, @PathVariable String domain, @PathVariable String companycode, HttpServletRequest request, HttpServletResponse response) {
		try {
			String validDomain = domain;

			if (validDomain == null || validDomain.isEmpty()) {
				LOGGER.warn("Cannot find user " + username + " in domain " + domain);
				throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Cannot find user " + username + " in domain " + domain);
			}
			boolean hasAccessToCompany = userProfileCompanyService.hasAccessToCompany(username, domain, companycode);
			return new ResponseEntity<>(hasAccessToCompany, HttpStatus.OK);
		} catch (Exception exc) {
			return handleException(exc);
		}
	}

	@ApiOperation(value = "Search get profiles by Staff_IDs", response = EhProfile.class, tags = "User profile API")
	@RequestMapping(value = "/profile/staffids/search", method = RequestMethod.GET)
	public ResponseEntity<?> getProfileByStaffIds(@RequestParam String staffId, @RequestParam Long cliniqueId, @RequestParam Long providerId, HttpServletRequest request, HttpServletResponse response) {
		try {
			LOGGER.info("Search Doctor Profile by: Staff_id= " + staffId + ", Clinique_ID= " + cliniqueId + " and Provider_ID= " + providerId);
			List<EhProfile> ehProfiles = userProfileService.findByPStaffIds(staffId, cliniqueId, providerId);

			if (ehProfiles == null || ehProfiles.isEmpty()) {
				LOGGER.warn("Cannot find user by: Staff_id= " + staffId + ", Clinique_ID= " + cliniqueId + " and Provider_ID= " + providerId);
				throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Cannot find profile by: Staff_id= " + staffId + ", Clinique_ID= " + cliniqueId + " and Provider_ID= " + providerId);
			} else if (ehProfiles != null && !ehProfiles.isEmpty() && ehProfiles.size() > 1) {
				LOGGER.warn("More than one user found by: Staff_id= " + staffId + ", Clinique_ID= " + cliniqueId + " and Provider_ID= " + providerId);
				throw new HttpClientErrorException(HttpStatus.CONFLICT, "More than one user found by: Staff_id= " + staffId + ", Clinique_ID= " + cliniqueId + " and Provider_ID= " + providerId);
			} else {
				return new ResponseEntity<>(ehProfiles.get(0), HttpStatus.OK);
			}
		} catch (Exception exc) {
			return handleException(exc);
		}
	}

	/**
	 * Parses a list of {@link CompanyPayload} to a list of {@link GrantedCompany}
	 * 
	 * @param companiesInPayload  {@link List} of {@link CompanyPayload}
	 * @return {@link List} of {@link GrantedCompany}
	 */
	private List<GrantedCompany> grantedCompaniesFromPayload(List<CompanyPayload> companiesInPayload) {
		List<GrantedCompany> grantedCompanies = new ArrayList<>();
		if (companiesInPayload != null && !companiesInPayload.isEmpty()) {
			for (CompanyPayload payloadCompany : companiesInPayload) {
				List<HospService> hospServices = new ArrayList<>();
				List<PatientUnit> patientUnits = new ArrayList<>();
				if (payloadCompany.getPatientUnits() != null) {
					for (String unit : payloadCompany.getPatientUnits()) {
						PatientUnit patientUnit = new PatientUnit(unit);
						patientUnits.add(patientUnit);
					}
				}
				if (payloadCompany.getHospServices() != null) {
					for (String service : payloadCompany.getHospServices()) {
						HospService hospService = new HospService(service);
						hospServices.add(hospService);
					}
				}
				GrantedCompany grantedCompany = new GrantedCompany();
				grantedCompany.setId(payloadCompany.getCompanyId());
				grantedCompany.setStaffIds(payloadCompany.getStaffIds());
				grantedCompany.setPatientunits(patientUnits);
				grantedCompany.setHospservices(hospServices);
				grantedCompanies.add(grantedCompany);
			}
		}
		return grantedCompanies;
	}

	public Logger getLogger() {
		return LOGGER;
	}

	/**
	 * Overwrites the handler from the parent class. After the special treatment it calls parent implementation
	 * 
	 * @param exc The exception that was raised
	 * @return
	 */
	@Override
	public ResponseEntity<?> handleException(Exception exc) {
		GenericResponsePayload grp = new GenericResponsePayload(exc.getMessage());
		if (exc instanceof PSQLException || exc instanceof DataAccessException) {
			getLogger().warn("", exc);
			return (ResponseEntity<?>) handleDataException(new DataException(exc));
		} else if (exc instanceof NotFoundException) {
			getLogger().warn("", exc);
			return new ResponseEntity<>(grp, HttpStatus.NOT_FOUND);
		} else if (exc instanceof HttpClientErrorException) {
			getLogger().error("", exc);
			return new ResponseEntity<>(grp, ((HttpClientErrorException) exc).getStatusCode());
		} else if (exc instanceof HttpServerErrorException) {
			getLogger().error("", exc);
			return new ResponseEntity<>(grp, HttpStatus.BAD_GATEWAY);
		} else if (exc instanceof NotFoundException) {
			getLogger().error("", exc);
			return new ResponseEntity<>(grp, HttpStatus.NOT_FOUND);
		}

		return super.handleException(exc);
	}

	/**
	 * Give the implementation for the data exception expected at the parent class
	 * 
	 * @param exc The exception that was raised
	 * @return
	 */
	@Override
	public ResponseEntity<GenericResponsePayload> handleDataException(DataException exc) {
		GenericResponsePayload grp = new GenericResponsePayload();
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		String message = "DEFAULT_INTERNAL_SERVER_ERROR_MESSAGE";
		Throwable rootCause = null;
		try {
			rootCause = ExceptionUtils.getRootCause(exc.getException());
		} catch (Exception exception) {
		}
		
		if ((rootCause != null) && (rootCause instanceof PSQLException)) {
			if (PostgreSQLUtils.SQLSTATE_DUPLICATEKEY_ERROR.equalsIgnoreCase(((PSQLException) rootCause).getSQLState())) {
				status = HttpStatus.CONFLICT;
				message = "Could not update data, due to a duplicate key error";
				grp.setMessage(message);
				return new ResponseEntity<>(grp, status);
			} else if (PostgreSQLUtils.SQLSTATE_FOREIGNKEYVIOLATION_ERROR.equalsIgnoreCase(((PSQLException) rootCause).getSQLState())) {
				status = HttpStatus.CONFLICT;
				message = "Could not update data, due to a foreign key violation error";
				grp.setMessage(message);
				return new ResponseEntity<>(grp, status);
			}
		}
		grp.setMessage((message == null || message.isEmpty()) ? DEFAULT_INTERNAL_SERVER_ERROR_MESSAGE : message);
		return new ResponseEntity<>(grp, status);
	}
}
