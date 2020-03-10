/**
 * 
 */
package net.fluance.security.ehprofile.test.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpException;
import org.apache.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.xml.sax.SAXException;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import net.fluance.app.data.model.identity.AccessControl;
import net.fluance.app.data.model.identity.CompanyStaffId;
import net.fluance.app.data.model.identity.EhProfile;
import net.fluance.app.data.model.identity.GrantedCompany;
import net.fluance.app.data.model.identity.HospService;
import net.fluance.app.data.model.identity.PatientUnit;
import net.fluance.app.data.model.identity.UserType;
import net.fluance.app.spring.util.Constants;
import net.fluance.app.test.AbstractWebIntegrationTest;
import net.fluance.security.core.model.jpa.Role;
import net.fluance.security.core.repository.jpa.IProfileRepository;
import net.fluance.security.core.repository.jpa.IRoleRepository;
import net.fluance.security.core.repository.jpa.IUserCompanyIdentityRepository;
import net.fluance.security.ehprofile.service.UserProfileRolesService;
import net.fluance.security.ehprofile.service.UserProfileService;
import net.fluance.security.ehprofile.support.helper.UserProfileHelper;
import net.fluance.security.ehprofile.test.Application;

@EnableJpaRepositories(basePackages = {"net.fluance.security.core", "net.fluance.security.ehprofile"})
@EntityScan(basePackages = {"net.fluance.security.core", "net.fluance.security.ehprofile"})
@ComponentScan(basePackages = {"net.fluance.security.core", "net.fluance.security.ehprofile"})
@SpringBootTest(classes = Application.class)
public class UserProfileServiceTestOld extends AbstractWebIntegrationTest {

	private static final String LOCAL_USER_NAME = "nurse";
	private static final String LOCAL_USER_NAME2 = "financial";
	private static final String CORPORATE_USER_NAME = "fluancetestuser2";
	private static final String LOCAL_DOMAIN_NAME = "PRIMARY";
	private static final String ADMIN_ROLE_NAME = "admin";
	private static final String SUPERADMIN_ROLE_NAME = "superadmin";
	private static final String NURSE_ROLE_NAME = "nurse";
	private static final String ADMINISTRATIVE_ROLE_NAME = "administrative";
	private static final String PHYSICIAN_ROLE_NAME = "physician";
	private static final Integer COMPANY1_ID = 1;
	private static final Integer COMPANY2_ID = 2;
	private static final Integer COMPANY3_ID = 3;
	private static final String[] COMPANY1_UNITS = new String[] {"01", "02","03","04","05","06","07","08","09","10","CH","DI","HY","LA","OC","RT","RU"};
	private static final String[] COMPANY2_UNITS = new String[] {"1","2","2R","IP","P3","P4"};
	private static final String[] COMPANY3_UNITS = new String[] {"01","02","03"};
	private static final String[] COMPANY1_SERVICES = new String[] { "1ANG", "1CAR", "1DER", "1INF", "1GYN",
			"1MGE", "1MIN", "1NEP", "1ORT", "1PED", "1PNE", "1DIA", "2CHI", "2DIE", "1HYP", "2HYP", "2LAB", "2ONC",
			"ONCO", "2RTH", "RTH", "2RHU" };
	private static final String[] COMPANY2_SERVICES = new String[] { "S1", "S2", "TAKL", "RES", "IPS", "PWIT",
			"PBAU", "AMBI", "THER" };
	private static final String[] COMPANY3_SERVICES = new String[] { "GALD", "GOD1", "GOD2", "GOD3", "GOFI", "INF",
			"PHY", "RAD", "PS" };
	private static final List<PatientUnit> COMPANY1_PATIENTUNITS = new ArrayList<>();
	private static final List<PatientUnit> COMPANY2_PATIENTUNITS = new ArrayList<>();
	private static final List<PatientUnit> COMPANY3_PATIENTUNITS = new ArrayList<>();
	private static final List<HospService> COMPANY1_HOSPSERVICES = new ArrayList<>();
	private static final List<HospService> COMPANY2_HOSPSERVICES = new ArrayList<>();
	private static final List<HospService> COMPANY3_HOSPSERVICES = new ArrayList<>();
	
