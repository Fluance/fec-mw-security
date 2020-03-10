package net.fluance.security.ehprofile.web.controller.idm;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import net.fluance.app.data.exception.DataException;
import net.fluance.app.web.servlet.controller.AbstractRestController;
import net.fluance.app.web.support.payload.response.GenericResponsePayload;
import net.fluance.security.ehprofile.service.idm.IdentityManagementService;

/**
 *
 */
@RestController
@RequestMapping("/identity/domains/**")
public class IdentityDomainController extends AbstractRestController {

	private static Logger LOGGER = LogManager.getLogger(IdentityDomainController.class);

	@Autowired
	private IdentityManagementService idMService;

	@ApiOperation(value = "List all identity domains", response = GenericResponsePayload.class, tags = "Identity Management API")
	@RequestMapping(value = "", method = RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> list(HttpServletRequest request, HttpServletResponse response) {
		try {
			LOGGER.info("Trying to retrieve the list of available ID domains ...");
			List<? extends Object> availableDomains = idMService.availableIdDomains();
			LOGGER.info("Recieved the list of available ID domains: " + availableDomains);
			return new ResponseEntity<>(availableDomains, HttpStatus.OK);
		} catch (IllegalStateException ise) {
			LOGGER.error(ExceptionUtils.getStackTrace(ise));
			return new ResponseEntity<>(new GenericResponsePayload("Could not retrieve the list of ID domains due to a gateway error"), HttpStatus.BAD_GATEWAY);
		} catch (Exception iae) {
			LOGGER.error(ExceptionUtils.getStackTrace(iae));
			return new ResponseEntity<>(new GenericResponsePayload("Could not retrieve the list of ID domains due to an internal error"), HttpStatus.INTERNAL_SERVER_ERROR);
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
