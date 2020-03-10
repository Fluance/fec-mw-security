package net.fluance.security.ehprofile.test.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import io.swagger.models.Swagger;
import net.fluance.app.data.model.identity.EhProfile;
import net.fluance.app.test.AbstractWebIntegrationTest;
import net.fluance.app.web.util.swagger.SwaggerSpecUtils;
import net.fluance.commons.codec.PKIUtils;
import net.fluance.security.core.model.jpa.Role;
import net.fluance.security.core.repository.jpa.IProfileRepository;
import net.fluance.security.core.repository.jpa.IRoleRepository;
import net.fluance.security.ehprofile.service.UserProfileService;
import net.fluance.security.ehprofile.test.Application;
import net.fluance.security.ehprofile.test.TestConfig;
import net.fluance.security.ehprofile.test.UserProfileTestConfig;

/**
 *
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class UserProfileControllerTest extends AbstractWebIntegrationTest {

	private static Logger LOGGER = LogManager.getLogger(UserProfileControllerTest.class);
	
	private static final String ADMIN_ROLE_NAME = "sysadmin";
	private static final String FINANCIAL_ROLE_NAME = "financial";
	private static final String NURSE_ROLE_NAME = "nurse";
	private static final String ADMINISTRATIVE_ROLE_NAME = "administrative";
	private static final String PHYSICIAN_ROLE_NAME = "physician";

	@Value("${admin.username}")
	private String adminUsername;
	@Value("${admin.password}")
	private String adminPassword;
	@Value("${ownprofile.username}")
	private String ownProfileUsername;
	@Value("${ownprofile.password}")
	private String ownProfilePassword;
	@Value("${nonadmin.username}")
	private String nonAdminUsername;
	@Value("${nonadmin.password}")
	private String nonAdminPassword;
	
	@Value("${context-path}")
	private String contextPath;
	
	@Autowired
    private ResourceLoader resourceLoader;
	
	private final String certificateAlias = "fluance";
	
	@Value("${server.ssl.key-store}")
	private String sslKeyStore;
	@Value("${server.ssl.key-store-password}")
	private String sslKeyStorePassword;
	@Value("${server.ssl.key-store-type}")
	private String sslKeyStoreType;
	@Value("${server.ssl.trust-store}")
	private String sslTrustStore;
	@Value("${server.ssl.trust-store-password}")
	private String sslTrustStorePassword;
	@Value("${server.ssl.trust-store-type}")
	private String sslTrustStoreType;
	
	private PublicKey publicKey;
	
	private TestRestTemplate restTemplate;
	private Role sysAdminRole;
	private Role nurseRole;
	private Role physicianRole;
	private Role administrativeRole;
	private Role financialRole;
	@Autowired
	private IProfileRepository profileRepository;
	Map<String, Header> headersMap;
	private static final String OAUTH2_OWNPROFILE_HEADER = "oauth2.ownprofile.header";
	private static final String OAUTH2_ADMIN_HEADER = "oauth2.admin.header";
	private static final String OAUTH2_NONADMIN_HEADER = "oauth2.nonadmon.header";
	@Autowired
	private TestConfig testConfig;
	@Autowired
	private UserProfileService userProfileService;
	@Autowired
	IRoleRepository roleRepository;
	@Autowired
	private UserProfileTestConfig userProfileTestConfig;
	private Swagger swaggerSpec;
	private String swaggerSpecAbsolutePath;
	private String createResourcePath;
	private String readResourcePath;
	private String updateLanguageResourcePath;
	private String readRolesResourcePath;
	private String deleteResourcePath;
	private String grantCompanyResourcePath;
	private String setCompanyResourcePath;
	private String revokeCompanyResourcePath;
	private String grantPatientUnitResourcePath;
	private String setPatientUnitResourcePath;
	private String revokePatientUnitResourcePath;
	private String grantHospServiceResourcePath;
	private String setHospServiceResourcePath;
	private String revokeHospServiceResourcePath;
	private String grantRoleResourcePath;
	private String setRoleResourcePath;
	private String revokeRoleResourcePath;
	private String checkCompany;
	@SuppressWarnings("unused")
	private String apiConsumesList;
	private String apiConsumes;

	@Autowired
	private Wso2IsMockController authServerMockController;
	private static final String DOMAIN = "PRIMARY";
	
	@PostConstruct
	public void init() {
		createResourcePath = "/";
		readResourcePath = "/" + ownProfileUsername + "/fluance";
		updateLanguageResourcePath = "/" + ownProfileUsername + "/fluance";
		readRolesResourcePath = "/" + ownProfileUsername + "/fluance/roles";
		deleteResourcePath = "/" + ownProfileUsername + "/fluance";
		grantCompanyResourcePath = "/companies/grant";
		setCompanyResourcePath = "/companies/set";
		revokeCompanyResourcePath = "/companies/revoke";
		grantPatientUnitResourcePath = "/patientunits/grant";
		setPatientUnitResourcePath = "/patientunits/set";
		revokePatientUnitResourcePath = "/patientunits/revoke";
		grantHospServiceResourcePath = "/hospservices/grant";
		setHospServiceResourcePath = "/hospservices/set";
		revokeHospServiceResourcePath = "/hospservices/revoke";
		grantRoleResourcePath = "/roles/grant";
		setRoleResourcePath = "/roles/set";
		revokeRoleResourcePath = "/roles/revoke";
		checkCompany = "/" + ownProfileUsername + "/fluance";
	}
	
	@Before
	public void setUp() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
        String keyStorePath = new File(classLoader.getResource("keystore.jks").getFile()).getAbsolutePath();
        assertTrue(new File(keyStorePath).exists());
        String trustStorePath = new File(classLoader.getResource("truststore.jks").getFile()).getAbsolutePath();
        assertTrue(new File(trustStorePath).exists());
       
        LOGGER.info("keyStorePath: " + keyStorePath);
        LOGGER.info("trustStorePath: " + trustStorePath);
		
		System.setProperty("javax.net.ssl.keyStore", keyStorePath);
		System.setProperty("javax.net.ssl.keyStoreType", "JKS");
		System.setProperty("javax.net.ssl.keyStorePassword", "fluance");	
		
		System.setProperty("javax.net.ssl.trustStore", trustStorePath);
		System.setProperty("javax.net.ssl.trustStoreType", "JKS");
		System.setProperty("javax.net.ssl.trustStorePassword", "fluance");
		
		publicKey = PKIUtils.readPublicKey(certificateAlias, PKIUtils.DEFAULT_CERTIFICATE_TYPE);
		
		String swaggerSpecFilePath = userProfileTestConfig.getUserProfileSpecFile();
		specLocation = userProfileTestConfig.getSpecsLocation();
		swaggerSpecAbsolutePath = specLocation + swaggerSpecFilePath;
		swaggerSpec = SwaggerSpecUtils.load(swaggerSpecAbsolutePath);
		
		List<String> apiConsumesList = swaggerSpec.getConsumes();
		assertNotNull(apiConsumesList);
		assertTrue(apiConsumesList.size() == 1);
		apiConsumes = apiConsumesList.get(0);
		assertEquals(userProfileTestConfig.getRequestContentType(), apiConsumes);

		restTemplate = new TestRestTemplate();
		headersMap = new HashMap<>();

		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		MappingJackson2HttpMessageConverter jsonMessageConverter = new MappingJackson2HttpMessageConverter();
		jsonMessageConverter.setObjectMapper(new ObjectMapper());
		messageConverters.add(jsonMessageConverter);
//		restTemplate.setMessageConverters(messageConverters);
		baseUrl = "http://localhost:" + serverPort + swaggerSpec.getBasePath();
		authBaseUrl = "http://localhost:" + serverPort + contextPath;

		assertNotNull(authServerMockController.getAuthorizationServerMock().getUserByUsernameAndDomain(adminUsername, DOMAIN));
		assertNotNull(authServerMockController.getAuthorizationServerMock().getUserByUsernameAndDomain(ownProfileUsername, DOMAIN));
		assertNotNull(authServerMockController.getAuthorizationServerMock().getUserByUsernameAndDomain(nonAdminUsername, DOMAIN));
		
		String adminAccessToken = getAccessToken(adminUsername, DOMAIN);
		String ownProfileAccessToken = getAccessToken(ownProfileUsername, DOMAIN);
		String nonAdminAccessToken = getAccessToken(nonAdminUsername, DOMAIN);

		headersMap.put(OAUTH2_ADMIN_HEADER, new BasicHeader("Authorization", "Bearer " + adminAccessToken));
		headersMap.put(OAUTH2_OWNPROFILE_HEADER, new BasicHeader("Authorization", "Bearer " + ownProfileAccessToken));
		headersMap.put(OAUTH2_NONADMIN_HEADER, new BasicHeader("Authorization", "Bearer " + nonAdminAccessToken));
		
		List<Header> headers = new ArrayList<>();
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));

		sysAdminRole = new Role();
		sysAdminRole.setName(ADMIN_ROLE_NAME);
		sysAdminRole.setDescription("ADMIN_ROLE_NAME" + " Desc");
		administrativeRole = new Role();
		administrativeRole.setName(ADMINISTRATIVE_ROLE_NAME);
		administrativeRole.setDescription("ADMINISTRATIVE_ROLE_NAME" + " Desc");
		financialRole = new Role();
		financialRole.setName(FINANCIAL_ROLE_NAME);
		financialRole.setDescription("FINANCIAL_ROLE_NAME" + " Desc");
		physicianRole = new Role();
		physicianRole.setName(PHYSICIAN_ROLE_NAME);
		physicianRole.setDescription("PHYSICIAN_ROLE_NAME" + " Desc");
		nurseRole = new Role();
		nurseRole.setName(NURSE_ROLE_NAME);
		nurseRole.setDescription("NURSE_ROLE_NAME" + " Desc");
		
		sysAdminRole = roleRepository.save(sysAdminRole);
		assertNotNull(sysAdminRole);
		administrativeRole = roleRepository.save(administrativeRole);
		assertNotNull(administrativeRole);
		financialRole = roleRepository.save(financialRole);
		assertNotNull(financialRole);
		physicianRole = roleRepository.save(physicianRole);
		assertNotNull(physicianRole);
		nurseRole = roleRepository.save(nurseRole);
		assertNotNull(nurseRole);
		
		
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), createResourcePath, headers,
				null, userProfileTestConfig.getCreateProfileRequestPayload(), ContentType.parse(apiConsumes), userProfileTestConfig.getRequestCharset());
		
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
	}

	@Test
	public void checkOkOnCreate() throws KeyManagementException, UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException, ParseException, ProcessingException {
		profileRepository.deleteAll();
		List<Header> headers = new ArrayList<>();
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));

		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), createResourcePath, headers,
				null, userProfileTestConfig.getCreateProfileRequestPayload(), ContentType.parse(apiConsumes), userProfileTestConfig.getRequestCharset());

		// Check content of the returned list
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);

		// test status code
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// test headers
		assertTrue(SwaggerSpecUtils.areHeadersValid(response, swaggerSpec, createResourcePath, HttpMethod.POST.name(), HttpStatus.OK));
	}

	/**
	 * 
	 * @throws KeyManagementException
	 * @throws UnsupportedOperationException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws HttpException
	 * @throws URISyntaxException
	 * @throws ParseException
	 * @throws ProcessingException
	 * @throws TransformerException 
	 * @throws TransformerFactoryConfigurationError 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XPathExpressionException 
	 * @throws NumberFormatException 
	 */
	@Test
	public void checkOkOnDelete() throws Exception {
		List<Header> headers = new ArrayList<>();
		
		// Read by admin
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.DELETE.name(), deleteResourcePath, headers,
				null, null, null, null);

		// Check content of the returned list
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);

		// test status code
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// test headers
		assertTrue(SwaggerSpecUtils.areHeadersValid(response, swaggerSpec, deleteResourcePath, HttpMethod.DELETE.name(), HttpStatus.OK));
		EhProfile deleteProfile = userProfileService.find(userProfileTestConfig.getExpectedUsername(), userProfileTestConfig.getExpectedDomain());
		assertNull(deleteProfile);
	}
	
	@Test
	public void checkOkOnRead() throws KeyManagementException, UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException, ParseException, ProcessingException {
		List<Header> headers = new ArrayList<>();
		
		// Read by admin
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.GET.name(), readResourcePath, headers,
				null, null, null, null);

		// Check content of the returned list
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);

		// test status code
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// test headers
		assertTrue(SwaggerSpecUtils.areHeadersValid(response, swaggerSpec, readResourcePath, HttpMethod.GET.name(), HttpStatus.OK));
		
		// Read own profile
		headers.clear();
		headers.add(headersMap.get(OAUTH2_OWNPROFILE_HEADER));
		response = invokeResource(HttpMethod.GET.name(), readResourcePath, headers,
				null, null, null, null);
		// test status code
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void checkOkOnReadRoles() throws KeyManagementException, UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException, ParseException, ProcessingException {
		List<Header> headers = new ArrayList<>();
		
		// Read by admin
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.GET.name(), readRolesResourcePath, headers,
				null, null, null, null);

		// Check content of the returned list
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		
		// test status code
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// Test response body
		ObjectMapper objectMapper = new ObjectMapper();
		List<String> roles = objectMapper.readValue(jsonResponse,
				objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
		assertNotNull(roles);
		assertEquals(2, roles.size());
		assertTrue(roles.contains(ADMINISTRATIVE_ROLE_NAME));
		assertTrue(roles.contains(PHYSICIAN_ROLE_NAME));


		// Read own profile
		headers.clear();
		headers.add(headersMap.get(OAUTH2_OWNPROFILE_HEADER));
		response = invokeResource(HttpMethod.GET.name(), readRolesResourcePath, headers,
				null, null, null, null);
		jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		// test status code
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		roles = objectMapper.readValue(jsonResponse,
				objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
		assertNotNull(roles);
		assertEquals(2, roles.size());
		assertTrue(roles.contains(ADMINISTRATIVE_ROLE_NAME));
		assertTrue(roles.contains(PHYSICIAN_ROLE_NAME));
	}
	
	@Test
	public void checkOkOnLanguageUpdate() throws KeyManagementException, UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException, ParseException, ProcessingException, NumberFormatException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
		List<Header> headers = new ArrayList<>();
		
		// Admin must be able to set companies
		headers.clear();
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), updateLanguageResourcePath + "/fr", headers,
				null, "{}", ContentType.parse(apiConsumes), userProfileTestConfig.getRequestCharset());
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		assertTrue(SwaggerSpecUtils.areHeadersValid(response, swaggerSpec, updateLanguageResourcePath, HttpMethod.POST.name(), HttpStatus.OK));
	}
	
	/**
	 * 
	 * @throws KeyManagementException
	 * @throws UnsupportedOperationException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws HttpException
	 * @throws URISyntaxException
	 * @throws ParseException
	 * @throws ProcessingException
	 * @throws TransformerException 
	 * @throws TransformerFactoryConfigurationError 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XPathExpressionException 
	 * @throws NumberFormatException 
	 */
	@Test
	public void checkOkOnCompaniesGrant() throws Exception {
		List<Header> headers = new ArrayList<>();
		
		// Admin must be able to grant companies
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), grantCompanyResourcePath, headers,
				null, userProfileTestConfig.getGrantCompaniesRequestPayload(), ContentType.parse(apiConsumes), userProfileTestConfig.getRequestCharset());
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		assertTrue(SwaggerSpecUtils.areHeadersValid(response, swaggerSpec, grantCompanyResourcePath, HttpMethod.POST.name(), HttpStatus.OK));
		assertEquals(userProfileTestConfig.getGrantCompaniesResponseMessage(), jsonResponse);
		
		//Check that changes have been successfully done
		EhProfile updatedProfile = userProfileService.find(userProfileTestConfig.getExpectedUsername(), userProfileTestConfig.getExpectedDomain());
		JsonNode startCompaniesNode = new ObjectMapper().readTree(userProfileTestConfig.getCreateProfileRequestPayload());
		int startCompaniesNb = startCompaniesNode.get("grants").get("companies").size();
		JsonNode newCompaniesNode = new ObjectMapper().readTree(userProfileTestConfig.getGrantCompaniesRequestPayload());
		int newCompaniesNb = newCompaniesNode.get("companies").size();
