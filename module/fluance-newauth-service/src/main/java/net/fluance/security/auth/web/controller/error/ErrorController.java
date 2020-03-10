package net.fluance.security.auth.web.controller.error;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Super implementation for the default error controller of Spring<br>
 * The flow will be redirect to the default URL
 * 
 */
@RestController
public class ErrorController implements org.springframework.boot.autoconfigure.web.ErrorController{

	private Logger logger = LogManager.getLogger();
    private static final String PATH = "/error";

    @Value("${clients.fe.url}")
	private String feDefaultUrl;

    @RequestMapping(value = PATH)
    public void error(HttpServletResponse response) throws IOException {
    	logger.error("Error... Redirecting to the FE DEFAULT URL: {}", feDefaultUrl);
		response.sendRedirect(feDefaultUrl);
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}