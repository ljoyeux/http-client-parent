package fr.devlogic.util.http;

import fr.devlogic.util.http.spring.HttpRequestTemplateConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {HttpRequestTemplateConfiguration.class, ConfRequestTemplate.class})
public class HttpHttpRequestTemplateConfigurationTest {
    @Autowired
    HttpRequestTemplate httpRequestTemplate;

    @Autowired
    @Qualifier("request-template-client")
    HttpRequestTemplate httpRequestTemplateClient;

    @Test
    public void notNull() {
        Assertions.assertNotNull(httpRequestTemplate);
        Assertions.assertNotNull(httpRequestTemplateClient);
        Assertions.assertNotEquals(httpRequestTemplate, httpRequestTemplateClient);
    }
}
