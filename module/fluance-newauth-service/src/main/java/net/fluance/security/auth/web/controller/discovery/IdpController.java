package net.fluance.security.auth.web.controller.discovery;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import net.fluance.security.auth.web.controller.AbstractAuthRestController;
import net.fluance.security.core.model.jdbc.Idp;
import net.fluance.security.core.repository.jdbc.IdpRepository;

@RestController
@RequestMapping("/idps")
public class IdpController extends AbstractAuthRestController{

	private static Logger LOGGER = LogManager.getLogger(IdpController.class);

	@Autowired
	private IdpRepository idpDiscoveryRepository;

	@ApiOperation(value = "Discover all available IDP's", tags = {"discovery API"})
	@RequestMapping(value = "", method = RequestMethod.GET)
	public ResponseEntity<?> idps(HttpServletRequest request, HttpServletResponse response) {
		try{
			LOGGER.info("Get list of available Identity Providers.");
			List<Idp> idps = idpDiscoveryRepository.findAll();

			LOGGER.info("Return list of available Identity Providers.");
			return new ResponseEntity<>(idps, HttpStatus.OK);
		} catch (Exception e) {
			return handleException(e);
		}
	}

	@Override
	public Logger getLogger() {
		return null;
	}
}
