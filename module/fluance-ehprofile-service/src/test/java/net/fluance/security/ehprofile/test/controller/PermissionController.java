/**
 * 
 */
package net.fluance.security.ehprofile.test.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.fluance.app.security.service.support.entitlement.EntitlementDecision;
import net.fluance.app.security.service.support.entitlement.PermissionEvaluateRequestBody;
import net.fluance.app.security.service.support.entitlement.PreparedPermissionEvaluateRequestBody;
import net.fluance.app.test.mock.AuthorizationServerMock;
import net.fluance.app.web.support.payload.response.GenericResponsePayload;

/**
 *
 */
@RestController
@PropertySource({"classpath:test.properties"})
public class PermissionController {

	@Autowired
	protected AuthorizationServerMock authorizationServerMock;
	protected static final Map<String, String> DOMAIN_MAPPINGS = new HashMap<String, String>();
	private static final Logger LOGGER = LogManager.getLogger(PermissionController.class);
	@Value("${user.domain}")
	private String domain;
	@Value("${admin.username}")
	private String adminUsername;
	@Value("${admin.password}")
	private String adminPassword;
	@Value("${ownprofile.username}")
	private String ownProfileUsername;
	@Value("${ownprofile.password}")
	private String ownProfilePassword;
	@Value("${nonadmin.username}")
	private String nonAdminUsername;
	@Value("${nonadmin.password}")
	private String nonAdminPassword;

	static {
		DOMAIN_MAPPINGS.put("local", "PRIMARY");
		DOMAIN_MAPPINGS.put("fluance", "PRIMARY");
	}

	@RequestMapping(value = "/xacml/evaluate", method = RequestMethod.GET)
	public ResponseEntity<?> evaluate(@RequestBody(required = false) String payload, @RequestParam(required = false) String resource, @RequestParam(required = false) String username, @RequestParam(required = false) String domain, @RequestParam(required = false) String action, @RequestParam(name = "user_roles", required = false) List<String> roles, HttpServletRequest request,
			HttpServletResponse response) throws JsonParseException, JsonMappingException, IOException {
		boolean isBodyContentRequest = (payload != null) && (resource == null) && (username == null) && (domain == null) && (action == null) && (roles == null);
		if(!isBodyContentRequest) {
			return evaluate(resource, username, domain, action, null);
		} else {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode payloadNode = mapper.readTree(payload);
			if(payloadNode == null || !payloadNode.has("resource") || !payloadNode.has("username") || !payloadNode.has("domain") || !payloadNode.has("action")) {
				return new ResponseEntity<GenericResponsePayload>(new GenericResponsePayload("resource, username, domain and action are mandatory"), HttpStatus.BAD_REQUEST);
			}
			PermissionEvaluateRequestBody body = null;
			if(payloadNode.has("user_roles")){
				body = mapper.readValue(payload, PreparedPermissionEvaluateRequestBody.class);
			} else {
				body = mapper.readValue(payload, PermissionEvaluateRequestBody.class);
			}
			return evaluate(body);
		}
	}

	/**
	 * 
	 * @param payload
	 * @return
	 */
	public ResponseEntity<?> evaluate(PermissionEvaluateRequestBody payload) {
		return evaluate(payload.getResource(), payload.getUsername(), payload.getDomain(), payload.getAction(), ((payload instanceof PreparedPermissionEvaluateRequestBody) ? ((PreparedPermissionEvaluateRequestBody)payload).getUserRoles() : null));
	}

	/**
	 * 
	 * @param resource
	 * @param username
	 * @param domain
	 * @param action
	 * @param roles
	 * @return
	 */
	private ResponseEntity<?> evaluate(String resource, String username, String domain, String action, List<String> roles) {
		boolean isOwnProfile = ("GET".equalsIgnoreCase(action) && resource.startsWith("http://localhost:8080/ehprofile/profile/" + ownProfileUsername + "/" + domain)) && ownProfileUsername.equals(username);
		boolean allowed = isOwnProfile || adminUsername.equals(username);
		if(allowed) {
			return new ResponseEntity<EntitlementDecision>(EntitlementDecision.PERMIT, HttpStatus.OK);
		} else {
			return new ResponseEntity<EntitlementDecision>(EntitlementDecision.DENY, HttpStatus.FORBIDDEN);
		}
	}
	
}