//		assertTrue(updatedProfile.getGrants().getGrantedCompanies().size()==(startCompaniesNb+newCompaniesNb));
	}
	
	@Test
	public void checkOkOnCompaniesSet() throws KeyManagementException, UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException, ParseException, ProcessingException, NumberFormatException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
		List<Header> headers = new ArrayList<>();
		
		// Admin must be able to set companies
		headers.clear();
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), setCompanyResourcePath, headers,
				null, userProfileTestConfig.getSetCompaniesRequestPayload(), ContentType.parse(apiConsumes), userProfileTestConfig.getRequestCharset());
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		assertTrue(SwaggerSpecUtils.areHeadersValid(response, swaggerSpec, setCompanyResourcePath, HttpMethod.POST.name(), HttpStatus.OK));
	}
	
	@Test
	public void checkOkOnCompaniesRevoke() throws KeyManagementException, UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException, ParseException, ProcessingException, NumberFormatException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
		List<Header> headers = new ArrayList<>();
		headers.clear();
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), revokeCompanyResourcePath, headers,
				null, userProfileTestConfig.getRevokeCompaniesRequestPayload(), ContentType.parse(apiConsumes), userProfileTestConfig.getRequestCharset());
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		assertTrue(SwaggerSpecUtils.areHeadersValid(response, swaggerSpec, revokeCompanyResourcePath, HttpMethod.POST.name(), HttpStatus.OK));
	}
	
	@Test
	public void checkOkOnHospServicesGrant() throws Exception {
		List<Header> headers = new ArrayList<>();
		
		// Admin must be able to grant hospservices
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), grantHospServiceResourcePath, headers,
				null, userProfileTestConfig.getGrantHospServicesRequestPayload(), ContentType.parse(apiConsumes), userProfileTestConfig.getRequestCharset());
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		assertTrue(SwaggerSpecUtils.areHeadersValid(response, swaggerSpec, grantHospServiceResourcePath, HttpMethod.POST.name(), HttpStatus.OK));
		assertEquals(userProfileTestConfig.getGrantHospServicesResponseMessage(), jsonResponse);
		
		//Check that changes have been successfully done
		EhProfile updatedProfile = userProfileService.find(userProfileTestConfig.getExpectedUsername(), userProfileTestConfig.getExpectedDomain());
		JsonNode startHospSvcNode = new ObjectMapper().readTree(userProfileTestConfig.getCreateProfileRequestPayload());
		int startHospSvcNb = startHospSvcNode.get("grants").get("companies").get(0).get("hospservices").size();
		JsonNode newHospSvcNode = new ObjectMapper().readTree(userProfileTestConfig.getGrantHospServicesRequestPayload());
		int newHospSvcNb = newHospSvcNode.get("hospservices").size();
