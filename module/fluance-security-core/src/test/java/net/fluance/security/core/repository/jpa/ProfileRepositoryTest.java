/**
 * 
 */
package net.fluance.security.core.repository.jpa;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.PostConstruct;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.fluance.app.data.model.identity.UserType;
import net.fluance.app.test.AbstractTest;
import net.fluance.security.core.Application;
import net.fluance.security.core.model.jpa.Profile;
import net.fluance.security.core.model.jpa.Role;
import net.fluance.security.core.model.jpa.UserCompany;
import net.fluance.security.core.model.jpa.UserCompanyIdentity;
import net.fluance.security.core.repository.jpa.IProfileRepository;
import net.fluance.security.core.repository.jpa.IRoleRepository;
import net.fluance.security.core.util.CompanyUtils;
import net.fluance.security.core.repository.jpa.IUserCompanyIdentityRepository;

@ComponentScan("net.fluance.security.core")
@SpringBootTest(classes = Application.class)
public class ProfileRepositoryTest extends AbstractTest {

	private static final String USER1_NAME = "localtestuser1";
	private static final String USER2_NAME = "localtestuser2";
	private static final String USER3_NAME = "localtestuser3";
	private static final String LOCAL_DOMAIN_NAME = "PRIMARY";
	private static final String USER_TYPE = "user";
	
	private static final String ROLE1_NAME = "admin";
	private static final String ROLE2_NAME = "superadmin";
	@Autowired
	private IRoleRepository roleRepository;
	@Autowired
	private IProfileRepository profileRepository;
	@Autowired
	private IUserCompanyRepository userCompanyRepository;
	@Autowired
	private IUserCompanyIdentityRepository userCompanyIdentityRepository;
//	@Autowired
//	private IUserCompanyHospserviceRepository userCompanyHospserviceRepository;
//	@Autowired
//	private IUserCompanyPatientunitRepository userCompanyPatientunitRepository;
	
	private static final Integer COMPANY1_ID = 1;
	private static final Integer COMPANY2_ID = 2;
	private static final Integer COMPANY3_ID = 3;
	private static final String[] COMPANY1_SERVICES = new String[] { "1ANG", "1CAR", "1DER", "1INF", "1GYN","1MGE", "1MIN", "1NEP", "1ORT", "1PED", "1PNE", "1DIA", "2CHI", "2DIE", "1HYP", "2HYP", "2LAB", "2ONC","ONCO", "2RTH", "RTH", "2RHU" };
	private static final String[] COMPANY2_SERVICES = new String[] { "S1", "S2", "TAKL", "RES", "IPS", "PWIT","PBAU", "AMBI", "THER" };
	private static final String[] COMPANY3_SERVICES = new String[] { "GALD", "GOD1", "GOD2", "GOD3", "GOFI", "INF","PHY", "RAD", "PS" };
	private static final String[] COMPANY1_UNITS = new String[] {"01", "02","03","04","05","06","07","08","09","10","CH","DI","HY","LA","OC","RT","RU"};
	private static final String[] COMPANY2_UNITS = new String[] {"1","2","2R","IP","P3","P4"};
	private static final String[] COMPANY3_UNITS = new String[] {"01","02","03"};
	
	private Role role1;
	private Role role2;
	
	private static final int PROVIDER1_ID = 1;
	private static final int PROVIDER2_ID = 2;
	private static final String COMPANY1_PROVIDER1_STAFFID = "2362";
	private static final String COMPANY2_PROVIDER1_STAFFID = "1032";
	private static final String COMPANY3_PROVIDER1_STAFFID = "1001";
	private static final String COMPANY4_PROVIDER1_STAFFID = "210";
	private static final String COMPANY5_PROVIDER1_STAFFID = "212";
	
	private ObjectNode userCompany1UnitsAndServices;
	private ObjectNode userCompany2UnitsAndServices;
	private ObjectNode userCompany3UnitsAndServices;
	
	@PostConstruct
	public void init() {
		userCompany1UnitsAndServices = CompanyUtils.unitsAndServicesJson(COMPANY1_UNITS, COMPANY1_SERVICES);
		userCompany2UnitsAndServices = CompanyUtils.unitsAndServicesJson(COMPANY2_UNITS, COMPANY2_SERVICES);
		userCompany3UnitsAndServices = CompanyUtils.unitsAndServicesJson(COMPANY3_UNITS, COMPANY3_SERVICES);
	}
	
