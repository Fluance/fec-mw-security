package net.fluance.security.ehprofile.web.controller.userclientdata;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import io.swagger.annotations.ApiOperation;
import net.fluance.app.data.exception.DataException;
import net.fluance.app.data.model.identity.User;
import net.fluance.app.web.servlet.controller.AbstractRestController;
import net.fluance.app.web.support.payload.response.GenericResponsePayload;
import net.fluance.security.core.model.jpa.UserClientData;
import net.fluance.security.ehprofile.model.userclientdata.UserClientDataSaveRequestPayload;
import net.fluance.security.ehprofile.service.userclientdata.UserClientDataService;

@RestController
@RequestMapping("/userdata")
public class UserClientDataController extends AbstractRestController {

	private static Logger LOGGER = LogManager.getLogger(UserClientDataController.class);
	
	@Autowired
	private UserClientDataService userClientDataService;
	
	/**
	 * 
	 * @param payload
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Write user client data", response = GenericResponsePayload.class, tags = "User client Data API")
	@RequestMapping(value = "/{username}/{domain}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> save(@RequestBody UserClientDataSaveRequestPayload payload, @PathVariable String username, @PathVariable String domain, HttpServletRequest request,
			HttpServletResponse response) {

		GenericResponsePayload grp = new GenericResponsePayload();
		HttpStatus status = HttpStatus.ACCEPTED;

		try {
			if(domain == null || domain.isEmpty()){
				LOGGER.warn("Cannot find user " + username + " in domain " + domain);
				throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Cannot find user " + username + " in domain " + domain);
			}
			
			UserClientData savedClientData = userClientDataService.saveClient(payload, username, domain);
			
			if (savedClientData != null) {
				status = HttpStatus.OK;
				grp.setMessage("Successfully saved data for user " + domain + "/" + username + " and client " + payload.getClientId());
			} else {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
				grp.setMessage("Unknown error. Could not save data for user " + domain + "/" + username + " and client " + payload.getClientId());
			}
			
			return new ResponseEntity<>(grp, status);
		} catch (Exception exc) {
			return handleException(exc);
		}
	}

	@ApiOperation(value = "Read user client data", response = UserClientData.class, tags = "User client Data API")
	@RequestMapping(value = "/{username}/{domain}/{client_id}", method = RequestMethod.GET)
	public ResponseEntity<?> get(@PathVariable String username, @PathVariable String domain, @PathVariable("client_id") String clientId, HttpServletRequest request,
			HttpServletResponse response) {

		try {
			return new ResponseEntity<>(userClientDataService.getUserData(clientId, username, domain), HttpStatus.OK);
		}catch (Exception exc) {
			return handleException(exc);
		}
	}

	@ApiOperation(value = "Read user client data from user in request data", response = UserClientData.class, tags = "User client Data API")
	@RequestMapping(value = "/my/{client_id}", method = RequestMethod.GET)
	public ResponseEntity<?> getMyClientData(@PathVariable("client_id") String clientId, HttpServletRequest request,
			HttpServletResponse response) {
		User user = (User) request.getAttribute(User.USER_KEY);
		if(user == null){
			return new ResponseEntity<>("Unable to retrieve User", HttpStatus.NOT_FOUND);
		}
		String username = user.getUsername();
		String domain = user.getDomain();

		try {
			return new ResponseEntity<>(userClientDataService.getUserData(clientId, username, domain), HttpStatus.OK);
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