	private static final int PROVIDER1_ID = 1;
	private static final String COMPANY1_PROVIDER1_STAFFID = "1";
	private static final String COMPANY2_PROVIDER1_STAFFID = "2";
	private static final String COMPANY3_PROVIDER1_STAFFID = "1";
	@SuppressWarnings("unused")
	private static final String COMPANY3_PROVIDER1_STAFFID2 = "3";
	
	static {
		for(String unit : COMPANY1_UNITS) {
			COMPANY1_PATIENTUNITS.add(new PatientUnit(unit));
		}
		for(String unit : COMPANY2_UNITS) {
			COMPANY2_PATIENTUNITS.add(new PatientUnit(unit));
		}
		for(String unit : COMPANY3_UNITS) {
			COMPANY3_PATIENTUNITS.add(new PatientUnit(unit));
		}
		for(String svc : COMPANY1_SERVICES) {
			COMPANY1_HOSPSERVICES.add(new HospService(svc));
		}
		for(String svc : COMPANY2_SERVICES) {
			COMPANY2_HOSPSERVICES.add(new HospService(svc));
		}
		for(String svc : COMPANY3_SERVICES) {
			COMPANY3_HOSPSERVICES.add(new HospService(svc));
		}
	}
	
	@Autowired
	private UserProfileService userProfileService;
	@Autowired
	private UserProfileRolesService userProfileRolesService;
	@Autowired
	private IUserCompanyIdentityRepository userCompanyIdentityRepository;
	@Autowired
	private IRoleRepository roleRepository;
	@Autowired
	private IProfileRepository profileRepository;
	
	
	@Autowired
	private UserProfileHelper userProfileHelper;
	
	@Before
	public void setup() throws Exception {
		clean();
		init();
	}
	
	@After
	public void tearDown() {
		try {
			clean();
		} catch (Exception e) {
			getLogger().error(ExceptionUtils.getStackTrace(e));
		}
	}

	@Test
	public void mustCreateTest() throws Exception {
		clean();
		EhProfile profile = new EhProfile();
		profile.setUsername(LOCAL_USER_NAME);
		profile.setDomain(LOCAL_DOMAIN_NAME);
		profile.setUsertype(UserType.USER.getValue());

		AccessControl grants = new AccessControl();
		grants.setRoles(new ArrayList<String>());

		List<GrantedCompany> grantedCompanies = new ArrayList<>();
		GrantedCompany grantedCompany = new GrantedCompany(COMPANY2_ID, "PKL", COMPANY2_PATIENTUNITS, COMPANY2_HOSPSERVICES);
		
		CompanyStaffId comp2StaffId = new CompanyStaffId();
		comp2StaffId.setProviderId(PROVIDER1_ID);
		comp2StaffId.setStaffId(COMPANY2_PROVIDER1_STAFFID);
		grantedCompany.getStaffIds().add(comp2StaffId);
		
		grantedCompanies.add(grantedCompany);
		
		grants.setGrantedCompanies(grantedCompanies);
		profile.setGrants(grants);
		
		boolean saved = userProfileService.create(profile);
		
		assertTrue(saved);
	}
	
