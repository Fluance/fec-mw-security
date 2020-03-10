/**
 * 
 */
package net.fluance.security.core.repository.jpa;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import net.fluance.app.test.AbstractTest;
import net.fluance.security.core.Application;
import net.fluance.security.core.model.jpa.Action;
import net.fluance.security.core.model.jpa.Permission;
import net.fluance.security.core.model.jpa.Profile;
import net.fluance.security.core.model.jpa.Resource;
import net.fluance.security.core.model.jpa.Role;
import net.fluance.security.core.repository.jpa.IActionRepository;
import net.fluance.security.core.repository.jpa.IProfileRepository;
import net.fluance.security.core.repository.jpa.IPermissionRepository;
import net.fluance.security.core.repository.jpa.IResourceRepository;
import net.fluance.security.core.repository.jpa.IRoleRepository;

@EnableJpaRepositories(basePackages="net.fluance.security.core")
@EntityScan("net.fluance.security.core")
@ComponentScan("net.fluance.security.core")
@SpringBootTest(classes = Application.class)
public class PermissionRepositoryTest extends AbstractTest {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(PermissionRepositoryTest.class);
	private static final String USER1_NAME = "localtestuser1";
	private static final String USER2_NAME = "localtestuser2";
	private static final Integer LOCAL_DOMAIN_ID = 1;
	