//		assertTrue(updatedProfile.getGrants().getGrantedCompanies().get(0).getHospservices().size()==(startHospSvcNb+newHospSvcNb));
	}
	
	@Test
	public void checkOkOnHospServicesSet() throws Exception {
		List<Header> headers = new ArrayList<>();
		
		// Admin must be able to set hospservices
		headers.clear();
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), setHospServiceResourcePath, headers,
				null, userProfileTestConfig.getSetHospServicesRequestPayload(), ContentType.parse(apiConsumes), userProfileTestConfig.getRequestCharset());
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		assertTrue(SwaggerSpecUtils.areHeadersValid(response, swaggerSpec, setHospServiceResourcePath, HttpMethod.POST.name(), HttpStatus.OK));
	}
	
	@Test
	public void checkOkOnHospServicesRevoke() throws KeyManagementException, UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException, ParseException, ProcessingException, NumberFormatException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
		List<Header> headers = new ArrayList<>();
		
		headers.clear();
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), revokeHospServiceResourcePath, headers,
				null, userProfileTestConfig.getSetHospServicesRequestPayload(), ContentType.parse(apiConsumes), userProfileTestConfig.getRequestCharset());
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		assertTrue(SwaggerSpecUtils.areHeadersValid(response, swaggerSpec, revokeHospServiceResourcePath, HttpMethod.POST.name(), HttpStatus.OK));
	}
	
	/**
	 * 
	 * @throws KeyManagementException
	 * @throws UnsupportedOperationException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws HttpException
	 * @throws URISyntaxException
	 * @throws ParseException
	 * @throws ProcessingException
	 * @throws TransformerException 
	 * @throws TransformerFactoryConfigurationError 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XPathExpressionException 
	 * @throws NumberFormatException 
	 */
	@Test
	public void checkOkOnPatientUnitsGrant() throws Exception {
		List<Header> headers = new ArrayList<>();
		
		// Admin must be able to grant patientunits
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), grantPatientUnitResourcePath, headers,
				null, userProfileTestConfig.getGrantPatientUnitsRequestPayload(), ContentType.parse(apiConsumes), userProfileTestConfig.getRequestCharset());
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		assertTrue(SwaggerSpecUtils.areHeadersValid(response, swaggerSpec, grantPatientUnitResourcePath, HttpMethod.POST.name(), HttpStatus.OK));
		assertEquals(userProfileTestConfig.getGrantPatientUnitsResponseMessage(), jsonResponse);
		
		//Check that changes have been successfully done
		EhProfile updatedProfile = userProfileService.find(userProfileTestConfig.getExpectedUsername(), userProfileTestConfig.getExpectedDomain());
		JsonNode startPatientUnitNode = new ObjectMapper().readTree(userProfileTestConfig.getCreateProfileRequestPayload());
		int startPatientUnitNb = startPatientUnitNode.get("grants").get("companies").get(0).get("patientunits").size();
		JsonNode newPatientUnitNode = new ObjectMapper().readTree(userProfileTestConfig.getGrantPatientUnitsRequestPayload());
		int newPatientUnitNb = newPatientUnitNode.get("patientunits").size();
