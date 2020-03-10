/**
 * 
 */
package net.fluance.security.core.repository.jpa;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import net.fluance.app.test.AbstractTest;
import net.fluance.security.core.Application;
import net.fluance.security.core.model.jpa.Profile;
import net.fluance.security.core.model.jpa.UserCompany;
import net.fluance.security.core.repository.jpa.IProfileRepository;
import net.fluance.security.core.util.CompanyUtils;

@ComponentScan("net.fluance.security.core")
@SpringBootTest(classes = Application.class)
public class UserCompanyRepositoryTest extends AbstractTest {

	@Autowired
	private IUserCompanyRepository userCompanyRepository;
	@Autowired
	private IProfileRepository profileRepository;
	private static final String USER1_NAME = "localtestuser1";
	private static final String USER3_NAME = "localtestuser3";
	private static final Integer COMPANY1_ID = 1;
	private static final Integer COMPANY2_ID = 2;
	private static final Integer COMPANY3_ID = 3;
	private static final String LOCAL_DOMAIN_NAME = "PRIMARY";
	
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

	private JsonNode userCompany1UnitsAndServices;
	private JsonNode userCompany2UnitsAndServices;
	private JsonNode userCompany3UnitsAndServices;
	
	@PostConstruct
	public void init() {
		userCompany1UnitsAndServices = CompanyUtils.unitsAndServicesJson(COMPANY1_UNITS, COMPANY1_SERVICES);
		userCompany2UnitsAndServices = CompanyUtils.unitsAndServicesJson(COMPANY2_UNITS, COMPANY2_SERVICES);
		userCompany3UnitsAndServices = CompanyUtils.unitsAndServicesJson(COMPANY3_UNITS, COMPANY3_SERVICES);
	}
	
	@Before
	public void setup() {
		userCompanyRepository.deleteAll();
		profileRepository.deleteAll();
		Profile userProfile1 = new Profile();
		userProfile1.setDomainName(LOCAL_DOMAIN_NAME);
		userProfile1.setUsername(USER1_NAME);
		profileRepository.save(userProfile1);
		Profile userProfile2 = new Profile();
		userProfile2.setDomainName(LOCAL_DOMAIN_NAME);
		userProfile2.setUsername(USER3_NAME);
		profileRepository.save(userProfile2);
		
		userProfile1 = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		userProfile2 = profileRepository.findByUsernameAndDomainName(USER3_NAME, LOCAL_DOMAIN_NAME);
		
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
	}
	
	@Test
	public void mustCreateByNamesTest() {
		Profile profile = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		UserCompany userCompany = new UserCompany();
		userCompany.setCompanyId(COMPANY2_ID);
		userCompany.setProfileId(profile.getId());
		userCompany.setUnitsAndServices(userCompany2UnitsAndServices);
		UserCompany createdUserCompany = userCompanyRepository.save(userCompany);
		assertNotNull(createdUserCompany);
	}
	
	@Test
	public void mustCreateTest() {
		Profile profile = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(profile);
		List<UserCompany> userCompany2Grants = userCompanyRepository.findByProfileIdAndCompanyId(profile.getId(), COMPANY1_ID);
		assertNotNull(userCompany2Grants);
		assertEquals(1, userCompany2Grants.size());
		UserCompany currentUserCompany = userCompany2Grants.get(0);
		assertNotNull(currentUserCompany);
		
		UserCompany userCompany2 = new UserCompany();
		userCompany2.setCompanyId(COMPANY2_ID);
		userCompany2.setProfileId(profile.getId());
		userCompany2.setUnitsAndServices(userCompany2UnitsAndServices);

		UserCompany createdUserCompany = userCompanyRepository.save(userCompany2);
		assertNotNull(createdUserCompany);
		assertEquals(COMPANY2_ID, createdUserCompany.getCompanyId());
		assertEquals(profile.getId(), createdUserCompany.getProfileId());
		assertEquals(userCompany2UnitsAndServices, createdUserCompany.getUnitsAndServices());
	}
	
