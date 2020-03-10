/**
 * 
 */
package net.fluance.security.core.repository.jpa;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Transactional;

import net.fluance.app.test.AbstractTest;
import net.fluance.commons.codec.MD5Utils;
import net.fluance.security.core.Application;
import net.fluance.security.core.ClientTestApplication;
import net.fluance.security.core.model.jpa.Client;
import net.fluance.security.core.model.jpa.UserClientData;
import net.fluance.security.core.repository.jpa.IClientRepository;
import net.fluance.security.core.repository.jpa.IUserClientDataRepository;

@EnableJpaRepositories(basePackages="net.fluance")
@EntityScan("net.fluance")
@ComponentScan("net.fluance")
@SpringBootTest(classes = Application.class)
public class UserClientDataRepositoryTest extends AbstractTest {

	private static final Logger LOGGER = LogManager.getLogger(UserClientDataRepositoryTest.class);
	private static final String EXPECTED_APP_NAME = "My client application";
	private static final String EXPECTED_APP_DESCRIPTION = "Description of My client application";
	private static final String USER1_NAME = "localtestuser1";
	private static final String USER2_NAME = "localtestuser2";
	private static final String LOCAL_DOMAIN_NAME = "PRIMARY";
	private static final String USER_HISTORY1 = "{\"last_company\":1,\"last_unit\":1}";
	private static final String USER_HISTORY2 = "{\"last_company\":2,\"last_unit\":2}";
	private static final String USER_PREFERENCES1 = "{\"preflang\":\"fr\"}";
	private static final String USER_PREFERENCES2 = "{\"preflang\":\"de\"}";
	
