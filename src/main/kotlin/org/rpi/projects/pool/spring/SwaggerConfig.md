//package org.rpi.projects.pool.spring
//
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.context.annotation.Profile
//import org.springframework.http.HttpHeaders.AUTHORIZATION
//import org.springframework.web.reactive.config.WebFluxConfigurer
//import springfox.documentation.builders.ApiInfoBuilder
//import springfox.documentation.builders.PathSelectors
//import springfox.documentation.builders.RequestHandlerSelectors
//import springfox.documentation.service.ApiKey
//import springfox.documentation.service.AuthorizationScope
//import springfox.documentation.service.Contact
//import springfox.documentation.service.SecurityReference
//import springfox.documentation.spi.DocumentationType
//import springfox.documentation.spi.service.contexts.SecurityContext
//import springfox.documentation.spring.web.plugins.Docket
//import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux
//import java.util.*
//import org.springframework.web.reactive.config.ResourceHandlerRegistry
//
//
//
//
//@Configuration
//@EnableSwagger2WebFlux
//@Profile("swagger")
//class SwaggerConfig(@Value("\${spring.application.name}") val name: String): WebFluxConfigurer {
//
//    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
//        registry.addResourceHandler("/swagger-ui.html**")
//                .addResourceLocations("classpath:/META-INF/resources/")
//
//        registry.addResourceHandler("/webjars/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/")
//    }
//
//    @Bean
//    fun documentation(): Docket {
//        return Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(metadata())
//                .groupName(name)
//                .select()
//                .apis(RequestHandlerSelectors.basePackage(""))
//                .build()
//                .useDefaultResponseMessages(false)
//                .securitySchemes(listOf(ApiKey(AUTHORIZATION, AUTHORIZATION, "header")))
//                .securityContexts(listOf(SecurityContext.builder()
//                        .securityReferences(listOf(SecurityReference(AUTHORIZATION, arrayOf(AuthorizationScope("global", "accessEverything")))))
//                        .forPaths(PathSelectors.any())
//                        .build()))
//                .genericModelSubstitutes(Optional::class.java)
//    }
//
//    private fun metadata() = ApiInfoBuilder()
//            .title(name)
//            .contact(Contact("MJG", "", "mehdi.jaqir.pro@gmail.com"))
//            .version("1.0")
//            .build()
//}
//
//


//    implementation("io.springfox:springfox-swagger2:${project.extra["swaggerVersion"]}")
//    implementation("io.springfox:springfox-spring-webflux:${project.extra["swaggerVersion"]}")
//    implementation("io.springfox:springfox-swagger-ui:${project.extra["swaggerVersion"]}")
