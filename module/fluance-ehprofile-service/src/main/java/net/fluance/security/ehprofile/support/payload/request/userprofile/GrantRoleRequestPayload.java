/**
 * 
 */
package net.fluance.security.ehprofile.support.payload.request.userprofile;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class GrantRoleRequestPayload {
	private String username;
	@JsonProperty("domain")
	private String domainName;
	private List<String> roles;
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
	 * @return the domainName
	 */
	public String getDomainName() {
		return domainName;
	}
	/**
	 * @param domainName the domainName to set
	 */
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	/**
	 * @return the roles
	 */
	public List<String> getRoles() {
		return roles;
	}
	/**
	 * @param roles the roles to set
	 */
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

}
