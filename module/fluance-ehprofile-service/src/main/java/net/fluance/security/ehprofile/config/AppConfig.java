/**
 * 
 */
package net.fluance.security.ehprofile.config;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;

import net.fluance.app.security.util.JwtHelper;
import net.fluance.app.security.util.OAuth2Helper;

/**
 *
 */
@Component
@Configuration
public class AppConfig {

	@Value("${entreprise.userstore.name}")
	private String entrepriseUserstoreName;
	@Value("${entreprise.userstore.type}")
	private String entrepriseUserstoreType;
	
	@Value("${jwt.signing-algorithm.supported}")
	private String jwtSupportedSigningAlgorithmsProp;
	private List<String> jwtSupportedSigningAlgorithms;
	
	private final String oauth2RedirectType = "oauth2";
	private final Charset defaultCharset = Charset.forName("UTF-8");

	@PostConstruct
	public void init() {
		jwtSupportedSigningAlgorithms = new ArrayList<>();
		StringTokenizer algosTokenizer = new StringTokenizer(jwtSupportedSigningAlgorithmsProp, ",");
		while (algosTokenizer.hasMoreElements()) {
			jwtSupportedSigningAlgorithms.add(algosTokenizer.nextToken());
		}
	}
	
    /**
	 * @return the defaultCharset
	 */
	public Charset getDefaultCharset() {
		return defaultCharset;
	}

	/**
	 * @return the entrepriseUserstoreName
	 */
	public String getEntrepriseUserstoreName() {
		return entrepriseUserstoreName;
	}

	/**
	 * @return the entrepriseUserstoreType
	 */
	public String getEntrepriseUserstoreType() {
		return entrepriseUserstoreType;
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfig() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	/**
	 * @return the oauth2RedirectType
	 */
	public String getOauth2RedirectType() {
		return oauth2RedirectType;
	}
	
	/**
	 * @return the jwtSupportedSigningAlgorithms
	 */
	public List<String> getJwtSupportedSigningAlgorithms() {
		return jwtSupportedSigningAlgorithms;
	}
	
	@Bean
	public OAuth2Helper oAuth2Helper() {
		return new OAuth2Helper();
	}
	
	@Bean
	public JwtHelper jwtHelper() {
		return new JwtHelper();
	}
	
}
