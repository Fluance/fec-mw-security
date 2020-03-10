/**
 * 
 */
package net.fluance.security.ehprofile.support.payload.request.userprofile;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class BasicUserProfileUpdatePayload {
	protected String username;
	@JsonProperty("domain")
	protected String domainName;
	
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
}
