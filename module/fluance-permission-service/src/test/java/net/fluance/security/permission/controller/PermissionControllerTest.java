package net.fluance.security.permission.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import io.swagger.models.Swagger;
import net.fluance.app.security.service.support.entitlement.EntitlementDecision;
import net.fluance.app.test.AbstractWebIntegrationTest;
import net.fluance.app.web.util.swagger.SwaggerSpecUtils;
import net.fluance.security.permission.PermissionTestConfig;
import net.fluance.security.permission.app.Application;
import net.fluance.security.permission.support.payload.response.XacmlPolicyTestResponse;

@ComponentScan("net.fluance.security.permission")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class PermissionControllerTest extends AbstractWebIntegrationTest {

	private static Logger LOGGER = LogManager.getLogger(PermissionControllerTest.class);

	private TestRestTemplate restTemplate;
	Map<String, Header> headersMap;
	@Autowired
	private PermissionTestConfig permissionTestConfig;
	private Swagger swaggerSpec;
	private String swaggerSpecAbsolutePath;
	private String xacmlEvaluateResourcePath = "/xacml/evaluate";
	private String policyTestResourcePath = "/xacml/policy/test";
	@SuppressWarnings("unused")
	private String apiConsumesList;
	@Autowired
    private ResourceLoader resourceLoader;

	@Autowired
	private WebApplicationContext webAppContext;

	@Before
	public void setUp() throws Exception {
		String swaggerSpecFilePath = permissionTestConfig.getPermissionSpecFile();
		specLocation = permissionTestConfig.getSpecsLocation();
		swaggerSpecAbsolutePath = specLocation + swaggerSpecFilePath;
		swaggerSpec = SwaggerSpecUtils.load(swaggerSpecAbsolutePath);

		restTemplate = new TestRestTemplate();
		headersMap = new HashMap<>();

		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		MappingJackson2HttpMessageConverter jsonMessageConverter = new MappingJackson2HttpMessageConverter();
		jsonMessageConverter.setObjectMapper(new ObjectMapper());
		messageConverters.add(jsonMessageConverter);
//		restTemplate.setMessageConverters(messageConverters);
		baseUrl = "http://localhost:" + serverPort + swaggerSpec.getBasePath();

		mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
	}
	
	@Test
	public void checkXacmlPermit() throws TransformerFactoryConfigurationError, Exception {

		String strContentType = permissionTestConfig.getRequestContentType()
				+ ((permissionTestConfig.getRequestCharset() != null) ? (";charset=" + permissionTestConfig.getRequestCharset())
						: "");
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode createRequestContentNode = mapper.createObjectNode();
		ArrayNode jsonRolesNode = mapper.createArrayNode();
		createRequestContentNode.put("resource", permissionTestConfig.getResourceApplicable());
		createRequestContentNode.put("username", "test");
		createRequestContentNode.put("domain", "test");
		createRequestContentNode.put("action", permissionTestConfig.getPermittedAction());
		jsonRolesNode.add(permissionTestConfig.getPermittedRole1());
		jsonRolesNode.add(permissionTestConfig.getPermittedRole2());
		createRequestContentNode.set("user_roles", jsonRolesNode);
		
		String content = createRequestContentNode.toString();
		
		mockMvc.perform(MockMvcRequestBuilders.get(xacmlEvaluateResourcePath)
				.content(content)
				.contentType(MediaType.parseMediaType(strContentType)))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(EntitlementDecision.PERMIT)));
		
		mockMvc.perform(MockMvcRequestBuilders.get(xacmlEvaluateResourcePath)
				.content(content)
				.contentType(MediaType.parseMediaType(strContentType)))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(EntitlementDecision.PERMIT)));

		createRequestContentNode.put("resource", permissionTestConfig.getResourceApplicable());
		createRequestContentNode.put("username", "test");
		createRequestContentNode.put("domain", "test");
		createRequestContentNode.put("action", permissionTestConfig.getPermittedAction());
		jsonRolesNode.add(permissionTestConfig.getPermittedRole1());
		jsonRolesNode.add(permissionTestConfig.getUnpermittedRole1());
		createRequestContentNode.set("user_roles", jsonRolesNode);
		
		mockMvc.perform(MockMvcRequestBuilders.get(xacmlEvaluateResourcePath)
				.content(createRequestContentNode.toString())
				.contentType(MediaType.parseMediaType(strContentType)))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(EntitlementDecision.PERMIT)));
		
		@SuppressWarnings("unchecked")
		List<String> userRoles =  (List<String>)(mapper.readValue(mapper.writeValueAsString(createRequestContentNode.get("user_roles")), mapper.getTypeFactory().constructCollectionType(List.class, String.class)));
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(xacmlEvaluateResourcePath)
				.param("resource", createRequestContentNode.get("resource").textValue())
				.param("username", createRequestContentNode.get("username").textValue())
				.param("domain", createRequestContentNode.get("domain").textValue())
				.param("action", createRequestContentNode.get("action").textValue());
		request.param("user_roles", userRoles.get(0), userRoles.get(1), userRoles.get(2), userRoles.get(3));
		
		mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(EntitlementDecision.PERMIT)));
	}
	
	@Test
	public void checkXacmlNotApplicable() throws TransformerFactoryConfigurationError, Exception {

		String strContentType = permissionTestConfig.getRequestContentType()
				+ ((permissionTestConfig.getRequestCharset() != null) ? (";charset=" + permissionTestConfig.getRequestCharset())
						: "");
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode createRequestContentNode = mapper.createObjectNode();
		createRequestContentNode.put("resource", permissionTestConfig.getResourceNotApplicable());
		createRequestContentNode.put("username", "test");
		createRequestContentNode.put("domain", "test");
		createRequestContentNode.put("action", permissionTestConfig.getPermittedAction());
		ArrayNode jsonRolesNode = mapper.createArrayNode();
		jsonRolesNode.add(permissionTestConfig.getPermittedRole1());
		jsonRolesNode.add(permissionTestConfig.getPermittedRole2());
		createRequestContentNode.set("user_roles", jsonRolesNode);
		
		mockMvc.perform(MockMvcRequestBuilders.get(xacmlEvaluateResourcePath)
				.content(createRequestContentNode.toString())
				.contentType(MediaType.parseMediaType(strContentType)))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(EntitlementDecision.NOT_APPLICABLE)));

	}
	
	@Test
	public void checkXacmlDenyForRole() throws TransformerFactoryConfigurationError, Exception {

		String strContentType = permissionTestConfig.getRequestContentType()
				+ ((permissionTestConfig.getRequestCharset() != null) ? (";charset=" + permissionTestConfig.getRequestCharset())
						: "");
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode createRequestContentNode = mapper.createObjectNode();
		createRequestContentNode.put("resource", permissionTestConfig.getResourceApplicable());
		createRequestContentNode.put("username", "test");
		createRequestContentNode.put("domain", "test");
		createRequestContentNode.put("action", permissionTestConfig.getPermittedAction());
		ArrayNode jsonRolesNode = mapper.createArrayNode();
		jsonRolesNode.add(permissionTestConfig.getUnpermittedRole1());
		jsonRolesNode.add(permissionTestConfig.getUnpermittedRole2());
		createRequestContentNode.set("user_roles", jsonRolesNode);
		
		mockMvc.perform(MockMvcRequestBuilders.get(xacmlEvaluateResourcePath)
				.content(createRequestContentNode.toString())
				.contentType(MediaType.parseMediaType(strContentType)))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(EntitlementDecision.DENY)));

	}
	
	@Test
	public void checkXacmlDenyForAction() throws TransformerFactoryConfigurationError, Exception {

		String strContentType = permissionTestConfig.getRequestContentType()
				+ ((permissionTestConfig.getRequestCharset() != null) ? (";charset=" + permissionTestConfig.getRequestCharset())
						: "");
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode createRequestContentNode = mapper.createObjectNode();
		ArrayNode jsonRolesNode = mapper.createArrayNode();
		
		// Check for allowed roles and unallowed action 1
		createRequestContentNode.put("resource", permissionTestConfig.getResourceApplicable());
		createRequestContentNode.put("username", "test");
		createRequestContentNode.put("domain", "test");
		createRequestContentNode.put("action", permissionTestConfig.getUnpermittedAction1());
		jsonRolesNode.add(permissionTestConfig.getPermittedRole1());
		jsonRolesNode.add(permissionTestConfig.getPermittedRole2());
		createRequestContentNode.set("user_roles", jsonRolesNode);
		
		mockMvc.perform(MockMvcRequestBuilders.get(xacmlEvaluateResourcePath)
				.content(createRequestContentNode.toString())
				.contentType(MediaType.parseMediaType(strContentType)))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(EntitlementDecision.DENY)));
		
		// Check for allowed roles and unallowed action 2
		createRequestContentNode.put("resource", permissionTestConfig.getResourceApplicable());
		createRequestContentNode.put("username", "test");
		createRequestContentNode.put("domain", "test");
		createRequestContentNode.put("action", permissionTestConfig.getUnpermittedAction2());
		jsonRolesNode.add(permissionTestConfig.getPermittedRole1());
		jsonRolesNode.add(permissionTestConfig.getPermittedRole2());
		createRequestContentNode.set("user_roles", jsonRolesNode);
		
		mockMvc.perform(MockMvcRequestBuilders.get(xacmlEvaluateResourcePath)
				/*.header("Authorization", "Basic " + webConfig.getPermissionManagementUsername() + ":" + webConfig.getPermissionManagementUserPassword())*/.content(createRequestContentNode.toString())
				.contentType(MediaType.parseMediaType(strContentType)))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(EntitlementDecision.DENY)));

	}
	
	@Test
	public void checkPolicyTest() throws Exception {

		Resource requestContentResource = resourceLoader.getResource("classpath:sample-request-body.json");
		File requestContentFile = requestContentResource.getFile();
		
		Resource policyResource = resourceLoader.getResource("classpath:xacml-policies/FakePolicy.xml");
		InputStream policyFileInputStream = policyResource.getInputStream();
		MockMultipartFile policyFile = new MockMultipartFile("file", "FakePolicy.xml", null, policyFileInputStream);
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode requestPayload = mapper.readTree(requestContentFile);
		
		String testData = mapper.readTree(requestContentFile).toString();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.fileUpload(policyTestResourcePath).file(policyFile)
				.content(requestPayload.toString())
				.param("testData", testData))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andReturn();

		assertNotNull(result.getResponse());
		assertNotNull(result.getResponse().getContentAsString());
		assertNotEquals(result.getResponse().getContentAsString(), "");
		XacmlPolicyTestResponse response = mapper.readValue(result.getResponse().getContentAsString(), XacmlPolicyTestResponse.class);
		assertEquals("FakePolicy", response.getPolicyUnderTest().getId());
		assertNotNull(response.getPolicyUnderTest().getRules());
		assertEquals(2, response.getPolicyUnderTest().getRules().size());
		assertEquals(".*/fake-api/.*", response.getPolicyUnderTest().getApiTargetRegex());
		assertNotNull(response.getTestResults());
		assertEquals(5, response.getTestResults().size());
	}
	
	@Override
	public void tearDown() {
		swaggerSpec = null;
		mockMvc = null;
	}

	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	@Override
	protected boolean checkOAuth2Authorization(Object... params) {
		return false;
	}

	@Override
	public void checkOk(Object... args) throws KeyManagementException, UnsupportedOperationException,
			NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException,
			ProcessingException, NumberFormatException, ParseException, XPathExpressionException,
			ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
	}
}