	@Test
	public void mustSaveByUsernameAndDomainNameTest() {
		Profile profile = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(profile);
		List<UserCompany> userCompanies = userCompanyRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(userCompanies);
		assertEquals(2, userCompanies.size());

		UserCompany userCompany2 = new UserCompany();
		userCompany2.setCompanyId(COMPANY2_ID);
		userCompany2.setProfileId(profile.getId());
		userCompany2.setUnitsAndServices(userCompany2UnitsAndServices);
		UserCompany createdUserCompany = userCompanyRepository.save(userCompany2);
		
		assertNotNull(createdUserCompany);
		assertEquals(COMPANY2_ID, createdUserCompany.getCompanyId());
		assertEquals(profile.getId(), createdUserCompany.getProfileId());
		assertEquals(profile.getId(), createdUserCompany.getProfileId());
		assertEquals(userCompany2UnitsAndServices, createdUserCompany.getUnitsAndServices());
	}

	@Test
	public void mustFindTest() {
		List<UserCompany> allUsersCompanies = userCompanyRepository.findAll();
		assertNotNull(allUsersCompanies);
		assertEquals(4, allUsersCompanies.size());
		
		Profile profile = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(profile);
		Integer userId = profile.getId();
		
		List<UserCompany> user1Companies = userCompanyRepository.findByProfileId(userId);
		assertNotNull(user1Companies);
		assertEquals(2, user1Companies.size());
		assertEquals(userCompany1UnitsAndServices, user1Companies.get(0).getUnitsAndServices());
		assertEquals(userCompany3UnitsAndServices, user1Companies.get(1).getUnitsAndServices());

		List<UserCompany> user2Companies = userCompanyRepository.findByProfileId(userId);
		assertNotNull(user2Companies);
		assertEquals(2, user2Companies.size());
		assertEquals(userCompany1UnitsAndServices, user2Companies.get(0).getUnitsAndServices());
		assertEquals(userCompany3UnitsAndServices, user2Companies.get(1).getUnitsAndServices());
	}