	@Before
	public void setup() {
		profileRepository.deleteAll();
		roleRepository.deleteAll();
		
		Profile userProfile1 = new Profile();
		userProfile1.setDomainName(LOCAL_DOMAIN_NAME);
		userProfile1.setUsername(USER1_NAME);
				
		role1 = new Role();
		role1.setName(ROLE1_NAME);
		role2 = new Role();
		role2.setName(ROLE2_NAME);
		Role newRole1 = roleRepository.save(role1);
		Role newRole2 = roleRepository.save(role2);
		
		userProfile1.assignRole(newRole1);
		userProfile1.assignRole(newRole2);
		
		profileRepository.save(userProfile1);
		Profile userProfile2 = new Profile();
		userProfile2.setDomainName(LOCAL_DOMAIN_NAME);
		userProfile2.setUsername(USER2_NAME);
		
		userProfile2.assignRole(role2);
		
		profileRepository.save(userProfile2);
		
		UserCompanyIdentity userCompanyIdentity1 = new UserCompanyIdentity();
		userCompanyIdentity1.setCompanyId(COMPANY1_ID);
		userCompanyIdentity1.setProfileId(userProfile1.getId());
		userCompanyIdentity1.setProviderId(PROVIDER1_ID);
		userCompanyIdentity1.setStaffId(COMPANY1_PROVIDER1_STAFFID);
		userCompanyIdentityRepository.save(userCompanyIdentity1);
		
		UserCompanyIdentity userCompanyIdentity2 = new UserCompanyIdentity();
		userCompanyIdentity2.setCompanyId(COMPANY3_ID);
		userCompanyIdentity2.setProfileId(userProfile1.getId());
		userCompanyIdentity2.setProviderId(PROVIDER1_ID);
		userCompanyIdentity2.setStaffId(COMPANY2_PROVIDER1_STAFFID);
		userCompanyIdentityRepository.save(userCompanyIdentity2);
		
		UserCompanyIdentity userCompanyIdentity3 = new UserCompanyIdentity();
		userCompanyIdentity3.setCompanyId(COMPANY2_ID);
		userCompanyIdentity3.setProfileId(userProfile2.getId());
		userCompanyIdentity3.setProviderId(PROVIDER1_ID);
		userCompanyIdentity3.setStaffId(COMPANY3_PROVIDER1_STAFFID);
		userCompanyIdentityRepository.save(userCompanyIdentity3);
		
		UserCompanyIdentity userCompanyIdentity4 = new UserCompanyIdentity();
		userCompanyIdentity4.setCompanyId(COMPANY3_ID);
		userCompanyIdentity4.setProfileId(userProfile2.getId());
		userCompanyIdentity4.setProviderId(PROVIDER1_ID);
		userCompanyIdentity4.setStaffId(COMPANY4_PROVIDER1_STAFFID);
		userCompanyIdentityRepository.save(userCompanyIdentity4);
		
		// Assign companies and units/services to users
		UserCompany userCompany1 = new UserCompany();
		userCompany1.setCompanyId(COMPANY1_ID);
		userCompany1.setProfileId(userProfile1.getId());
		userCompany1.setUnitsAndServices(userCompany1UnitsAndServices);
		userCompanyRepository.save(userCompany1);
		
		UserCompany userCompany2 = new UserCompany();
		userCompany2.setCompanyId(COMPANY3_ID);
		userCompany2.setProfileId(userProfile1.getId());
		userCompany2.setUnitsAndServices(userCompany3UnitsAndServices);
		userCompanyRepository.save(userCompany2);
		
		UserCompany userCompany3 = new UserCompany();
		userCompany3.setCompanyId(COMPANY2_ID);
		userCompany3.setProfileId(userProfile2.getId());
		userCompany3.setUnitsAndServices(userCompany2UnitsAndServices);
		userCompany3.setUnitsAndServices(userCompany2UnitsAndServices);
		userCompanyRepository.save(userCompany3);
		
		UserCompany userCompany4 = new UserCompany();
		userCompany4.setCompanyId(COMPANY3_ID);
		userCompany4.setProfileId(userProfile2.getId());
		userCompany4.setUnitsAndServices(userCompany3UnitsAndServices);
		userCompanyRepository.save(userCompany4);
		
		
		
		
		
		
		
		
		
		
		
		
//		for (String patienUnit : COMPANY1_UNITS) {
//			UserCompanyPatientunit userCompanyPatientunit = new UserCompanyPatientunit();
//			userCompanyPatientunit.setProfileId(userProfile1.getId());
//			userCompanyPatientunit.setCompanyId(COMPANY1_ID);
//			userCompanyPatientunit.setPatientUnit(patienUnit);
//			userCompanyPatientunitRepository.save(userCompanyPatientunit);
//		}
//		for (String patienUnit : COMPANY3_UNITS) {
//			UserCompanyPatientunit userCompanyPatientunit = new UserCompanyPatientunit();
//			userCompanyPatientunit.setProfileId(userProfile1.getId());
//			userCompanyPatientunit.setCompanyId(COMPANY3_ID);
//			userCompanyPatientunit.setPatientUnit(patienUnit);
//			userCompanyPatientunitRepository.save(userCompanyPatientunit);
//		}
//		for (String patienUnit : COMPANY2_UNITS) {
//			UserCompanyPatientunit userCompanyPatientunit = new UserCompanyPatientunit();
//			userCompanyPatientunit.setProfileId(userProfile2.getId());
//			userCompanyPatientunit.setCompanyId(COMPANY2_ID);
//			userCompanyPatientunit.setPatientUnit(patienUnit);
//			userCompanyPatientunitRepository.save(userCompanyPatientunit);
//		}
//		
//		for (String hospService : COMPANY1_SERVICES) {
//			UserCompanyHospservice userCompanyHospservice = new UserCompanyHospservice();
//			userCompanyHospservice.setProfileId(userProfile1.getId());
//			userCompanyHospservice.setCompanyId(COMPANY1_ID);
//			userCompanyHospservice.setHospService(hospService);
//			userCompanyHospserviceRepository.save(userCompanyHospservice);
//		}
//		for (String hospService : COMPANY3_SERVICES) {
//			UserCompanyHospservice userCompanyHospservice = new UserCompanyHospservice();
//			userCompanyHospservice.setProfileId(userProfile1.getId());
//			userCompanyHospservice.setCompanyId(COMPANY3_ID);
//			userCompanyHospservice.setHospService(hospService);
//			userCompanyHospserviceRepository.save(userCompanyHospservice);
//		}
//		for (String hospService : COMPANY2_SERVICES) {
//			UserCompanyHospservice userCompanyHospservice = new UserCompanyHospservice();
//			userCompanyHospservice.setProfileId(userProfile2.getId());
//			userCompanyHospservice.setCompanyId(COMPANY2_ID);
//			userCompanyHospservice.setHospService(hospService);
//			userCompanyHospserviceRepository.save(userCompanyHospservice);
//		}
	}
	
