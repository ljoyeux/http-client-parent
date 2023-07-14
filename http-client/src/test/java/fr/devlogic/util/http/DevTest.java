package fr.devlogic.util.http;

import fr.devlogic.util.http.impl.apache.ApacheHttpRequestTemplate;
import fr.devlogic.util.http.impl.apache.HttpUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;

public class DevTest {
    private static HttpRequestTemplateFactory factory;

    @BeforeAll
    public static void init() {
        factory = new HttpRequestTemplateFactory();
    }

    @Test
    public void url() {
        HttpRequestTemplate httpRequestTemplate = factory.getObject();
        Assertions.assertEquals("http://localhost:8080/path/12345", httpRequestTemplate.url("http://localhost:8080/path/{id}").pathParam("id", "12345").url());
        Assertions.assertEquals("http://localhost:8080/path/12345", httpRequestTemplate.url("http://localhost:8080/path/{id}").pathParam("12345").url());

        Assertions.assertEquals("http://localhost:8080/path/qwerty/12345", httpRequestTemplate.url("http://localhost:8080/path/{p1}/{p2}").pathParam("qwerty").pathParam("12345").url());

        Assertions.assertEquals("http://localhost:8080/path/qwerty/12345?user=login&password=mdp", httpRequestTemplate.url("http://localhost:8080/path/{p1}/{p2}")
                .pathParam("qwerty").pathParam("12345")
                .queryParam("user", "login").queryParam("password", "mdp").url());

        String pathParam = "sdgegtgrw";
        String url = httpRequestTemplate.url("http://localhost:8080/path/1887-789/{service-habilitation-panier}/user").pathParam(pathParam).url();
        Assertions.assertEquals("http://localhost:8080/path/1887-789/" + pathParam + "/user", url);
        System.out.println(url);
    }

    @Test
    public void constructor() throws NoSuchMethodException {
        Class<ApacheHttpRequestTemplate> requestTemplateClass = ApacheHttpRequestTemplate.class;
        Constructor<ApacheHttpRequestTemplate> declaredConstructor = requestTemplateClass.getDeclaredConstructor(requestTemplateClass);

        Assertions.assertNotNull(declaredConstructor);
    }

    @Test
    public void contentType() {
        Assertions.assertEquals(new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8), HttpUtils.getContentType("application/json; charset=utf-8"));
        Assertions.assertEquals(new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8), HttpUtils.getContentType("application/json; charset='utf-8'"));
        Assertions.assertEquals(new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8), HttpUtils.getContentType("application/json; charset=\"utf-8\""));
        Assertions.assertEquals(new ContentType(MediaType.TEXT_XML, null), HttpUtils.getContentType("text/xml"));

        Assertions.assertEquals("application/json; charset=UTF-8", new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8).toString());
        Assertions.assertEquals("text/xml", new ContentType(MediaType.TEXT_XML, null).toString());

        Assertions.assertEquals(new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8), HttpUtils.getContentType("application/json; charset =  \"utf-8\" "));
        Assertions.assertEquals(new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8), HttpUtils.getContentType("application/json; coucou; charset =  \"utf-8\" "));

    }

    @Test
    public void encoding() {
        String str = "àéèù";
        byte[] bytesUTF8 = str.getBytes(StandardCharsets.UTF_8);
        byte[] bytesWindows = str.getBytes(StandardCharsets.ISO_8859_1);
        try {
            Assertions.assertArrayEquals(bytesUTF8, bytesWindows);
            Assertions.fail();
        } catch (AssertionFailedError ex) {
            // ok
        }
        System.out.println("utf8 " + bytesUTF8.length + " dump \t\t" + dump(bytesUTF8));
        System.out.println("windows " + bytesWindows.length + " dump \t\t" + dump(bytesWindows));

        String s = new String(bytesWindows, StandardCharsets.ISO_8859_1);

        Assertions.assertEquals(str, s);
    }

    private static String dump(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder();
        for(byte b: data) {
            stringBuilder.append(String.format("%02x ", b));
        }

        return stringBuilder.toString();
    }
}