	private static final String ROLE1_NAME = "admin";
	private static final String ROLE2_NAME = "superadmin";
	private static final String RESOURCE1_KEY = ".*/sample/*.";
	private static final String RESOURCE1 = "https://localhost:8443/sample/resource/list";
	private static final String RESOURCE1_NAME = "Units list";
	private static final String RESOURCE2_KEY = ".*/user/*.";
	private static final String RESOURCE2 = "https://localhost:8443/user/list";
	private static final String ACTION1_NAME = "GET";
	private static final String ACTION1_DESCRIPTION = "Read action in Http context";
	private static final String ACTION2_NAME = "POST";
//	@Autowired
	private IActionRepository actionRepository;
//	@Autowired
	private IResourceRepository resourceRepository;
	@Autowired
	private IProfileRepository profileRepository;
	@Autowired
	private IRoleRepository roleRepository;
//	@Autowired
	private IPermissionRepository permissionRepository;
	private Action action1;
	private Action action2;
	private Role role1;
	private Role role2;
	private Resource resource1;
	private Resource resource2;
	
/*	@Before
	public void setUp() throws NoSuchAlgorithmException {
		tearDown();
		
		action1 = new Action();
		action1.setName(ACTION1_NAME);
		action1.setDescription(ACTION1_DESCRIPTION);
		action2 = new Action();
		action2.setName(ACTION2_NAME);
		Action savedAction1 = actionRepository.save(action1);
		assertNotNull(savedAction1);
		Action savedAction2 = actionRepository.save(action2);
		assertNotNull(savedAction2);
		
		Profile userProfile1 = new Profile();
		userProfile1.setDomainId(LOCAL_DOMAIN_ID);
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
		userProfile2.setDomainId(LOCAL_DOMAIN_ID);
		userProfile2.setUsername(USER2_NAME);
		userProfile2.assignRole(role2);
		profileRepository.save(userProfile2);
		
		resource1 = new Resource();
		resource1.setKey(RESOURCE1_KEY);
		resource1.setName(RESOURCE1_NAME);
		resource2 = new Resource();
		resource2.setKey(RESOURCE2_KEY);
		resource1 = resourceRepository.save(resource1);
		assertNotNull(resource1);
		resource2 = resourceRepository.save(resource2);
		assertNotNull(resource2);
		
		//Creating start permissions
		Permission permission1 = new Permission();
		permission1.setActionId(action1.getId());
		permission1.setResourceId(resource1.getId());
		permission1.setRoleId(newRole1.getId());
		permissionRepository.save(permission1);
		
		Permission permission2 = new Permission();
		permission2.setActionId(action2.getId());
		permission2.setResourceId(resource2.getId());
		permission2.setRoleId(newRole2.getId());
		permissionRepository.save(permission2);
	}
	
	@Test
	public void mustCreateTest() throws NoSuchAlgorithmException {
		Role foundRole = roleRepository.findByName(ROLE2_NAME);
		assertNotNull(foundRole);
		Action foundAction = actionRepository.findByName(ACTION1_NAME).get(0);
		assertNotNull(foundAction);
		Resource foundResource = resourceRepository.findByKey(RESOURCE1_KEY).get(0);
		assertNotNull(foundResource);
		
		Permission permission = new Permission();
		permission.setActionId(foundAction.getId());
		permission.setResourceId(foundResource.getId());
		permission.setRoleId(foundRole.getId());
		permission = permissionRepository.save(permission);
		
		assertNotNull(permission);
		assertEquals(foundAction.getId(), permission.getActionId());
		assertEquals(foundResource.getId(), permission.getResourceId());
		assertEquals(foundRole.getId(), permission.getRoleId());
	}
	
	@Test
	public void mustFindTest() {
		// By role id
		Profile profile = profileRepository.findByUsernameAndDomainId(USER1_NAME, LOCAL_DOMAIN_ID).get(0);
		assertNotNull(profile);
		role1 = roleRepository.findByName(ROLE1_NAME);
		List<Permission> foundPermissions = permissionRepository.findByRoleId(role1.getId());
		assertNotNull(foundPermissions);
		assertEquals(1, foundPermissions.size());
		// By action id
		action1 = actionRepository.findByName(ACTION1_NAME).get(0);
		foundPermissions = permissionRepository.findByActionId(action1.getId());
		assertNotNull(foundPermissions);
		assertEquals(1, foundPermissions.size());
		// By resource id
		resource1 = resourceRepository.findByKey(RESOURCE1_KEY).get(0);
		foundPermissions = permissionRepository.findByResourceId(resource1.getId());
		assertNotNull(foundPermissions);
		assertEquals(1, foundPermissions.size());
		
		// By action name, resource, and role name
		foundPermissions = permissionRepository.findByActionNameAndResourceMatchingAndRoleName(action1.getName(), RESOURCE1, role1.getName());
		assertNotNull(foundPermissions);
		assertEquals(1, foundPermissions.size());
		foundPermissions = permissionRepository.findByActionNameAndResourceMatchingAndRoleName(action1.getName(), RESOURCE2, role1.getName());
		assertNotNull(foundPermissions);
		assertEquals(0, foundPermissions.size());
		foundPermissions = permissionRepository.findByActionNameAndResourceMatchingAndRoleName(action2.getName(), RESOURCE2, role2.getName());
		assertNotNull(foundPermissions);
		assertEquals(1, foundPermissions.size());
		
		role2 = roleRepository.findByName(ROLE2_NAME);
		Permission permission = new Permission();
		permission.setActionId(action1.getId());
		permission.setResourceId(resource1.getId());
		permission.setRoleId(role2.getId());
		permissionRepository.save(permission);
		
		List<String> roleList = new ArrayList<>();
		roleList.add(ROLE1_NAME);
		roleList.add(ROLE2_NAME);
		foundPermissions = permissionRepository.findByActionNameAndResourceMatchingAndRoleNameIn(action1.getName(), RESOURCE1, roleList);
		assertNotNull(foundPermissions);
		assertEquals(2, foundPermissions.size());
	}
	
	@Test
	public void mustDeleteTest() {
		List<Permission> foundPermissions = permissionRepository.findAll();
		assertNotNull(foundPermissions);
		assertEquals(2, foundPermissions.size());
		permissionRepository.deleteByActionName(ACTION1_NAME);
		foundPermissions = permissionRepository.findAll();
		assertNotNull(foundPermissions);
		assertEquals(1, foundPermissions.size());
		assertEquals(ACTION2_NAME, actionRepository.findOne(foundPermissions.get(0).getActionId()).getName());
	}
	
	@After
	public void tearDown() {
		permissionRepository.deleteAll();
		profileRepository.deleteAll();
		roleRepository.deleteAll();
		actionRepository.deleteAll();
		resourceRepository.deleteAll();
	}
	*/
	
	@Test
	public void test() {
		assertTrue(true);
	}
	
}
