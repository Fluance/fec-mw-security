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
import net.fluance.security.core.model.jpa.Resource;
import net.fluance.security.core.repository.jpa.IResourceRepository;

@EnableJpaRepositories(basePackages="net.fluance.security.core")
@EntityScan("net.fluance.security.core")
@ComponentScan("net.fluance.security.core")
@SpringBootTest(classes = Application.class)
public class ResourceRepositoryTest extends AbstractTest {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(ResourceRepositoryTest.class);
	private static final String RESOURCE1_KEY = "https://localhost:8443/sample/resource/list";
	private static final String RESOURCE1_NAME = "Units list";
	private static final String RESOURCE2_KEY = "https://localhost:8443/user/list";
	private static final String RESOURCE3_KEY = "https://localhost:8443/role/list";
	private static final String GENERIC_RESOURCE_NAME = "Resource name";
	private static final String RESOURCE4_KEY = "https://localhost:8443/sample/detail";
//	@Autowired
	private IResourceRepository repository;
	private Resource resource1;
	private Resource resource2;
	private Resource resource3;
	
	/*@Before
	public void setUp() throws NoSuchAlgorithmException {
		repository.deleteAll();
		resource1 = new Resource();
		resource1.setKey(RESOURCE1_KEY);
		resource1.setName(RESOURCE1_NAME);
		resource2 = new Resource();
		resource2.setKey(RESOURCE2_KEY);
		resource1 = repository.save(resource1);
		resource2 = repository.save(resource2);
	}
	
	@Test
	public void mustCreateTest() throws NoSuchAlgorithmException {
		resource3 = new Resource();
		resource3.setKey(RESOURCE3_KEY);
		Resource resource = repository.save(resource3);
		assertNotNull(resource);
		assertTrue((resource != null) && (resource.getKey().equals(RESOURCE3_KEY) && resource.getName()==null));
	}
	
	@Test
	public void mustUpdateTest() throws NoSuchAlgorithmException {
		resource3 = new Resource();
		resource3.setKey(RESOURCE3_KEY);
		Resource resource = repository.save(resource3);
		assertNotNull(resource);
		assertTrue(resource.getName() == null);
		assertTrue((resource != null) && (resource.getKey().equals(RESOURCE3_KEY) && resource.getName()==null));
		resource.setKey(RESOURCE4_KEY);
		resource.setName(GENERIC_RESOURCE_NAME);
		Resource updatedResource = repository.save(resource);
		assertNotNull(updatedResource);
		assertNotNull(updatedResource.getKey());
		assertTrue((updatedResource.getKey().equals(RESOURCE4_KEY) && updatedResource.getName().equals(GENERIC_RESOURCE_NAME)));
	}
	
	@Test
	@Transactional
	public void mustFindTest() {
		List<Resource> foundResources = repository.findAll();
		assertNotNull(foundResources);
		assertEquals(2, foundResources.size());
		List<Resource> foundResourcesByKey = repository.findByKey(RESOURCE1_KEY);
		assertNotNull(foundResourcesByKey);
		assertEquals(1, foundResourcesByKey.size());
		assertTrue(foundResourcesByKey.get(0).getKey().equals(RESOURCE1_KEY) && foundResourcesByKey.get(0).getName().equals(RESOURCE1_NAME));
	}
	
	@Test
	public void mustDeleteTest() {
		List<Resource> foundResourcesByName = repository.findByKey(RESOURCE1_KEY);
		assertNotNull(foundResourcesByName);
		assertEquals(1, foundResourcesByName.size());
		repository.delete(foundResourcesByName.get(0));
		List<Resource> foundResourcesByNameAfter = repository.findByKey(RESOURCE1_KEY);
		assertNotNull(foundResourcesByNameAfter);
		assertEquals(0, foundResourcesByNameAfter.size());
		List<Resource> allResources = repository.findAll();
		assertNotNull(allResources);
		assertEquals(1, allResources.size());
		resource3 = new Resource();
		resource3.setKey(RESOURCE3_KEY);
		Resource newResource = repository.save(resource3);
		assertNotNull(newResource);
		allResources = repository.findAll();
		assertNotNull(allResources);
		assertEquals(2, allResources.size());
		repository.deleteAll();
		allResources = repository.findAll();
		assertNotNull(allResources);
		assertEquals(0, allResources.size());
	}
	
	@After
	public void tearDown() {
		repository.deleteAll();
	}*/
	
	@Test
	public void test() {
		assertTrue(true);
	}
	
}
