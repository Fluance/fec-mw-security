package net.fluance.security.ehprofile.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.fluance.app.data.model.identity.CompanyStaffId;
import net.fluance.security.core.model.jpa.UserCompany;
import net.fluance.security.core.repository.jpa.IProfileRepository;
import net.fluance.security.core.repository.jpa.IUserCompanyIdentityRepository;
import net.fluance.security.core.repository.jpa.IUserCompanyRepository;
import net.fluance.security.core.support.exception.NotFoundException;
import net.fluance.security.ehprofile.service.UserProfileService;
import net.fluance.security.ehprofile.service.UserProfileValidatorService;
import net.fluance.security.ehprofile.test.utils.CompanyStaffIdMocks;
import net.fluance.security.ehprofile.test.utils.EhProfileMocks;
import net.fluance.security.ehprofile.test.utils.GrantedCompanyMocks;
import net.fluance.security.ehprofile.test.utils.HospServiceMocks;
import net.fluance.security.ehprofile.test.utils.PatientUnitMocks;
import net.fluance.security.ehprofile.test.utils.ProfileMocks;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserProfileCompanyServiceImplTest {
	
	private static final String USERNAME = "test";
	private static final String DOMAIN = "PRIMARY";

	@TestConfiguration
	static class Configuration {
		@Bean
		public UserProfileCompanyServiceImpl userProfileCompanyService() {
			return new UserProfileCompanyServiceImpl();
		}
		@Bean
		public UserProfileValidatorService userProfileValidatorService() {
			return new UserProfileValidatorServiceImpl();
		}
		@Bean
		PropertyPlaceholderConfigurer propConfig() {
			PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
			
			Properties properties = new Properties();
			properties.put("identity.domains.default", DOMAIN);
			
			propertyPlaceholderConfigurer.setProperties(properties);

			propertyPlaceholderConfigurer.setSystemPropertiesModeName("SYSTEM_PROPERTIES_MODE_OVERRIDE");

			return propertyPlaceholderConfigurer;
		}
	}

	@Before
	public void setUp() throws NotFoundException {
		Mockito.reset(userCompanyIdentityRepository);
		Mockito.reset(userCompanyRepository);
		Mockito.reset(profileRepository);
		Mockito.reset(userProfileService);
		
		//Set default answer for userProfileValidatorService. The profile always exsits		
		Mockito.when(profileRepository.findProfilesByUsernameAndDomainName(USERNAME, DOMAIN)).thenReturn(ProfileMocks.getDefaultProfileInList(USERNAME, DOMAIN));
		
		//Set default answer for userCompanyRepository.save(UserCompany.class)), return same class
		doAnswer(new Answer<UserCompany>() {
			@Override
			public UserCompany answer(InvocationOnMock invocation) throws Throwable {
				UserCompany userCompany = invocation.getArgumentAt(0, UserCompany.class);
				
				return userCompany;
			}
		}).when(userCompanyRepository).save(Mockito.any(UserCompany.class));
	}

	@MockBean
	private IUserCompanyIdentityRepository userCompanyIdentityRepository;
	@MockBean
	private IUserCompanyRepository userCompanyRepository;
	@MockBean
	private IProfileRepository profileRepository;
	//UserProfileService is test separated
	@MockBean
	private UserProfileService userProfileService;
	
	@Autowired
	private UserProfileCompanyServiceImpl userProfileCompanyService;

	//grantCompany
	@Test
	public void grantCompany_should_grant() throws Exception {
		Integer companyId = 1;
		List<String> patientUnits = PatientUnitMocks.getListOfPatientUnitsAsListOfStrings();
		List<String> hospServices = HospServiceMocks.getListOfHospServiceAsListOfStrings();
		List<CompanyStaffId> staffIds = CompanyStaffIdMocks.getListOfCompanyStaffId();
		//Set values to null for one of the instance, the call to userCompanyIdentityRepository.saveByUsernameAndDomainName(...) should not to be raised
		staffIds.get(1).setProviderId(null);
		staffIds.get(1).setStaffId(null);

		boolean granted = userProfileCompanyService.grantCompany(USERNAME, DOMAIN, companyId, patientUnits, hospServices, staffIds);
		
		verify(userCompanyIdentityRepository, times(9)).saveByUsernameAndDomainName(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString()); 
		assertEquals("Should return true", true, granted);
	}
	
	@Test
	public void grantCompany_save_return_null_should_grant() throws Exception {
		Integer companyId = 1;
		List<String> patientUnits = PatientUnitMocks.getListOfPatientUnitsAsListOfStrings();
		List<String> hospServices = HospServiceMocks.getListOfHospServiceAsListOfStrings();
		List<CompanyStaffId> staffIds = CompanyStaffIdMocks.getListOfCompanyStaffId();
		
		Mockito.when(userCompanyRepository.save(Mockito.any(UserCompany.class))).thenReturn(null);
		
		boolean granted = userProfileCompanyService.grantCompany(USERNAME, DOMAIN, companyId, patientUnits, hospServices, staffIds);
				
		verify(userCompanyIdentityRepository, never()).saveByUsernameAndDomainName(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
		assertEquals("Should return true", true, granted);
	}
	
	@Test
	public void grantCompany_doamin_null_should_return() throws Exception {
		Integer companyId = 1;
		List<String> patientUnits = PatientUnitMocks.getListOfPatientUnitsAsListOfStrings();
		List<String> hospServices = HospServiceMocks.getListOfHospServiceAsListOfStrings();
		List<CompanyStaffId> staffIds = null;
		
		boolean granted = userProfileCompanyService.grantCompany(USERNAME, null, companyId, patientUnits, hospServices, staffIds);
		
		verify(userCompanyIdentityRepository, never()).saveByUsernameAndDomainName(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
		assertEquals("Should return true", true, granted);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void grantCompany_companyId_null() throws Exception {
		List<String> patientUnits = PatientUnitMocks.getListOfPatientUnitsAsListOfStrings();
		List<String> hospServices = HospServiceMocks.getListOfHospServiceAsListOfStrings();
		List<CompanyStaffId> staffIds = new ArrayList<>();
		
		userProfileCompanyService.grantCompany(USERNAME, DOMAIN, null, patientUnits, hospServices, staffIds);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void grantCompany_username_null() throws Exception {
		List<String> patientUnits = PatientUnitMocks.getListOfPatientUnitsAsListOfStrings();
		List<String> hospServices = HospServiceMocks.getListOfHospServiceAsListOfStrings();
		List<CompanyStaffId> staffIds = new ArrayList<>();
		
		userProfileCompanyService.grantCompany(null, DOMAIN, 1, patientUnits, hospServices, staffIds);
	}
	
	@Test(expected=NotFoundException.class)
	public void grantCompany_foo_username_and_domain() throws Exception {
		List<String> patientUnits = PatientUnitMocks.getListOfPatientUnitsAsListOfStrings();
		List<String> hospServices = HospServiceMocks.getListOfHospServiceAsListOfStrings();
		List<CompanyStaffId> staffIds = new ArrayList<>();
		
		userProfileCompanyService.grantCompany("foo", "foodomain", 1, patientUnits, hospServices, staffIds);
	}
	
	//grantCompanies
	@Test
	public void grantCompanies_should_return() throws Exception {
		boolean allGranted = userProfileCompanyService.grantCompanies(USERNAME, DOMAIN, GrantedCompanyMocks.getListOfGrantedCompany());
	
		verify(userCompanyRepository, times(10)).save(Mockito.any(UserCompany.class));
		verify(userCompanyIdentityRepository, times(100)).saveByUsernameAndDomainName(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
		assertEquals("Should return true", true, allGranted);
	}
	
	@Test
	public void grantCompanies_null_companies_list_should_return() throws Exception {
		boolean allGranted = userProfileCompanyService.grantCompanies(USERNAME, DOMAIN, null);
		
		verify(userCompanyRepository, never()).save(Mockito.any(UserCompany.class));
		verify(userCompanyIdentityRepository, never()).saveByUsernameAndDomainName(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
		assertEquals("Should return true", true, allGranted);
	}
	
	@Test
	public void grantCompanies_one_company_not_grant() throws Exception {
		doAnswer(new Answer<UserCompany>() {
			@Override
			public UserCompany answer(InvocationOnMock invocation) throws Throwable {				
				UserCompany userCompany = invocation.getArgumentAt(0, UserCompany.class);
				
				if(userCompany.getCompanyId() == 1) {
					return null;
				} else {
					return userCompany;
				}
			}
		}).when(userCompanyRepository).save(Mockito.any(UserCompany.class));
		
		boolean allGranted = userProfileCompanyService.grantCompanies(USERNAME, DOMAIN, GrantedCompanyMocks.getListOfGrantedCompany());
		
		verify(userCompanyRepository, times(10)).save(Mockito.any(UserCompany.class));
		verify(userCompanyIdentityRepository, times(90)).saveByUsernameAndDomainName(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
		assertEquals("Should return true", true, allGranted);
	}
	
	//setCompanies
	@Test
	public void setCompanies_should_return() throws Exception {
		boolean allSetted = userProfileCompanyService.setCompanies(USERNAME, DOMAIN, GrantedCompanyMocks.getListOfGrantedCompany());
		
		verify(userCompanyIdentityRepository, times(1)).deleteByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(userCompanyRepository, times(1)).deleteByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(userCompanyRepository, times(10)).save(Mockito.any(UserCompany.class));
		verify(userCompanyIdentityRepository, times(100)).saveByUsernameAndDomainName(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
		assertEquals("Should return true", true, allSetted);
	}
	
	@Test
	public void setCompanies_null_companies_list_should_return() throws Exception {
		boolean allSetted = userProfileCompanyService.setCompanies(USERNAME, DOMAIN, null);
		
		verify(userCompanyIdentityRepository, never()).deleteByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(userCompanyRepository, never()).deleteByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(userCompanyRepository, never()).save(Mockito.any(UserCompany.class));
		verify(userCompanyIdentityRepository, never()).saveByUsernameAndDomainName(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
		assertEquals("Should return true", true, allSetted);
	}
	
	@Test
	public void setCompanies_one_company_not_grant() throws Exception {
		doAnswer(new Answer<UserCompany>() {
			@Override
			public UserCompany answer(InvocationOnMock invocation) throws Throwable {				
				UserCompany userCompany = invocation.getArgumentAt(0, UserCompany.class);
				
				if(userCompany.getCompanyId() == 1) {
					return null;
				} else {
					return userCompany;
				}
			}
		}).when(userCompanyRepository).save(Mockito.any(UserCompany.class));
		
		boolean allSetted = userProfileCompanyService.setCompanies(USERNAME, DOMAIN, GrantedCompanyMocks.getListOfGrantedCompany());
		
		verify(userCompanyIdentityRepository, times(1)).deleteByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(userCompanyRepository, times(1)).deleteByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(userCompanyRepository, times(10)).save(Mockito.any(UserCompany.class));
		verify(userCompanyIdentityRepository, times(90)).saveByUsernameAndDomainName(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
		assertEquals("Should return true", true, allSetted);
	}
	
	//revokeCompany
	@Test
	public void revokeCompany_should_return() {
		boolean revoked = userProfileCompanyService.revokeCompany(USERNAME, DOMAIN, 1);
				
		verify(userCompanyIdentityRepository, times(1)).deleteByUsernameAndDomainNameAndCompanyId(USERNAME, DOMAIN, 1);
		verify(userCompanyRepository, times(1)).deleteByUsernameAndDomainNameAndCompanyId(USERNAME, DOMAIN, 1);
		assertEquals("Should return true", true, revoked);
	}
	
	@Test
	public void revokeCompany_company_null_should_return() {
		boolean revoked = userProfileCompanyService.revokeCompany(USERNAME, DOMAIN, null);
				
		verify(userCompanyIdentityRepository, never()).deleteByUsernameAndDomainNameAndCompanyId(USERNAME, DOMAIN, 1);
		verify(userCompanyRepository, never()).deleteByUsernameAndDomainNameAndCompanyId(USERNAME, DOMAIN, 1);	
		assertEquals("Should return true", true, revoked);
	}
	
	//revokeCompanies
	@Test 
	public void revokeCompanies_should_return() {
		boolean revoked = userProfileCompanyService.revokeCompanies(USERNAME, DOMAIN, 1, null, null);
		
		verify(userCompanyIdentityRepository, times(1)).deleteByUsernameAndDomainNameAndCompanyId(USERNAME, DOMAIN, 1);
		verify(userCompanyRepository, times(1)).deleteByUsernameAndDomainNameAndCompanyId(USERNAME, DOMAIN, 1);
		assertEquals("Should return true", true, revoked);
	}
	
	//hasAccessToCompany
	@Test
	public void hasAccessToCompany_has_access() throws Exception {
		Mockito.when(userProfileService.find(USERNAME, DOMAIN)).thenReturn(EhProfileMocks.getEhProfile(USERNAME, DOMAIN));
		
		boolean hasAccess = userProfileCompanyService.hasAccessToCompany(USERNAME, DOMAIN, "HOSP1");
		
		verify(userProfileService, times(1)).find(USERNAME, DOMAIN);
		assertEquals("Should return true", true, hasAccess);
	}
	
	@Test
	public void hasAccessToCompany_not_has_access() throws Exception {
		Mockito.when(userProfileService.find(USERNAME, DOMAIN)).thenReturn(EhProfileMocks.getEhProfile(USERNAME, DOMAIN));
		
		boolean hasAccess = userProfileCompanyService.hasAccessToCompany(USERNAME, DOMAIN, "HOSPFOO");
		
		verify(userProfileService, times(1)).find(USERNAME, DOMAIN);
		assertEquals("Should return false", false, hasAccess);
	}
	
	@Test(expected=net.fluance.app.web.util.exceptions.NotFoundException.class)
	public void hasAccessToCompany_NotFoundException() throws Exception {
		Mockito.when(userProfileService.find(USERNAME, DOMAIN)).thenReturn(null);
		
		userProfileCompanyService.hasAccessToCompany(USERNAME, DOMAIN, "HOSP1");
		
		verify(userProfileService, times(1)).find(USERNAME, DOMAIN);
	}

}