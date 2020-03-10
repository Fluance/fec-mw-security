/**
 * 
 */
package net.fluance.security.auth.config.helper.jwt;

import java.io.IOException;
import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.fluance.commons.json.jwt.JWTUtils;
import net.fluance.commons.json.jwt.JWTUtils.JwtPart;


public class JWTAuthenticationToken extends AbstractAuthenticationToken {

	private final Object principal; // Should be final (?)
	private Collection<? extends GrantedAuthority>  authorities;
	
	private String jwtAssertion;


	public JWTAuthenticationToken(String jwtAssertion, String principal) throws JsonProcessingException, IOException {
        super(null);
        
        this.jwtAssertion = jwtAssertion;

        if(principal != null) {
        	this.principal = principal;
        } else {
        	if (!JWTUtils.isJwt(jwtAssertion)) {
        		throw new IllegalArgumentException("Not a valid JWT: " + jwtAssertion);
        	}
        	
        	ObjectNode jwtPayload = JWTUtils.getPart(jwtAssertion, JwtPart.PAYLOAD);
        	
        	if(jwtPayload != null && jwtPayload.has(JWTUtils.SUBJECT_KEY)
        			&& jwtPayload.get(JWTUtils.SUBJECT_KEY).isTextual()) {
        		this.principal = jwtPayload.get(JWTUtils.SUBJECT_KEY).textValue();
        	} else {
        		this.principal = null;
        	}
        }
    }
	
	/* (non-Javadoc)
	 * @see org.springframework.security.core.Authentication#getCredentials()
	 */
	@Override
	public Object getCredentials() {
		// TODO Auto-generated method stub
		return "";
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.core.Authentication#getPrincipal()
	 */
	@Override
	public Object getPrincipal() {
		return principal;
	}
 
    @SuppressWarnings("unchecked")
	@Override
    public Collection<GrantedAuthority> getAuthorities() {
        return (Collection<GrantedAuthority>) authorities;
    }

	@Override
	public Object getDetails(){
		return sessionIndex;
	}

	private String sessionIndex;
	private String issuer;
	
	public void setSessionIndex(String sessionIndex){
		this.sessionIndex = sessionIndex;
	}

	public void setIssuer(String issuer){
		this.issuer = issuer;
	}

	public String getIssuer() {
		return this.issuer;
	}
	
	public String getSessionIndex() {
		return this.sessionIndex;
	}
	
	public String getJwtAssertion() {
		return jwtAssertion;
	}

}
