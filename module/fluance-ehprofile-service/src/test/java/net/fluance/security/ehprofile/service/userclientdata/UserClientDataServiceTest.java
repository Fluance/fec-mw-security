package net.fluance.security.ehprofile.service.userclientdata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.fluance.security.core.repository.jpa.IProfileRepository;
import net.fluance.security.core.repository.jpa.IUserClientDataRepository;
import net.fluance.security.core.support.exception.NotFoundException;
import net.fluance.security.ehprofile.model.userclientdata.UserClientData;
import net.fluance.security.ehprofile.test.utils.ProfileMocks;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserClientDataServiceTest {
	private static final String USERNAME = "test";
	private static final String DOMAIN = "PRIMARY";
	private static final String CLIENT_ID = "4e842a40-8edd-4660-bd69-abcd88dd0f03";
	private static final Integer PROFILE_ID = 1;
	private static final String PREFERENCES = "{\"prefLang\":\"en\",\"prefUnits\":[]}";
	private static final String HISTORY = "{\"favoriteSearches\":[{\"timeStamp\":1534497164277,\"__criteria\":[{\"key\":\"field\",\"value\":\"1955-03-05\",\"isOptional\":true,\"isDisplayable\":true,\"isImportant\":true,\"isRemovable\":false,\"displayTransformation\":{\"isDisplayable\":true,\"isImportant\":true}}]}]}";

	@TestConfiguration
	static class Configuration {
		
		@Bean
		public UserClientDataService userClientDataService() {
			return new UserClientDataService();
		}
	}
	
	@Before
	public void setUp() {
		Mockito.reset(userClientDataRepository);
		Mockito.reset(profileRepository);
		Mockito.when(profileRepository.findByUsernameAndDomainName(USERNAME, DOMAIN)).thenReturn(ProfileMocks.getDefaultProfile(USERNAME, DOMAIN));
	}
	
	@MockBean
	private IUserClientDataRepository userClientDataRepository;
	@MockBean
	private IProfileRepository profileRepository;
	
	@Autowired
	UserClientDataService userClientDataService;
	
	@Test
	public void getUserData_should_return() throws NotFoundException {
		Mockito.when(userClientDataRepository.findByProfileIdAndClientId(PROFILE_ID, UUID.fromString(CLIENT_ID))).thenReturn(generateUserClientData());
		
		UserClientData userClientData = userClientDataService.getUserData(CLIENT_ID, USERNAME, DOMAIN);
		
		assertNotNull("Must return client data", userClientData);
		assertNotNull("Must have Preferences", userClientData.getPreferences());
		assertNotNull("Must have History", userClientData.getHistory());
	}
	
	@Test
	public void getUserData_should_return_empty_preferences() throws NotFoundException {
		Mockito.when(userClientDataRepository.findByProfileIdAndClientId(1, UUID.fromString(CLIENT_ID))).thenReturn(null);
		
		UserClientData userClientData = userClientDataService.getUserData(CLIENT_ID, USERNAME, DOMAIN);
		
		assertNotNull("Must return client data", userClientData);
		assertNull("No preferences", userClientData.getPreferences());
		assertNull("No History", userClientData.getHistory());
	}
	
	@Test(expected = NotFoundException.class)
	public void getUserData_not_found_username_domain() throws NotFoundException {
		userClientDataService.getUserData(CLIENT_ID, null, null);
	}
	
	@Test(expected = NotFoundException.class)
	public void getUserData_not_found_profile_not_exist() throws NotFoundException {
		Mockito.when(profileRepository.findByUsernameAndDomainName(USERNAME, DOMAIN)).thenReturn(null);
		userClientDataService.getUserData(CLIENT_ID, null, null);
	}
	
	private net.fluance.security.core.model.jpa.UserClientData generateUserClientData() {
		net.fluance.security.core.model.jpa.UserClientData userClientData = new net.fluance.security.core.model.jpa.UserClientData();
		
		userClientData.setProfileId(PROFILE_ID);
		userClientData.setClientId(UUID.fromString(CLIENT_ID));
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			userClientData.setPreferences(mapper.readTree(PREFERENCES));
			userClientData.setHistory(mapper.readTree(HISTORY));
		} catch (Exception e) {;}
		
		return userClientData;
	}
}