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
import net.fluance.security.ehprofile.test.utils.HospServiceMocks;
import net.fluance.security.ehprofile.test.utils.UserCompanyMocks;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserProfileHospServiceServiceImplTest {
	
	private static final String USERNAME = "test";
	private static final String DOMAIN = "PRIMARY";
	
	@TestConfiguration
	static class Configuration {
		@Bean
		public UserProfileHospServiceServiceImpl userProfileHospServiceService() {
			return new UserProfileHospServiceServiceImpl();
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
	UserProfileHospServiceServiceImpl userProfileHospServiceService;
	
	@Test
	public void grantHospService_should_grant() throws Exception {
		boolean granted = userProfileHospServiceService.grantHospService(USERNAME, DOMAIN, 1, "HOSP1");
		
		verify(userCompanyRepository, times(1)).findByUsernameAndDomainNameAndCompanyId(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));
		assertEquals("Should return true", true, granted);
	}
	
	@Test
	public void grantHospService_save_return_null_should_not_grant() throws Exception {
		Mockito.when(userCompanyRepository.save(Mockito.any(UserCompany.class))).thenReturn(null);
		
		boolean granted = userProfileHospServiceService.grantHospService(USERNAME, DOMAIN, 1, "HOSP1");
				
		verify(userCompanyRepository, times(1)).findByUsernameAndDomainNameAndCompanyId(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));
		assertEquals("Should return false", false, granted);
	}
	
	@Test
	public void grantHospService_doamin_null_should_return() throws Exception {
		List<String> hospServices = HospServiceMocks.getListOfHospServiceAsListOfStrings();
		boolean granted = userProfileHospServiceService.grantHospService(USERNAME, null, 1, hospServices.get(0));
		
		verify(userCompanyRepository, times(1)).findByUsernameAndDomainNameAndCompanyId(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));
		assertEquals("Should return true", true, granted);
	}
	
	@Test
	public void grantHospServices_foo_username_and_domain() throws Exception {
		boolean granted = userProfileHospServiceService.grantHospServices("foo", "foodomain", 1, HospServiceMocks.getListOfHospServiceAsListOfStrings());
		
		verify(userCompanyRepository, times(1)).findByUsernameAndDomainNameAndCompanyId(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));
		assertEquals("Should return true", true, granted);
	}
	
	@Test
	public void grantHospServices_foo_username_and_domain_services_empty() throws Exception {
		boolean granted = userProfileHospServiceService.grantHospServices(USERNAME, DOMAIN, 1, new ArrayList<String>());
		
		verify(userCompanyRepository, never()).findByUsernameAndDomainNameAndCompanyId(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
		verify(userCompanyRepository, never()).save(Mockito.any(UserCompany.class));
		assertEquals("Should return true", true, granted);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void grantHospServices_foo_username_and_domain_services_null_llegalArgumentExceptionIllegalArgumentException() throws Exception {
		boolean allGranted = userProfileHospServiceService.grantHospServices(USERNAME, DOMAIN, 1, null);
		
		assertEquals("Should return true", true, allGranted);
	}
	
	@Test
	public void grantHospServices_one_service_not_grant() throws Exception {
		Mockito.when(userCompanyRepository.save(Mockito.any(UserCompany.class))).thenReturn(null);
		
		boolean allGranted = userProfileHospServiceService.grantHospServices(USERNAME, DOMAIN, 1, HospServiceMocks.getListOfHospServiceAsListOfStrings());
		
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));
		assertEquals("Should return false", false, allGranted);
	}
	
	@Test
	public void setHospServices_should_return() throws Exception {
		Integer companyId = 1;
		List<String> hospServices = HospServiceMocks.getListOfHospServiceAsListOfStrings();
		
		boolean allSetted = userProfileHospServiceService.setHospServices(USERNAME, DOMAIN, companyId, hospServices);
		
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));
		assertEquals("Should return true", true, allSetted);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void setHospServices_null_services_list_IllegalArgumentException() throws Exception {
		userProfileHospServiceService.setHospServices(USERNAME, DOMAIN, 1, null);
	}
	
	@Test
	public void setHospServices_one_company_not_grant() throws Exception {
		Mockito.when(userCompanyRepository.save(Mockito.any(UserCompany.class))).thenReturn(null);
		
		boolean allSetted = userProfileHospServiceService.setHospServices(USERNAME, DOMAIN, 1, HospServiceMocks.getListOfHospServiceAsListOfStrings());
		
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));
		assertEquals("Should return false", false, allSetted);
	}
	
	@Test
	public void revokeHospService_should_return() throws Exception {
		boolean revoked = userProfileHospServiceService.revokeHospService(USERNAME, DOMAIN, 1, HospServiceMocks.getListOfHospServiceAsListOfStrings().get(0));
				
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));		
		assertEquals("Should return true", true, revoked);
	}
	
	@Test
	public void grantHospServices_should_return() throws Exception {
		boolean allGranted = userProfileHospServiceService.grantHospServices(USERNAME, DOMAIN, 1, HospServiceMocks.getListOfHospServiceAsListOfStrings());
		
		verify(userCompanyRepository, times(1)).save(Mockito.any(UserCompany.class));
		assertEquals("Should return true", true, allGranted);
	}
	
	@Test
	public void revokeHospService_service_null_should_return() throws Exception {
		Integer companyId = 1;
		boolean revoked = userProfileHospServiceService.revokeHospService(USERNAME, DOMAIN, companyId, null);
			
		verify(userCompanyRepository, never()).deleteByUsernameAndDomainNameAndCompanyId(USERNAME, DOMAIN, 1);		
		assertEquals("Should return true", true, revoked);
	}	
}