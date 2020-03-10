package net.fluance.security.permission.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import net.fluance.app.security.service.support.entitlement.EntitlementDecision;
import net.fluance.app.security.service.xacml.XacmlPDP;
import net.fluance.security.core.model.jpa.Profile;
import net.fluance.security.core.repository.jpa.IProfileRepository;

@RunWith(SpringRunner.class)
public class XacmlPermissionServiceTest {

	@TestConfiguration
	static class XacmlPermissionServiceTestConfiguration {
		@Bean
		public XacmlPermissionService xacmlPermissionService() {
			return new XacmlPermissionService();
		}
	}
	
	@Before
	public void setUp() throws Exception {
		Mockito.reset(xacmlPDP);
		Mockito.reset(profileRepository);
		Profile profile = new Profile();
		profile.setUsername("Test");
		profile.setDomainName("LOCAL");
		
		Mockito.when(profileRepository.findByUsernameAndDomainName(Mockito.anyString(), Mockito.anyString())).thenReturn(profile);
		
		Mockito.when(xacmlPDP.evaluate(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyObject())).thenReturn(EntitlementDecision.PERMIT);
	}
	
	@Autowired
	private XacmlPermissionService xacmlPermissionService;
	
	@MockBean
	private XacmlPDP xacmlPDP;
	
	@MockBean
	private IProfileRepository profileRepository;
	
	@Test
	public void evaluate_shouldReturn() throws JsonParseException, JsonMappingException, JsonProcessingException, IOException, Exception {
		EntitlementDecision entitlementDecision;
		
		String payload = null;
		String resource = null;
		String username = null;
		String domain = null;
		String action = null;
		List<String> roles = null;
		
		username = "test";
		domain = "LOCAL";
		
		payload = "{\"resource\":\"https://mojito.dev.fluance.net:8443/ehprofile/profile/my\",\"username\":\"amartinez\",\"domain\":\"PRIMARY\",\"action\":\"GET\"}";
		entitlementDecision = xacmlPermissionService.evaluate(payload, resource, username, domain, action, roles);		
		assertNotNull("Should return value", entitlementDecision);
		assertEquals("Should Permit", entitlementDecision.getDecision(), "Permit");
		payload = null;
		
		payload = "{\"resource\":\"https://mojito.dev.fluance.net:8443/ehprofile/profile/my\",\"username\":\"amartinez\",\"domain\":\"PRIMARY\",\"action\":\"GET\",\"user_roles\":[role1,role2]}";
		entitlementDecision = xacmlPermissionService.evaluate(payload, resource, username, domain, action, roles);		
		assertNotNull("Should return value", entitlementDecision);
		assertEquals("Should Permit", entitlementDecision.getDecision(), "Permit");
		payload = null;
		
		resource = "foo";		
		action = "GET";
		roles = null;
		entitlementDecision = xacmlPermissionService.evaluate(payload, resource, username, domain, action, roles);		
		assertNotNull("Should return value", entitlementDecision);
		assertEquals("Should Permit", entitlementDecision.getDecision(), "Permit");
				
		roles = Arrays.asList("role1","role2");
		entitlementDecision = xacmlPermissionService.evaluate(payload, resource, username, domain, action, roles);		
		assertNotNull("Should return value", entitlementDecision);
		assertEquals("Should Permit", entitlementDecision.getDecision(), "Permit");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void evaluate_username_domain_null() throws JsonParseException, JsonMappingException, JsonProcessingException, IOException, Exception {
		String payload = null;
		String resource = null;
		String username = null;
		String domain = null;
		String action = null;
		List<String> roles = null;
		
		resource = "foo";		
		action = "GET";
		xacmlPermissionService.evaluate(payload, resource, username, domain, action, roles);		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void evaluate_username_domain_null_in_payload() throws JsonParseException, JsonMappingException, JsonProcessingException, IOException, Exception {
		String payload = null;
		String resource = null;
		String username = null;
		String domain = null;
		String action = null;
		List<String> roles = null;
		
		payload = "{\"resource\":\"https://mojito.dev.fluance.net:8443/ehprofile/profile/my\",\"action\":\"GET\"}";
		xacmlPermissionService.evaluate(payload, resource, username, domain, action, roles);		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void evaluate_resource_null_in_payload() throws JsonParseException, JsonMappingException, JsonProcessingException, IOException, Exception {
		String payload = null;
		String resource = null;
		String username = null;
		String domain = null;
		String action = null;
		List<String> roles = null;
		
		payload = "{\"username\":\"amartinez\",\"domain\":\"PRIMARY\",\"action\":\"GET\"}";
		xacmlPermissionService.evaluate(payload, resource, username, domain, action, roles);		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void evaluate_action_null_in_payload() throws JsonParseException, JsonMappingException, JsonProcessingException, IOException, Exception {
		String payload = null;
		String resource = null;
		String username = null;
		String domain = null;
		String action = null;
		List<String> roles = null;
		
		payload = "{\"resource\":\"https://mojito.dev.fluance.net:8443/ehprofile/profile/my\",\"username\":\"amartinez\",\"domain\":\"PRIMARY\"}";
		xacmlPermissionService.evaluate(payload, resource, username, domain, action, roles);		
	}
}