//		assertTrue(updatedProfile.getGrants().getGrantedCompanies().get(0).getPatientunits().size()==(startPatientUnitNb+newPatientUnitNb));
	}
	
	@Test
	public void checkOkOnPatientUnitsSet() throws KeyManagementException, UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException, ParseException, ProcessingException, NumberFormatException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
		List<Header> headers = new ArrayList<>();
		
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), setPatientUnitResourcePath, headers,
				null, userProfileTestConfig.getSetPatientUnitsRequestPayload(), ContentType.parse(apiConsumes), userProfileTestConfig.getRequestCharset());
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		assertTrue(SwaggerSpecUtils.areHeadersValid(response, swaggerSpec, setPatientUnitResourcePath, HttpMethod.POST.name(), HttpStatus.OK));
	}
	
	@Test
	public void checkOkOnPatientUnitsRevoke() throws KeyManagementException, UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException, ParseException, ProcessingException, NumberFormatException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
		List<Header> headers = new ArrayList<>();
		
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), revokePatientUnitResourcePath, headers,
				null, userProfileTestConfig.getSetPatientUnitsRequestPayload(), ContentType.parse(apiConsumes), userProfileTestConfig.getRequestCharset());
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		assertTrue(SwaggerSpecUtils.areHeadersValid(response, swaggerSpec, revokePatientUnitResourcePath, HttpMethod.POST.name(), HttpStatus.OK));
	}
	
	/**
	 * 
	 * @throws KeyManagementException
	 * @throws UnsupportedOperationException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws HttpException
	 * @throws URISyntaxException
	 * @throws ParseException
	 * @throws ProcessingException
	 * @throws TransformerException 
	 * @throws TransformerFactoryConfigurationError 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XPathExpressionException 
	 * @throws NumberFormatException 
	 */
	@Test
	public void checkOkOnRolesGrant() throws Exception {
		List<Header> headers = new ArrayList<>();
		
		// Admin must be able to grant roles
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), grantRoleResourcePath, headers,
				null, userProfileTestConfig.getGrantRolesRequestPayload(), ContentType.parse(apiConsumes), userProfileTestConfig.getRequestCharset());
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		assertTrue(SwaggerSpecUtils.areHeadersValid(response, swaggerSpec, grantRoleResourcePath, HttpMethod.POST.name(), HttpStatus.OK));
		ObjectMapper mapper = new ObjectMapper();
		JsonNode reqBodyContentJson = mapper.readTree(userProfileTestConfig.getGrantRolesRequestPayload());
		ArrayNode jsonRolesToGrant = (ArrayNode) reqBodyContentJson.get("roles");
		List<String> rolesToGrant = jsonRolesToGrant.findValuesAsText("roles");
