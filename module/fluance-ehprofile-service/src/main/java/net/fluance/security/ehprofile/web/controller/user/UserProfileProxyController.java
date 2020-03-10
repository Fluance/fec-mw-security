package net.fluance.security.ehprofile.web.controller.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import net.fluance.app.data.exception.DataException;
import net.fluance.app.web.servlet.controller.AbstractRestController;
import net.fluance.app.web.util.RequestHelper;
import net.fluance.security.ehprofile.service.UserProfileProxyService;

/**
 *
 */
@RestController
public class UserProfileProxyController extends AbstractRestController {

	private static Logger LOGGER = LogManager.getLogger(UserProfileProxyController.class);
	
	@Autowired
	private UserProfileProxyService userProfileProxyService;
	@Autowired
	private RequestHelper requestHelper;
	
	@ApiOperation(value = "Check if user has access to a company at a partner's", response = Boolean.class, tags = "User profile API", notes="Use this end-point to check if a company is granted to a User or Not in the specified partner's domain")
	@RequestMapping(value = "/partners/{trustedpartner}/users/{username}/{companycode}/exists", method = RequestMethod.GET)
	public ResponseEntity<?> checkCompany(@PathVariable("trustedpartner") String trustedPartner, @PathVariable("username") String username, @PathVariable("companycode") String companyCode, HttpServletRequest request, HttpServletResponse response) {
		try {
			String accessToken = requestHelper.accessToken(request);
			ResponseEntity<?> responseEntity = userProfileProxyService.checkCompany(trustedPartner, username, companyCode, accessToken);
			return new ResponseEntity<>(responseEntity.getBody(), responseEntity.getStatusCode());
		}catch (Exception exc) {
			return handleException(exc);
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