	@Test
	public void mustListTest() {

	}
	
	@Test
	public void mustFindTest() {
		List<Profile> profiles = profileRepository.findAll();
		assertNotNull(profiles);
		assertEquals(Integer.valueOf(2), (profiles!=null) ? Integer.valueOf(profiles.size()) : Integer.valueOf(-1));
		
		Profile profile1 = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(profile1);
		assertNotNull(profile1.getRoles());
		assertEquals(2, profile1.getRoles().size());
		
		Profile profile2 = profileRepository.findByUsernameAndDomainName(USER2_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(profiles);
		assertNotNull(profile2.getRoles());
		assertEquals(1, profile2.getRoles().size());
		
		// By username and domain name
		Profile profile = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(profile);
		assertNotNull(profile.getRoles());
		assertEquals(2, profile.getRoles().size());
	}
	
	@Test
	public void mustCreateTest() {
		Profile userProfile3 = new Profile();
		userProfile3.setDomainName(LOCAL_DOMAIN_NAME);
		userProfile3.setUsername(USER3_NAME);
		
		Role role1 = roleRepository.findByName(ROLE1_NAME);
		Role role2 = roleRepository.findByName(ROLE2_NAME);
		
		userProfile3.assignRole(role1);
		userProfile3.assignRole(role2);
		
		Profile createdUser = profileRepository.save(userProfile3);
		assertNotNull(createdUser);
		assertEquals(LOCAL_DOMAIN_NAME, ((createdUser!=null) ? createdUser.getDomainName() : null));
		assertEquals(USER3_NAME, (createdUser!=null) ? createdUser.getUsername() : null);
		
		assertNotNull(createdUser.getRoles());
		assertEquals(2, createdUser.getRoles().size());
	}
	
	@Test
	public void mustSaveByUsernameAndDomainNameTest() {
		int createdProfileNb = profileRepository.saveByUsernameDomainNameAndUsertype(USER3_NAME, LOCAL_DOMAIN_NAME, USER_TYPE);
		assertEquals(1, createdProfileNb);
		Profile createdProfile = profileRepository.findByUsernameAndDomainName(USER3_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(createdProfile);
		assertEquals(LOCAL_DOMAIN_NAME, createdProfile.getDomainName());
		assertEquals(USER3_NAME, createdProfile.getUsername());
		
		assertNotNull(createdProfile.getRoles());
		assertEquals(0, createdProfile.getRoles().size());
	}
	
	@Test
	public void mustDeleteByUsernameAndDomainNameTest() {
		Profile foundProfile = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(foundProfile);
		Integer profileId = foundProfile.getId();
		
		List<UserCompanyIdentity> userCompanyIdentities = userCompanyIdentityRepository.findByProfileId(profileId);
		assertNotNull(userCompanyIdentities);
		assertEquals(2, userCompanyIdentities.size());
		List<Role> userRoles = roleRepository.getByProfileId(profileId);
		assertNotNull(userRoles);
		assertEquals(2, userRoles.size());
		
		profileRepository.deleteByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		Profile newFoundProfile = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		assertNull(newFoundProfile);
		
		userCompanyIdentities = userCompanyIdentityRepository.findByProfileId(profileId);
		assertNotNull(userCompanyIdentities);
		assertEquals(0, userCompanyIdentities.size());
		
		userRoles = roleRepository.getByProfileId(profileId);
		assertNotNull(userRoles);
		assertEquals(0, userRoles.size());
	}
	
	@After
	public void tearDown() {
		profileRepository.deleteAll();
		roleRepository.deleteAll();
	}
	
}
