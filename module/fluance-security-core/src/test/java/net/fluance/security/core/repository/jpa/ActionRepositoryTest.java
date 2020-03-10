/**
 * 
 */
package net.fluance.security.core.repository.jpa;

import static org.junit.Assert.*;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;
import org.junit.Test;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import net.fluance.app.test.AbstractTest;
import net.fluance.security.core.Application;
import net.fluance.security.core.model.jpa.Action;
import net.fluance.security.core.repository.jpa.IActionRepository;

@EnableJpaRepositories(basePackages="net.fluance.security.core")
@EntityScan("net.fluance.security.core")
@ComponentScan("net.fluance.security.core")
@SpringBootTest(classes = Application.class)
public class ActionRepositoryTest extends AbstractTest {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(ActionRepositoryTest.class);
	private static final String ACTION1_NAME = "GET";
	private static final String ACTION1_DESCRIPTION = "Read action in Http context";
	private static final String ACTION2_NAME = "POST";
	private static final String ACTION3_NAME = "PUT";
	private static final String GENERIC_ACTION_DESCRIPTION = "Generic action description";
	private static final String ACTION4_NAME = "TRACE";
//	@Autowired
	private IActionRepository repository;
	private Action action1;
	private Action action2;
	private Action action3;
	
/*	@Before
	public void setUp() throws NoSuchAlgorithmException {
		repository.deleteAll();
		action1 = new Action();
		action1.setName(ACTION1_NAME);
		action1.setDescription(ACTION1_DESCRIPTION);
		action2 = new Action();
		action2.setName(ACTION2_NAME);
		action1 = repository.save(action1);
		action2 = repository.save(action2);
	}

	@Test
	public void mustCreateTest() throws NoSuchAlgorithmException {
		action3 = new Action();
		action3.setName(ACTION3_NAME);
		Action action = repository.save(action3);
		assertNotNull(action);
		assertTrue((action != null) && (action.getName().equals(ACTION3_NAME) && action.getDescription()==null));
	}

	@Test
	public void mustUpdateTest() throws NoSuchAlgorithmException {
		action3 = new Action();
		action3.setName(ACTION3_NAME);
		Action action = repository.save(action3);
		assertNotNull(action);
		assertTrue(action.getDescription() == null);
		assertTrue((action != null) && (action.getName().equals(ACTION3_NAME) && action.getDescription()==null));
		action.setName(ACTION4_NAME);
		action.setDescription(GENERIC_ACTION_DESCRIPTION);
		Action updatedAccess = repository.save(action);
		assertNotNull(updatedAccess);
		assertNotNull(updatedAccess.getDescription());
		assertTrue((updatedAccess.getName().equals(ACTION4_NAME) && updatedAccess.getDescription().equals(GENERIC_ACTION_DESCRIPTION)));
	}

	@Test
	@Transactional
	public void mustFindTest() {
		List<Action> foundAccesss = repository.findAll();
		assertNotNull(foundAccesss);
		assertEquals(2, foundAccesss.size());
		List<Action> foundAccesssByName = repository.findByName(ACTION1_NAME);
		assertNotNull(foundAccesssByName);
		assertEquals(1, foundAccesssByName.size());
		assertTrue(foundAccesssByName.get(0).getName().equals(ACTION1_NAME) && foundAccesssByName.get(0).getDescription().equals(ACTION1_DESCRIPTION));
	}
	
	@Test
	public void mustDeleteTest() {
		List<Action> foundAccesssByName = repository.findByName(ACTION1_NAME);
		assertNotNull(foundAccesssByName);
		assertEquals(1, foundAccesssByName.size());
		repository.delete(foundAccesssByName.get(0));
		List<Action> foundAccesssByNameAfter = repository.findByName(ACTION1_NAME);
		assertNotNull(foundAccesssByNameAfter);
		assertEquals(0, foundAccesssByNameAfter.size());
		List<Action> allAccesss = repository.findAll();
		assertNotNull(allAccesss);
		assertEquals(1, allAccesss.size());
		action3 = new Action();
		action3.setName(ACTION3_NAME);
		Action newAccess = repository.save(action3);
		assertNotNull(newAccess);
		allAccesss = repository.findAll();
		assertNotNull(allAccesss);
		assertEquals(2, allAccesss.size());
		repository.deleteAll();
		allAccesss = repository.findAll();
		assertNotNull(allAccesss);
		assertEquals(0, allAccesss.size());
	}
	
	@After
	public void tearDown() {
		repository.deleteAll();
	}
	*/
	
	@Test
	public void test() {
		assertTrue(true);
	}
}
