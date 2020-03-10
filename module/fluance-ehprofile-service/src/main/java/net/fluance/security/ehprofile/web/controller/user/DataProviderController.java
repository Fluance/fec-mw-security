package net.fluance.security.ehprofile.web.controller.user;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import net.fluance.app.data.exception.DataException;
import net.fluance.app.data.util.db.PostgreSQLUtils;
import net.fluance.app.web.servlet.controller.AbstractRestController;
import net.fluance.app.web.support.payload.response.GenericResponsePayload;
import net.fluance.security.core.model.jpa.DataProvider;
import net.fluance.security.core.repository.jpa.IDataProviderRepository;

/**
 *
 */
@RestController
public class DataProviderController extends AbstractRestController {

	private static Logger LOGGER = LogManager.getLogger(DataProviderController.class);

	@Autowired
	private IDataProviderRepository dataProviderRepository;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.fluance.app.web.servlet.controller.AbstractController#getLogger()
	 */
	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	@ApiOperation(value = "Data providers List", response = DataProvider.class, responseContainer = "List", tags="User API")
	@RequestMapping(value = "/dataproviders", method = RequestMethod.GET)
	public ResponseEntity<?> readRoles(HttpServletRequest request, HttpServletResponse response) {
		
		HttpStatus status = HttpStatus.ACCEPTED;
		List<DataProvider> providers = new ArrayList<>();
		GenericResponsePayload responsePayload = new GenericResponsePayload();
		try {
			providers = dataProviderRepository.findAll();
			if (providers == null) {
				responsePayload.setMessage("Providers not Found");
				status = HttpStatus.NOT_FOUND;
				return new ResponseEntity<>(responsePayload, status);
			} else {
				status = HttpStatus.OK;
			}
			
			return new ResponseEntity<>(providers, status);
			
		} catch (Exception exc) {
			return handleException(exc);
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
