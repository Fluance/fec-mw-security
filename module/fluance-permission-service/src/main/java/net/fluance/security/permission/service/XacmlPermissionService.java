package net.fluance.security.permission.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.fluance.app.security.service.support.entitlement.EntitlementDecision;
import net.fluance.app.security.service.support.entitlement.PermissionEvaluateRequestBody;
import net.fluance.app.security.service.support.entitlement.PreparedPermissionEvaluateRequestBody;
import net.fluance.app.security.service.xacml.XacmlPDP;
import net.fluance.security.core.model.jpa.Profile;
import net.fluance.security.core.model.jpa.Role;
import net.fluance.security.core.repository.jpa.IProfileRepository;

@Service
public class XacmlPermissionService {
	private static Logger LOGGER = LogManager.getLogger(XacmlPermissionService.class);
	
	@Autowired
	private XacmlPDP xacmlPDP;
	
	@Autowired
	private IProfileRepository profileRepository;
	
	/**
	 * Returns the final {@link EntitlementDecision}<br> 
	 * Only if <b>payload</b> is not null and the other parameters are null <b>payload</b> will be use to generate the {@link EntitlementDecision}
	 * 
	 * @param payload is a JSON representation of {@link PermissionEvaluateRequestBody} or {@link PreparedPermissionEvaluateRequestBody} if it has roles
	 * @param resource, resource to evaluate the permissions for the user
	 * @param username
	 * @param domain
	 * @param action Usually request type, POST, GET, PUT ...
	 * @param roles String list of roles, can be null
	 * @return
	 * @throws Exception
	 * @throws IOException
	 * @throws JsonProcessingException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 */
	public EntitlementDecision evaluate(String payload, String resource, String userName, String domain, String action, List<String> roles) throws Exception, IOException, JsonProcessingException, JsonParseException, JsonMappingException {
		EntitlementDecision entitlementDecision = null;
		
		PermissionEvaluateRequestBody permissionEvaluateRequestBody = null;

		if(!isBodyContentRequest(payload, resource, userName, domain, action, roles)) {
			permissionEvaluateRequestBody = createPermissionEvaluateRequestBody(resource, userName, domain, action, roles);
		} else {
			permissionEvaluateRequestBody = createPermissionEvaluateRequestBody(payload);
		}
		
		if(permissionEvaluateRequestBody != null) {			
			entitlementDecision = evaluatePermissionEvaluateRequestBody(permissionEvaluateRequestBody);
		}
		
		return entitlementDecision;
	}

	/**
	 * Generates a instance of {@link PermissionEvaluateRequestBody} or if <b>roles</b> is not empty an instance of {@link PreparedPermissionEvaluateRequestBody} that is a extension.
	 * 
	 * @param resource, resource to evaluate the permissions for the user
	 * @param username
	 * @param domain
	 * @param action Usually request type, POST, GET, PUT ...
	 * @param roles String list of roles, can be null
	 * @return
	 * @throws IOException
	 * @throws JsonProcessingException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 */
	private PermissionEvaluateRequestBody createPermissionEvaluateRequestBody(String resource, String username, String domain, String action, List<String> roles)
			throws IOException, JsonProcessingException, JsonParseException, JsonMappingException {
		PermissionEvaluateRequestBody permissionEvaluateRequestBody = null;
		if(roles == null) {
			permissionEvaluateRequestBody = new PermissionEvaluateRequestBody(resource, username, domain, action);
		} else {
			permissionEvaluateRequestBody = new PreparedPermissionEvaluateRequestBody(resource, username, domain, action, roles);
		}						
		
		return permissionEvaluateRequestBody;
	}
	
