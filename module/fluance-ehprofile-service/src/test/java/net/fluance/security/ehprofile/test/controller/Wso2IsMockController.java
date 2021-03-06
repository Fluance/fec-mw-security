/**
 * 
 */
package net.fluance.security.ehprofile.test.controller;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.fluance.app.security.service.support.entitlement.EntitlementDecision;
import net.fluance.app.security.service.xacml.XacmlPDP;
import net.fluance.app.test.mock.OAuth2ValidationResponse;
import net.fluance.app.test.mock.controller.AbstractWso2IsMockController;
import net.fluance.commons.codec.PKIUtils;
import net.fluance.commons.json.jwt.JWTUtils;
import net.fluance.commons.xml.XMLUtils;

/**
 *
 */
@RestController
public class Wso2IsMockController extends AbstractWso2IsMockController {
	
	private static final Logger LOGGER = LogManager.getLogger(Wso2IsMockController.class);
	private static final String DECISION_BYATTR_REQUEST_SUBJECT_XPATH = "/Envelope/Body/getDecisionByAttributes/subject";
	private static final String DECISION_BYATTR_REQUEST_RESOURCE_XPATH = "/Envelope/Body/getDecisionByAttributes/resource";
	private static final String DECISION_BYATTR_REQUEST_ACTION_XPATH = "/Envelope/Body/getDecisionByAttributes/action";
	
	@Value("${server.ssl.key-alias}")
	private String sslKeyAlias;
	@Value("${server.ssl.key-password}")
	private String sslKeyPassword;
	
	@Autowired
	private XacmlPDP xacmlPdp;
//	@Autowired
//	private CoreConfig coreConfig;
	
	@PostConstruct
	public void init() {
		// Adding domains
		authorizationServerMock.addDomain(1, "PRIMARY");
		// Adding users
		authorizationServerMock.addUser("fluance", "password", "PRIMARY");
		authorizationServerMock.addUser("administrative", "password", "PRIMARY");
		authorizationServerMock.addUser("financial", "password", "PRIMARY");
		authorizationServerMock.addUser("nurse", "password", "PRIMARY");
		authorizationServerMock.addUser("physician", "password", "PRIMARY");
		authorizationServerMock.addUser("sysadmin", "password", "PRIMARY");
		authorizationServerMock.addUser("notalloweduser", "password", "PRIMARY");
		// Adding roles
		authorizationServerMock.addRole("administrative");
		authorizationServerMock.addRole("financial");
		authorizationServerMock.addRole("nurse");
		authorizationServerMock.addRole("physician");
		authorizationServerMock.addRole("sysadmin");
		authorizationServerMock.addRole("profileawareapp");
		// Assigning roles
		authorizationServerMock.assignRole("fluance", "PRIMARY", "sysadmin");
		authorizationServerMock.assignRole("fluance", "PRIMARY", "profileawareapp");
		authorizationServerMock.assignRole("administrative", "PRIMARY", "administrative");
		authorizationServerMock.assignRole("financial", "PRIMARY", "financial");
		authorizationServerMock.assignRole("nurse", "PRIMARY", "nurse");
		authorizationServerMock.assignRole("physician", "PRIMARY", "physician");
		authorizationServerMock.assignRole("sysadmin", "PRIMARY", "sysadmin");
	}
	
