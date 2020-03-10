/**
 * 
 */
package net.fluance.security.permission.support.helper.xacml;

import java.util.List;

public class Policy {
	private String id;
	private String apiTargetRegex;
	List<PolicyRule> rules;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getApiTargetRegex() {
		return apiTargetRegex;
	}

	public void setApiTargetRegex(String apiTargetRegex) {
		this.apiTargetRegex = apiTargetRegex;
	}

	public List<PolicyRule> getRules() {
		return rules;
	}

	public void setRules(List<PolicyRule> rules) {
		this.rules = rules;
	}
}