	@Test
	public void mustUpdatePatientUnits() throws JsonProcessingException {
		List<UserCompany> allUsersCompanies = userCompanyRepository.findAll();
		assertNotNull(allUsersCompanies);
		assertEquals(4, allUsersCompanies.size());
		
		Profile profile1 = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		Profile profile2 = profileRepository.findByUsernameAndDomainName(USER3_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(profile1);
		assertNotNull(profile2);
		
		List<UserCompany> user1Companies = userCompanyRepository.findByProfileId(profile1.getId());
		List<UserCompany> user2Companies = userCompanyRepository.findByProfileId(profile2.getId());
		assertNotNull(user1Companies);
		assertNotNull(user2Companies);
		assertEquals(2, user1Companies.size());
		assertEquals(2, user2Companies.size());
		JsonNode user1Company1UnitsAndServices = companyById(COMPANY1_ID, user1Companies).getUnitsAndServices();
		JsonNode user2Company1UnitsAndServices = companyById(COMPANY2_ID, user2Companies).getUnitsAndServices();
		
		// Replace the current units of the user
		List<String> newUser1Company1UnitsList = Arrays.asList(new String[]{"03", "09", "HY"});
		JsonNode newUser1Company1Units = CompanyUtils.replaceUnitsOrServices(user1Company1UnitsAndServices, newUser1Company1UnitsList, CompanyUtils.PATIENTUNIT_JSON_PROPERTY);
		UserCompany user1Company1 = companyById(COMPANY1_ID, user1Companies);
		user1Company1.setUnitsAndServices(newUser1Company1Units);
		user1Company1 = userCompanyRepository.save(user1Company1);
		assertNotNull(user1Companies);
		assertTrue(CollectionUtils.isEqualCollection(newUser1Company1UnitsList, CompanyUtils.patientUnitNames(user1Company1.getUnitsAndServices())));
		
		// Remove units
		List<String> user2Company1UnitsList = CompanyUtils.patientUnitNames(user2Company1UnitsAndServices);
		List<String> user2Company1UnitsToRemove = Arrays.asList(new String[]{"2R", "P4"});
		List<String> newUser2Company1UnitsList = (List<String>) CollectionUtils.subtract(user2Company1UnitsList, user2Company1UnitsToRemove);
		JsonNode newUser2Company1Units = CompanyUtils.removeUnitsOrServices(user2Company1UnitsAndServices, user2Company1UnitsToRemove, CompanyUtils.PATIENTUNIT_JSON_PROPERTY);
		UserCompany user2Company1 = user2Companies.get(0);
		user2Company1.setUnitsAndServices(newUser2Company1Units);
		user2Company1 = userCompanyRepository.save(user2Company1);
		assertNotNull(user2Companies);
		assertTrue(CollectionUtils.isEqualCollection(newUser2Company1UnitsList, CompanyUtils.patientUnitNames(user2Company1.getUnitsAndServices())));
		
		// Add new units to existing
		user2Companies = userCompanyRepository.findByProfileId(profile2.getId());
		assertNotNull(user2Companies);
		assertEquals(2, user2Companies.size());
		List<String> user2Company1UnitsToAdd = Arrays.asList(new String[]{"2R", "P4"});
		newUser2Company1Units = CompanyUtils.addUnitsOrServices(user2Company1UnitsAndServices, user2Company1UnitsToAdd, CompanyUtils.PATIENTUNIT_JSON_PROPERTY);
		user2Company1 = companyById(COMPANY2_ID, user2Companies);
		user2Company1.setUnitsAndServices(newUser2Company1Units);
		user2Company1 = userCompanyRepository.save(user2Company1);
		assertNotNull(user2Companies);
		assertTrue(CollectionUtils.isEqualCollection(CompanyUtils.patientUnitNames(userCompany2UnitsAndServices), CompanyUtils.patientUnitNames(user2Company1.getUnitsAndServices())));
	}

	@Test
	public void mustUpdateHospServices() throws JsonProcessingException {
		List<UserCompany> allUsersCompanies = userCompanyRepository.findAll();
		assertNotNull(allUsersCompanies);
		assertEquals(4, allUsersCompanies.size());
		
		Profile profile1 = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		Profile profile2 = profileRepository.findByUsernameAndDomainName(USER3_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(profile1);
		assertNotNull(profile2);
		
		List<UserCompany> user1Companies = userCompanyRepository.findByProfileId(profile1.getId());
		List<UserCompany> user2Companies = userCompanyRepository.findByProfileId(profile2.getId());
		assertNotNull(user1Companies);
		assertNotNull(user2Companies);
		assertEquals(2, user1Companies.size());
		assertEquals(2, user2Companies.size());
		JsonNode user1Company1UnitsAndServices = companyById(COMPANY1_ID, user1Companies).getUnitsAndServices();
		JsonNode user2Company1UnitsAndServices = companyById(COMPANY2_ID, user2Companies).getUnitsAndServices();
		
		// Replace the current units of the user
		List<String> newUser1Company1ServicesList = Arrays.asList(new String[]{"1DIA", "2CHI", "2DIE", "1HYP", "2HYP", "2LAB"});
		JsonNode newUser1Company1Services = CompanyUtils.replaceUnitsOrServices(user1Company1UnitsAndServices, newUser1Company1ServicesList, CompanyUtils.HOSPSERVICE_JSON_PROPERTY);
		UserCompany user1Company1 = companyById(COMPANY1_ID, user1Companies);
		user1Company1.setUnitsAndServices(newUser1Company1Services);
		user1Company1 = userCompanyRepository.save(user1Company1);
		assertNotNull(user1Companies);
		assertTrue(CollectionUtils.isEqualCollection(newUser1Company1ServicesList, CompanyUtils.hospServiceNames(user1Company1.getUnitsAndServices())));
		
		// Remove units
		List<String> user2Company1ServicesList = CompanyUtils.hospServiceNames(user2Company1UnitsAndServices);
		List<String> user2Company1ServicesToRemove = Arrays.asList(new String[]{"TAKL", "RES", "IPS"});
		List<String> newUser2Company1ServicesList = (List<String>) CollectionUtils.subtract(user2Company1ServicesList, user2Company1ServicesToRemove);
		JsonNode newUser2Company1Services = CompanyUtils.removeUnitsOrServices(user2Company1UnitsAndServices, user2Company1ServicesToRemove, CompanyUtils.HOSPSERVICE_JSON_PROPERTY);
		UserCompany user2Company1 = user2Companies.get(0);
		user2Company1.setUnitsAndServices(newUser2Company1Services);
		user2Company1 = userCompanyRepository.save(user2Company1);
		assertNotNull(user2Companies);
		assertTrue(CollectionUtils.isEqualCollection(newUser2Company1ServicesList, CompanyUtils.hospServiceNames(user2Company1.getUnitsAndServices())));
		
		// Add new units to existing
		user2Companies = userCompanyRepository.findByProfileId(profile2.getId());
		assertNotNull(user2Companies);
		assertEquals(2, user2Companies.size());
		List<String> user2Company1ServicesToAdd = Arrays.asList(new String[]{"TAKL", "RES", "IPS"});
		newUser2Company1Services = CompanyUtils.addUnitsOrServices(user2Company1UnitsAndServices, user2Company1ServicesToAdd, CompanyUtils.HOSPSERVICE_JSON_PROPERTY);
		user2Company1 = companyById(COMPANY2_ID, user2Companies);
		user2Company1.setUnitsAndServices(newUser2Company1Services);
		user2Company1 = userCompanyRepository.save(user2Company1);
		assertNotNull(user2Companies);
		assertTrue(CollectionUtils.isEqualCollection(CompanyUtils.hospServiceNames(userCompany2UnitsAndServices), CompanyUtils.hospServiceNames(user2Company1.getUnitsAndServices())));
	}

	@Test
	public void mustDeleteByIdTest() {
		Profile profile = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(profile);
		Integer userId = profile.getId();
		List<UserCompany> userCompanies = userCompanyRepository.findByProfileIdAndCompanyId(userId, COMPANY3_ID);
		assertNotNull(userCompanies);
		assertEquals(1, userCompanies.size());
		assertEquals(COMPANY3_ID, userCompanies.get(0).getCompanyId());
		userCompanyRepository.deleteByProfileIdAndCompanyId(userId, COMPANY3_ID);
		userCompanies = userCompanyRepository.findByProfileIdAndCompanyId(userId, COMPANY3_ID);
		assertNotNull(userCompanies);
		assertEquals(0, userCompanies.size());
	}
	
	@Test
	public void mustDeleteTest() {
		Profile profile = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		List<UserCompany> usersCompanies = userCompanyRepository.findByProfileId(profile.getId());
		assertNotNull(usersCompanies);
		assertEquals(2, usersCompanies.size());
		usersCompanies = userCompanyRepository.findByProfileIdAndCompanyId(profile.getId(), COMPANY1_ID);
		assertNotNull(usersCompanies);
		assertEquals(1, usersCompanies.size());
		userCompanyRepository.deleteByProfileIdAndCompanyId(profile.getId(), COMPANY1_ID);
		usersCompanies = userCompanyRepository.findByProfileIdAndCompanyId(profile.getId(), COMPANY1_ID);
		assertNotNull(usersCompanies);
		assertEquals(0, usersCompanies.size());
		usersCompanies = userCompanyRepository.findByCompanyId(COMPANY3_ID);
		assertNotNull(usersCompanies);
		assertEquals(2, usersCompanies.size());
		userCompanyRepository.deleteByCompanyId(COMPANY3_ID);
		usersCompanies = userCompanyRepository.findByCompanyId(COMPANY3_ID);
		assertNotNull(usersCompanies);
		assertEquals(0, usersCompanies.size());
	}
	
	private UserCompany companyById(int companyId, List<UserCompany> userCompanies) {
		for(UserCompany userCompany : userCompanies) {
			if(companyId == userCompany.getCompanyId()) {
				return userCompany;
			}
		}
		return null;
	}
	
	@After
	public void tearDown() {
		userCompanyRepository.deleteAll();
		profileRepository.deleteAll();
	}
	
}
