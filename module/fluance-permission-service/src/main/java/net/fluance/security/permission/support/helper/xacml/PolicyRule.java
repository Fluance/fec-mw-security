/**
 * 
 */
package net.fluance.security.permission.support.helper.xacml;

import java.util.List;

public class PolicyRule {

	private String id;
	private String effect;
	private String resourceRegex;
	private String actionRegex;
	private List<String> roles;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEffect() {
		return effect;
	}

	public void setEffect(String effect) {
		this.effect = effect;
	}

	public String getResourceRegex() {
		return resourceRegex;
	}

	public void setResourceRegex(String resourceRegex) {
		this.resourceRegex = resourceRegex;
	}

	public String getActionRegex() {
		return actionRegex;
	}

	public void setActionRegex(String actionRegex) {
		this.actionRegex = actionRegex;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	@Override
	public String toString() {
		return "PolicyRule [id=" + id + ", effect=" + effect + ", resourceRegex=" + resourceRegex + ", actionRegex="
				+ actionRegex + ", roles=" + roles + "]";
	}
	
}
