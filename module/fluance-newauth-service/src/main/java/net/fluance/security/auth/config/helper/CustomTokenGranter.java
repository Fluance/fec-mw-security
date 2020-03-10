/**
 * 
 */
package net.fluance.security.auth.config.helper;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import net.fluance.commons.codec.PKIUtils;

public abstract class CustomTokenGranter extends AbstractTokenGranter {
	protected final String ISSUER_KEY = "issuer";

	protected Map<String, PublicKey> trustedPublicKeys;
	protected String keyStore;
	protected String keyStoreType;
	protected String keyStorePassword;
	protected String trustStore;
	protected String trustStorePassword;
	protected String trustStoreType;
	protected Logger logger;

	protected CustomTokenGranter(AuthorizationServerTokenServices tokenServices,
			ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType) throws Exception {
		super(tokenServices, clientDetailsService, requestFactory, grantType);
		trustedPublicKeys = new HashMap<>();
		keyStore = System.getProperty("javax.net.ssl.keyStore");
		keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
		keyStoreType = System.getProperty("javax.net.ssl.keyStoreType");
		trustStore = System.getProperty("javax.net.ssl.trustStore");
		trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
		trustStoreType = System.getProperty("javax.net.ssl.trustStoreType");
		trustedPublicKeys = PKIUtils.readPublicKeys(new File(trustStore), trustStorePassword, trustStoreType);
	}

	@PostConstruct
	public void init() throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
			UnrecoverableEntryException, IOException, javax.security.cert.CertificateException {
	}

	protected abstract String subjectId(Object assertion) throws Exception;

	protected abstract boolean isAssertionValid(Object assertion) throws Exception;

	protected abstract Logger getLogger();

}
