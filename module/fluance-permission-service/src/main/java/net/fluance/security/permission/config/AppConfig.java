/**
 * 
 */
package net.fluance.security.permission.config;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.fluance.app.security.service.xacml.BalanaXacmlPDP;
import net.fluance.security.permission.support.helper.xacml.XacmlHelper;

@Component
@Configuration
public class AppConfig {
	
	private final Charset defaultCharset = Charset.forName("UTF-8");
	@Value("${tmp.dir}")
	private String tmpDir;
	
    /**
	 * @return the defaultCharset
	 */
	public Charset getDefaultCharset() {
		return defaultCharset;
	}

	public String getTmpDir() {
		return tmpDir;
	}

	@Bean
	public BalanaXacmlPDP xacmlPDP() {
		return new BalanaXacmlPDP();
	}
	
	@Bean
	public XacmlHelper XacmlHelper() {
		return new XacmlHelper();
	}
	
	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
	    MappingJackson2HttpMessageConverter converter = 
	        new MappingJackson2HttpMessageConverter(new ObjectMapper());
	    converter.setSupportedMediaTypes(Arrays.asList(MediaType.ALL));
	    return converter;
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfig() {
		return new PropertySourcesPlaceholderConfigurer();
	}

}