//		List<String> rolesToGrant = new ArrayList<>();
		
//		assertEquals(userProfileTestConfig.getGrantRolesResponseMessage(), jsonResponse);
		
		//Check that changes have been successfully done
		EhProfile updatedProfile = userProfileService.find(userProfileTestConfig.getExpectedUsername(), userProfileTestConfig.getExpectedDomain());
		JsonNode startRoleNode = new ObjectMapper().readTree(userProfileTestConfig.getCreateProfileRequestPayload());
		int startRoleNb = startRoleNode.get("grants").get("roles").size();
		JsonNode newRoleNode = new ObjectMapper().readTree(userProfileTestConfig.getGrantRolesRequestPayload());
		int newRoleNb = newRoleNode.get("roles").size();
		//+1 role because of the everyone role
		//assertTrue(updatedProfile.getGrants().getRoles().size()==(startRoleNb+newRoleNb));
	}
	
	@Test
	public void checkOkOnRolesSet() throws KeyManagementException, UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException, ParseException, ProcessingException, NumberFormatException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
		List<Header> headers = new ArrayList<>();

		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), setRoleResourcePath, headers,
				null, userProfileTestConfig.getSetRolesRequestPayload(), ContentType.parse(apiConsumes), userProfileTestConfig.getRequestCharset());
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		assertTrue(SwaggerSpecUtils.areHeadersValid(response, swaggerSpec, setRoleResourcePath, HttpMethod.POST.name(), HttpStatus.OK));
	}
	
	@Test
	public void checkOkOnRolesRevoke() throws KeyManagementException, UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException, ParseException, ProcessingException, NumberFormatException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
		List<Header> headers = new ArrayList<>();
		
		headers.clear();
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), revokeRoleResourcePath, headers,
				null, userProfileTestConfig.getSetRolesRequestPayload(), ContentType.parse(apiConsumes), userProfileTestConfig.getRequestCharset());
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		assertTrue(SwaggerSpecUtils.areHeadersValid(response, swaggerSpec, revokeRoleResourcePath, HttpMethod.POST.name(), HttpStatus.OK));
	}

