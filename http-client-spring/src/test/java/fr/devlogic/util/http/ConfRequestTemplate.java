package fr.devlogic.util.http;

import fr.devlogic.util.http.spring.HttpRequestTemplateFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfRequestTemplate {
    @Bean(name = "request-template-client")
    public HttpRequestTemplate httpRequestTemplateClient(HttpRequestTemplateFactory factory) {
        return factory.getObject();
    }
}
