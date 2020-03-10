/**
 * 
 */
package net.fluance.security.core.repository.jpa;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;
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
import net.fluance.security.core.repository.jpa.IClientRepository;

@EnableJpaRepositories(basePackages="net.fluance")
@EntityScan("net.fluance")
@ComponentScan("net.fluance")
@SpringBootTest(classes = Application.class)
public class ClientRepositoryTest extends AbstractTest {

	private static final Logger LOGGER = LogManager.getLogger(ClientRepositoryTest.class);
	private static final String EXPECTED_APP_NAME = "My client application";
	private static final String EXPECTED_APP_DESCRIPTION = "Description of My client application";
	@Autowired
	private IClientRepository repository;
	private Client expectedClientApp;
	
	@Before
	public void setUp() throws NoSuchAlgorithmException {
		repository.deleteAll();
		expectedClientApp = new Client();
		expectedClientApp.setName(EXPECTED_APP_NAME);
		expectedClientApp.setDescription(EXPECTED_APP_DESCRIPTION);
		UUID clientId = UUID.randomUUID();
		String clientSecret = clientId.toString() + new Date().toString();
		expectedClientApp.setId(clientId);
		String hashedSecret = MD5Utils.md5HexHash(clientSecret);
		expectedClientApp.setSecret(hashedSecret);
		LOGGER.info("Expected Client ID: " + expectedClientApp.getId());
		expectedClientApp = repository.save(expectedClientApp);
	}
	
	@Test
	public void mustCreateTest() throws NoSuchAlgorithmException {
		repository.deleteAll();
		repository.deleteAll();
		expectedClientApp = new Client();
		expectedClientApp.setName(EXPECTED_APP_NAME);
		expectedClientApp.setDescription(EXPECTED_APP_DESCRIPTION);
		UUID clientId = UUID.randomUUID();
		String clientSecret = clientId.toString() + new Date().toString();
		expectedClientApp.setId(clientId);
		String hashedSecret = MD5Utils.md5HexHash(clientSecret);
		expectedClientApp.setSecret(hashedSecret);
		LOGGER.info("Expected Client ID: " + expectedClientApp.getId());
		Client createdClientApp = repository.save(expectedClientApp);
		LOGGER.info("[testCreate()] Client ID (expected): " + expectedClientApp.getId());
		LOGGER.info("[testCreate()] Client ID (created): " + createdClientApp.getId());
		assertTrue((createdClientApp != null) && (expectedClientApp.getId().equals(createdClientApp.getId())));
		assertTrue((createdClientApp != null) && (expectedClientApp.getSecret().equals(createdClientApp.getSecret())));
		assertTrue((createdClientApp != null) && (expectedClientApp.getName().equals(createdClientApp.getName())));
		assertTrue((createdClientApp != null) && (expectedClientApp.getDescription().equals(createdClientApp.getDescription())));
	}
	
	@Test
	@Transactional
	public void mustFindByClientIdTest() {
		Client foundClientApp = repository.findOne(expectedClientApp.getId());
		assertTrue((foundClientApp != null) && (expectedClientApp.getId().equals(foundClientApp.getId())));
		assertTrue((foundClientApp != null) && (expectedClientApp.getSecret().equals(foundClientApp.getSecret())));
		assertTrue((foundClientApp != null) && (expectedClientApp.getName().equals(foundClientApp.getName())));
		assertTrue((foundClientApp != null) && (expectedClientApp.getDescription().equals(foundClientApp.getDescription())));
	}
	
	@Test
	public void mustUpdateTest() {
		Client clientApp = repository.findByName(expectedClientApp.getName());
		String newName = "New name";
		String newDescription = "New description";
		if(clientApp != null) {
			clientApp.setName(newName);
			clientApp.setDescription(newDescription);
			assertTrue((newName.equals(clientApp.getName()) && (newDescription.equals(clientApp.getDescription()))));
		}
	}
	
	@Test
	public void mustDeleteTest() {
		Client clientApp = repository.findByName(expectedClientApp.getName());
		if(clientApp != null) {
			repository.delete(clientApp);
			clientApp = repository.findByName(expectedClientApp.getName());
			assertNull(clientApp);
		}
	}
	
	@After
	public void tearDown() {
		repository.deleteAll();
	}
	
}
