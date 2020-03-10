package net.fluance.security.auth.web.controller;

import java.sql.SQLException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ResponseStatus;

import javassist.NotFoundException;
import net.fluance.app.security.util.exception.InvalidTokenException;
import net.fluance.app.web.util.exceptions.BadRequestException;
import net.fluance.app.web.util.exceptions.ManagedException;
import net.fluance.app.web.util.exceptions.UnauthorizedException;

/**
 * The class adds an error handler for the know different exception types and the managemente of these exceptions.<br>
 * The goal is avoid nont managed exceptions that deserve in an 500 error
 * 
 */
public abstract class AbstractAuthRestController {
	
	public abstract Logger getLogger();
	
	/**
	 * Manage the given Exception, log it, and return the correct status in the ResponseEntity 
	 * 
	 * @param exc
	 * @return  {@link ResponseEntity}
	 */
	public ResponseEntity<?> handleException(Exception exc) {		
		getLogger().warn("Handling exception: {}", exc.getMessage());
		
		ResponseEntity<?> responseEntity;
		
		if (exc instanceof UnauthorizedException || 
				exc instanceof net.fluance.app.security.util.exception.UnauthorizedException ||
				exc instanceof InvalidTokenException) {			
			responseEntity =  new ResponseEntity<>(exc.getMessage(), HttpStatus.UNAUTHORIZED);
		} else if (exc instanceof IllegalArgumentException) {
			responseEntity =  new ResponseEntity<>(exc.getMessage(), HttpStatus.BAD_REQUEST);
		} else if (exc instanceof BadRequestException) {
			responseEntity =  new ResponseEntity<>(exc.getMessage(), HttpStatus.BAD_REQUEST);
		}  else if (exc instanceof NotFoundException){
			responseEntity =  new ResponseEntity<>(exc.getMessage(), HttpStatus.NOT_FOUND);
		} else if (exc instanceof EmptyResultDataAccessException){
			HttpStatus status = HttpStatus.NOT_FOUND;
			responseEntity =  new ResponseEntity<>(exc.getMessage(), status);
		} else if (exc instanceof BadSqlGrammarException){
			getLogger().warn("Error on the SQL: {}", ((BadSqlGrammarException) exc).getRootCause());
			responseEntity =  new ResponseEntity<>("Data base error", HttpStatus.BAD_REQUEST);
		} else if (exc instanceof SQLException){
			getLogger().warn("Data base acess error: {}", exc.getMessage());
			responseEntity =  new ResponseEntity<>("Data base error", HttpStatus.CONFLICT);
		}else if (exc instanceof ManagedException){
			HttpStatus status = null;
			ResponseStatus annotation = AnnotatedElementUtils.findMergedAnnotation(exc.getClass(), ResponseStatus.class);
			if(annotation != null) {
				status = annotation.value();
			} else {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
			}
			getLogger().warn("Managed exception");
			responseEntity =  new ResponseEntity<>(exc.getMessage(), status);
		} else {
			getLogger().error("Unknow exception");
			
			responseEntity = new ResponseEntity<>(exc.getMessage(), HttpStatus.BAD_REQUEST);
		}
		
		getLogger().warn(ExceptionUtils.getStackTrace(exc));
		getLogger().warn("Return status will be: {}", responseEntity.getStatusCode());
		
		return responseEntity;
	}
}