	@Test
	public void mustGrantRolesTest() throws Exception {
		List<String> rolesToGrant = Arrays.asList(new String[]{NURSE_ROLE_NAME, ADMINISTRATIVE_ROLE_NAME});
		List<String> currentRoles = userProfileRolesService.findUserRoles(LOCAL_USER_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(currentRoles);
		assertEquals(1, currentRoles.size());
		boolean rolesGranted = userProfileRolesService.grantRoles(LOCAL_USER_NAME, LOCAL_DOMAIN_NAME, rolesToGrant);
		assertTrue(rolesGranted);
		currentRoles = userProfileRolesService.findUserRoles(LOCAL_USER_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(currentRoles);
		assertEquals(3, currentRoles.size());
	}
	
	@Test
	public void mustSetRolesTest() throws Exception {
		List<String> currentRoles = userProfileRolesService.findUserRoles(LOCAL_USER_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(currentRoles);
		assertEquals(1, currentRoles.size());
		
		List<String> rolesToGrant = Arrays.asList(new String[]{NURSE_ROLE_NAME, ADMINISTRATIVE_ROLE_NAME});
		boolean rolesGranted = userProfileRolesService.grantRoles(LOCAL_USER_NAME, LOCAL_DOMAIN_NAME, rolesToGrant);
		assertTrue(rolesGranted);
		currentRoles = userProfileRolesService.findUserRoles(LOCAL_USER_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(currentRoles);
		assertEquals(3, currentRoles.size());
		
		List<String> rolesToSet = Arrays.asList(new String[]{PHYSICIAN_ROLE_NAME, ADMINISTRATIVE_ROLE_NAME});
		boolean rolesSet = userProfileRolesService.setRoles(LOCAL_USER_NAME, LOCAL_DOMAIN_NAME, rolesToSet);
		assertTrue(rolesSet);
		currentRoles = userProfileRolesService.findUserRoles(LOCAL_USER_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(currentRoles);
		assertEquals(2, currentRoles.size());
		assertTrue(!currentRoles.contains(NURSE_ROLE_NAME));
		assertTrue(currentRoles.contains("physician"));
		assertTrue(currentRoles.contains("administrative"));
	}

	
	public void init() throws Exception {
		EhProfile profile = new EhProfile();
		profile.setUsername(LOCAL_USER_NAME);
		profile.setDomain(LOCAL_DOMAIN_NAME);
		profile.setUsertype(UserType.USER.getValue());

		Role adminRole = new Role();
		adminRole.setName(ADMIN_ROLE_NAME);
		adminRole.setDescription("Role1 Desc");
		Role superAdminRole = new Role();
		superAdminRole.setName(SUPERADMIN_ROLE_NAME);
		superAdminRole.setDescription("Role2 Desc");
		Role administrativeRole = new Role();
		administrativeRole.setName(ADMINISTRATIVE_ROLE_NAME);
		administrativeRole.setDescription("Administrative role  Desc");
		Role nurseRole = new Role();
		nurseRole.setName(NURSE_ROLE_NAME);
		nurseRole.setDescription("Nurse role  Desc");
		Role physicianRole = new Role();
		physicianRole.setName(PHYSICIAN_ROLE_NAME);
		physicianRole.setDescription("Physician role  Desc");
		
		adminRole = roleRepository.save(adminRole);
		superAdminRole = roleRepository.save(superAdminRole);
		nurseRole = roleRepository.save(nurseRole);
		administrativeRole = roleRepository.save(administrativeRole);
		physicianRole = roleRepository.save(physicianRole);
		
		AccessControl grants = new AccessControl();
		grants.setRoles(new ArrayList<String>());

		List<GrantedCompany> grantedCompanies = new ArrayList<>();
		GrantedCompany grantedCompany1 = new GrantedCompany(COMPANY1_ID, "CME", COMPANY1_PATIENTUNITS, COMPANY1_HOSPSERVICES);
		CompanyStaffId comp1StaffId = new CompanyStaffId();
		comp1StaffId.setProviderId(PROVIDER1_ID);
		comp1StaffId.setStaffId(COMPANY1_PROVIDER1_STAFFID);
		grantedCompany1.getStaffIds().add(comp1StaffId);
		
		GrantedCompany grantedCompany2 = new GrantedCompany(COMPANY3_ID, "CAM", COMPANY3_PATIENTUNITS, COMPANY3_HOSPSERVICES);
		CompanyStaffId comp2StaffId = new CompanyStaffId();
		comp2StaffId.setProviderId(PROVIDER1_ID);
		comp2StaffId.setStaffId(COMPANY3_PROVIDER1_STAFFID);
		grantedCompany2.getStaffIds().add(comp2StaffId);
		
		grantedCompanies.add(grantedCompany1);
		grantedCompanies.add(grantedCompany2);
		
		grants.setGrantedCompanies(grantedCompanies);
		
		grants.getRoles().add(adminRole.getName());
		
		profile.setGrants(grants);
		
		userProfileService.create(profile);
	}
	
	public void clean() throws Exception {
		userCompanyIdentityRepository.deleteAll();
		profileRepository.deleteAll();
		roleRepository.deleteAll();
	}

	@Override
	public void setUp() throws Exception {
	}

	@Override
	protected boolean checkOAuth2Authorization(Object... params) {
		return false;
	}

	@Override
	public void checkOk(Object... params) throws KeyManagementException, UnsupportedOperationException,
			NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException,
			ProcessingException, NumberFormatException, ParseException, XPathExpressionException,
			ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
	}

	@Override
	public Logger getLogger() {
		return LogManager.getLogger(UserProfileServiceTestOld.class);
	}
	
}