	/**
	 * Creates an instance of {@link PermissionEvaluateRequestBody} base on the <b>payload<b/>
	 * 
	 * @param payload is a JSON representation of {@link PermissionEvaluateRequestBody} or {@link PreparedPermissionEvaluateRequestBody} if it has roles
	 * @return
	 * @throws IOException
	 * @throws JsonProcessingException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 */
	private PermissionEvaluateRequestBody createPermissionEvaluateRequestBody(String payload)
			throws IOException, JsonProcessingException, JsonParseException, JsonMappingException {
		PermissionEvaluateRequestBody permissionEvaluateRequestBody = null;
	
		ObjectMapper mapper = new ObjectMapper();
		
		JsonNode payloadNode = mapper.readTree(payload);
		
		if(payloadNode == null || !payloadNode.has("resource") || !payloadNode.has("username") || !payloadNode.has("domain") || !payloadNode.has("action")) {
			throw new IllegalArgumentException("resource, username, domain and action are mandatory");
		} else {
			permissionEvaluateRequestBody = null;
			if(payloadNode.has("user_roles")){
				permissionEvaluateRequestBody = mapper.readValue(payload, PreparedPermissionEvaluateRequestBody.class);
			} else {
				permissionEvaluateRequestBody = mapper.readValue(payload, PermissionEvaluateRequestBody.class);
			}	
		}
		
		return permissionEvaluateRequestBody;
	}


	private boolean isBodyContentRequest(String payload, String resource, String username, String domain, String action, List<String> roles) {
		return (payload != null) && (resource == null) && (username == null) && (domain == null) && (action == null) && (roles == null);
	}
	
	/**
	 * Evaluates the request body using the current implementation of {@link XacmlPDP} 
	 * 
	 * @param permissionEvaluateRequestBody is an instance of {@link PermissionEvaluateRequestBody} but if roles are included will be {@link PreparedPermissionEvaluateRequestBody}
	 * @return
	 * @throws Exception 
	 */
	public EntitlementDecision evaluatePermissionEvaluateRequestBody(PermissionEvaluateRequestBody permissionEvaluateRequestBody) throws Exception {
		
		if(permissionEvaluateRequestBody == null || permissionEvaluateRequestBody.getUsername() == null || permissionEvaluateRequestBody.getDomain() == null) {
			throw new IllegalArgumentException("username and domain are mandatory");
		}
		
		LOGGER.info("Permission evaluation requested: " + permissionEvaluateRequestBody.toString());
		EntitlementDecision decision;
		
		if(permissionEvaluateRequestBody instanceof PreparedPermissionEvaluateRequestBody) {
			PreparedPermissionEvaluateRequestBody preparedPermissionEvaluateRequestBody = (PreparedPermissionEvaluateRequestBody) permissionEvaluateRequestBody;
		
			decision = xacmlPDP.evaluate(permissionEvaluateRequestBody.getResource(),
					permissionEvaluateRequestBody.getAction(),
					permissionEvaluateRequestBody.getUsername(),						
					preparedPermissionEvaluateRequestBody.getUserRoles());	
		} else {
			decision = evaluateWithIntegratedRoles(permissionEvaluateRequestBody);
		}
				
		LOGGER.info("Permission evaluation decision for request " + permissionEvaluateRequestBody.toString() + ": " + decision.getDecision());
		
		return decision;
	}
	
	/**
	 * Evaluate a the request request by finding roles in system.<br>
	 * To use by client which is not able to get the user roles.
	 * @param permissionEvaluateRequestBody
	 * @return
	 * @throws Exception
	 */
	public EntitlementDecision evaluateWithIntegratedRoles(PermissionEvaluateRequestBody permissionEvaluateRequestBody) throws Exception {
		List<Profile> userProfiles = profileRepository.findProfilesByUsernameAndDomainName(permissionEvaluateRequestBody.getUsername(), permissionEvaluateRequestBody.getDomain());
		
		Profile userProfile = null;
		
		if(userProfiles.size() > 0) {
			userProfile =userProfiles.get(0);
			if(userProfiles.size() > 1) {
				LOGGER.warn("More than one user for this user name: {}", permissionEvaluateRequestBody.getUsername());
			}
		}
		
		if(userProfile == null) {
			return EntitlementDecision.DENY;
		}
		
		List<String> userRoles = new ArrayList<>();
		for (Role role : userProfile.getRoles()) {
			userRoles.add(role.getName());
			LOGGER.debug("User role found for ' " + permissionEvaluateRequestBody.getDomain() + "/" + permissionEvaluateRequestBody.getUsername() + " : " + role.getName());
		}
		EntitlementDecision decision = xacmlPDP.evaluate(permissionEvaluateRequestBody.getResource(), permissionEvaluateRequestBody.getAction(), permissionEvaluateRequestBody.getUsername(), userRoles);
		return decision;
	}

}
