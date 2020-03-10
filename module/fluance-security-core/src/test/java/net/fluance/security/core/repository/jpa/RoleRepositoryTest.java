/**
 * 
 */
package net.fluance.security.core.repository.jpa;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;
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
import org.springframework.transaction.annotation.Transactional;

import net.fluance.app.test.AbstractTest;
import net.fluance.security.core.Application;
import net.fluance.security.core.model.jpa.Role;
import net.fluance.security.core.repository.jpa.IRoleRepository;

@EnableJpaRepositories(basePackages="net.fluance.security.core")
@EntityScan("net.fluance.security.core")
@ComponentScan("net.fluance.security.core")
@SpringBootTest(classes = Application.class)
public class RoleRepositoryTest extends AbstractTest {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(RoleRepositoryTest.class);
	private static final String ROLE1_NAME = "admin";
	private static final String ROLE1_DESCRIPTION = "admin role description";
	private static final String ROLE2_NAME = "superadmin";
	private static final String ROLE3_NAME = "hyperadmin";
	private static final String GENERIC_ROLE_DESCRIPTION = "admin role description";
	private static final String ROLE4_NAME = "megaadmin";
	@Autowired
	private IRoleRepository repository;
	private Role role1;
	private Role role2;
	private Role role3;
	
	@Before
	public void setUp() throws NoSuchAlgorithmException {
		repository.deleteAll();
		role1 = new Role();
		role1.setName(ROLE1_NAME);
		role1.setDescription(ROLE1_DESCRIPTION);
		role2 = new Role();
		role2.setName(ROLE2_NAME);
		role1 = repository.save(role1);
		role2 = repository.save(role2);
	}
	
	@Test
	public void mustCreateTest() throws NoSuchAlgorithmException {
		role3 = new Role();
		role3.setName(ROLE3_NAME);
		Role role = repository.save(role3);
		assertNotNull(role);
		assertTrue((role != null) && (role.getName().equals(ROLE3_NAME) && role3.getDescription()==null));
	}
	
	@Test
	public void mustUpdateTest() throws NoSuchAlgorithmException {
		role3 = new Role();
		role3.setName(ROLE3_NAME);
		Role role = repository.save(role3);
		assertNotNull(role);
		assertTrue(role.getDescription() == null);
		assertTrue((role != null) && (role.getName().equals(ROLE3_NAME) && role.getDescription()==null));
		role.setName(ROLE4_NAME);
		role.setDescription(GENERIC_ROLE_DESCRIPTION);
		Role updatedRole = repository.save(role);
		assertNotNull(updatedRole);
		assertNotNull(updatedRole.getDescription());
		assertTrue((updatedRole.getName().equals(ROLE4_NAME) && updatedRole.getDescription().equals(GENERIC_ROLE_DESCRIPTION)));
	}
	
	@Test
	@Transactional
	public void mustFindTest() {
		List<Role> foundRoles = repository.findAll();
		assertNotNull(foundRoles);
		assertEquals(2, foundRoles.size());
		Role rolesByName = repository.findByName(ROLE1_NAME);
		assertNotNull(rolesByName);
		assertTrue(rolesByName.getName().equals(ROLE1_NAME) && rolesByName.getDescription().equals(ROLE1_DESCRIPTION));
	}
	
	@Test
	public void mustDeleteTest() {
		Role rolesByName = repository.findByName(ROLE1_NAME);
		assertNotNull(rolesByName);
		repository.delete(rolesByName);
		List<Role> allRoles = repository.findAll();
		assertNotNull(allRoles);
		assertEquals(1, allRoles.size());
		role3 = new Role();
		role3.setName(ROLE3_NAME);
		Role newRole = repository.save(role3);
		assertNotNull(newRole);
		allRoles = repository.findAll();
		assertNotNull(allRoles);
		assertEquals(2, allRoles.size());
		repository.deleteAll();
		allRoles = repository.findAll();
		assertNotNull(allRoles);
		assertEquals(0, allRoles.size());
	}
	
	@After
	public void tearDown() {
		repository.deleteAll();
	}
	
}
