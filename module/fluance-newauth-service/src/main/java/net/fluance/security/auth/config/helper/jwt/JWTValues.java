package net.fluance.security.auth.config.helper.jwt;

import net.fluance.app.data.model.identity.ThirdPartyUserReference;

/**
 * Values to set on the map for the OAuth2AccessToken from the JWT assertion
 */
public class JWTValues {
	private JWTValues(){}
	
	/**
	 * This field name must match with {@link ThirdPartyUserReference} json property set on {@link User}
	 */
	public static final String THIRD_PARTY_FIELD = "thirdPartyUser";
	public static final String ACTUAL_USERNAME = "actualUserName";
	public static final String ACTUAL_FIRSTNAME = "actualFirstName";
	public static final String ACTUAL_LASTNAME = "actualLastName";
	public static final String ACTUAL_EMAIL = "actualEmail";
}
