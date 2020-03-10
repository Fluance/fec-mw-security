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

import net.fluance.security.core.model.jpa.Profile;
import net.fluance.security.core.model.jpa.Role;
import net.fluance.security.core.model.jpa.RoleMock;
import net.fluance.security.core.repository.jpa.IProfileRepository;
import net.fluance.security.core.repository.jpa.IRoleRepository;
import net.fluance.security.core.support.exception.NotFoundException;
import net.fluance.security.ehprofile.service.UserProfileValidatorService;
import net.fluance.security.ehprofile.test.utils.ProfileMocks;
import net.fluance.security.ehprofile.test.utils.RolesMocks;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserProfileRolesServiceImplTest {
	
	private static final String USERNAME = "test";
	private static final String DOMAIN = "PRIMARY";
	private static final String DEFAULT_ROLE = "everyone";

	@TestConfiguration
	static class Configuration {
		@Bean
		public UserProfileRolesServiceImpl userProfileRolesService() {
			return new UserProfileRolesServiceImpl();
		}
		@Bean
		public UserProfileValidatorService userProfileValidatorService() {
			return new UserProfileValidatorServiceImpl();
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
	public void setUp() {
		Mockito.reset(profileRepository);
		Mockito.reset(roleRepository);
		
		//Set default answer for userProfileValidatorService. The profile always exsits		
		Mockito.when(profileRepository.findProfilesByUsernameAndDomainName(USERNAME, DOMAIN)).thenReturn(ProfileMocks.getDefaultProfileInList(USERNAME, DOMAIN));
		
		//Set default answer for findByUsernameAndDomainName, always will return a value
		doAnswer(new Answer<Profile>() {
			@Override
			public Profile answer(InvocationOnMock invocation) throws Throwable {
				return ProfileMocks.getDefaultProfile(USERNAME, DOMAIN);
			}
		}).when(profileRepository).findByUsernameAndDomainName(Mockito.anyString(), Mockito.anyString());
		
		//Set default answer for profileRepository.save(Profile.class)), return same instance as it hab been saved
		doAnswer(new Answer<Profile>() {
			@Override
			public Profile answer(InvocationOnMock invocation) throws Throwable {
				return invocation.getArgumentAt(0, Profile.class);
			}
		}).when(profileRepository).save(Mockito.any(Profile.class));
		
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
	}
	
	@MockBean
	private IProfileRepository profileRepository;
	@MockBean
	private IRoleRepository roleRepository;
	
	@Autowired
	UserProfileRolesServiceImpl userProfileRolesService;
	
	//grantRoles
	@Test
	public void grantRoles_should_grant() throws Exception {
		boolean granted = userProfileRolesService.grantRoles(USERNAME, DOMAIN, RolesMocks.getListOfRolesAsListOfStirngs());
		
		verify(profileRepository, times(1)).findProfilesByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(profileRepository, times(1)).save(Mockito.any(Profile.class));
		assertEquals("Should return true", true, granted);
	}
	
	@Test
	public void grantRoles_should_not_grant() throws Exception {
		Mockito.when(roleRepository.findByName(Mockito.anyString())).thenReturn(null);
		
		boolean granted = userProfileRolesService.grantRoles(USERNAME, DOMAIN, RolesMocks.getListOfRolesAsListOfStirngs());
		
		verify(profileRepository, times(1)).findProfilesByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(profileRepository, never()).save(Mockito.any(Profile.class));
		assertEquals("Should return false", false, granted);
	}
	
	@Test(expected = NotFoundException.class)
	public void grantRoles_profile_not_exists_NotFoundException() throws Exception {
		Mockito.when(profileRepository.findProfilesByUsernameAndDomainName(USERNAME, DOMAIN)).thenReturn(new ArrayList<Profile>());
		
		boolean granted = userProfileRolesService.grantRoles(USERNAME, DOMAIN, RolesMocks.getListOfRolesAsListOfStirngs());
		
		assertEquals("Should return false", false, granted);
	}
	
	//setRoles
	@Test
	public void setRoles_should_set() throws Exception {
		boolean setted = userProfileRolesService.setRoles(USERNAME, DOMAIN, RolesMocks.getListOfRolesAsListOfStirngs());
		
		verify(profileRepository, times(1)).findProfilesByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(profileRepository, times(2)).save(Mockito.any(Profile.class));
		assertEquals("Should return true", true, setted);
	}
	
	@Test
	public void setRoles_should_not_set() throws Exception {
		doAnswer(new Answer<Profile>() {
			@Override
			public Profile answer(InvocationOnMock invocation) throws Throwable {
				Profile profile =invocation.getArgumentAt(0, Profile.class);
				profile.setRoles(new ArrayList<Role>());
				
				return profile;
			}
		}).when(profileRepository).save(Mockito.any(Profile.class));
		
		boolean setted = userProfileRolesService.setRoles(USERNAME, DOMAIN, RolesMocks.getListOfRolesAsListOfStirngs());
		
		verify(profileRepository, times(1)).findProfilesByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(profileRepository, times(2)).save(Mockito.any(Profile.class));
		assertEquals("Should return false", false, setted);
	}
	
	@Test(expected = NotFoundException.class)
	public void setRoles_profile_not_exists_NotFoundException() throws Exception {
		Mockito.when(profileRepository.findProfilesByUsernameAndDomainName(USERNAME, DOMAIN)).thenReturn(new ArrayList<Profile>());
		
		userProfileRolesService.setRoles(USERNAME, DOMAIN, RolesMocks.getListOfRolesAsListOfStirngs());
	}
	
	//revokeRoles
	@Test
	public void revokeRoles_should_remove() throws Exception {
		boolean removed = userProfileRolesService.revokeRoles(USERNAME, DOMAIN, RolesMocks.getListOfRolesAsListOfStirngs());
		
		verify(profileRepository, times(1)).findProfilesByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(roleRepository, times(5)).findByName(Mockito.anyString());
		verify(profileRepository, times(1)).save(Mockito.any(Profile.class));
		assertEquals("Should return true", true, removed);
	}
	
	@Test(expected=NotFoundException.class)
	public void revokeRoles_foo_user_() throws Exception {
		userProfileRolesService.revokeRoles("foo", "fooDomain", RolesMocks.getListOfRolesAsListOfStirngs());
	}
	
	@Test
	public void revokeRoles_null_roles_list_should_return_true() throws Exception {
		boolean removed = userProfileRolesService.revokeRoles(USERNAME, DOMAIN, null);
		
		verify(profileRepository, never()).findProfilesByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(roleRepository, never()).findByName(Mockito.anyString());
		verify(profileRepository, never()).save(Mockito.any(Profile.class));
		assertEquals("Should return true", true, removed);
	}
	
	@Test
	public void revokeRoles_should_return_false() throws Exception {
		//Save action will return a user with roles that is supposed to be remove
		doAnswer(new Answer<Profile>() {
			@Override
			public Profile answer(InvocationOnMock invocation) throws Throwable {
				return ProfileMocks.getDefaultProfile(USERNAME, DOMAIN);
			}
		}).when(profileRepository).save(Mockito.any(Profile.class));
		
		boolean removed = userProfileRolesService.revokeRoles(USERNAME, DOMAIN, RolesMocks.getListOfRolesAsListOfStirngs());
		
		verify(profileRepository, times(1)).findProfilesByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(roleRepository, times(5)).findByName(Mockito.anyString());
		verify(profileRepository, times(1)).save(Mockito.any(Profile.class));
		assertEquals("Should return false", false, removed);
	}
	
	//findUserRoles
	@Test
	public void findUserRoles_should_return_list() throws Exception {
		doAnswer(new Answer<List<Role>>() {
			@Override
			public List<Role> answer(InvocationOnMock invocation) throws Throwable {
				return RolesMocks.getListOfRoles();
			}
		}).when(roleRepository).getByProfileId(1);
		
		userProfileRolesService.findUserRoles(USERNAME, DOMAIN);
		
		verify(profileRepository, times(1)).findProfilesByUsernameAndDomainName(USERNAME, DOMAIN);
		verify(roleRepository, times(1)).getByProfileId(1);
	}
	
	@Test(expected=NotFoundException.class)
	public void findUserRoles_NotFoundException() throws Exception {
		userProfileRolesService.findUserRoles("foo", "foodomain");
	}
}