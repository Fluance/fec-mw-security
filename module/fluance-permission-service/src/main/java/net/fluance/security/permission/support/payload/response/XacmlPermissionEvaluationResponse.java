/**
 * 
 */
package net.fluance.security.permission.support.payload.response;

public class XacmlPermissionEvaluationResponse {

	private String decision;

	public XacmlPermissionEvaluationResponse() {}
	
	/**
	 * @param decision
	 */
	public XacmlPermissionEvaluationResponse(String decision) {
		super();
		this.decision = decision;
	}

	/**
	 * @return the decision
	 */
	public String getDecision() {
		return decision;
	}

	/**
	 * @param decision the decision to set
	 */
	public void setDecision(String decision) {
		this.decision = decision;
	}
	
}
