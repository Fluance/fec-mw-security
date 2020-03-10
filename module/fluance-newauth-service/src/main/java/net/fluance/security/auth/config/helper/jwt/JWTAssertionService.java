package net.fluance.security.auth.config.helper.jwt;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.fluance.app.security.util.OAuth2Helper;
import net.fluance.app.web.util.exceptions.UnknownIssuerException;
import net.fluance.commons.json.jwt.JWTUtils;
import net.fluance.commons.json.jwt.JWTUtils.JwtPart;
import net.fluance.commons.net.HttpUtils;

@Service
public class JWTAssertionService {
	
	private Logger LOGGER = LogManager.getLogger();
	public static final String ISSUER_KEY = "issuer";
	public static final String USERNAME_KEY = "name_id";
	public static final String GRANT_TYPE = "grant_type";
	
	@Value("${oauth2.service.token.url}")
	private String oAuth2TokenUrl;
	@Value("${oauth2.service.client.id}")
	private String oAuth2ClientId;
	@Value("${oauth2.service.client.secret}")
	private String oAuth2ClientSecret;
	@Value("${application.user.shared-password}")
	private String applicationUserSharedPassword;
	@Value("${api.patient.url}")
	private String patientServiceUrl;
	@Value("${oauth2.service.url.getToken}")
	private String urlOAuth2RequestTokent;

	@Autowired
	OAuth2Helper oAuth2Helper;
	
	public boolean isPatientValid(String assertion) throws Exception {
		boolean isPatientDataValid = false;
		JsonNode jwtPayload = JWTUtils.getPart((String) assertion, JwtPart.PAYLOAD);
		if (jwtPayload == null || !jwtPayload.has(JWTUtils.ISSUER_KEY)){
			LOGGER.error("The element '" + JWTUtils.ISSUER_KEY + "' is not present in the JWT Token's payload: " + jwtPayload);
			throw new UnknownIssuerException("The element '" + JWTUtils.ISSUER_KEY + "' is not present in the JWT Token's payload: " + jwtPayload);
		}
		String oAuth2TokenString = oAuth2Helper.getAuth2SrvToken(urlOAuth2RequestTokent, "fluance", applicationUserSharedPassword);
		DefaultOAuth2AccessToken oAuth2Token = new ObjectMapper().readValue(oAuth2TokenString, DefaultOAuth2AccessToken.class);
		long pid = jwtPayload.get("pid").longValue();
		String firstName = URLEncoder.encode(jwtPayload.get("firstName").textValue(), "UTF-8");
		String lastName = URLEncoder.encode(jwtPayload.get("lastName").textValue(), "UTF-8");
		String birthDate = URLEncoder.encode(jwtPayload.get("birthDate").textValue(), "UTF-8");
		String params = "?pid=" + pid + "&firstname=" + firstName + "&lastname=" + lastName + "&birthdate=" + birthDate + "&limit=1";
		CloseableHttpResponse patientServiceResponse = sendRequest(patientServiceUrl + params , oAuth2Token.getValue());
		String jsonReponse = EntityUtils.toString(patientServiceResponse.getEntity());
		LOGGER.debug(patientServiceUrl + " Response = " + patientServiceResponse.getStatusLine().getStatusCode());
		if (patientServiceResponse.getStatusLine().getStatusCode() == 200) {
			ObjectMapper mapper = new ObjectMapper();
			List<?> patients = mapper.readValue(jsonReponse, mapper.getTypeFactory().constructCollectionType(List.class, Object.class));
			if (patients != null && !patients.isEmpty()){
				isPatientDataValid = true;
			} else {
				LOGGER.warn("Patient Not Found | Params : " + params);
			}
		} else {
			LOGGER.warn("Unable to Load Patient Data");
			isPatientDataValid = false;
		}
		return isPatientDataValid;
	}

	/**
	 * Send a real http requestusing the complete URL and the access token
	 * 
	 * @param fullUri
	 * @param token
	 *            accessToken
	 * @return CloseableHttpResponse
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws HttpException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	protected CloseableHttpResponse sendRequest(String fullUri, String token) throws URISyntaxException,
	KeyManagementException, NoSuchAlgorithmException, KeyStoreException, HttpException, IOException {
		URI uri = HttpUtils.buildUri(fullUri);
		HttpGet get = HttpUtils.buildGet(uri, null);
		get.setHeader("Authorization", "Bearer " + token);
		CloseableHttpResponse response = HttpUtils.sendGet(get, true);
		return response;
	}
}
