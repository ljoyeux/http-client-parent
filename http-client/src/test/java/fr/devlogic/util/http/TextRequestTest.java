package fr.devlogic.util.http;

import fr.devlogic.util.http.HttpRequestTemplateFactory;
import fr.devlogic.util.http.MediaType;
import fr.devlogic.util.http.HttpResponse;
import fr.devlogic.util.http.HttpRequestTemplate;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.socket.PortFactory;

public class TextRequestTest {
    private static ClientAndServer clientAndServer;
    private static String url;
    private static HttpRequestTemplateFactory factory;

    @BeforeAll
    public static void init() {
        clientAndServer = ClientAndServer.startClientAndServer(PortFactory.findFreePort());
        url = String.format("http://localhost:%d", clientAndServer.getPort());
        factory = new HttpRequestTemplateFactory();
    }

    @Test
    public void simpleTest() {
        HttpRequestTemplate httpRequestTemplate = factory.getObject();
        httpRequestTemplate.accept(MediaType.APPLICATION_JSON);
        HttpRequest request = HttpRequest.request("/text");

        clientAndServer.when(request)
                .respond(org.mockserver.model.HttpResponse.response("0").withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN.toString()));

        HttpResponse httpResponse = httpRequestTemplate.get(url + "/text");

        Assertions.assertEquals(MediaType.TEXT_PLAIN, httpResponse.contentType().getMediaType());
        String content = httpResponse.getContent(String.class);
        Assertions.assertEquals("0", content);

        clientAndServer.clear(request);

    }

    @AfterAll
    public static void stop() {
        clientAndServer.stop();
    }
}
