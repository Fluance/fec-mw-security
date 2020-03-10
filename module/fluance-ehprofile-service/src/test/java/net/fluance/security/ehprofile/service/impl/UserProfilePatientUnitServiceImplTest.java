package net.fluance.security.ehprofile.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.fluance.security.core.model.jpa.UserCompany;
import net.fluance.security.core.repository.jpa.IUserCompanyRepository;
import net.fluance.security.ehprofile.test.utils.PatientUnitMocks;
import net.fluance.security.ehprofile.test.utils.UserCompanyMocks;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserProfilePatientUnitServiceImplTest {

	private static final String USERNAME = "test";
	private static final String DOMAIN = "PRIMARY";
	
	@TestConfiguration
	static class Configuration {
		@Bean
		public UserProfilePatientUnitServiceImpl userProfilePatientUnitServiceImpl() {
			return new UserProfilePatientUnitServiceImpl();
		}
	}
	
	@Before
	public void setUp() {
		Mockito.reset(userCompanyRepository);
		
		//findByUsernameAndDomainNameAndCompanyId return a default value using the companyId 
		doAnswer(new Answer<UserCompany>(){
			@Override
			public UserCompany answer(InvocationOnMock invocation) throws Throwable {
				return UserCompanyMocks.getUserCompany(invocation.getArgumentAt(2, Integer.class), 1);
			}			
		}).when(userCompanyRepository).findByUsernameAndDomainNameAndCompanyId(USERNAME, DOMAIN, 1);
		
		//userCompanyRepository.save(...) return same instance, as it has been saved
		doAnswer(new Answer<UserCompany>(){
			@Override
			public UserCompany answer(InvocationOnMock invocation) throws Throwable {
				return invocation.getArgumentAt(0, UserCompany.class);
			}
		}).when(userCompanyRepository).save(Mockito.any(UserCompany.class));
	}
	
	@MockBean
	private IUserCompanyRepository userCompanyRepository;
	
	@Autowired
	UserProfilePatientUnitServiceImpl userProfilePatientUnitService;
	
	@Test
	public void grantPatientUnit_should_grant() throws Exception {
		boolean granted = userProfilePatientUnitService.grantPatientUnit(USERNAME, DOMAIN, 1, "HOSP1");
		
		verify(userCompanyRepository, times(1)).findByUsernameAndDomainNameAndCompanyId(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));
		assertEquals("Should return true", true, granted);
	}
	
	@Test
	public void grantPatientUnit_save_return_null_should_not_grant() throws Exception {
		Mockito.when(userCompanyRepository.save(Mockito.any(UserCompany.class))).thenReturn(null);
		
		boolean granted = userProfilePatientUnitService.grantPatientUnit(USERNAME, DOMAIN, 1, "HOSP1");
				
		verify(userCompanyRepository, times(1)).findByUsernameAndDomainNameAndCompanyId(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));
		assertEquals("Should return false", false, granted);
	}
	
	@Test
	public void grantPatientUnit_doamin_null_should_return() throws Exception {
		List<String> hospServices = PatientUnitMocks.getListOfPatientUnitsAsListOfStrings();
		boolean granted = userProfilePatientUnitService.grantPatientUnit(USERNAME, null, 1, hospServices.get(0));
		
		verify(userCompanyRepository, times(1)).findByUsernameAndDomainNameAndCompanyId(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));
		assertEquals("Should return true", true, granted);
	}
	
	@Test
	public void grantPatientUnits_foo_username_and_domain() throws Exception {
		List<String> hospServices = PatientUnitMocks.getListOfPatientUnitsAsListOfStrings();
		boolean granted = userProfilePatientUnitService.grantPatientUnits("foo", "foodomain", 1, hospServices);
		
		verify(userCompanyRepository, times(1)).findByUsernameAndDomainNameAndCompanyId(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));
		assertEquals("Should return true", true, granted);
	}
	
	@Test
	public void grantHospServices_foo_username_and_domain_services_empty() throws Exception {
		boolean granted = userProfilePatientUnitService.grantPatientUnits(USERNAME, DOMAIN, 1, new ArrayList<String>());
		
		verify(userCompanyRepository, never()).findByUsernameAndDomainNameAndCompanyId(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
		verify(userCompanyRepository, never()).save(Mockito.any(UserCompany.class));
		assertEquals("Should return true", true, granted);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void grantPatientUnits_null_services_list_services_null_IllegalArgumentExceptionIllegalArgumentException() throws Exception {
		Integer companyId = 1;
		boolean allGranted = userProfilePatientUnitService.grantPatientUnits(USERNAME, DOMAIN, companyId, null);

		assertEquals("Should return true", true, allGranted);
	}
	
	@Test
	public void grantPatientUnits_one_service_not_grant() throws Exception {
		Mockito.when(userCompanyRepository.save(Mockito.any(UserCompany.class))).thenReturn(null);
		
		boolean allGranted = userProfilePatientUnitService.grantPatientUnits(USERNAME, DOMAIN, 1, PatientUnitMocks.getListOfPatientUnitsAsListOfStrings());
		
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));
		assertEquals("Should return false", false, allGranted);
	}
	
	@Test
	public void setPatientUnits_should_return() throws Exception {
		Integer companyId = 1;
		List<String> hospServices = PatientUnitMocks.getListOfPatientUnitsAsListOfStrings();
		
		boolean allSetted = userProfilePatientUnitService.setPatientUnits(USERNAME, DOMAIN, companyId, hospServices);
		
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));
		assertEquals("Should return true", true, allSetted);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void setPatientUnits_null_services_list_IllegalArgumentException() throws Exception {
		userProfilePatientUnitService.setPatientUnits(USERNAME, DOMAIN, 1, null);
	}
	
	@Test
	public void setPatientUnits_one_company_not_grant() throws Exception {
		Mockito.when(userCompanyRepository.save(Mockito.any(UserCompany.class))).thenReturn(null);
		
		boolean allSetted = userProfilePatientUnitService.setPatientUnits(USERNAME, DOMAIN, 1, PatientUnitMocks.getListOfPatientUnitsAsListOfStrings());
		
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));
		assertEquals("Should return false", false, allSetted);
	}
	
	@Test
	public void revokePatientUnit_should_return() throws Exception {
		boolean revoked = userProfilePatientUnitService.revokePatientUnit(USERNAME, DOMAIN, 1, PatientUnitMocks.getListOfPatientUnitsAsListOfStrings().get(0));
				
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));		
		assertEquals("Should return true", true, revoked);
	}
	
	@Test
	public void grantPatientUnits_should_return() throws Exception {
		boolean allGranted = userProfilePatientUnitService.grantPatientUnits(USERNAME, DOMAIN, 1, PatientUnitMocks.getListOfPatientUnitsAsListOfStrings());
		
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));
		assertEquals("Should return true", true, allGranted);
	}
	
	@Test
	public void revokePatientUnit_service_null_should_return() throws Exception {
		Integer companyId = 1;
		boolean revoked = userProfilePatientUnitService.revokePatientUnit(USERNAME, DOMAIN, companyId, null);
			
		verify(userCompanyRepository, never()).deleteByUsernameAndDomainNameAndCompanyId(USERNAME, DOMAIN, 1);		
		assertEquals("Should return true", true, revoked);
	}	
	
}