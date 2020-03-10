/**
 * 
 */
package net.fluance.security.ehprofile.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import net.fluance.app.web.support.payload.response.GenericResponsePayload;
import net.fluance.security.core.model.partner.Partner;
import net.fluance.security.core.util.TrustedPartnerUtils;
import net.fluance.security.ehprofile.config.YamlConfig;

/**
 *
 */
@Service
public class UserProfileProxyService {

	private static final Logger LOGGER = LogManager.getLogger(UserProfileProxyService.class);
	
	@Autowired
	private YamlConfig yamlConfig;
	
	@Value("${jwt.partner.token.url}")
	private String partnerJwtTokenUrl;
	
	/**
	 * Checks the <code>companyCode</code> for the given <code>username</code> and <code>trustedPartner</code>. The <code>trustedPartner</code> must exists and it must have the
	 * correct configuration to obtain the JWT token for calling it's configured check url as well.
	 * 
	 * @param trustedPartner
	 * @param username
	 * @param companyCode
	 * @param authzToken
	 * @return {@Link ResponseEntity} to the call to the configured check url for the partner.
	 */
	public ResponseEntity<?> checkCompany(String trustedPartner, String userName, String companyCode, String authzToken) {
		try {
			Partner partner = null;

			if(trustedPartner != null) {
				partner = TrustedPartnerUtils.searchPartnerByName(yamlConfig.getPartners(), trustedPartner);
			}
			if(partner == null) {
				return new ResponseEntity<>(new GenericResponsePayload("Unknown partner: " + trustedPartner), HttpStatus.BAD_REQUEST);
			}

			LOGGER.info("checking company " + companyCode + " for user " + userName + " at partner " + partner.getName());
			ResponseEntity<?> responseEntity = checkCompany(partner.getName(), authzToken, partner.getGrantedCompanyCheckUrl(), userName, companyCode);
			
			return responseEntity;
		} catch (Exception exc) {
			return new ResponseEntity<>(new GenericResponsePayload("Error occured: " + exc.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Checks the <code>companyCode</code> for the given <code>username</code>. The <code>tokenToExchange</code> must allow to obtain a valid JWT token that
	 * will be use to request <code>partnerCheckUrl</code>.
	 * 
	 * @param partner
	 * @param tokenToExchange
	 * @param partnerCheckUrl
	 * @param username
	 * @param companyCode
	 * @return the {@Link ResponseEntity} for the request to <code>partnerCheckUrl</code>
	 * @throws RestClientException
	 * @throws MalformedURLException
	 */
	private ResponseEntity<?> checkCompany(String partner, String tokenToExchange, String partnerCheckUrl, String userName, String companyCode) throws RestClientException, MalformedURLException {
		ResponseEntity<?> jwtResponseEntity = obtainJwt(partner, tokenToExchange);
		
		if(!HttpStatus.OK.equals(jwtResponseEntity.getStatusCode())) {
			return new ResponseEntity<>(new GenericResponsePayload("Could not get authorization token to call " + partnerCheckUrl + ":" + jwtResponseEntity.getBody()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		String jwt = (String) jwtResponseEntity.getBody();
		
		// URL params
		Map<String, String> pathVariables = new HashMap<String, String>();
		pathVariables.put("username", userName);
		pathVariables.put("companycode", companyCode);
		pathVariables.put("trustedpartner", partner);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(partnerCheckUrl);
		UriComponents checkUriComponents = uriComponentsBuilder.buildAndExpand(pathVariables);
		URI checkUri = checkUriComponents.toUri();
		LOGGER.info("Sending company check request for company " + companyCode + " and user " + userName + " to " + checkUri.toURL().toString());
		
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
		HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
		RestTemplate restTemplate = new RestTemplate();
		try {
			ResponseEntity<?> responseEntity = restTemplate.exchange(checkUri.toURL().toString(), HttpMethod.GET, httpEntity, String.class);
			LOGGER.info("Recieved response from " + checkUri.toURL().toString() + ": {statusCode=" + responseEntity.getStatusCode() + ", body=" + responseEntity.getBody() + "}");
			return responseEntity;
		} catch(Exception exception) {
			LOGGER.error(ExceptionUtils.getStackTrace(exception));
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_GATEWAY);
		}
	}
	
	/**
	 * Request for a valid JWT token using as barer token <code>tokenToExchange</code>
	 * The URI that will be request is configured with the property jwt.partner.token.url
	 * 
	 * @param partner, only used for logging
	 * @param tokenToExchange
	 * @return The response for the request.
	 * @throws MalformedURLException 
	 * @throws RestClientException 
	 */
	private ResponseEntity<?> obtainJwt(String partner, String tokenToExchange) throws RestClientException, MalformedURLException {
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(partnerJwtTokenUrl).queryParam("trustedPartner", partner);
		URI tokenUri = uriComponentsBuilder.buildAndExpand().toUri();
		
		LOGGER.info("Requesing JWT at " + tokenUri.toURL().toString() + " for partner " + partner);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + tokenToExchange);
		HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = restTemplate.exchange(tokenUri.toURL().toString(), HttpMethod.GET, httpEntity, String.class);
		String jwtResponseContent = responseEntity.getBody();
		LOGGER.info("Got JWT from " + tokenUri.toURL().toString() + " for partner " + partner + ": " + jwtResponseContent);
		return responseEntity;
	}
	
}