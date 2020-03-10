/**
 * 
 */
package net.fluance.security.auth.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import net.fluance.app.web.servlet.filter.CORSFilter;
import net.fluance.app.web.util.RequestHelper;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {
	
	@Bean
	public FilterRegistrationBean corsFilterBean(
			CORSFilter corsFilter) {

		FilterRegistrationBean corsFilterRegistrationBean = new FilterRegistrationBean();

		corsFilterRegistrationBean.setFilter(corsFilter);

		corsFilterRegistrationBean.addUrlPatterns("/*");

		corsFilterRegistrationBean.setEnabled(true);

		return corsFilterRegistrationBean;
	}
	
	@Bean
	public CORSFilter corsFilter() {
		return new CORSFilter();
	}
	
	@Bean
	public RequestHelper requestHelper() {
		return new RequestHelper();
	}
	
}
