package fr.devlogic.util.http.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpRequestTemplateConfiguration {

    @Bean(name = "httpRequestTemplate")
    public HttpRequestTemplateFactory httpRequestTemplateFactory() {
        return new HttpRequestTemplateFactory();
    }
}