	@Override
	public String validateAccessToken(@RequestBody String bodyContent) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, javax.security.cert.CertificateException {
		LOGGER.info("Validation request received: " + bodyContent);
		
		OAuth2ValidationResponse response = null;
		
		Node contentDoc = XMLUtils.xmlStringToDocument(bodyContent).getDocumentElement();
		String accessToken = XMLUtils.queryString(contentDoc, ACCESS_TOKEN_VALIDATION_REQUEST_XPATH);
		
		LOGGER.info("Access token: " + accessToken);
		
		if(accessToken != null) {
			response = authorizationServerMock.validateOAuth2AccessToken(accessToken);//, true, DOMAIN_MAPPINGS);
			LOGGER.info("Validation result for access token " + accessToken + ": (valid: " + response.isValid() + ", user: " + response.getAuthzUser() + ")");
		}

		// Generate JWT
		PrivateKey signingKey = PKIUtils.readPrivateKey(sslKeyAlias, sslKeyPassword);
		
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode header = (ObjectNode) objectMapper.readTree("{\"typ\":\"JWT\",\"alg\":\"RS512\",\"x5t\":\"N2U4MzVmNjdjZjgwMjNlNDQwOTRlOTlkNzM1ODY5N2QxY2YwYjAyNA\"}");
		ObjectNode payload = (ObjectNode) objectMapper.readTree("{\"iss\":\"http://wso2.org/gateway\",\"exp\":1470295814849,\"http://wso2.org/gateway/subscriber\":\"" + response.getAuthzUser() + "\",\"http://wso2.org/gateway/applicationname\":\"OAuth2 Service provider\",\"http://wso2.org/gateway/enduser\":\"admin\",\"http://wso2.org/claims/role\":\"admin\"}");
		
		String jwt = JWTUtils.buildToken(signingKey, header, payload);
		
		String validationXmlResponse = "<Envelope>";
		validationXmlResponse += "<Body>";
		validationXmlResponse += "<validateResponse><return>";
		validationXmlResponse += "<authorizationContextToken>";
		validationXmlResponse += "<authorizationContextToken>";
		validationXmlResponse += "<tokenString>" + jwt + "</tokenString>";
		validationXmlResponse += "<tokenType>JWT</tokenType>";
		validationXmlResponse += "</authorizationContextToken>";		
		validationXmlResponse += "</authorizationContextToken>";
		validationXmlResponse += "<valid>" + Boolean.toString(response.isValid()) + "</valid>";
		validationXmlResponse += "<authorizedUser>" + response.getAuthzUser() + "</authorizedUser>";
		validationXmlResponse += "</return></validateResponse>";
		validationXmlResponse += "</Body>";
		validationXmlResponse += "</Envelope>";
	
		return validationXmlResponse;
	}
	
	
	@RequestMapping(value = "/services/EntitlementService", method = {RequestMethod.POST, RequestMethod.GET}, consumes = MediaType.ALL_VALUE)
	public String decisionByAttributes(@RequestBody String bodyContent) throws Exception {
		LOGGER.info("Entitlement request received: " + bodyContent);
		
		String decision = EntitlementDecision.INDETERMINATE.getDecision();
		List<String> userRoles = new ArrayList<>();
		String resource = null;
		String action = null;
		String domain = null;
		String username = null;
		
		Node contentDoc = XMLUtils.xmlStringToDocument(bodyContent).getDocumentElement();
		String subject = XMLUtils.queryString(contentDoc, DECISION_BYATTR_REQUEST_SUBJECT_XPATH);
		// Will not be able to make a decision if domain is unknown
		if(subject.contains("/")) {
			StringTokenizer strTok = new StringTokenizer(subject, "/");
			domain = strTok.nextToken();
			username = strTok.nextToken();
			userRoles = authorizationServerMock.userRoles(username, domain);
			resource = XMLUtils.queryString(contentDoc, DECISION_BYATTR_REQUEST_RESOURCE_XPATH);
			action = XMLUtils.queryString(contentDoc, DECISION_BYATTR_REQUEST_ACTION_XPATH);
		}
		
		LOGGER.info("Username: " + username + ", domain: " + domain + ", resource: " + resource + ", roles: " + userRoles + ", action: " + action);
		
		if(userRoles == null || resource == null || action == null) {
			throw new IllegalArgumentException("Resource, action and roles are mandatory");
		}

		decision = xacmlPdp.evaluate(resource, action, (domain + "/" + username), userRoles).getDecision();

		LOGGER.info("PDP entitlement decision: " + decision);
		
		String decisionXmlResponse = "<Envelope>";
		decisionXmlResponse += "<Body>";
		decisionXmlResponse += "<getDecisionByAttributesResponse>";
		decisionXmlResponse += "<return>";
		decisionXmlResponse += "<Response><Result><Decision>" + decision + "</Decision></Result></Response>";
		decisionXmlResponse += "</return>";
		decisionXmlResponse += "</getDecisionByAttributesResponse>";
		decisionXmlResponse += "</Body>";
		decisionXmlResponse += "</Envelope>";
	
		LOGGER.info("Sending entitlement response: " + decisionXmlResponse);
		
		return decisionXmlResponse;
	}
	
}
