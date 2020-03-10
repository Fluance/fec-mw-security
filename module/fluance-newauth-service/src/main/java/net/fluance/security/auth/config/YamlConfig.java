package net.fluance.security.auth.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import net.fluance.security.core.model.partner.Partner;

/**
 * Reads the properties that are defined in yaml files 
 * For complex properties the POJO that models the values are mandatory.
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class YamlConfig {
	
	private List<Partner> partners;

	public List<Partner> getPartners() {
		return partners;
	}

	public void setPartners(List<Partner> partners) {
		this.partners = partners;
	}

}
