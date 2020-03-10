package net.fluance.security.permission.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.fluance.app.security.service.support.entitlement.EntitlementDecision;
import net.fluance.app.security.service.xacml.XacmlPDP;
import net.fluance.commons.codec.MD5Utils;
import net.fluance.security.permission.config.AppConfig;
import net.fluance.security.permission.support.helper.xacml.Policy;
import net.fluance.security.permission.support.helper.xacml.XacmlHelper;
import net.fluance.security.permission.support.payload.request.XacmlPolicyTest;
import net.fluance.security.permission.support.payload.request.XacmlPolicyTestRequestContent;
import net.fluance.security.permission.support.payload.response.XacmlPolicyTestResponse;
import net.fluance.security.permission.support.payload.response.XacmlPolicyTestResult;

@Service
public class XacmlPermissionTestService {
	
	@Autowired
	private XacmlPDP xacmlPDP;
	@Autowired
	private AppConfig appConfig;
	@Autowired
	private XacmlHelper xacmlHelper;
	
	
	/**
	 * Test if the given representation as JSON string of a  {@link XacmlPolicyTestRequestContent} match with the given policy file
	 * 
	 * @param testDataStr
	 * @param file
	 * @param internalLogger
	 * @throws Exception 
	 */
	public XacmlPolicyTestResponse evaluateTest(String testDataStr, MultipartFile file, Logger internalLogger) throws Exception {
		XacmlPolicyTestResponse responseBody = null;
		String destDirBase = file.getOriginalFilename() + new Date().toString();
		File destDir = null;
		try {
			String destDirHash = MD5Utils.md5HexHash(destDirBase);
			destDir = new File(appConfig.getTmpDir() + File.separator + destDirHash);
			destDir.mkdirs();
			File tmpFile = new File(destDir.getAbsolutePath() + File.separator + file.getOriginalFilename());
			file.transferTo(tmpFile);
			if (tmpFile.exists()) {
				internalLogger.info("Successfully transfered file " + file.getOriginalFilename() + " to " + destDir);
				internalLogger.info("Starting test of policy from file  " + file.getOriginalFilename());

				Policy policy = xacmlHelper.loadFromFile(tmpFile);

				responseBody = new XacmlPolicyTestResponse();

				responseBody.setPolicyUnderTest(policy);

				ObjectMapper objectMapper = new ObjectMapper();
				XacmlPolicyTestRequestContent testData = objectMapper.readValue(testDataStr,
						XacmlPolicyTestRequestContent.class);

				for (XacmlPolicyTest test : testData.getTestsToPerform()) {
					List<XacmlPolicyTestResult> results = test(test, policy, internalLogger);
					responseBody.getTestResults().addAll(results);
				}				
			}
		} finally {
			if (destDir.isDirectory()) {
				destDir.delete();
			}
		}
		
		return responseBody;
	}

	private List<XacmlPolicyTestResult> test(XacmlPolicyTest test, Policy policy, Logger internalLogger) throws Exception {
		internalLogger.info("Performing test " + new ObjectMapper().writeValueAsString(test) + " ...");

		if (test == null) {
			throw new IllegalArgumentException("Test case must be specified");
		}

		List<String> roles = (test.getRoles() != null) ? test.getRoles() : new ArrayList<String>();
		List<XacmlPolicyTestResult> testResults = new ArrayList<>();

		// Test "empty action" when no action is provided in the test case
		if (test.getActions() == null || test.getActions().size() == 0) {
			XacmlPolicyTestResult result = testForAction(test.getResource(), "", test.getUser(), roles);
			testResults.add(result);
		} else if (test.getActions().size() == 1) {
			XacmlPolicyTestResult result = testForAction(test.getResource(), test.getActions().get(0), test.getUser(),
					roles);
			testResults.add(result);
		} else {
			for (String action : test.getActions()) {
				XacmlPolicyTestResult result = testForAction(test.getResource(), action, test.getUser(), roles);
				testResults.add(result);
			}
		}

		return testResults;
	}

	private XacmlPolicyTestResult testForAction(String url, String action, String user, List<String> roles)
			throws Exception {
		XacmlPolicyTestResult result = new XacmlPolicyTestResult();
		result.setUrl(url);
		result.setAction(action);
		result.setUser(user);
		if (roles == null || roles.isEmpty()) {
			List<String> evalRoles = new ArrayList<String>();
			result.setRoles(evalRoles);
			EntitlementDecision evaluateResult = xacmlPDP.evaluate(url, action, user, evalRoles);
			result.setPdpEvaluationResponse(evaluateResult);
		} else {
			// Testing for each role separately
			for (String role : roles) {
				List<String> evalRoles = Arrays.asList(new String[] { role });
				result.setRoles(evalRoles);
				EntitlementDecision evaluateResult = xacmlPDP.evaluate(url, action, user, evalRoles);
				result.setPdpEvaluationResponse(evaluateResult);
			}
		}
		return result;
	}
}
