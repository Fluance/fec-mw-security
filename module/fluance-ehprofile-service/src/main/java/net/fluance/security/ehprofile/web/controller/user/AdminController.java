package net.fluance.security.ehprofile.web.controller.user;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiOperation;
import net.fluance.app.data.exception.DataException;
import net.fluance.app.data.util.db.PostgreSQLUtils;
import net.fluance.app.web.servlet.controller.AbstractRestController;
import net.fluance.app.web.support.payload.response.GenericResponsePayload;
import net.fluance.security.core.model.response.ImportProfileResult;
import net.fluance.security.core.support.exception.NotFoundException;
import net.fluance.security.core.util.Constants;
import net.fluance.security.ehprofile.service.AdminService;

@RestController
@RequestMapping("/admin")
public class AdminController extends AbstractRestController {

	private static Logger LOGGER = LogManager.getLogger(AdminController.class);

	@Autowired
	private AdminService adminService;

	@ApiOperation(value = "Import Profile by Excel File", response = ImportProfileResult.class, tags = "Admin API", notes="")
	@PostMapping(value = "/import/excel")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public ResponseEntity<?> importExcel(
			@RequestParam(value="file") MultipartFile file,
			HttpServletRequest request, 
			HttpServletResponse response) {
		try {
			
			if(file == null || file.isEmpty()) {
				throw new IllegalArgumentException("The file is empty");
			}
			
			if(!Constants.XLSX_MEDIATYPE.equals(file.getContentType())) {
				throw new IllegalArgumentException("The file is not an Excel 2007 File (Microsoft Excel Open XML Format Spreadsheet)");
			}			
			
			ImportProfileResult importResult = adminService.importProfileFile(file);
			
			return new ResponseEntity<>(importResult, HttpStatus.OK);
		} catch (Exception e) {
			return handleException(e);
		}
	}
	
	@ApiOperation(value = "Return all the roles available", response = ImportProfileResult.class, tags = "Admin API", notes="")
	@GetMapping(value = "/roles")
	public ResponseEntity<?> findRoles(
			HttpServletRequest request, 
			HttpServletResponse response) {
		try {			
			return new ResponseEntity<List<String>>(adminService.findAllRoles(), HttpStatus.OK);
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