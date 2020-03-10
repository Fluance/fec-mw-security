/**
 * 
 */
package net.fluance.security.core.repository.jpa;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import net.fluance.app.test.AbstractTest;
import net.fluance.security.core.Application;
import net.fluance.security.core.model.jpa.Profile;
import net.fluance.security.core.model.jpa.UserCompanyIdentity;
import net.fluance.security.core.repository.jpa.IProfileRepository;
import net.fluance.security.core.repository.jpa.IUserCompanyIdentityRepository;

@ComponentScan("net.fluance.security.core")
@SpringBootTest(classes = Application.class)
public class UserCompanyIdentityRepositoryTest extends AbstractTest {

	@Autowired
	private IUserCompanyIdentityRepository userCompanyIdentityRepository;
	@Autowired
	private IProfileRepository profileRepository;
	private static final String USER1_NAME = "localtestuser1";
	private static final String USER3_NAME = "localtestuser3";
	private static final Integer COMPANY1_ID = 1;
	private static final Integer COMPANY2_ID = 2;
	private static final Integer COMPANY3_ID = 3;
	private static final String LOCAL_DOMAIN_NAME = "PRIMARY";

	private static final int PROVIDER1_ID = 1;
	private static final String COMPANY1_PROVIDER1_STAFFID = "2362";
	private static final String COMPANY2_PROVIDER1_STAFFID = "1032";
	private static final String COMPANY3_PROVIDER1_STAFFID = "1001";
	private static final String COMPANY4_PROVIDER1_STAFFID = "210";
	private static final String COMPANY5_PROVIDER1_STAFFID = "212";
	
	@Before
	public void setup() {
		userCompanyIdentityRepository.deleteAll();
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
		
		UserCompanyIdentity userCompany1 = new UserCompanyIdentity();
		userCompany1.setCompanyId(COMPANY1_ID);
		userCompany1.setProfileId(userProfile1.getId());
		userCompany1.setProviderId(PROVIDER1_ID);
		userCompany1.setStaffId(COMPANY1_PROVIDER1_STAFFID);
		userCompanyIdentityRepository.save(userCompany1);
		
		UserCompanyIdentity userCompany2 = new UserCompanyIdentity();
		userCompany2.setCompanyId(COMPANY3_ID);
		userCompany2.setProfileId(userProfile1.getId());
		userCompany2.setProviderId(PROVIDER1_ID);
		userCompany2.setStaffId(COMPANY2_PROVIDER1_STAFFID);
		userCompanyIdentityRepository.save(userCompany2);
		
		UserCompanyIdentity userCompany3 = new UserCompanyIdentity();
		userCompany3.setCompanyId(COMPANY2_ID);
		userCompany3.setProfileId(userProfile2.getId());
		userCompany3.setProviderId(PROVIDER1_ID);
		userCompany3.setStaffId(COMPANY3_PROVIDER1_STAFFID);
		userCompanyIdentityRepository.save(userCompany3);
		
		UserCompanyIdentity userCompany4 = new UserCompanyIdentity();
		userCompany4.setCompanyId(COMPANY3_ID);
		userCompany4.setProfileId(userProfile2.getId());
		userCompany4.setProviderId(PROVIDER1_ID);
		userCompany4.setStaffId(COMPANY4_PROVIDER1_STAFFID);
		userCompanyIdentityRepository.save(userCompany4);
	}
	
	@Test
	public void mustCreateTest() {
		Profile profile = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		UserCompanyIdentity userCompanyIdentity = new UserCompanyIdentity();
		userCompanyIdentity.setCompanyId(COMPANY2_ID);
		userCompanyIdentity.setProfileId(profile.getId());
		userCompanyIdentity.setProviderId(PROVIDER1_ID);
		userCompanyIdentity.setStaffId(COMPANY5_PROVIDER1_STAFFID);
		
		UserCompanyIdentity createdUserCompany = userCompanyIdentityRepository.save(userCompanyIdentity);
		assertNotNull(createdUserCompany);
	}
	
	@Test
	public void mustCreateByNamesTest() {
		Profile profile = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		UserCompanyIdentity userCompanyIdentity = new UserCompanyIdentity();
		userCompanyIdentity.setCompanyId(COMPANY2_ID);
		userCompanyIdentity.setProfileId(profile.getId());
		userCompanyIdentity.setProviderId(PROVIDER1_ID);
		userCompanyIdentity.setStaffId(COMPANY5_PROVIDER1_STAFFID);
		UserCompanyIdentity createdUserCompany = userCompanyIdentityRepository.save(userCompanyIdentity);
		assertNotNull(createdUserCompany);
	}
	
	@Test
	public void mustFindTest() {
		List<UserCompanyIdentity> allUsersCompanies = userCompanyIdentityRepository.findAll();
		assertNotNull(allUsersCompanies);
		assertEquals(4, allUsersCompanies.size());
		Profile profile = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		Integer userId = profile.getId();
		List<UserCompanyIdentity> usersCompanies = userCompanyIdentityRepository.findByProfileId(userId);
		assertNotNull(usersCompanies);
		
		usersCompanies = userCompanyIdentityRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		assertNotNull(usersCompanies);
		assertEquals(2, usersCompanies.size());
		
		usersCompanies = userCompanyIdentityRepository.findByUsernameAndDomainNameAndCompanyId(USER1_NAME, LOCAL_DOMAIN_NAME, COMPANY1_ID);
		assertNotNull(usersCompanies);
		assertEquals(1, usersCompanies.size());
	}
	
	@Test
	public void mustDeleteTest() {
		Profile profile = profileRepository.findByUsernameAndDomainName(USER1_NAME, LOCAL_DOMAIN_NAME);
		List<UserCompanyIdentity> usersCompanies = userCompanyIdentityRepository.findByProfileId(profile.getId());
		assertNotNull(usersCompanies);
		assertEquals(2, usersCompanies.size());
		usersCompanies = userCompanyIdentityRepository.findByProfileIdAndCompanyId(profile.getId(), COMPANY1_ID);
		assertNotNull(usersCompanies);
		assertEquals(1, usersCompanies.size());
		userCompanyIdentityRepository.deleteByProfileIdAndCompanyId(profile.getId(), COMPANY1_ID);
		usersCompanies = userCompanyIdentityRepository.findByProfileIdAndCompanyId(profile.getId(), COMPANY1_ID);
		assertNotNull(usersCompanies);
		assertEquals(0, usersCompanies.size());
		usersCompanies = userCompanyIdentityRepository.findByCompanyId(COMPANY3_ID);
		assertNotNull(usersCompanies);
		assertEquals(2, usersCompanies.size());
		userCompanyIdentityRepository.deleteByCompanyId(COMPANY3_ID);
		usersCompanies = userCompanyIdentityRepository.findByCompanyId(COMPANY3_ID);
		assertNotNull(usersCompanies);
		assertEquals(0, usersCompanies.size());
	}
	
	@After
	public void tearDown() {
		userCompanyIdentityRepository.deleteAll();
		profileRepository.deleteAll();
	}
	
}
