package com.roominspection.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Swagger API文档配置
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket apiV1() {
        return createDocket("API v1", "机房巡检系统 API v1", "v1");
    }

    @Bean
    public Docket apiV2() {
        return createDocket("API v2", "机房巡检系统 API v2（开发中）", "v2");
    }

    private Docket createDocket(String groupName, String description, String version) {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName(groupName)
                .apiInfo(apiInfo(description))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.roominspection.backend.controller"))
                .paths(PathSelectors.ant("/api/" + version + "/**"))
                .build()
                .protocols(new HashSet<>(Arrays.asList("http", "https")))
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts())
                .enableUrlTemplating(true);
    }

    private ApiInfo apiInfo(String description) {
        return new ApiInfoBuilder()
                .title("机房巡检系统 API文档")
                .description(description)
                .version("1.0.0")
                .contact(new Contact("技术支持", "http://www.roominspection.com", "support@roominspection.com"))
                .license("Apache 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0")
                .build();
    }

    private List<ApiKey> securitySchemes() {
        List<ApiKey> apiKeys = new ArrayList<>();
        // Token认证
        apiKeys.add(new ApiKey("Token", "Authorization", "header"));
        // API密钥认证
        apiKeys.add(new ApiKey("AppID", "X-App-ID", "header"));
        apiKeys.add(new ApiKey("Signature", "X-Signature", "header"));
        apiKeys.add(new ApiKey("Timestamp", "X-Timestamp", "header"));
        return apiKeys;
    }

    private List<SecurityContext> securityContexts() {
        List<SecurityContext> contexts = new ArrayList<>();
        contexts.add(SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex("/api/.*"))
                .build());
        return contexts;
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        return Arrays.asList(
                new SecurityReference("Token", new AuthorizationScope[]{authorizationScope}),
                new SecurityReference("AppID", new AuthorizationScope[]{authorizationScope}),
                new SecurityReference("Signature", new AuthorizationScope[]{authorizationScope}),
                new SecurityReference("Timestamp", new AuthorizationScope[]{authorizationScope})
        );
    }
}
