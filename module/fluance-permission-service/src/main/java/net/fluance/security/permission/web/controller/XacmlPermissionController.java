package net.fluance.security.permission.web.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiOperation;
import net.fluance.app.data.exception.DataException;
import net.fluance.app.security.service.support.entitlement.EntitlementDecision;
import net.fluance.app.web.servlet.controller.AbstractRestController;
import net.fluance.app.web.support.payload.response.GenericResponsePayload;
import net.fluance.security.permission.service.XacmlPermissionService;
import net.fluance.security.permission.service.XacmlPermissionTestService;
import net.fluance.security.permission.support.payload.response.XacmlPolicyTestResponse;

@RestController
@RequestMapping("/xacml/**")
public class XacmlPermissionController extends AbstractRestController {
	private static Logger LOGGER = LogManager.getLogger(XacmlPermissionController.class);
	
	@Autowired
	private XacmlPermissionService  xacmlPermissionService;
	
	@Autowired
	XacmlPermissionTestService xacmlPermissionTestService;	

	@ApiOperation(value = "Evaluate a request using existing XACML policies", response = EntitlementDecision.class, tags = "Permission API")
	@RequestMapping(value = "/evaluate", method = RequestMethod.GET/*, consumes = MediaType.APPLICATION_JSON_VALUE*/)
	public ResponseEntity<?> evaluate(@RequestBody(required = false) String payload, @RequestParam(required = false) String resource, @RequestParam(required = false) String username, @RequestParam(required = false) String domain, @RequestParam(required = false) String action, @RequestParam(name = "user_roles", required = false) List<String> roles, HttpServletRequest request,
			HttpServletResponse response) {		
		
		try {			
			return new ResponseEntity<EntitlementDecision>(xacmlPermissionService.evaluate(payload, resource, username, domain, action, roles), HttpStatus.OK);
		} catch(IOException | IllegalArgumentException exc) {
						
			getLogger().error(exc.getMessage());
			
			return new ResponseEntity<>(new GenericResponsePayload(exc.getMessage()), HttpStatus.BAD_REQUEST);
		} catch(Exception exc) {
			getLogger().error(exc.getMessage());
			
			return new ResponseEntity<>(new GenericResponsePayload(exc.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Test a Xacml policy", response = EntitlementDecision.class, tags = "Permission API")
	@RequestMapping(value = "/policy/test", method = RequestMethod.POST)
	public ResponseEntity<?> testPolicyGet(@RequestParam(name = "testData", required = true) String testDataStr,
			@RequestParam(name = "file") MultipartFile file) {
		
		if (file == null) {
			getLogger().warn("Failed to upload policy file");
			return new ResponseEntity<GenericResponsePayload>(new GenericResponsePayload("Failed to upload policy file"), HttpStatus.BAD_REQUEST);
		} else if (file.isEmpty()) {
			getLogger().warn("Policy file " + file.getOriginalFilename() + " is empty. Nothing to do");			
			return new ResponseEntity<GenericResponsePayload>(new GenericResponsePayload("Policy file " + file.getOriginalFilename() + " is empty. Nothing to do"), HttpStatus.OK);
		} else {
			getLogger().info("Processing file  " + file.getOriginalFilename());
			
			try {
				return new ResponseEntity<XacmlPolicyTestResponse>(xacmlPermissionTestService.evaluateTest(testDataStr, file, getLogger()), HttpStatus.OK);
			} catch (Exception e) {
				LOGGER.error(ExceptionUtils.getStackTrace(e));				
				return new ResponseEntity<GenericResponsePayload>(new GenericResponsePayload("Error(s) occured while trying to import some profiles from "
						+ file.getOriginalFilename() + " => " + e.getMessage()), HttpStatus.BAD_REQUEST);
			}
		}
	}

	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	@Override
	public Object handleDataException(DataException exc) {
		return null;
	}
}
