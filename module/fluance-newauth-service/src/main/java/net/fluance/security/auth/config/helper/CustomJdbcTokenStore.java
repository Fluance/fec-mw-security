package net.fluance.security.auth.config.helper;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;


public class CustomJdbcTokenStore extends JdbcTokenStore {
	
	private static final String selectAccessTokenFromAuthenticationSql = "select token_id, token from oauth_access_token where authentication_id = ?";
	private static final String selectAccessTokenAuthenticationSql = "select token_id, authentication from oauth_access_token where token_id = ?";
	
	private static final String GET_ACCESS_TOKEN = "[getAccessToken]";
	private static final String GET_CURRENT_ACCESS_TOKEN = "[getCurrentAccessToken]";
	private static final String READ_CURRENT_AUTHENTICATION = "[readCurrentAuthentication]";
	
	private static final Logger LOG = LogManager.getLogger(CustomJdbcTokenStore.class);
	
	private AuthenticationKeyGenerator authenticationKeyGenerator;
	private final JdbcTemplate jdbcTemplate;

	public CustomJdbcTokenStore(DataSource dataSource) {
		super(dataSource);
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
		LOG.info(GET_ACCESS_TOKEN + "Init...");
		LOG.debug(GET_ACCESS_TOKEN + "{" + authentication +  "}");
		OAuth2AccessToken accessToken = null;

		String key = authenticationKeyGenerator.extractKey(authentication);
		try {
			accessToken = jdbcTemplate.queryForObject(selectAccessTokenFromAuthenticationSql,
					new RowMapper<OAuth2AccessToken>() {
						public OAuth2AccessToken mapRow(ResultSet rs, int rowNum) throws SQLException {
							return deserializeAccessToken(rs.getBytes(2));
						}
					}, key);
		}
		catch (EmptyResultDataAccessException e) {
			LOG.warn(GET_ACCESS_TOKEN + "Failed to find access token for authentication");
		}
		catch (IllegalArgumentException e) {
			LOG.warn(GET_ACCESS_TOKEN + "Could not extract access token for authentication");
		}

		if (accessToken != null) {
			OAuth2Authentication currentAuthentication = readCurrentAuthentication(accessToken.getValue());
			
			//If it is not possible gt the current authentication the flow must be tha same as new or different key
			if(currentAuthentication == null) {
				LOG.warn(GET_ACCESS_TOKEN + "Imposible to read current authentication, will be remove and overwrite");
			}
			
			if(currentAuthentication == null || (currentAuthentication != null && !key.equals(authenticationKeyGenerator.extractKey(currentAuthentication)))) {
				LOG.info(GET_ACCESS_TOKEN + "Removing access token...");
				removeAccessToken(accessToken.getValue());
				
				LOG.info(GET_ACCESS_TOKEN + "Storing access token...");
				storeAccessToken(accessToken, authentication);
			}
		}
		
		LOG.info(GET_ACCESS_TOKEN + "return");
		return accessToken;
	}
	
	public OAuth2AccessToken getCurrentAccessToken(OAuth2Authentication authentication) {
		LOG.info(GET_CURRENT_ACCESS_TOKEN + "Init...");
		LOG.debug(GET_CURRENT_ACCESS_TOKEN + "{" + authentication +  "}");
		OAuth2AccessToken accessToken = null;

		String key = authenticationKeyGenerator.extractKey(authentication);
		try {
			accessToken = jdbcTemplate.queryForObject(selectAccessTokenFromAuthenticationSql,
					new RowMapper<OAuth2AccessToken>() {
						public OAuth2AccessToken mapRow(ResultSet rs, int rowNum) throws SQLException {
							return deserializeAccessToken(rs.getBytes(2));
						}
					}, key);
		}
		catch (EmptyResultDataAccessException e) {
			LOG.warn(GET_CURRENT_ACCESS_TOKEN + "Failed to find access token for authentication");
		}
		catch (IllegalArgumentException e) {
			LOG.warn(GET_CURRENT_ACCESS_TOKEN + "Could not extract access token for authentication");
		}
		
		LOG.info(GET_CURRENT_ACCESS_TOKEN + "return");
		return accessToken;
	}
	
	
	/**
	 * The method logs the error and return null if there is any error.
	 * Null return must be managed like it is impossible to read the content of the row
	 * 
	 * @param token
	 * @return OAuth2Authentication with the DB data or null
	 */
	public OAuth2Authentication readCurrentAuthentication(String token) {		
		LOG.info(READ_CURRENT_AUTHENTICATION + "Init...");
		LOG.debug(READ_CURRENT_AUTHENTICATION + "{" + token +  "}");
		
		OAuth2Authentication authentication = null;

		try {
			authentication = jdbcTemplate.queryForObject(selectAccessTokenAuthenticationSql,
					new RowMapper<OAuth2Authentication>() {
						public OAuth2Authentication mapRow(ResultSet rs, int rowNum) throws SQLException {
							return deserializeAuthentication(rs.getBytes(2));
						}
					}, extractTokenKey(token));
		}
		catch (EmptyResultDataAccessException e) {
			LOG.warn(READ_CURRENT_AUTHENTICATION + "Failed to find access token for token " + token);
		}
		catch (IllegalArgumentException e) {
			LOG.warn(READ_CURRENT_AUTHENTICATION + "Failed to deserialize authentication for " + token);
		}

		LOG.info(READ_CURRENT_AUTHENTICATION + "return");
		return authentication;
	}
	
	public void setAuthenticationKeyGenerator(AuthenticationKeyGenerator authenticationKeyGenerator) {
		super.setAuthenticationKeyGenerator(authenticationKeyGenerator);
		this.authenticationKeyGenerator = authenticationKeyGenerator;
	}

}