	@Autowired
	private IClientRepository clientRepository;
	@Autowired
	private IProfileRepository profileRepository;
	@Autowired
	private IUserClientDataRepository userClientDataRepository;
	
//	@Before
//	public void setUp() throws NoSuchAlgorithmException {
//		tearDown();
//		int createdUserNb = profileRepository.saveByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
//		if(createdUserNb != 1) {
//			throw new IllegalStateException("User access must work in order to test " + getClass().getSimpleName());
//		}
//		
//		Profile founddUser = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME); 
//		if(founddUser == null) {
//			throw new IllegalStateException("User '" + LOCAL_DOMAIN_NAME + "/" + USER1_NAME + "' must exist in order to test " + getClass().getSimpleName());
//		}
//		
//		Client client = new Client();
//		client.setName(EXPECTED_APP_NAME);
//		client.setDescription(EXPECTED_APP_DESCRIPTION);
//		UUID clientId = UUID.randomUUID();
//		String clientSecret = clientId.toString() + new Date().toString();
//		client.setId(clientId);
//		String hashedSecret = MD5Utils.md5HexHash(clientSecret);
//		client.setSecret(hashedSecret);
//		Client createdClient = clientRepository.save(client);
//		LOGGER.info("Created Client ID: " + createdClient.getId());
//
//		UserClientData clientData = new UserClientData();
//		clientData.setClientId(createdClient.getId());
//		clientData.setProfileId(founddUser.getId());
//		clientData.setPreferences(new JSONObject(USER_PREFERENCES1));
//		clientData.setHistory(new JSONObject(USER_HISTORY1));
//		userClientDataRepository.save(clientData);
//	}
	
//	@Test
//	@Transactional
//	public void mustCreateTest() throws NoSuchAlgorithmException {
//		Profile profile = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
//		Client client = clientRepository.findByName(EXPECTED_APP_NAME);
//		UserClientData clientData = new UserClientData();
//		clientData.setClientId(client.getId());
//		clientData.setProfileId(profile.getId());
//		clientData.setPreferences(new JSONObject(USER_PREFERENCES1));
//		clientData.setHistory(new JSONObject(USER_HISTORY1));
//		UserClientData createdData = userClientDataRepository.save(clientData);
//		assertNotNull(createdData);
//		String userHistory = (createdData.getHistory() != null) ? createdData.getHistory().toString() : null;
//		String userPrefs = (createdData.getPreferences() != null) ? createdData.getPreferences().toString() : null;
//		assertEquals(USER_HISTORY1, userHistory);
//		assertEquals(USER_PREFERENCES1, userPrefs);
//	}
//	
//	@Test
//	@Transactional
//	public void mustFindByClientIdTest() throws NoSuchAlgorithmException {
//		int createdUserNb = profileRepository.saveByUsernameAndDomainName(USER2_NAME, LOCAL_DOMAIN_NAME);
//		if(createdUserNb != 1) {
//			throw new IllegalStateException("User access must work in order to test " + getClass().getSimpleName());
//		}
//		Profile foundUser = profileRepository.findByUsernameAndDomainName(USER2_NAME, LOCAL_DOMAIN_NAME); 
//		if(foundUser == null) {
//			throw new IllegalStateException("User '" + LOCAL_DOMAIN_NAME + "/" + USER2_NAME + "' must exist in order to test " + getClass().getSimpleName());
//		}
//		Client foundClientApp = clientRepository.findByName(EXPECTED_APP_NAME);
//		assertTrue(foundClientApp != null);
//		
//		Client client2 = new Client();
//		client2.setName("Another client");
//		client2.setDescription("Another client description");
//		UUID clientId = UUID.randomUUID();
//		String clientSecret = clientId.toString() + new Date().toString();
//		client2.setId(clientId);
//		String hashedSecret = MD5Utils.md5HexHash(clientSecret);
//		client2.setSecret(hashedSecret);
//		Client createdClient2 = clientRepository.save(client2);
//		LOGGER.info("Created Client 2 ID: " + createdClient2.getId());
//		
//		UserClientData clientData = new UserClientData();
//		clientData.setClientId(foundClientApp.getId());
//		clientData.setProfileId(foundUser.getId());
//		clientData.setPreferences(new JSONObject(USER_PREFERENCES2));
//		clientData.setHistory(new JSONObject(USER_HISTORY2));
//		userClientDataRepository.save(clientData);
//		
//		UserClientData clientData2 = new UserClientData();
//		clientData2.setClientId(createdClient2.getId());
//		clientData2.setProfileId(foundUser.getId());
//		clientData2.setPreferences(new JSONObject(USER_PREFERENCES1));
//		clientData2.setHistory(new JSONObject(USER_HISTORY1));
//		userClientDataRepository.save(clientData2);
//		
//		List<UserClientData> foundData = userClientDataRepository.findByClientId(foundClientApp.getId());
//		assertNotNull(foundData);
//		assertEquals(2, foundData.size());
//	}
//	
//	@Test
//	@Transactional
//	public void mustFindByUserIdTest() throws NoSuchAlgorithmException {
//		int createdUserNb = profileRepository.saveByUsernameAndDomainName(USER2_NAME, LOCAL_DOMAIN_NAME);
//		if(createdUserNb != 1) {
//			throw new IllegalStateException("User access must work in order to test " + getClass().getSimpleName());
//		}
//		Profile foundUser = profileRepository.findByUsernameAndDomainName(USER2_NAME, LOCAL_DOMAIN_NAME); 
//		if(foundUser == null) {
//			throw new IllegalStateException("User '" + LOCAL_DOMAIN_NAME + "/" + USER2_NAME + "' must exist in order to test " + getClass().getSimpleName());
//		}
//		Client foundClientApp = clientRepository.findByName(EXPECTED_APP_NAME);
//		assertTrue(foundClientApp != null);
//		
//		Client client2 = new Client();
//		client2.setName("Another client");
//		client2.setDescription("Another client description");
//		UUID clientId = UUID.randomUUID();
//		String clientSecret = clientId.toString() + new Date().toString();
//		client2.setId(clientId);
//		String hashedSecret = MD5Utils.md5HexHash(clientSecret);
//		client2.setSecret(hashedSecret);
//		Client createdClient2 = clientRepository.save(client2);
//		LOGGER.info("Created Client 2 ID: " + createdClient2.getId());
//		
//		UserClientData clientData = new UserClientData();
//		clientData.setClientId(foundClientApp.getId());
//		clientData.setProfileId(foundUser.getId());
//		clientData.setPreferences(new JSONObject(USER_PREFERENCES2));
//		clientData.setHistory(new JSONObject(USER_HISTORY2));
//		userClientDataRepository.save(clientData);
//		
//		UserClientData clientData2 = new UserClientData();
//		clientData2.setClientId(createdClient2.getId());
//		clientData2.setProfileId(foundUser.getId());
//		clientData2.setPreferences(new JSONObject(USER_PREFERENCES1));
//		clientData2.setHistory(new JSONObject(USER_HISTORY1));
//		userClientDataRepository.save(clientData2);
//		
//		List<UserClientData> foundData = userClientDataRepository.findByProfileId(foundUser.getId());
//		assertNotNull(foundData);
//		assertEquals(2, foundData.size());
//	}
//	
//	@Test
//	@Transactional
//	public void mustFindByUserIdAndClientIdTest() throws NoSuchAlgorithmException {
//		int createdUserNb = profileRepository.saveByUsernameAndDomainName(USER2_NAME, LOCAL_DOMAIN_NAME);
//		if(createdUserNb != 1) {
//			throw new IllegalStateException("User access must work in order to test " + getClass().getSimpleName());
//		}
//		Profile foundUser = profileRepository.findByUsernameAndDomainName(USER2_NAME, LOCAL_DOMAIN_NAME); 
//		if(foundUser == null) {
//			throw new IllegalStateException("User '" + LOCAL_DOMAIN_NAME + "/" + USER2_NAME + "' must exist in order to test " + getClass().getSimpleName());
//		}
//		Client foundClientApp = clientRepository.findByName(EXPECTED_APP_NAME);
//		assertTrue(foundClientApp != null);
//		
//		Client client2 = new Client();
//		client2.setName("Another client");
//		client2.setDescription("Another client description");
//		UUID clientId = UUID.randomUUID();
//		String clientSecret = clientId.toString() + new Date().toString();
//		client2.setId(clientId);
//		String hashedSecret = MD5Utils.md5HexHash(clientSecret);
//		client2.setSecret(hashedSecret);
//		Client createdClient2 = clientRepository.save(client2);
//		LOGGER.info("Created Client 2 ID: " + createdClient2.getId());
//		
//		UserClientData clientData = new UserClientData();
//		clientData.setClientId(foundClientApp.getId());
//		clientData.setProfileId(foundUser.getId());
//		clientData.setPreferences(new JSONObject(USER_PREFERENCES2));
//		clientData.setHistory(new JSONObject(USER_HISTORY2));
//		userClientDataRepository.save(clientData);
//		
//		UserClientData clientData2 = new UserClientData();
//		clientData2.setClientId(createdClient2.getId());
//		clientData2.setProfileId(foundUser.getId());
//		clientData2.setPreferences(new JSONObject(USER_PREFERENCES1));
//		clientData2.setHistory(new JSONObject(USER_HISTORY1));
//		userClientDataRepository.save(clientData2);
//		
//		UserClientData foundData = userClientDataRepository.findByProfileIdAndClientId(foundUser.getId(), clientData2.getClientId());
//		assertNotNull(foundData);
//		assertEquals(foundUser.getId(), foundData.getProfileId());
//		assertEquals(clientData2.getClientId(), foundData.getClientId());
//	}
//	
//	@Test
//	@Transactional
//	public void mustDeleteByClientIdTest() throws NoSuchAlgorithmException {
//		int createdUserNb = profileRepository.saveByUsernameAndDomainName(USER2_NAME, LOCAL_DOMAIN_NAME);
//		if(createdUserNb != 1) {
//			throw new IllegalStateException("User access must work in order to test " + getClass().getSimpleName());
//		}
//		Profile foundUser = profileRepository.findByUsernameAndDomainName(USER2_NAME, LOCAL_DOMAIN_NAME); 
//		if(foundUser == null) {
//			throw new IllegalStateException("User '" + LOCAL_DOMAIN_NAME + "/" + USER2_NAME + "' must exist in order to test " + getClass().getSimpleName());
//		}
//		Client foundClientApp = clientRepository.findByName(EXPECTED_APP_NAME);
//		assertTrue(foundClientApp != null);
//		
//		Client client2 = new Client();
//		client2.setName("Another client");
//		client2.setDescription("Another client description");
//		UUID clientId = UUID.randomUUID();
//		String clientSecret = clientId.toString() + new Date().toString();
//		client2.setId(clientId);
//		String hashedSecret = MD5Utils.md5HexHash(clientSecret);
//		client2.setSecret(hashedSecret);
//		Client createdClient2 = clientRepository.save(client2);
//		LOGGER.info("Created Client 2 ID: " + createdClient2.getId());
//		
//		UserClientData clientData = new UserClientData();
//		clientData.setClientId(foundClientApp.getId());
//		clientData.setProfileId(foundUser.getId());
//		clientData.setPreferences(new JSONObject(USER_PREFERENCES2));
//		clientData.setHistory(new JSONObject(USER_HISTORY2));
//		userClientDataRepository.save(clientData);
//		
//		List<UserClientData> foundData = userClientDataRepository.findByClientId(foundClientApp.getId());
//		assertEquals(2, foundData.size());
//		
//		userClientDataRepository.deleteByClientId(foundClientApp.getId());
//		
//		List<UserClientData> foundDataAfter = userClientDataRepository.findByClientId(foundClientApp.getId());
//		assertEquals(0, foundDataAfter.size());
//	}
//	
//	@Test
//	public void mustDeleteByUserIdTest() throws NoSuchAlgorithmException {
//		Profile foundUser = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
//		
//		assertNotNull(foundUser);
//		List<UserClientData> foundData = userClientDataRepository.findByProfileId(foundUser.getId());
//		assertNotNull(foundData);
//		assertTrue(foundData.size() > 0);
//		
//		userClientDataRepository.deleteByProfileId(foundUser.getId());
//		
//		List<UserClientData> foundDataAfter = userClientDataRepository.findByProfileId(foundUser.getId());
//		assertEquals(0, foundDataAfter.size());
//		
//	}
//	
//	@Test
//	@Transactional
//	public void mustUpdateHistoryTest() throws NoSuchAlgorithmException {
//		int createdUserNb = profileRepository.saveByUsernameAndDomainName(USER2_NAME, LOCAL_DOMAIN_NAME);
//		if(createdUserNb != 1) {
//			throw new IllegalStateException("User access must work in order to test " + getClass().getSimpleName());
//		}
//		Profile foundUser = profileRepository.findByUsernameAndDomainName(USER2_NAME, LOCAL_DOMAIN_NAME); 
//		if(foundUser == null) {
//			throw new IllegalStateException("User '" + LOCAL_DOMAIN_NAME + "/" + USER2_NAME + "' must exist in order to test " + getClass().getSimpleName());
//		}
//		Client foundClientApp = clientRepository.findByName(EXPECTED_APP_NAME);
//		assertTrue(foundClientApp != null);
//		
//		UserClientData clientData = new UserClientData();
//		clientData.setClientId(foundClientApp.getId());
//		clientData.setProfileId(foundUser.getId());
//		clientData.setPreferences(new JSONObject(USER_PREFERENCES2));
//		clientData.setHistory(new JSONObject(USER_HISTORY2));
//		userClientDataRepository.save(clientData);
//		
//		UserClientData foundData = userClientDataRepository.findByProfileIdAndClientId(foundUser.getId(), foundClientApp.getId());
//		assertNotNull(foundData);
//		String userHistory = (foundData.getHistory() != null) ? foundData.getHistory().toString() : null;
//		assertEquals(USER_HISTORY2, userHistory);
//		
//		String newHistoryStr = "{\"last_company\":1,\"last_unit\":1,\"lastSearches\":{\"searchOne\":\"companies\"}}";
//		JSONObject newHistory = new JSONObject(newHistoryStr);
//		foundData.setHistory(newHistory);
//		UserClientData foundDataAfter = userClientDataRepository.save(foundData);
//		assertNotNull(foundDataAfter);
//		assertTrue(newHistory.similar(foundDataAfter.getHistory()));
//		//Preferences must remain unchanged
//		assertTrue(foundData.getPreferences().similar(foundDataAfter.getPreferences()));
//	}
//	
//	@Test
//	@Transactional
//	public void mustUpdatePreferencesTest() throws NoSuchAlgorithmException {
//		int createdUserNb = profileRepository.saveByUsernameAndDomainName(USER2_NAME, LOCAL_DOMAIN_NAME);
//		if(createdUserNb != 1) {
//			throw new IllegalStateException("User access must work in order to test " + getClass().getSimpleName());
//		}
//		Profile foundUser = profileRepository.findByUsernameAndDomainName(USER2_NAME, LOCAL_DOMAIN_NAME); 
//		if(foundUser == null) {
//			throw new IllegalStateException("User '" + LOCAL_DOMAIN_NAME + "/" + USER2_NAME + "' must exist in order to test " + getClass().getSimpleName());
//		}
//		Client foundClientApp = clientRepository.findByName(EXPECTED_APP_NAME);
//		assertTrue(foundClientApp != null);
//		
//		UserClientData clientData = new UserClientData();
//		clientData.setClientId(foundClientApp.getId());
//		clientData.setProfileId(foundUser.getId());
//		clientData.setPreferences(new JSONObject(USER_PREFERENCES2));
//		clientData.setHistory(new JSONObject(USER_HISTORY2));
//		userClientDataRepository.save(clientData);
//		
//		UserClientData foundData = userClientDataRepository.findByProfileIdAndClientId(foundUser.getId(), foundClientApp.getId());
//		assertNotNull(foundData);
//		String userHistory = (foundData.getHistory() != null) ? foundData.getHistory().toString() : null;
//		assertEquals(USER_HISTORY2, userHistory);
//		
//		String newPrefStr = "{\"preferredLanguage\":\"ru\",\"preferredLayout\":\"default\"}";
//		JSONObject newPreferences = new JSONObject(newPrefStr);
//		foundData.setPreferences(newPreferences);
//		UserClientData foundDataAfter = userClientDataRepository.save(foundData);
//		assertNotNull(foundDataAfter);
//		assertTrue(foundData.getHistory().similar(foundDataAfter.getHistory()));
//		//Preferences must remain unchanged
//		assertTrue(newPreferences.similar(foundDataAfter.getPreferences()));
//	}
//	
//	@After
//	public void tearDown() {
//		userClientDataRepository.deleteAll();
//		clientRepository.deleteAll();
//		profileRepository.deleteAll();
//	}

	@Test
	public void test() {}
	
}