//	@Test
//	Disable test Since it's not working.
	public void checkOkOnCompanyCheck() throws KeyManagementException, UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException, ParseException, ProcessingException {
		List<Header> headers = new ArrayList<>();
		
		// Test Granted company
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.GET.name(), checkCompany + "/CDG", headers, null, null, null, null);
		// Check content of the returned list
		String jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		// test status code
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		// Test response body
		ObjectMapper objectMapper = new ObjectMapper();
		Boolean exists = objectMapper.readValue(jsonResponse, Boolean.class);
		assertNotNull(exists);
		assertTrue(exists);

		// NOT Granted company
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		response = invokeResource(HttpMethod.GET.name(), checkCompany + "/ANY_COMPANY_CODE", headers, null, null, null, null);
		// Check content of the returned list
		jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		// test status code
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		// Test response body
		objectMapper = new ObjectMapper();
		exists = objectMapper.readValue(jsonResponse, Boolean.class);
		assertNotNull(exists);
		assertTrue(exists);

		// Read by a non admin
		headers.add(headersMap.get(OAUTH2_NONADMIN_HEADER));
		response = invokeResource(HttpMethod.GET.name(), checkCompany + "/CDG", headers, null, null, null, null);
		// Check content of the returned list
		jsonResponse = EntityUtils.toString(response.getEntity());
		LOGGER.info("Response.Body = " + jsonResponse);
		// test status code
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
	}
	
	@Override
	@After
	public void tearDown() {
		swaggerSpec = null;
		profileRepository.deleteAll();
		roleRepository.deleteAll();
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
	public void checkOk(Object... args)
			throws KeyManagementException, UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException,
			IOException, HttpException, URISyntaxException, ProcessingException, NumberFormatException, ParseException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
	}
}
