package net.fluance.security.ehprofile.web.controller.user;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.keycloak.representations.idm.UserRepresentation;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import io.swagger.annotations.ApiOperation;
import net.fluance.app.data.exception.DataException;
import net.fluance.app.data.util.db.PostgreSQLUtils;
import net.fluance.app.web.servlet.controller.AbstractRestController;
import net.fluance.app.web.support.payload.response.GenericResponsePayload;
import net.fluance.app.web.util.exceptions.BadRequestException;
import net.fluance.security.core.model.MyTeam;
import net.fluance.security.core.service.KeycloakUserService;
import net.fluance.security.core.support.exception.NotFoundException;
import net.fluance.security.ehprofile.util.ResponseUtils;

/**
 *
 *
 */
@RestController
@RequestMapping("/IAM")
public class IamController extends AbstractRestController {

	private static Logger LOGGER = LogManager.getLogger(IamController.class);
	
	@Autowired
	private KeycloakUserService keycloackProfileService;
	
	@ApiOperation(value = "Search Profile By Email", response = UserRepresentation.class, tags = "IAM API", notes="Search a user by the email or the username, not both")
	@GetMapping(value = "/search")
	public ResponseEntity<?> searchProfile(@RequestParam(required=false) String email, @RequestParam(required=false) String username, HttpServletRequest request, HttpServletResponse response) {
		try {
			if(StringUtils.isEmpty(email) && StringUtils.isEmpty(username)) {
				throw new BadRequestException("Email or Username is required");
			}
			if(!StringUtils.isEmpty(email) && !StringUtils.isEmpty(username)) {
				throw new BadRequestException("Only email or username can be used in this request, not both.");
			}
			return new ResponseEntity<>(keycloackProfileService.getUserByEmail(email, username), HttpStatus.OK);
		} catch (Exception e) {
			return handleException(e);
		}
	}

	@ApiOperation(value = "Search Profile By Email", response = UserRepresentation.class, tags = "IAM API", notes="Search a user by the email or the username, not both")
	@GetMapping(value = "/search/Profiles")
	public ResponseEntity<?> searchProfiles(@RequestParam(required=false) String email, @RequestParam(required=false) String username, HttpServletRequest request, HttpServletResponse response) {
		try {
			if(StringUtils.isEmpty(email) && StringUtils.isEmpty(username)) {
				throw new BadRequestException("Email or Username is required");
			}
			if(!StringUtils.isEmpty(email) && !StringUtils.isEmpty(username)) {
				throw new BadRequestException("Only email or username can be used in this request, not both.");
			}
			return new ResponseEntity<>(keycloackProfileService.getUsersByEmail(email, username), HttpStatus.OK);
		} catch (Exception e) {
			return handleException(e);
		}
	}
	
	@ApiOperation(value = "Search By Criteria", response = List.class, tags = "IAM API", notes="The results only need to match with one the parameters given")
	@GetMapping(value = "/search/bycriteria")
	public ResponseEntity<?> byCriteria(
			@RequestParam(required=false, value="firstname") String firstName, 
			@RequestParam(required=false, value="lastname") String lastName, 
			@RequestParam(required=false, value="username") List<String> userNames,
			@RequestParam(required=false) Integer limit,  
			@RequestParam(required=false) Integer offset, 
			HttpServletRequest request, 
			HttpServletResponse response) 
	{
		try {
			if(StringUtils.isEmpty(firstName) && StringUtils.isEmpty(lastName) && CollectionUtils.isEmpty(userNames)) {
				throw new BadRequestException("One parameter is required at least");
			}
			return new ResponseEntity<>(keycloackProfileService.byCriteria(firstName, lastName, userNames, limit, offset), HttpStatus.OK);
		} catch (Exception e) {
			return handleException(e);
		}
	}
	
