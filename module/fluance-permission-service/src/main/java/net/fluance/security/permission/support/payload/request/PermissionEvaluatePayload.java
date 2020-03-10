/**
 * 
 */
package net.fluance.security.permission.support.payload.request;

import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PermissionEvaluatePayload {

	private String resource;
	private String username;
	private String action;
	@JsonProperty("user_roles")
	private List<String> userRoles;
	
	/**
	 * @return the resource
	 */
	public String getResource() {
		return resource;
	}
	
	/**
	 * @param resource the resource to set
	 */
	public void setResource(String resource) {
		this.resource = resource;
	}
	
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}
	
	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}
	
	/**
	 * @return the userRoles
	 */
	public List<String> getUserRoles() {
		return userRoles;
	}
	
	/**
	 * @param userRoles the userRoles to set
	 */
	public void setUserRoles(List<String> userRoles) {
		this.userRoles = userRoles;
	}
	
	@Override
	public String toString() {
		String body = "{";
		body += "\"resource\":\"" + resource + "\",";
		body += "\"action\":\"" + action + "\",";
		body += "\"username\":\"" + username + "\",";
		body += "\"user_roles\":";
		if(userRoles != null) {
			body += "[";
			Iterator<String> rolesIter = userRoles.iterator();
			while(rolesIter.hasNext()) {
				body += "\"" + rolesIter.next() + "\"";
				if(rolesIter.hasNext()) {
					body += ",";
				}
			}
			body += "]";
		} else {
			body += userRoles;
		}
		
		body += "}";
		return body;
	}
}
