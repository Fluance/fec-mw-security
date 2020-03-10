/**
 * 
 */
package net.fluance.security.permission.support.payload.request;

import java.util.List;

public class XacmlPolicyTestRequestContent {

	private List<XacmlPolicyTest> testsToPerform;

	public List<XacmlPolicyTest> getTestsToPerform() {
		return testsToPerform;
	}

	public void setTestsToPerform(List<XacmlPolicyTest> testsToPerform) {
		this.testsToPerform = testsToPerform;
	}
	
}
