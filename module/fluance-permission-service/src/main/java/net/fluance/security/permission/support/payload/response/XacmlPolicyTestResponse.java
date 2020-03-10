/**
 * 
 */
package net.fluance.security.permission.support.payload.response;

import java.util.ArrayList;
import java.util.List;

import net.fluance.security.permission.support.helper.xacml.Policy;

public class XacmlPolicyTestResponse {

	private Policy policyUnderTest;
	private List<XacmlPolicyTestResult> testResults;
	
	public XacmlPolicyTestResponse() {
		this.testResults = new ArrayList<>();
	}
	
	public XacmlPolicyTestResponse(Policy policyUnderTest, List<XacmlPolicyTestResult> testResults) {
		super();
		this.policyUnderTest = policyUnderTest;
		this.testResults = testResults;
	}

	public Policy getPolicyUnderTest() {
		return policyUnderTest;
	}
	
	public void setPolicyUnderTest(Policy policyUnderTest) {
		this.policyUnderTest = policyUnderTest;
	}

	public List<XacmlPolicyTestResult> getTestResults() {
		return testResults;
	}

	public void setTestResults(List<XacmlPolicyTestResult> testResults) {
		this.testResults = testResults;
	}

	
}
