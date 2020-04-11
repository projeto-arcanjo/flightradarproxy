package br.com.cmabreu;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig extends WebMvcConfigurationSupport {
    
	@Value("${spring.application.name}")
    String applicationName;	
    
	@Bean
	public Docket productApi() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select().apis(RequestHandlerSelectors.basePackage("br.com.cmabreu.controller") )
				.paths( PathSelectors.any() )
				.build()
				.apiInfo( this.informacoesApi().build() );

	}


	private ApiInfoBuilder informacoesApi() {
 
		ApiInfoBuilder apiInfoBuilder = new ApiInfoBuilder();
 
		apiInfoBuilder.title("X-Plane Proxy");
		apiInfoBuilder.description("Proxy HLA para o X-Plane");
		apiInfoBuilder.version("1.0");
		apiInfoBuilder.termsOfServiceUrl( "" );
		apiInfoBuilder.license("Carlos Magno Abreu");
		apiInfoBuilder.licenseUrl("");
		apiInfoBuilder.contact( this.contato() );
 
		return apiInfoBuilder;
 
	}
	
	
	private Contact contato() {
 		return new Contact(	"Carlos Magno Abreu", "http://www.cmabreu.com.br/", "");	
	}
	
	
	@Override
	protected void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("swagger-ui.html")
		.addResourceLocations("classpath:/META-INF/resources/");

		registry.addResourceHandler("/webjars/**")
		.addResourceLocations("classpath:/META-INF/resources/webjars/");
	}	
	

}
