package net.fluance.security.permission.util.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import net.fluance.app.test.AbstractWebIntegrationTest;
import net.fluance.security.permission.app.Application;
import net.fluance.security.permission.support.helper.xacml.Policy;
import net.fluance.security.permission.support.helper.xacml.PolicyRule;
import net.fluance.security.permission.support.helper.xacml.XacmlHelper;

@ComponentScan("net.fluance.security.permission")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class XacmlHelperTest extends AbstractWebIntegrationTest {

	private static Logger LOGGER = LogManager.getLogger(XacmlHelperTest.class);
	private static final String ALLOW_RULE_ID = "Allow-Rule";
	private static final String DENY_RULE_ID = "Deny-Rule";
	private static final String ALLOW_RULE_EFFECT = "Permit";
	private static final String DENY_RULE_EFFECT = "Deny";

	Map<String, Header> headersMap;
	@Autowired
	private XacmlHelper xacmlHelper;
	@Autowired
    private ResourceLoader resourceLoader;
	private String testPolicyFilePath;
	
	@Before
	public void setUp() throws Exception {
		testPolicyFilePath = "xacml-policies/FakePolicy.xml";
	}
	
	@Test
	public void checkLoadFromFile() throws TransformerFactoryConfigurationError, Exception {
		assertNotNull(xacmlHelper);
		File policyFile = loadFromClassPath(getClass(), testPolicyFilePath);
		assertNotNull(policyFile);
		assertTrue(policyFile.isFile());
		Policy policy = xacmlHelper.loadFromFile(policyFile);
		LOGGER.info("Testing policy " + policy.getId() + " ( applying to " + policy.getApiTargetRegex());
		assertNotNull(policy);
		assertEquals("FakePolicy", policy.getId());
		assertEquals(".*/fake-api/.*", policy.getApiTargetRegex());
		
		List<PolicyRule> policyRules = policy.getRules();
		for(PolicyRule rule : policyRules) {
			LOGGER.info("Testing rule " + rule + " in policy " + policy.getId());
			assertNotNull(rule.getId());
			assertFalse(rule.getId().isEmpty());
			assertTrue((ALLOW_RULE_ID.equals(rule.getId())) || (DENY_RULE_ID.equals(rule.getId())));
			assertNotNull(rule.getEffect());
			assertFalse(rule.getEffect().isEmpty());
			assertTrue((ALLOW_RULE_EFFECT.equals(rule.getEffect()) && ALLOW_RULE_ID.equals(rule.getId())) || (DENY_RULE_EFFECT.equals(rule.getEffect()) && DENY_RULE_ID.equals(rule.getId())));
			if(ALLOW_RULE_ID.equals(rule.getId())) {
				assertTrue(((rule.getResourceRegex() != null) && !(rule.getResourceRegex().isEmpty())) || (rule.getResourceRegex() == null));
				assertTrue((rule.getActionRegex() != null && !rule.getActionRegex().isEmpty()) || rule.getActionRegex() == null);
				//Resource regex must be (not and not empty) or (null)
				List<String> applicableRoles = rule.getRoles();
				assertNotNull(applicableRoles);
				assertTrue(applicableRoles.contains("role1"));
				assertTrue(applicableRoles.contains("role2"));
				assertTrue(applicableRoles.contains("role3"));
				assertTrue(applicableRoles.contains("role4"));
			}
		}
	}
	
	private File loadFromClassPath(Class<?> requesterClass, String classPathRelativeLocation) throws IOException {
		Resource resource = resourceLoader.getResource("classpath:"+classPathRelativeLocation);
        File file = resource.getFile();
        return file;
	}

	@Override
	public void tearDown() {}

	@Override
	protected boolean checkOAuth2Authorization(Object... params) {
		return false;
	}

	@Override
	public void checkOk(Object... params) throws KeyManagementException, UnsupportedOperationException,
			NoSuchAlgorithmException, KeyStoreException, IOException, HttpException, URISyntaxException,
			ProcessingException, NumberFormatException, ParseException, XPathExpressionException,
			ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {}

	@Override
	public Logger getLogger() {
		return null;
	}
	
}
