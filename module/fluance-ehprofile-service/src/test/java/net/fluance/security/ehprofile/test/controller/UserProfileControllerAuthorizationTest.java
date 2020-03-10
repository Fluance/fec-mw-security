package net.fluance.security.ehprofile.test.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.UnsupportedCharsetException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import io.swagger.models.Swagger;
import net.fluance.app.test.AbstractWebIntegrationTest;
import net.fluance.app.web.util.swagger.SwaggerSpecUtils;
import net.fluance.security.core.model.jpa.Role;
import net.fluance.security.core.repository.jdbc.CompanyRepository;
import net.fluance.security.core.repository.jpa.IProfileRepository;
import net.fluance.security.core.repository.jpa.IRoleRepository;
import net.fluance.security.core.repository.jpa.IUserClientDataRepository;
import net.fluance.security.core.repository.jpa.IUserCompanyIdentityRepository;
import net.fluance.security.ehprofile.test.Application;
import net.fluance.security.ehprofile.test.TestConfig;
import net.fluance.security.ehprofile.test.UserProfileTestConfig;

/**
 *
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class UserProfileControllerAuthorizationTest extends AbstractWebIntegrationTest {

	private static Logger LOGGER = LogManager.getLogger(UserProfileControllerAuthorizationTest.class);

	private TestRestTemplate restTemplate;
	Map<String, Header> headersMap;
	private static final String OAUTH2_OWNPROFILE_HEADER = "oauth2.ownprofile.header";
	private static final String OAUTH2_ADMIN_HEADER = "oauth2.admin.header";
	private static final String OAUTH2_NONADMIN_HEADER = "oauth2.nonadmon.header";

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

	@Value("${jwt.header}")
	private String jwtHeader;
	@Value("${patient.jwt.payload}")
	private String patientJwtPayload;
	@Value("${ownprofile.jwt.payload}")
	private String ownProfileJwtPayload;
	@Value("${admin.jwt.payload}")
	private String adminJwtPayload;
	@Value("${nonadmin.jwt.payload}")
	private String nonAdminJwtPayload;
	@Value("${wrongissuer.jwt.payload}")
	private String wrongIssuerJwtPayload;

	@Value("${jwt.default.signing-algorithm}")
	private String defaultJwtSigningAlgorithm;
	@Value("${jwt.default.type}")
	private String defaultJwtType;

	@Value("${jwt.token.url}")
	private String jwtTokenUrl;

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

	private Role sysAdminRole;
	private Role nurseRole;
	private Role physicianRole;
	private Role administrativeRole;
	private Role financialRole;

	@Autowired
	private TestConfig testConfig;
	@Autowired
	private UserProfileTestConfig userProfileTestConfig;
	private Swagger swaggerSpec;
	private String swaggerSpecAbsolutePath;
	private String createResourcePath;
	private String readResourcePath;
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
	@SuppressWarnings("unused")
	private String apiConsumesList;
	private String apiConsumes;

	@Autowired
	protected IProfileRepository profileRepository;
	@Autowired
	protected IUserClientDataRepository userClientDataRepository;
	@Autowired
	protected IUserCompanyIdentityRepository userCompanyIdentityRepository;
	@Autowired
	protected CompanyRepository companyRepository;
	@Autowired
	IRoleRepository roleRepository;

	@Autowired
	private Wso2IsMockController authServerMockController;
	private static final String DOMAIN = "PRIMARY";

	private String patientJwt;
	private String employeeJwt;
	private String adminJwt;
	private String nonAdminJwt;
	private String wrongIssuerJwt;

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
		authBaseUrl = "http://localhost:" + serverPort/* + contextPath */;

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

		assertNotNull(authServerMockController.getAuthorizationServerMock().getUserByUsernameAndDomain(adminUsername,
				DOMAIN));
		assertNotNull(authServerMockController.getAuthorizationServerMock()
				.getUserByUsernameAndDomain(ownProfileUsername, DOMAIN));
		assertNotNull(authServerMockController.getAuthorizationServerMock().getUserByUsernameAndDomain(nonAdminUsername,
				DOMAIN));

		String adminAccessToken = getAccessToken(adminUsername, DOMAIN);
		String ownProfileAccessToken = getAccessToken(ownProfileUsername, DOMAIN);
		String nonAdminAccessToken = getAccessToken(nonAdminUsername, DOMAIN);

		headersMap.put(OAUTH2_ADMIN_HEADER, new BasicHeader("Authorization", "Bearer " + adminAccessToken));
		headersMap.put(OAUTH2_OWNPROFILE_HEADER, new BasicHeader("Authorization", "Bearer " + ownProfileAccessToken));
		headersMap.put(OAUTH2_NONADMIN_HEADER, new BasicHeader("Authorization", "Bearer " + nonAdminAccessToken));

		List<Header> headers = new ArrayList<>();
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));

		invokeResource(HttpMethod.POST.name(), createResourcePath, headers, null,
				userProfileTestConfig.getCreateProfileRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		// Create financial profile
		invokeResource(HttpMethod.POST.name(), createResourcePath, headers, null,
				userProfileTestConfig.getCreateNonAdminProfileRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
	}

	@PostConstruct
	public void init() {
		createResourcePath = "/";
		readResourcePath = "/" + ownProfileUsername + "/fluance";
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

	}

	@Test
	public void mustAllowAdminAndOwnOnReadTest() throws Exception {
		List<Header> headers = new ArrayList<>();

		// Read by admin
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.GET.name(), readResourcePath, headers, null, null,
				null, null);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// Read own profile
		headers.clear();
		headers.add(headersMap.get(OAUTH2_OWNPROFILE_HEADER));
		checkHttpStatusForbidden(HttpMethod.GET.name(), readResourcePath, headers, null, null, null, null);

		// Read by non-admin and non own profile
		headers.clear();
		headers.add(headersMap.get(OAUTH2_NONADMIN_HEADER));
		response = invokeResource(HttpMethod.GET.name(), readResourcePath, headers, null, null, null, null);
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
	}

	@Test
	public void mustAllowOnlyAdminOnDeleteTest()
			throws KeyManagementException, UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException,
			IOException, HttpException, URISyntaxException, NumberFormatException, XPathExpressionException,
			ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
		List<Header> headers = new ArrayList<>();

		// Delete by admin
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.DELETE.name(), deleteResourcePath, headers, null,
				null, null, null);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// Delete own profile
		headers.clear();
		headers.add(headersMap.get(OAUTH2_OWNPROFILE_HEADER));
		response = invokeResource(HttpMethod.DELETE.name(), deleteResourcePath, headers, null, null, null, null);
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());

		// Delete by non-admin and non own profile
		headers.clear();
		headers.add(headersMap.get(OAUTH2_NONADMIN_HEADER));
		response = invokeResource(HttpMethod.DELETE.name(), deleteResourcePath, headers, null, null, null, null);
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
	}

	@Test
	public void mustAllowOnlyAdminOnCreateTest() throws KeyManagementException, UnsupportedCharsetException,
			UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, ParseException, IOException,
			HttpException, URISyntaxException {
		profileRepository.deleteAll();
		List<Header> headers = new ArrayList<>();
		// Admin
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), createResourcePath, headers, null,
				userProfileTestConfig.getCreateProfileRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		// Own profile
		headers.clear();
		headers.add(headersMap.get(OAUTH2_OWNPROFILE_HEADER));
		response = invokeResource(HttpMethod.POST.name(), createResourcePath, headers, null,
				userProfileTestConfig.getCreateProfileRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
		// Others
		headers.clear();
		headers.add(headersMap.get(OAUTH2_NONADMIN_HEADER));
		response = invokeResource(HttpMethod.POST.name(), createResourcePath, headers, null,
				userProfileTestConfig.getCreateProfileRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
	}

	@Test
	public void mustAllowOnlyAdminOnCompanyGrantTest() throws KeyManagementException, UnsupportedCharsetException,
			UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, ParseException, IOException,
			HttpException, URISyntaxException {
		List<Header> headers = new ArrayList<>();

		// Admin must be able to grant companies
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), grantCompanyResourcePath, headers, null,
				userProfileTestConfig.getGrantCompaniesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// Own profile user must not be able to grant companies
		headers.clear();
		headers.add(headersMap.get(OAUTH2_OWNPROFILE_HEADER));
		response = invokeResource(HttpMethod.POST.name(), grantCompanyResourcePath, headers, null,
				userProfileTestConfig.getGrantCompaniesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());

		// NonAdmin user must not be able to grant companies
		headers.clear();
		headers.add(headersMap.get(OAUTH2_NONADMIN_HEADER));
		response = invokeResource(HttpMethod.POST.name(), grantCompanyResourcePath, headers, null,
				userProfileTestConfig.getGrantCompaniesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
	}

	@Test
	public void mustAllowOnlyAdminOnCompanySetTest() throws KeyManagementException, UnsupportedCharsetException,
			UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, ParseException, IOException,
			HttpException, URISyntaxException {
		List<Header> headers = new ArrayList<>();

		// Admin must be able to set companies
		headers.clear();
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), setCompanyResourcePath, headers, null,
				userProfileTestConfig.getSetCompaniesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// Own profile user must not be able to set companies
		headers.clear();
		headers.add(headersMap.get(OAUTH2_OWNPROFILE_HEADER));
		response = invokeResource(HttpMethod.POST.name(), setCompanyResourcePath, headers, null,
				userProfileTestConfig.getSetCompaniesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());

		// NonAdmin user must not be able to set companies
		headers.clear();
		headers.add(headersMap.get(OAUTH2_NONADMIN_HEADER));
		response = invokeResource(HttpMethod.POST.name(), setCompanyResourcePath, headers, null,
				userProfileTestConfig.getSetCompaniesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
	}

	@Test
	public void mustAllowOnlyAdminOnCompanyRevokeTest() throws KeyManagementException, UnsupportedCharsetException,
			UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, ParseException, IOException,
			HttpException, URISyntaxException {
		List<Header> headers = new ArrayList<>();

		// Admin must be able to revoke companies
		headers.clear();
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), revokeCompanyResourcePath, headers,
				null, userProfileTestConfig.getRevokeCompaniesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// Own profile user must not be able to revoke companies
		headers.clear();
		headers.add(headersMap.get(OAUTH2_OWNPROFILE_HEADER));
		response = invokeResource(HttpMethod.POST.name(), revokeCompanyResourcePath, headers, null,
				userProfileTestConfig.getRevokeCompaniesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());

		// NonAdmin user must not be able to revoke companies
		headers.clear();
		headers.add(headersMap.get(OAUTH2_NONADMIN_HEADER));
		response = invokeResource(HttpMethod.POST.name(), revokeCompanyResourcePath, headers, null,
				userProfileTestConfig.getRevokeCompaniesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
	}

	@Test
	public void mustAllowOnlyAdminOnPatientUnitGrantTest() throws KeyManagementException, UnsupportedCharsetException,
			UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, ParseException, IOException,
			HttpException, URISyntaxException {
		List<Header> headers = new ArrayList<>();

		// Admin must be able to grant patientunits
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), grantPatientUnitResourcePath, headers,
				null, userProfileTestConfig.getGrantPatientUnitsRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// Own profile user must not be able to grant patientunits
		headers.clear();
		headers.add(headersMap.get(OAUTH2_OWNPROFILE_HEADER));
		response = invokeResource(HttpMethod.POST.name(), grantPatientUnitResourcePath, headers, null,
				userProfileTestConfig.getGrantPatientUnitsRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());

		// NonAdmin user must not be able to grant patientunits
		headers.clear();
		headers.add(headersMap.get(OAUTH2_NONADMIN_HEADER));
		response = invokeResource(HttpMethod.POST.name(), grantPatientUnitResourcePath, headers, null,
				userProfileTestConfig.getGrantPatientUnitsRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
	}

	@Test
	public void mustAllowOnlyAdminOnPatientUnitSetTest() throws KeyManagementException, UnsupportedCharsetException,
			UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, ParseException, IOException,
			HttpException, URISyntaxException {
		List<Header> headers = new ArrayList<>();

		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), setPatientUnitResourcePath, headers,
				null, userProfileTestConfig.getSetPatientUnitsRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// Own profile user must not be able to set patientunits
		headers.clear();
		headers.add(headersMap.get(OAUTH2_OWNPROFILE_HEADER));
		response = invokeResource(HttpMethod.POST.name(), setPatientUnitResourcePath, headers, null,
				userProfileTestConfig.getSetPatientUnitsRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());

		// NonAdmin user must not be able to set patientunits
		headers.clear();
		headers.add(headersMap.get(OAUTH2_NONADMIN_HEADER));
		response = invokeResource(HttpMethod.POST.name(), setPatientUnitResourcePath, headers, null,
				userProfileTestConfig.getSetPatientUnitsRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
	}

	@Test
	public void mustAllowOnlyAdminOnPatientUnitRevokeTest() throws KeyManagementException, UnsupportedCharsetException,
			UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, ParseException, IOException,
			HttpException, URISyntaxException {
		List<Header> headers = new ArrayList<>();

		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), revokePatientUnitResourcePath, headers,
				null, userProfileTestConfig.getSetPatientUnitsRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// Own profile user must not be able to revoke patientunits
		headers.clear();
		headers.add(headersMap.get(OAUTH2_OWNPROFILE_HEADER));
		response = invokeResource(HttpMethod.POST.name(), revokePatientUnitResourcePath, headers, null,
				userProfileTestConfig.getSetPatientUnitsRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());

		// NonAdmin user must not be able to revoke patientunits
		headers.clear();
		headers.add(headersMap.get(OAUTH2_NONADMIN_HEADER));
		response = invokeResource(HttpMethod.POST.name(), revokePatientUnitResourcePath, headers, null,
				userProfileTestConfig.getSetPatientUnitsRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
	}

	@Test
	public void mustAllowOnlyAdminOnHospServiceGrantTest() throws KeyManagementException, UnsupportedCharsetException,
			UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, ParseException, IOException,
			HttpException, URISyntaxException {
		List<Header> headers = new ArrayList<>();

		// Admin must be able to grant hospservices
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), grantHospServiceResourcePath, headers,
				null, userProfileTestConfig.getGrantHospServicesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// Own profile user must not be able to grant hospservices
		headers.clear();
		headers.add(headersMap.get(OAUTH2_OWNPROFILE_HEADER));
		response = invokeResource(HttpMethod.POST.name(), grantHospServiceResourcePath, headers, null,
				userProfileTestConfig.getGrantHospServicesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());

		// NonAdmin user must not be able to grant hospservices
		headers.clear();
		headers.add(headersMap.get(OAUTH2_NONADMIN_HEADER));
		response = invokeResource(HttpMethod.POST.name(), grantHospServiceResourcePath, headers, null,
				userProfileTestConfig.getGrantHospServicesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
	}

	@Test
	public void mustAllowOnlyAdminOnHospServiceSetTest() throws KeyManagementException, UnsupportedCharsetException,
			UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, ParseException, IOException,
			HttpException, URISyntaxException {
		List<Header> headers = new ArrayList<>();

		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), setHospServiceResourcePath, headers,
				null, userProfileTestConfig.getSetHospServicesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// Own profile user must not be able to set hospservices
		headers.clear();
		headers.add(headersMap.get(OAUTH2_OWNPROFILE_HEADER));
		response = invokeResource(HttpMethod.POST.name(), setHospServiceResourcePath, headers, null,
				userProfileTestConfig.getSetHospServicesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());

		// NonAdmin user must not be able to set hospservices
		headers.clear();
		headers.add(headersMap.get(OAUTH2_NONADMIN_HEADER));
		response = invokeResource(HttpMethod.POST.name(), setHospServiceResourcePath, headers, null,
				userProfileTestConfig.getSetHospServicesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
	}

	@Test
	public void mustAllowOnlyAdminOnHospServiceRevokeTest() throws KeyManagementException, UnsupportedCharsetException,
			UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, ParseException, IOException,
			HttpException, URISyntaxException {
		List<Header> headers = new ArrayList<>();

		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), revokeHospServiceResourcePath, headers,
				null, userProfileTestConfig.getSetHospServicesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// Own profile user must not be able to revoke hospservices
		headers.clear();
		headers.add(headersMap.get(OAUTH2_OWNPROFILE_HEADER));
		response = invokeResource(HttpMethod.POST.name(), revokeHospServiceResourcePath, headers, null,
				userProfileTestConfig.getSetHospServicesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());

		// NonAdmin user must not be able to revoke hospservices
		headers.clear();
		headers.add(headersMap.get(OAUTH2_NONADMIN_HEADER));
		response = invokeResource(HttpMethod.POST.name(), revokeHospServiceResourcePath, headers, null,
				userProfileTestConfig.getSetHospServicesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
	}

	@Test
	public void mustAllowOnlyAdminOnRoleGrantTest() throws KeyManagementException, UnsupportedCharsetException,
			UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, ParseException, IOException,
			HttpException, URISyntaxException {
		List<Header> headers = new ArrayList<>();

		// Admin must be able to grant roles
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), grantRoleResourcePath, headers, null,
				userProfileTestConfig.getGrantRolesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// Own profile user must not be able to grant roles
		headers.clear();
		headers.add(headersMap.get(OAUTH2_OWNPROFILE_HEADER));
		response = invokeResource(HttpMethod.POST.name(), grantRoleResourcePath, headers, null,
				userProfileTestConfig.getGrantRolesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());

		// NonAdmin user must not be able to grant roles
		headers.clear();
		headers.add(headersMap.get(OAUTH2_NONADMIN_HEADER));
		response = invokeResource(HttpMethod.POST.name(), grantRoleResourcePath, headers, null,
				userProfileTestConfig.getGrantRolesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
	}

	@Test
	public void mustAllowOnlyAdminOnRoleSetTest() throws KeyManagementException, UnsupportedCharsetException,
			UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, ParseException, IOException,
			HttpException, URISyntaxException {
		List<Header> headers = new ArrayList<>();

		// Admin must be able to set roles
		headers.clear();
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), setRoleResourcePath, headers, null,
				userProfileTestConfig.getSetRolesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// Own profile user must not be able to set roles
		headers.clear();
		headers.add(headersMap.get(OAUTH2_OWNPROFILE_HEADER));
		response = invokeResource(HttpMethod.POST.name(), setRoleResourcePath, headers, null,
				userProfileTestConfig.getSetRolesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());

		// NonAdmin user must not be able to set roles
		headers.clear();
		headers.add(headersMap.get(OAUTH2_NONADMIN_HEADER));
		response = invokeResource(HttpMethod.POST.name(), setRoleResourcePath, headers, null,
				userProfileTestConfig.getSetRolesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
	}

	@Test
	public void mustAllowOnlyAdminOnRoleRevokeTest() throws KeyManagementException, UnsupportedCharsetException,
			UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, ParseException, IOException,
			HttpException, URISyntaxException {
		List<Header> headers = new ArrayList<>();

		// Admin must be able to revoke roles
		headers.clear();
		headers.add(headersMap.get(OAUTH2_ADMIN_HEADER));
		CloseableHttpResponse response = invokeResource(HttpMethod.POST.name(), revokeRoleResourcePath, headers, null,
				userProfileTestConfig.getSetRolesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());

		// Own profile user must not be able to revoke roles
		headers.clear();
		headers.add(headersMap.get(OAUTH2_OWNPROFILE_HEADER));
		response = invokeResource(HttpMethod.POST.name(), revokeRoleResourcePath, headers, null,
				userProfileTestConfig.getSetRolesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());

		// NonAdmin user must not be able to revoke roles
		headers.clear();
		headers.add(headersMap.get(OAUTH2_NONADMIN_HEADER));
		response = invokeResource(HttpMethod.POST.name(), revokeRoleResourcePath, headers, null,
				userProfileTestConfig.getSetRolesRequestPayload(), ContentType.parse(apiConsumes),
				userProfileTestConfig.getRequestCharset());
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
	}
	
	@Ignore
	@Test
	public void readProfileWithTrustedPartnerStrategyTest() throws KeyManagementException, UnsupportedOperationException, NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException {
		List<Header> headers = new ArrayList<>();
		headers.add(new BasicHeader("Authorization", "Bearer " + employeeJwt));
		CloseableHttpResponse response = invokeResource(HttpMethod.GET.name(), readResourcePath, headers,
				null, null, null, null);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		// A user must not be able to read another user's profile
		headers.clear();
		headers.add(new BasicHeader("Authorization", "Bearer " + nonAdminJwt));
		response = invokeResource(HttpMethod.GET.name(), readResourcePath.replace(ownProfileUsername, nonAdminUsername), headers,
				null, null, null, null);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		response = invokeResource(HttpMethod.GET.name(), readResourcePath, headers,
				null, null, null, null);
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
		// Unless he/she has the role for (admin for example)
		headers.clear();
		headers.add(new BasicHeader("Authorization", "Bearer " + adminJwt));
		response = invokeResource(HttpMethod.GET.name(), readResourcePath, headers,
				null, null, null, null);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		response = invokeResource(HttpMethod.GET.name(), readResourcePath.replace(ownProfileUsername, nonAdminUsername), headers,
				null, null, null, null);
		assertEquals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode());
		// If profile doesn't exist (using admin token)
		response = invokeResource(HttpMethod.GET.name(), readResourcePath.replace(ownProfileUsername, "unknownuser"), headers,
				null, null, null, null);
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusLine().getStatusCode());
		
		// Test Unauthorized if issuer is unknown
		headers.clear();
		headers.add(new BasicHeader("Authorization", "Bearer " + wrongIssuerJwt));
		response = invokeResource(HttpMethod.GET.name(), readResourcePath, headers,
				null, null, null, null);
		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusLine().getStatusCode());
	}
	
	@Ignore
	@Test
	public void readProfileWithSinglePatientStrategyTest() throws KeyManagementException, UnsupportedOperationException,
			NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException {
		List<Header> headers = new ArrayList<>();

		headers.add(new BasicHeader("Authorization", "Bearer " + patientJwt));
		CloseableHttpResponse response = invokeResource(HttpMethod.GET.name(), readResourcePath, headers,
				null, null, null, null);
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusLine().getStatusCode());
	}

	private String jwtRequestBodyString(boolean signed, String signingAlgo, String type, String headerCustomFields, String payloadCustomFields) throws JsonProcessingException, IOException {
		String body = "{\"signed\":" + signed + ",";
		body += "\"signingAlgorithm\":\"" + signingAlgo + "\",";
		body += "\"type\":\"" + type + "\",";
		body += "\"header\":" + headerCustomFields + ",";
		body += "\"payload\":" + payloadCustomFields;
		body += "}"; 
		
		return body;
	}
	
	@Override
	@After
	public void tearDown() {
		swaggerSpec = null;
		userCompanyIdentityRepository.deleteAll();
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
	public void checkOk(Object... args) throws KeyManagementException, UnsupportedOperationException,
			NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException,
			ProcessingException, NumberFormatException, ParseException, XPathExpressionException,
			ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
	}

}