	@ApiOperation(value = "Save User Type", response = String.class, tags = "IAM API", notes="Save a userType attribute on keycloak")
	@PostMapping(value = "/{username}/usertype/set")
	public ResponseEntity<?> saveUserType(@PathVariable String username, @RequestParam(value="usertype") List<String> userType, HttpServletRequest request, HttpServletResponse response) {
		try {
			Boolean result = keycloackProfileService.updateUserType(username, userType);
			if(!result){
				throw new HTTPException(HttpStatus.INTERNAL_SERVER_ERROR.value());
			}
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (Exception e) {
			return handleException(e);
		}
	}

	@ApiOperation(value = "Reset password", tags = "IAM API", notes="Set up a temporary password for the user. Then the user will have to reset the temporary password next time they log in.")
	@RequestMapping(value = "/{username}/passwordreset", method = RequestMethod.POST)
	public ResponseEntity<?> resetPassword(
			@PathVariable(value="username") String userName,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		try{
			Boolean result = keycloackProfileService.resetPassword(userName);
			if(!result){
				throw new HTTPException(HttpStatus.INTERNAL_SERVER_ERROR.value());
			}
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e){
			return handleException(e);
		}
	}
	
	@ApiOperation(value = "Get the User's Photo", tags = "IAM API", notes="Currently, it's using mocking data")
	@GetMapping(value="/{username}/thumbnailPhoto")
	@ResponseBody
	public ResponseEntity<?> getThumbnailPhoto(
			@PathVariable(value="username") String userName,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		try{
			byte[] thumbnail = keycloackProfileService.getThumbnailPhoto(userName);
			HttpHeaders headers = ResponseUtils.setContentTypeFromArray(thumbnail);
			return new ResponseEntity<>(thumbnail, headers, HttpStatus.OK);
		} catch (Exception e){
			return handleException(e);
		}
	}

	@ApiOperation(value = "Get My Team", response = MyTeam.class, tags = "IAM API", notes="Return the Manager, Colleages and Subordinates of the User if they exist")
	@GetMapping(value = "/{username}/myteam")
	public ResponseEntity<?> myTeam(
			@PathVariable String username, 
			HttpServletRequest request, 
			HttpServletResponse response) 
	{
		try {
			return new ResponseEntity<>(keycloackProfileService.getMyTeam(username), HttpStatus.OK);
		} catch (Exception e) {
			return handleException(e);
		}
	}

	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	@Override
	public ResponseEntity<?> handleException(Exception exc) {
		GenericResponsePayload grp = new GenericResponsePayload(exc.getMessage());
		if(exc instanceof PSQLException || exc instanceof DataAccessException) {
			getLogger().warn("", exc);
			return (ResponseEntity<?>) handleDataException(new DataException (exc));
		} else if(exc instanceof NotFoundException) {
			getLogger().warn("", exc);
			return new ResponseEntity<>(grp, HttpStatus.NOT_FOUND);
		} else if(exc instanceof HttpClientErrorException) {
			getLogger().error("", exc);
			return new ResponseEntity<>(grp, ((HttpClientErrorException)exc).getStatusCode());
		} else if(exc instanceof HttpServerErrorException) {
			getLogger().error("", exc);
			return new ResponseEntity<>(grp, HttpStatus.BAD_GATEWAY);
		} else if(exc instanceof HTTPException) {
			getLogger().error("", exc);
			return new ResponseEntity<>(grp, HttpStatus.BAD_GATEWAY);
		} 
		else {
			return super.handleException(exc);
		}
	}

	/**
	 * Give the implementation for the data exception expected at the parent class
	 * 
	 * @param exc The exception that was raised
	 * @return
	 */
	@Override
	public ResponseEntity<GenericResponsePayload> handleDataException(DataException exc) {
		GenericResponsePayload grp = new GenericResponsePayload();
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		String message = DEFAULT_INTERNAL_SERVER_ERROR_MESSAGE;
		Throwable rootCause = null;
		try {
			rootCause = ExceptionUtils.getRootCause(exc.getException());
		} catch(Exception exception) {}
		if((rootCause != null) && (rootCause instanceof PSQLException)) {
			if(PostgreSQLUtils.SQLSTATE_DUPLICATEKEY_ERROR.equalsIgnoreCase(((PSQLException)rootCause).getSQLState())) {				
				status = HttpStatus.CONFLICT;
				message = "Could not update data, due to a duplicate key error";
				grp.setMessage(message);
				return new ResponseEntity<>(grp, status);
			} else if(PostgreSQLUtils.SQLSTATE_FOREIGNKEYVIOLATION_ERROR.equalsIgnoreCase(((PSQLException)rootCause).getSQLState())) {				
				status = HttpStatus.CONFLICT;
				message = "Could not update data, due to a foreign key violation error";
				grp.setMessage(message);
				return new ResponseEntity<>(grp, status);
			} 
		}
		grp.setMessage((message == null || message.isEmpty()) ? DEFAULT_INTERNAL_SERVER_ERROR_MESSAGE : message);
		return new ResponseEntity<>(grp, status);
	}
}
