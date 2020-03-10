package net.fluance.security.ehprofile.config;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.Predicate;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@Configuration
@EnableSwagger2
@ConditionalOnProperty(name = "springfox.swagger.enable", havingValue = "true", matchIfMissing = false)
public class SwaggerConfig {

	@Bean
	public Docket api() {
		List<ResponseMessage> statusCodes = new ArrayList<>();
		statusCodes.add(new ResponseMessageBuilder().code(HttpStatus.NO_CONTENT.value()).message(HttpStatus.NO_CONTENT.getReasonPhrase()).build());
		statusCodes.add(new ResponseMessageBuilder().code(HttpStatus.BAD_REQUEST.value()).message(HttpStatus.BAD_REQUEST.getReasonPhrase()).build());
		statusCodes.add(new ResponseMessageBuilder().code(HttpStatus.UNAUTHORIZED.value()).message(HttpStatus.UNAUTHORIZED.getReasonPhrase()).build());
		statusCodes.add(new ResponseMessageBuilder().code(HttpStatus.FORBIDDEN.value()).message(HttpStatus.FORBIDDEN.getReasonPhrase()).build());
		statusCodes.add(new ResponseMessageBuilder().code(HttpStatus.NOT_FOUND.value()).message(HttpStatus.NOT_FOUND.getReasonPhrase()).build());
		statusCodes.add(new ResponseMessageBuilder().code(HttpStatus.UNPROCESSABLE_ENTITY.value()).message(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()).build());
		statusCodes.add(new ResponseMessageBuilder().code(HttpStatus.INTERNAL_SERVER_ERROR.value()).message(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()).build());
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(new ApiInfo("Middleware APIs", "Available Security API's Specifications", "2.0.0", "© 2016 Fluance", "Fluance Middleware", "© 2016 Fluance", "http://fluance.net/"))
				.useDefaultResponseMessages(true) 
				.globalResponseMessage(RequestMethod.GET, statusCodes)
				.select().apis(RequestHandlerSelectors.any()).paths(paths()).build();
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> paths() {
		return or(regex("/profile/.*"), regex("/oauth2/.*"), regex("/jwt/.*"), regex("/authenticate.*"), regex("/dataproviders"), regex("/IAM.*"), regex("/userdata.*"));
	}
	
}