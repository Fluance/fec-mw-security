package net.fluance.security.ehprofile.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

import net.fluance.app.data.model.identity.EhProfile;
import net.fluance.app.data.model.identity.ProfileMetadata;
import net.fluance.app.data.model.identity.User;
import net.fluance.app.security.service.IUserService;
import net.fluance.security.core.model.jdbc.Company;
import net.fluance.security.core.model.jpa.Profile;
import net.fluance.security.core.model.jpa.Role;
import net.fluance.security.core.model.jpa.RoleMock;
import net.fluance.security.core.model.jpa.UserCompany;
import net.fluance.security.core.repository.jdbc.CompanyRepository;
import net.fluance.security.core.repository.jpa.IProfileMetadataRepository;
import net.fluance.security.core.repository.jpa.IProfileRepository;
import net.fluance.security.core.repository.jpa.IRoleRepository;
import net.fluance.security.core.repository.jpa.IUserClientDataRepository;
import net.fluance.security.core.repository.jpa.IUserCompanyIdentityRepository;
import net.fluance.security.core.repository.jpa.IUserCompanyRepository;
import net.fluance.security.core.repository.jpa.IUserTypeRepository;
import net.fluance.security.core.service.UserIdentityService;
import net.fluance.security.core.support.exception.NotFoundException;
import net.fluance.security.ehprofile.service.UserProfileCompanyService;
import net.fluance.security.ehprofile.service.UserProfileRolesService;
import net.fluance.security.ehprofile.service.UserProfileValidatorService;
import net.fluance.security.ehprofile.test.utils.CompanyMocks;
import net.fluance.security.ehprofile.test.utils.EhProfileMocks;
import net.fluance.security.ehprofile.test.utils.ProfileMocks;
import net.fluance.security.ehprofile.test.utils.RolesMocks;
import net.fluance.security.ehprofile.test.utils.UserCompanyIdentityMocks;
import net.fluance.security.ehprofile.test.utils.UserCompanyMocks;
import net.fluance.security.ehprofile.test.utils.UserMocks;
import net.fluance.security.ehprofile.test.utils.UserTypeMocks;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserProfileServiceImplTest {
	
	private static final String USERNAME = "test";
	private static final String DOMAIN = "PRIMARY";
	private static final String USER_TYPE = "application";
	private static final String DEFAULT_ROLE = "everyone";

	@TestConfiguration
	static class Configuration {
		@Bean
		public UserProfileServiceImpl userProfileService() {
			return new UserProfileServiceImpl();
		}
		@Bean
		public UserProfileValidatorService userProfileValidatorService() {
			return new UserProfileValidatorServiceImpl();
		}
		@Bean
		UserProfileRolesService userProfileRolesService() {
			return new UserProfileRolesServiceImpl();
		}
		@Bean
		UserProfileCompanyService userProfileCompanyService() {
			return new UserProfileCompanyServiceImpl();
		}
		@Bean
		PropertyPlaceholderConfigurer propConfig() {
			PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
			
			Properties properties = new Properties();
			properties.put("default.role.name", DEFAULT_ROLE);
			properties.put("identity.domains.default", DOMAIN);
			
			propertyPlaceholderConfigurer.setProperties(properties);

			propertyPlaceholderConfigurer.setSystemPropertiesModeName("SYSTEM_PROPERTIES_MODE_OVERRIDE");

			return propertyPlaceholderConfigurer;
		}
	}

	@Before
	public void setUp() throws Exception {
		Mockito.reset(profileRepository);
		Mockito.reset(userCompanyIdentityRepository);
		Mockito.reset(userCompanyRepository);
		Mockito.reset(userTypeRepository);
		Mockito.reset(companyRepository);
		Mockito.reset(roleRepository);
		Mockito.reset(profileMetadataRepository);
		Mockito.reset(keycloakUserService);
		Mockito.reset(userIdentityService);
		Mockito.reset(userClientDataRepository);
		
		//Set default answer for userProfileValidatorService. The profile always exsits		
		Mockito.when(profileRepository.findProfilesByUsernameAndDomainName(USERNAME, DOMAIN)).thenReturn(ProfileMocks.getDefaultProfileInList(USERNAME, DOMAIN));
		//Users always exists at keycloak
		Mockito.when(keycloakUserService.isExistingUser(USERNAME, DOMAIN)).thenReturn(true);
		//Users can be save always
		Mockito.when(profileRepository.saveByUsernameDomainNameAndUsertype(USERNAME, DOMAIN, USER_TYPE)).thenReturn(1);
		//Set default answer for profileRepository.save(Profile.class)), return same instance as it hab been saved
		doAnswer(new Answer<Profile>() {
			@Override
			public Profile answer(InvocationOnMock invocation) throws Throwable {
				return invocation.getArgumentAt(0, Profile.class);
			}
		}).when(profileRepository).save(Mockito.any(Profile.class));
		//Set default answer for userCompanyRepository.save(UserCompany.class)), return same class
		doAnswer(new Answer<UserCompany>() {
			@Override
			public UserCompany answer(InvocationOnMock invocation) throws Throwable {
				UserCompany userCompany = invocation.getArgumentAt(0, UserCompany.class);
				
				return userCompany;
			}
		}).when(userCompanyRepository).save(Mockito.any(UserCompany.class));
		//default values for roles save
		doAnswer(new Answer<Role>() {
			@Override
			public Role answer(InvocationOnMock invocation) throws Throwable {
				RoleMock role = new RoleMock();
				
				role.setName(invocation.getArgumentAt(0, String.class));
				
				return role;
			}
		}).when(roleRepository).findByName(Mockito.anyString());		
		
		doAnswer(new Answer<Role>() {
			@Override
			public Role answer(InvocationOnMock invocation) throws Throwable {
				RoleMock role = new RoleMock();
				role.setName(DEFAULT_ROLE);
				return role;
			}
		}).when(roleRepository).findByName(DEFAULT_ROLE);
		//Value for user type APPLICATION
		Mockito.when(userTypeRepository.findByName(UserTypeMocks.getUserType().getName())).thenReturn(UserTypeMocks.getUserType());
		//Set default answer for findByUsernameAndDomainName, always will return a value
		doAnswer(new Answer<Profile>() {
			@Override
			public Profile answer(InvocationOnMock invocation) throws Throwable {
				return ProfileMocks.getDefaultProfile(USERNAME, DOMAIN);
			}
		}).when(profileRepository).findByUsernameAndDomainName(Mockito.anyString(), Mockito.anyString());
	}
	
	@MockBean
	private IProfileRepository profileRepository;
	@MockBean
	private IUserCompanyIdentityRepository userCompanyIdentityRepository;
	@MockBean
	private IUserCompanyRepository userCompanyRepository;
	@MockBean
	private IUserTypeRepository userTypeRepository;
	@MockBean
	private CompanyRepository companyRepository;
	@MockBean
	private IRoleRepository roleRepository;
	@MockBean
	private IProfileMetadataRepository profileMetadataRepository;
	@MockBean	
	private IUserService keycloakUserService;
	@MockBean
	private UserIdentityService userIdentityService;
	@MockBean
	private IUserClientDataRepository userClientDataRepository;
	
	@Autowired
	UserProfileServiceImpl userProfileService;
	
	//create
	@Test
	public void create_should_create() throws Exception {
		boolean created = userProfileService.create(EhProfileMocks.getEhProfile("TEST", DOMAIN));
		
		//it is call 12 times due to the data validation at the other services
		verify(profileRepository, times(12)).findProfilesByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(profileRepository, times(1)).saveByUsernameDomainNameAndUsertype(USERNAME, DOMAIN, USER_TYPE);
		verify(profileRepository, times(3)).save(Mockito.any(Profile.class));
		assertEquals("Should return true", true, created);
	}
	
	@Test(expected = IllegalStateException.class)
	public void create_cant_create_user() throws Exception {
		Mockito.when(profileRepository.saveByUsernameDomainNameAndUsertype(USERNAME, DOMAIN, USER_TYPE)).thenReturn(0);

		userProfileService.create(EhProfileMocks.getEhProfile("TEST", DOMAIN));
	}
	
	@Test(expected = NotFoundException.class)
	public void create_user_not_exists_keycloak() throws Exception {		
		Mockito.when(keycloakUserService.isExistingUser(USERNAME, DOMAIN)).thenReturn(false);
		
		userProfileService.create(EhProfileMocks.getEhProfile("TEST", DOMAIN));
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected = NotFoundException.class)
	public void create_user_not_exists_keycloak_NotFoundException() throws Exception {		
		Mockito.when(keycloakUserService.isExistingUser(USERNAME, DOMAIN)).thenThrow(NotFoundException.class);

		userProfileService.create(EhProfileMocks.getEhProfile("TEST", DOMAIN));
	}
	
	//delete
	@SuppressWarnings("unchecked")
	@Test
	public void delete_should_delete() throws Exception {
		//Set first answer the profile, and a answer for the second time and empty list to simulate deletion
		Mockito.when(profileRepository.findProfilesByUsernameAndDomainName(USERNAME, DOMAIN)).thenReturn(ProfileMocks.getDefaultProfileInList(USERNAME, DOMAIN), new ArrayList<Profile>());
		
		boolean deleted = userProfileService.delete(USERNAME, DOMAIN);
		
		verify(profileRepository, times(2)).findProfilesByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(userClientDataRepository, times(1)).deleteByProfileId(Mockito.anyInt());
		assertEquals("Should return true", true, deleted);
	}
	
	@Test
	public void delete_should_not_delete() throws Exception {		
		boolean deleted = userProfileService.delete(USERNAME, DOMAIN);
		
		verify(profileRepository, times(2)).findProfilesByUsernameAndDomainName(USERNAME, DOMAIN);
		assertEquals("Should return false", false, deleted);
	}
	
	@Test(expected = NotFoundException.class)
	public void delete_NotFoundException() throws Exception {
		userProfileService.delete("foo", "fooDomain");
		
		verify(profileRepository, times(1)).findProfilesByUsernameAndDomainName("foo", "fooDomain");
	}
	
	//find
	@Test
	public void find_should_return() throws Exception {
		doAnswer(new Answer<List<Role>>() {
			@Override
			public List<Role> answer(InvocationOnMock invocation) throws Throwable {
				return RolesMocks.getListOfRoles();
			}
		}).when(roleRepository).getByProfileId(1);
		
		Mockito.when(userCompanyIdentityRepository.findByProfileId(1)).thenReturn(UserCompanyIdentityMocks.getListOfUserCompanyIdentities(1));	
		
		Mockito.when(userCompanyRepository.findByUsernameAndDomainName(USERNAME, DOMAIN)).thenReturn(UserCompanyMocks.getUserCompanies(1));
		
		doAnswer(new Answer<Company>() {
			@Override
			public Company answer(InvocationOnMock invocation) throws Throwable {
				return CompanyMocks.getCompany(invocation.getArgumentAt(0, Integer.class));
			}
		}).when(companyRepository).findOne(Mockito.anyInt());
		
		EhProfile ehProfile =  userProfileService.find(USERNAME, DOMAIN);
		
		verify(profileRepository, times(2)).findProfilesByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(userCompanyIdentityRepository, times(1)).findByProfileId(1);
		verify(userCompanyRepository, times(1)).findByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(companyRepository, times(5)).findOne(Mockito.anyInt());
		verify(roleRepository, times(1)).getByProfileId(1);
		assertNotNull(ehProfile);
	}
	
	@Test(expected = NotFoundException.class)
	public void find_NotFoundException() throws Exception {
		userProfileService.find("foo", "foo");
	}
	
	//findByPStaffIds
	@Test
	public void findByPStaffIds_should_return_one() throws Exception {
		
		Mockito.when(profileRepository.findByStaffIds("100", Long.valueOf(1l), Long.valueOf(2l))).thenReturn(ProfileMocks.getDefaultProfileInList(USERNAME, DOMAIN));
		
		doAnswer(new Answer<List<Role>>() {
			@Override
			public List<Role> answer(InvocationOnMock invocation) throws Throwable {
				return RolesMocks.getListOfRoles();
			}
		}).when(roleRepository).getByProfileId(1);
		
		Mockito.when(userCompanyIdentityRepository.findByProfileId(1)).thenReturn(UserCompanyIdentityMocks.getListOfUserCompanyIdentities(1));	
		
		Mockito.when(userCompanyRepository.findByUsernameAndDomainName(USERNAME, DOMAIN)).thenReturn(UserCompanyMocks.getUserCompanies(1));
		
		doAnswer(new Answer<Company>() {
			@Override
			public Company answer(InvocationOnMock invocation) throws Throwable {
				return CompanyMocks.getCompany(invocation.getArgumentAt(0, Integer.class));
			}
		}).when(companyRepository).findOne(Mockito.anyInt());
		
		List<EhProfile> ehProfiles = userProfileService.findByPStaffIds("100", Long.valueOf(1l), Long.valueOf(2l));
		
		verify(profileRepository, times(2)).findProfilesByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(profileRepository, times(1)).findByStaffIds("100", Long.valueOf(1l), Long.valueOf(2l));	
		assertNotNull("Should return a list", ehProfiles);
		assertEquals("Only one result expected", 1, ehProfiles.size());
	}
	
	@Test
	public void findByPStaffIds_should_return_none() throws Exception {
		
		Mockito.when(profileRepository.findByStaffIds("100", Long.valueOf(1l), Long.valueOf(2l))).thenReturn(ProfileMocks.getDefaultProfileInList("foo", "foo"));
		
		doAnswer(new Answer<List<Role>>() {
			@Override
			public List<Role> answer(InvocationOnMock invocation) throws Throwable {
				return RolesMocks.getListOfRoles();
			}
		}).when(roleRepository).getByProfileId(1);
		
		Mockito.when(userCompanyIdentityRepository.findByProfileId(1)).thenReturn(UserCompanyIdentityMocks.getListOfUserCompanyIdentities(1));	
		
		Mockito.when(userCompanyRepository.findByUsernameAndDomainName(USERNAME, DOMAIN)).thenReturn(UserCompanyMocks.getUserCompanies(1));
		
		doAnswer(new Answer<Company>() {
			@Override
			public Company answer(InvocationOnMock invocation) throws Throwable {
				return CompanyMocks.getCompany(invocation.getArgumentAt(0, Integer.class));
			}
		}).when(companyRepository).findOne(Mockito.anyInt());
		
		List<EhProfile> ehProfiles = userProfileService.findByPStaffIds("100", Long.valueOf(1l), Long.valueOf(2l));
		
		
		verify(profileRepository, times(1)).findProfilesByUsernameAndDomainName("foo", "foo");
		verify(profileRepository, times(1)).findByStaffIds("100", Long.valueOf(1l), Long.valueOf(2l));	
		assertNotNull("Should return a list", ehProfiles);
		assertEquals("No one result expected", 0, ehProfiles.size());
	}
	
	//exists
	@Test
	public void exists_it_exists() {
		boolean exists = userProfileService.exists(USERNAME, DOMAIN);
		
		verify(profileRepository, times(1)).findProfilesByUsernameAndDomainName(USERNAME, DOMAIN);
		assertEquals("Should return true", true, exists);
	}
	
	@Test
	public void exists_it_not_exists() {
		boolean exists = userProfileService.exists("foo", "foo");
		
		verify(profileRepository, times(1)).findProfilesByUsernameAndDomainName("foo", "foo");
		assertEquals("Should return false", false, exists);
	}
	
	//searchProfilesBeginningWith
	@Test
	public void searchProfilesBeginningWith_should_return() {
		Mockito.when(profileRepository.findByUsernameBeginningWith("tes", DOMAIN)).thenReturn(ProfileMocks.getDefaultProfileInList(USERNAME, DOMAIN));
		
		Mockito.when(userIdentityService.getUserWithInfos(Mockito.any(User.class))).thenReturn(UserMocks.getUser(USERNAME, DOMAIN));
		
		List<User> users = userProfileService.searchProfilesBeginningWith("tes", DOMAIN);
		
		verify(profileRepository, times(1)).findByUsernameBeginningWith("tes", DOMAIN);
		verify(userIdentityService, times(1)).getUserWithInfos(Mockito.any(User.class));
		assertNotNull("Should return a list", users);
		assertEquals("Only one result expected", 1, users.size());
	}
	
	@Test
	public void searchProfilesBeginningWith_should_return_nothing() {
		Mockito.when(profileRepository.findByUsernameBeginningWith("tes", DOMAIN)).thenReturn(ProfileMocks.getDefaultProfileInList(USERNAME, DOMAIN));
		
		List<User> users = userProfileService.searchProfilesBeginningWith("foo", DOMAIN);
		
		verify(profileRepository, times(1)).findByUsernameBeginningWith("foo", DOMAIN);
		verify(userIdentityService, never()).getUserWithInfos(Mockito.any(User.class));
		assertNotNull("Should return a list", users);
		assertEquals("Not result expected", 0, users.size());
	}
	
	//updateLanguage
	@Test
	public void updateLanguage_it_saves() throws Exception {
		userProfileService.updateLanguage(USERNAME, DOMAIN, "en");
		
		verify(profileRepository, times(1)).findProfilesByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(profileRepository, times(1)).save(Mockito.any(Profile.class));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void updateLanguage_IllegalArgumentException() throws Exception {
		userProfileService.updateLanguage(USERNAME, DOMAIN, "es");
	}
	
	@Test(expected = NotFoundException.class)
	public void updateLanguage_NotFoundException() throws Exception {
		userProfileService.updateLanguage("foo", "foo", "en");
	}
	
	//saveMetadata
	@Test
	public void saveMetadata_it_saves() throws Exception {ProfileMetadata profileMetadata = new ProfileMetadata();
		profileMetadata.setEmail("test@email.ch");
		profileMetadata.setGender("M");
		
		userProfileService.saveMetadata(USERNAME, DOMAIN, profileMetadata);
		
		verify(profileRepository, times(1)).findProfilesByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(profileMetadataRepository, times(1)).save(Mockito.any(net.fluance.security.core.model.jpa.ProfileMetadata.class));
	}
	
	@Test(expected = NotFoundException.class)
	public void saveMetadata_NotFoundException() throws Exception {
		ProfileMetadata profileMetadata = new ProfileMetadata();
		profileMetadata.setEmail("test@email.ch");
		profileMetadata.setGender("M");
				
		userProfileService.saveMetadata("foo", "foo", profileMetadata);
	}
	
}