package net.fluance.security.auth.web.controller.jwt;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import io.swagger.annotations.ApiOperation;
import net.fluance.app.web.util.RequestHelper;
import net.fluance.security.auth.service.jwt.JwtService;
import net.fluance.security.auth.support.payload.request.JWTTokenRequestBody;
import net.fluance.security.auth.web.controller.AbstractAuthRestController;

@RestController
@RequestMapping("/jwt")
public class JwtController extends AbstractAuthRestController{

	private static Logger LOGGER = LogManager.getLogger(JwtController.class);

	@Autowired
	private JwtService jwtService;

	@Autowired
	private RequestHelper requestHelper;

	@ApiOperation(value = "Get a Jwt. To let the service decide the signing algorithm and/or the type, just set their values to null or don't provide them. "
			+ "Requires a Bearer authorization through Authorization header", response = String.class, tags = "JWT API")
	@RequestMapping(value = "/token", method = RequestMethod.POST)
	public ResponseEntity<?> token(@RequestBody JWTTokenRequestBody jwtTokenRequestBody, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			return new ResponseEntity<>(
					jwtService.generateTokenFromBody(jwtTokenRequestBody, requestHelper.accessToken(request)),
					HttpStatus.OK);
		} catch (Exception exc) {
			return handleException(exc);
		}
	}

	@ApiOperation(value = "Issue a JWT with default settings. If target application is provided, then a token for that application will be issued (if the application is known).", response = String.class, tags = "JWT API")
	@RequestMapping(value = "/token/default", method = RequestMethod.GET, produces = "text/plain")
	public ResponseEntity<?> defaultJwt(@RequestParam(name = "trustedPartner", required = false) String targetApp,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			return new ResponseEntity<>(jwtService.generateDefaultToken(targetApp, requestHelper.accessToken(request)),
					HttpStatus.OK);
		} catch (Exception exc) {
			return handleException(exc);
		}
	}

	@ApiOperation(value = "Exchange an existing, non-expired, JWT for a new one", response = String.class, tags = "JWT API")
	@RequestMapping(value = "/token/exchanged", method = RequestMethod.GET)
	public ResponseEntity<?> token(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			return new ResponseEntity<>(jwtService.generateDefaultToken(null, requestHelper.accessToken(request)),
					HttpStatus.OK);
		} catch (Exception exc) {
			return handleException(exc);
		}
	}

	@Override
	public Logger getLogger() {
		return LOGGER;
	}
}
