package net.fluance.security.auth.service.jwt;

import java.io.File;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.fluance.app.data.model.identity.User;
import net.fluance.app.security.util.JwtHelper;
import net.fluance.app.security.util.exception.UnsupportedAlgorithmException;
import net.fluance.app.web.util.exceptions.InvalidAuthenticationException;
import net.fluance.commons.json.jwt.JWTUtils;
import net.fluance.security.auth.config.AppConfig;
import net.fluance.security.auth.config.YamlConfig;
import net.fluance.security.auth.service.oauth2.OAuth2Service;
import net.fluance.security.auth.service.util.ThirdPartyUserUtil;
import net.fluance.security.auth.support.payload.request.JWTTokenRequestBody;
import net.fluance.security.core.model.partner.Partner;
import net.fluance.security.core.util.TrustedPartnerUtils;

/**
 * Different operations for create valid JWT tokens
 */
@Service
public class JwtService {
	
	private static Logger LOGGER = LogManager.getLogger(JwtService.class);
	
	@Autowired
	private JwtHelper jwtHelper;

	@Autowired
	private AppConfig appConfig;
	
	@Autowired
	private OAuth2Service oAuth2Service;
	
	@Autowired
	private YamlConfig yamlConfig;

	@Value("${app.jwt.issuer}")
	private String jwtIssuer;
	@Value("${jwt.default.type}")
	private String defaultJwtType;
	@Value("${jwt.default.signing-algorithm}")
	private String defaultJwtSigningAlgorithm;

	@Value("${jwt.default.validity-period}")
	private int defaultJwtValidityPeriod;
	
	// Oxygen public keys
	@Value("${javax.net.ssl.keyStore}")
	private String keyStoreFilePath;
	@Value("${javax.net.ssl.keyStorePassword}")
	private String keyStorePassword;
	@Value("${javax.net.ssl.keyStoreType}")
	private String keyStoreType;
	@Value("${jwt.specialPartner}")
	private String specialPartner;

	/**
	 * Generates a String that represents a JWT token using the given {@link JWTTokenRequestBody}.</br>
	 * If no issuer non issuer is set on the given jwtTokenRequestBody the default will be use, set at the property app.jwt.issuer
	 * 
	 * @param jwtTokenRequestBody
	 * @param accessToken, barer access token for a user that must be valid for all the next requests
	 * @return
	 * @throws Exception
	 */
	public String generateTokenFromBody(JWTTokenRequestBody jwtTokenRequestBody, String accessToken) throws Exception {
		String requestIssuer = null;
		
		if(jwtTokenRequestBody.getPayload() != null &&
				jwtTokenRequestBody.getPayload().has(JWTUtils.ISSUER_KEY) &&
				jwtTokenRequestBody.getPayload().get(JWTUtils.ISSUER_KEY).isTextual()) {
			requestIssuer = jwtTokenRequestBody.getPayload().get(JWTUtils.ISSUER_KEY).textValue();
		} else {
			requestIssuer = jwtIssuer;
		}
		
		return issueJwt(jwtTokenRequestBody.getSigningAlgorithm(), jwtTokenRequestBody.getType(), requestIssuer, accessToken, null, jwtTokenRequestBody.getHeader(), jwtTokenRequestBody.getPayload());
	}
	
