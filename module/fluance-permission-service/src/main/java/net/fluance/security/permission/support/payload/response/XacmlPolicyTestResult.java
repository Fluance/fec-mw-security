/**
 * 
 */
package net.fluance.security.permission.support.payload.response;

import java.util.List;

import net.fluance.app.security.service.support.entitlement.EntitlementDecision;

public class XacmlPolicyTestResult {

	private String url;
	private String user;
	private List<String> roles;
	private String action;
	private EntitlementDecision pdpEvaluationResponse;
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public EntitlementDecision getPdpEvaluationResponse() {
		return pdpEvaluationResponse;
	}

	public void setPdpEvaluationResponse(EntitlementDecision pdpEvaluationResponse) {
		this.pdpEvaluationResponse = pdpEvaluationResponse;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	
}
