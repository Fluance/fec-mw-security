package net.fluance.security.ehprofile.service.userclientdata;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.fluance.security.core.model.jpa.Profile;
import net.fluance.security.core.repository.jpa.IProfileRepository;
import net.fluance.security.core.repository.jpa.IUserClientDataRepository;
import net.fluance.security.core.support.exception.NotFoundException;
import net.fluance.security.ehprofile.model.userclientdata.UserClientData;
import net.fluance.security.ehprofile.model.userclientdata.UserClientDataSaveRequestPayload;

@Service
public class UserClientDataService {
	private static Logger LOGGER = LogManager.getLogger(UserClientDataService.class);
	
	@Autowired
	private IUserClientDataRepository userClientDataRepository;
	@Autowired
	private IProfileRepository profileRepository;
	
	/**
	 * Save the current data for the given user
	 * 
	 * @param payload {@link UserClientDataSaveRequestPayload}
	 * @param username
	 * @param domain
	 * @param savedClientData
	 * @return
	 * @throws NotFoundException
	 */
	public net.fluance.security.core.model.jpa.UserClientData saveClient(UserClientDataSaveRequestPayload payload, String userName, String domain) throws NotFoundException {
		
		net.fluance.security.core.model.jpa.UserClientData savedClientData = null;
		
		List<Profile> userProfiles = profileRepository.findProfilesByUsernameAndDomainName(userName, domain);
		
		Profile profile = null;
		
		if(userProfiles.size() > 0) {
			profile =userProfiles.get(0);
			if(userProfiles.size() > 1) {
				LOGGER.warn("More than one user for this user name: {}", userName);
			}
		}
		
		if(profile == null) {
			throw new NotFoundException("User profile for " + userName + "/" + domain + " does not exist");
		} else {

			net.fluance.security.core.model.jpa.UserClientData clientData = userClientDataRepository.findByProfileIdAndClientId(profile.getId(), UUID.fromString(payload.getClientId()));

			if(clientData == null) {
				clientData = new net.fluance.security.core.model.jpa.UserClientData();
				clientData.setProfileId(profile.getId());
				clientData.setClientId(UUID.fromString(payload.getClientId()));
			}
			clientData.setHistory(payload.getHistory());
			clientData.setPreferences(payload.getPreferences());

			savedClientData = userClientDataRepository.save(clientData);			
		}
		
		return savedClientData;
	}

	/**
	 * Gets the data for the given user
	 * 
	 * @param clientId
	 * @param username
	 * @param domain
	 * @return
	 * @throws NotFoundException
	 */
	public UserClientData getUserData(String clientId, String userName, String domain) throws NotFoundException {			
		String validDomain = domain;
				
		if(validDomain == null || validDomain.isEmpty()){
			LOGGER.warn("Cannot find user " + userName + " in domain " + domain);			
			throw new NotFoundException("Cannot find user " + userName + " in domain " + domain);
		}
		
		Profile profile = profileRepository.findByUsernameAndDomainName(userName, validDomain);
		if(profile == null) {
			throw new NotFoundException("User profile for " + userName + "/" + domain + " does not exist");
		}
		
		net.fluance.security.core.model.jpa.UserClientData clientData = userClientDataRepository.findByProfileIdAndClientId(profile.getId(), UUID.fromString(clientId));		
		String userHistory = null;
		String userPrefs = null;
		
		if(clientData != null) {
			userHistory = (clientData.getHistory() != null) ? clientData.getHistory().toString() : null;
			userPrefs = (clientData.getPreferences() != null) ? clientData.getPreferences().toString() : null;
		}
		
		return new UserClientData(userHistory, userPrefs);
	}
}
