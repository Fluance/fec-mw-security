/**
 * 
 */
package net.fluance.security.auth.config.helper.jwt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.exceptions.OAuth2ExceptionJackson2Deserializer;
import org.springframework.security.oauth2.common.exceptions.OAuth2ExceptionJackson2Serializer;

/**
 * Allows to get Spring to translate the exception and send the right message and codes to the client
 *
 */
@SuppressWarnings("serial")
@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = OAuth2ExceptionJackson2Serializer.class)
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = OAuth2ExceptionJackson2Deserializer.class)
public class InvalidSubjectIdException extends OAuth2Exception {

	public InvalidSubjectIdException(String msg) {
		super(msg);
	}
	
    public InvalidSubjectIdException(String msg, Throwable t) {
        super(msg, t);
    }
    
    @Override
    public String getOAuth2ErrorCode() {
        return "invalid_authentication_token";
    }
    
    @Override
    public int getHttpErrorCode() {
        return HttpStatus.UNAUTHORIZED.value();
    }

}
