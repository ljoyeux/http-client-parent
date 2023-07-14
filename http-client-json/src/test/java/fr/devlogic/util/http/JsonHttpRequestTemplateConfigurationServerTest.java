package fr.devlogic.util.http;

import com.fasterxml.jackson.databind.JsonNode;
import fr.devlogic.util.http.impl.apache.JsonHandler;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.mock.Expectation;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.socket.PortFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static fr.devlogic.util.http.Commons.*;

public class JsonHttpRequestTemplateConfigurationServerTest {

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

        Expectation[] respond = clientAndServer.when(HttpRequest.request("/json")).respond(HttpResponse.response("0").withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString()));

        String content = httpRequestTemplate.get(url + "/json").getContent(String.class);
        Assertions.assertEquals("0", content);
    }

    @Test
    public void encodingTest() {
        HttpRequestTemplate httpRequestTemplate = factory.getObject();
        httpRequestTemplate.accept(MediaType.APPLICATION_JSON);

        clientAndServer.when(HttpRequest.request("/encoding"))
                .respond(HttpResponse.response().withBody(ACCENTS_ISO_8859_1).withHeader(HttpHeaders.CONTENT_TYPE, new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.ISO_8859_1).toString()));

        String content = httpRequestTemplate.get(url + "/encoding").getContent(String.class);
        Assertions.assertEquals(ACCENTS, content);

        // bon encodage
        String jsonString = "\"" + ACCENTS + "\"";
        clientAndServer.when(HttpRequest.request("/json-encoding"))
                .respond(HttpResponse.response().withBody(jsonString.getBytes(StandardCharsets.ISO_8859_1)).withHeader(HttpHeaders.CONTENT_TYPE, new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.ISO_8859_1).toString()));

        JsonNode jsonNode = httpRequestTemplate.get(url + "/json-encoding").getContent(JsonNode.class);
        Assertions.assertEquals(ACCENTS, jsonNode.asText());

        // mauvais encodage
        clientAndServer.when(HttpRequest.request("/json-encoding-fail"))
                .respond(HttpResponse.response().withBody(jsonString.getBytes(StandardCharsets.ISO_8859_1)).withHeader(HttpHeaders.CONTENT_TYPE, new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8).toString()));

        JsonNode jsonNodeFail = httpRequestTemplate.get(url + "/json-encoding-fail").getContent(JsonNode.class);
        Assertions.assertNotEquals(ACCENTS, jsonNodeFail.asText());
    }

    @Test
    public void encodeModel() throws IOException {
        HttpRequestTemplate httpRequestTemplate = factory.getObject();
        httpRequestTemplate.accept(MediaType.APPLICATION_JSON);

        JsonHandler jsonHandler = new JsonHandler();

        Model model = new Model();
        model.setAccents(ACCENTS);

        jsonHandler.writeContent(model, new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.ISO_8859_1));
        HttpEntity httpEntity = jsonHandler.getHttpEntity();
        byte[] bytesISO8859 = EntityUtils.toByteArray(httpEntity);
        Assertions.assertTrue(find(ACCENTS_ISO_8859_1, bytesISO8859));
        Assertions.assertFalse(find(ACCENTS_UTF8, bytesISO8859));

        jsonHandler.writeContent(model, new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        httpEntity = jsonHandler.getHttpEntity();
        byte[] bytesUTF8 = EntityUtils.toByteArray(httpEntity);
        Assertions.assertFalse(find(ACCENTS_ISO_8859_1, bytesUTF8));
        Assertions.assertTrue(find(ACCENTS_UTF8, bytesUTF8));


        clientAndServer.when(HttpRequest.request("/model-iso8859"))
                .respond(HttpResponse.response().withBody(bytesISO8859).withHeader(HttpHeaders.CONTENT_TYPE, new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.ISO_8859_1).toString()));
        clientAndServer.when(HttpRequest.request("/model-utf8"))
                .respond(HttpResponse.response().withBody(bytesUTF8).withHeader(HttpHeaders.CONTENT_TYPE, new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8).toString()));

        Model contentISO8859 = httpRequestTemplate.get(url + "/model-iso8859").getContent(Model.class);
        Assertions.assertEquals(model, contentISO8859);

        Model contentUTF8 = httpRequestTemplate.get(url + "/model-utf8").getContent(Model.class);
        Assertions.assertEquals(model, contentUTF8);

        Model contentJavaTypeISO8859 = httpRequestTemplate.get(url + "/model-iso8859").getContent(new GenericType<Model>() {
        });
        Assertions.assertEquals(model, contentJavaTypeISO8859);
    }


    @AfterAll
    public static void stop() {
        clientAndServer.stop();
    }
}
