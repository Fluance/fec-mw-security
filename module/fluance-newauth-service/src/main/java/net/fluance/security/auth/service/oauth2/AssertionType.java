package net.fluance.security.auth.service.oauth2;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.fluance.app.security.auth.OAuth2AccessToken;
import net.fluance.commons.codec.Base64Utils;
import net.fluance.commons.json.JsonUtils;
import net.fluance.commons.json.jwt.JWTUtils;

public enum AssertionType {
	JWT("jwt", "jwtAssertion"),
	OAUTH2("", "");

	private String assertionName;
	private String keyOAuth2;
	
	private AssertionType(String assertionName, String keyOAuth2) {
		this.assertionName = assertionName;
		this.keyOAuth2 = keyOAuth2;
	}

	/**
	 * Give back the type of the assertion.
	 * @param assertion : the assertion we want to verify to know it's type.
	 * @return the AssertionType
	 * @throws IllegalArgumentException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static AssertionType assertionType(String assertion) throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException {
		
		if(JWTUtils.isJwt(assertion)){
			return AssertionType.JWT;
		}
		else if(isOAuth2(assertion)){
			return AssertionType.OAUTH2;
		}
		else {
			throw new IllegalArgumentException("unknown assertion");
		}
	}

	/**
	 * Check if the assertion is a JSON and corresponds to the Auth Token Structure.
	 * @param assertion : the OAUTH2 token.
	 * @return true : the assertion is a JSON. false : the assertion is not a JSON.
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 */
	private static boolean isOAuth2(String assertion) throws JsonProcessingException, IOException {
		if(Base64.isBase64(assertion)){
			assertion = Base64Utils.base64UrlDecode(assertion);
		}
		return JsonUtils.checkJsonCompatibility(assertion, OAuth2AccessToken.class);
	}

	public String getAssertionName() {
		return assertionName;
	}

	public String getKeyOAuth2() {
		return keyOAuth2;
	}
}


