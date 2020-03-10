package net.fluance.security.auth.web.controller.usersession;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import net.fluance.app.data.model.identity.User;
import net.fluance.app.web.util.exceptions.UnauthorizedException;
import net.fluance.security.auth.service.oauth2.OAuth2Service;
import net.fluance.security.auth.web.controller.AbstractAuthRestController;
import net.fluance.security.core.model.Count;
import net.fluance.security.core.model.jdbc.UserSessionData;
import net.fluance.security.core.repository.jdbc.UserSessionDataRepository;

@RestController
@RequestMapping("/sessions")
public class UserSessionController extends AbstractAuthRestController{

	private static Logger LOGGER = LogManager.getLogger(UserSessionController.class);
	
	@Autowired
	private OAuth2Service oAuth2Service;	
	
	@Autowired
	private UserSessionDataRepository userSessionDataRepository;

	/**
	 * Retrieves all the valid {@link UserSessionData} of the {@link User} associated with the token
	 *  
	 * @param token
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "Get User Sessions", response = UserSessionData.class, responseContainer = "list", tags = "User Sessions API")
	@RequestMapping(value = "", method = RequestMethod.GET)
	public ResponseEntity<?> getUserSessions(@RequestParam String token, HttpServletRequest request, HttpServletResponse response) throws Exception {
		List<UserSessionData> validUserSessions = new ArrayList<>();
		try {
			validUserSessions = getValidUserSessionsFromToken(token);
			return new ResponseEntity<List<UserSessionData>>(validUserSessions, HttpStatus.OK);
		} catch(Exception e) {
			return handleException(e);
		}
	}
	
	/**
	 * Retrieves all the valid {@link UserSessionData} of the {@link User} associated with the token and returns the quantity
	 * 
	 * @param token
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "Get User Sessions Count", response = Count.class, tags = "User Sessions API")
	@RequestMapping(value = "/count", method = RequestMethod.GET)
	public ResponseEntity<?> getUserSessionsCount(@RequestParam String token, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		Count result = new Count(0);
		try {
			List<UserSessionData> lUserSessionData = getValidUserSessionsFromToken(token);
			if(lUserSessionData != null){
				result = new Count(lUserSessionData.size());
			}
		} catch(Exception e) {
			handleException(e);
		}
		
		return new ResponseEntity<Count>(result, HttpStatus.OK);
	}
	
	private List<UserSessionData> getValidUserSessionsFromToken(String token) throws Exception {
		List<UserSessionData> validUserSessions = new ArrayList<>();
		UserSessionData userSessionData = userSessionDataRepository.findOne(token);
		
		if(userSessionData == null){
			throw new UnauthorizedException("Token Not valid");
		}
		
		if(userSessionData.getSubjectId() == null) {
			throw new RuntimeException("The User Id cannot be null from userSessionData.");
		}
		
		List<UserSessionData> userTokens = userSessionDataRepository.findTokenFromUser(userSessionData.getSubjectId());
		
		if( userTokens == null || userTokens.isEmpty()){
			throw new RuntimeException("No Token associated to the user");
		}
		
		for(UserSessionData concurrentUserSessionData : userTokens) {
			User concurrentUser = oAuth2Service.validateAccessTokent(concurrentUserSessionData.getId());
			if(concurrentUser != null) {					
				validUserSessions.add(concurrentUserSessionData);
			}
		}
		return validUserSessions;
	}

	@Override
	public Logger getLogger() {
		return LOGGER;
	}
}