	/**
	 * Generates a String that represents a JWT token.<br>
	 * If a <b>partnerName</b> is set it must exists on configured properties for partners
	 * 
	 * @param partnerName
	 * @param accessToken, barer access token for a user that must be valid for all the next requests
	 * @return
	 * @throws Exception 
	 */
	public String generateDefaultToken(String partnerName, String accessToken) throws Exception {
		Partner partner = null;

		if(partnerName != null) {
			partner = TrustedPartnerUtils.searchPartnerByName(yamlConfig.getPartners(), partnerName);
			if (partner == null) {
				throw new IllegalArgumentException("Unknown partner: " + partnerName);
			}
		}
		
		return issueJwt(null, null, null, accessToken, partner, null, null);
	}
	
	
	/**
	 * Gets a String representing a JWT token getting the data depending of the different values give.<br>
	 * If <b>signingAlgorithm</b> is null the default value configured on the property jwt.default.signing-algorithm will be use.<br>
	 * If <b>type</b> is null the default value configured on the property jwt.default.type will be use.<br>
	 * If <b>issuer</b> is null default will be use, set at the property app.jwt.issuer.<br>
	 * If <b>partner</b> is null the username and domain configured on {@link JwtHelper} will be use.<br>
	 * 
	 * @param signingAlgorithm, can be null
	 * @param type, can be null
	 * @param issuer, can be null
	 * @param accessToken, barer access token for a user that must be valid for all the next requests
	 * @param partner, can be null
	 * @param customHeaderClaims, can be null
	 * @param customPayloadClaims, can be null
	 * @return
	 * @throws CertificateException 
	 * @throws Exception
	 */
	private String issueJwt(String signingAlgorithm, String type, String issuer, String accessToken,
			Partner partner, ObjectNode customHeaderClaims, ObjectNode customPayloadClaims) throws Exception {

		User user = oAuth2Service.validateAccessTokent(accessToken);
		
		if (user == null) {
			throw new InvalidAuthenticationException("Invalid authorization");
		}

		// Check if the provided signing algorithm is supported
		if (signingAlgorithm != null && !appConfig.getJwtSupportedSigningAlgorithms().contains(signingAlgorithm)) {
			throw new UnsupportedAlgorithmException("Unsupported JWT signing algorithm: " + signingAlgorithm);
		}

		ObjectNode header = configureHeaderClaims(signingAlgorithm, type);		
		ObjectNode payload = configurePayloadClaims(user, partner);
		
		payload.put(JWTUtils.ISSUER_KEY, ((issuer == null || issuer.isEmpty()) ? jwtIssuer : issuer));

		if(customHeaderClaims != null) {
			final Iterator<String> customHeaderClaimNames = customHeaderClaims.fieldNames();
			while (customHeaderClaimNames.hasNext()) {
				String claimName = customHeaderClaimNames.next();
				header.set(claimName, customHeaderClaims.get(claimName));
			}
		}
		if(customPayloadClaims != null) {
			final Iterator<String> customPayloadClaimNames = customPayloadClaims.fieldNames();
			while (customPayloadClaimNames.hasNext()) {
				String claimName = customPayloadClaimNames.next();
				payload.set(claimName, customPayloadClaims.get(claimName));
			}
		}

		String jwt = null;
		if(partner != null && partner.getName() != null && !partner.getName().isEmpty() && specialPartner.equals(partner.getName())) {
			File keyStoreFile = new FileSystemResource(keyStoreFilePath).getFile();
			jwt = jwtHelper.build(header, payload, keyStoreFile, keyStorePassword, keyStoreType, partner.getSslKeyAlias());
		} else {
			jwt = jwtHelper.build(header, payload);
		}

		return jwt;
	}
	
	/**
	 * Return the {@link ObjectNode} with the claims.<br>
	 * If <b>partner</b> is null the username and domain configured on {@link JwtHelper} will be use.<br>
	 * If <b>partner</b> is not null and there is not validity period set on the partner's JWTspec the default validity period<br>
	 * set at the property jwt.default.validity-period will be use.
	 * 
	 * @param user
	 * @param partner
	 * @return
	 * @throws Exception
	 */
	private ObjectNode configurePayloadClaims(User user, Partner partner) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode payload = mapper.createObjectNode();

		Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
		if(partner != null) {
			// Use the validity period from the partner's configuration. If no value provided in configuration => use default validity period.
			if(partner.getJwtSpec().getPayload().getValidityPeriod() > 0) {
				calendar.add(Calendar.SECOND, partner.getJwtSpec().getPayload().getValidityPeriod());
			} else {
				calendar.add(Calendar.SECOND, defaultJwtValidityPeriod);
			}
			
			List<String> dynamicPayloadClaims = partner.getJwtSpec().getPayload().getDynamicClaims();			
			for (String claim : dynamicPayloadClaims) {
				switch (claim) {
				case JwtHelper.USERNAME_KEY:
					payload.put(JwtHelper.USERNAME_KEY, user.getUsername());
					break;
				case JwtHelper.DOMAIN_KEY:
					String currentDomain = user.getDomain();
					payload.put(JwtHelper.DOMAIN_KEY, currentDomain);
					break;
				default:
					throw new IllegalArgumentException("Unsupported claim: " + claim);
				}
			}
		} else {
			calendar.add(Calendar.SECOND, defaultJwtValidityPeriod);
			payload.put(JwtHelper.USERNAME_KEY, user.getUsername());
			payload.put(JwtHelper.DOMAIN_KEY, user.getDomain());
		}
		
		ThirdPartyUserUtil.addThirdPartyUserToPayload(user, payload);

		long expirationTime = calendar.getTimeInMillis();
		LOGGER.info("Expiration time in ms: " + expirationTime);
		payload.put(JWTUtils.EXPIRATION_TIME_KEY, expirationTime);
		
		return payload;
	}

	/**
	 * Returns the {@link ObjectNode} with the header for the request.<br>
	 * If <b>signingAlgo</b> is null the default value configured on the property jwt.default.signing-algorithm will be use.<br>
	 * If <b>reqType</b> is null the default value configured on the property jwt.default.type will be use.
	 * 
	 * @param signingAlgo
	 * @param reqType
	 * @return
	 * @throws Exception
	 */
	private ObjectNode configureHeaderClaims(String signingAlgo, String reqType) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode header = mapper.createObjectNode();

		String type = (reqType != null) ? reqType : defaultJwtType;
		String signingAlgorithm = (signingAlgo != null) ? signingAlgo : defaultJwtSigningAlgorithm;

		header.put(JWTUtils.TYPE_KEY, type);
		header.put(JWTUtils.SIGNING_ALGORITHM_KEY, signingAlgorithm);

		return header;
	}
}
