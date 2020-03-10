package net.fluance.security.auth.web.controller.access;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
	
	private Logger logger = LogManager.getLogger(CustomLogoutSuccessHandler.class);
	
	private static final String LOGOUT_SUCCESS_PREFIX = "[logout][LogoutSuccess]";
	
	private static final String REDIRECT_URL_PARAMETER = "url_redirect";
	
	@Value("${clients.fe.url}")
	private String uiUrl;
	
	@Override
	public void onLogoutSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		
		logger.info(LOGOUT_SUCCESS_PREFIX+"Logout success init...");
		
		String redirectUrl = uiUrl;
	
		String[] redirectValue = request.getParameterMap().get(REDIRECT_URL_PARAMETER);
		if(redirectValue != null) {
			logger.info(LOGOUT_SUCCESS_PREFIX+"Redirect uri set in request");
			redirectUrl = String.join("", redirectValue);
		}
		
		logger.info(LOGOUT_SUCCESS_PREFIX+"Redirecting...");
		logger.debug(LOGOUT_SUCCESS_PREFIX+"Redirecting to: " + redirectUrl);
		
		response.sendRedirect(redirectUrl);
	}

}
