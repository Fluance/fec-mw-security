/**
 * 
 */
package net.fluance.security.ehprofile.config;

import javax.servlet.DispatcherType;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import net.fluance.app.web.servlet.filter.BasicLoggingInterceptor;
import net.fluance.app.web.servlet.filter.CORSFilter;
import net.fluance.app.web.util.RequestHelper;
import net.fluance.security.ehprofile.web.filter.EntitelmentFilter;

/**
 *
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

	@Bean
	public FilterRegistrationBean entitelmentFilterRegistrationBean(EntitelmentFilter entitlementFilter) {
		FilterRegistrationBean userProfileEntitlementFilterRegistrationBean = new FilterRegistrationBean();
		userProfileEntitlementFilterRegistrationBean.setFilter(entitlementFilter);
		userProfileEntitlementFilterRegistrationBean.addUrlPatterns("/profile/*", "/identity/*", "/IAM/*", "/userdata/*");
		userProfileEntitlementFilterRegistrationBean.setEnabled(true);
		userProfileEntitlementFilterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD);
		return userProfileEntitlementFilterRegistrationBean;
	}

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
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
	    registry.addInterceptor(new BasicLoggingInterceptor());
	}
}
