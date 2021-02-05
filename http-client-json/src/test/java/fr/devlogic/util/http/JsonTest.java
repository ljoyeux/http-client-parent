package fr.devlogic.util.http;

import fr.devlogic.util.http.json.impl.JsonHandler;
import fr.devlogic.util.http.part.Part;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JsonTest {

    @Test
    public void writeContent() throws IOException {
        JsonHandler jsonHandler = new JsonHandler();
        HttpEntity httpEntity = jsonHandler.writeContent(Commons.ACCENTS, new ContentType(MediaType.TEXT_PLAIN, StandardCharsets.ISO_8859_1));
        byte[] bytes = EntityUtils.toByteArray(httpEntity);
        System.out.println(Commons.dump(bytes));
        Assertions.assertArrayEquals(Commons.ACCENTS_ISO_8859_1, Commons.subArray(bytes, 1, bytes.length - 1));
        Assertions.assertEquals('"', bytes[0]);
        Assertions.assertEquals('"', bytes[bytes.length - 1]);

        httpEntity = jsonHandler.writeContent(Commons.ACCENTS, new ContentType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8));
        bytes = EntityUtils.toByteArray(httpEntity);
        Assertions.assertArrayEquals(Commons.ACCENTS_UTF8, Commons.subArray(bytes, 1, bytes.length - 1));
        Assertions.assertEquals('"', bytes[0]);
        Assertions.assertEquals('"', bytes[bytes.length - 1]);

    }

    @Test
    public void writeObject() throws IOException {
        JsonHandler jsonHandler = new JsonHandler();
        Commons.Model model = new Commons.Model();
        model.setAccents(Commons.ACCENTS);

        HttpEntity httpEntity = jsonHandler.writeContent(model, new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.ISO_8859_1));
        byte[] bytes = EntityUtils.toByteArray(httpEntity);
        Assertions.assertTrue(Commons.find(Commons.ACCENTS_ISO_8859_1, bytes));
        Assertions.assertFalse(Commons.find(Commons.ACCENTS_UTF8, bytes));

        httpEntity = jsonHandler.writeContent(model, new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        bytes = EntityUtils.toByteArray(httpEntity);
        Assertions.assertFalse(Commons.find(Commons.ACCENTS_ISO_8859_1, bytes));
        Assertions.assertTrue(Commons.find(Commons.ACCENTS_UTF8, bytes));
    }

    @Test
    public void multipartISO8859() throws IOException {
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

        Part part = new Part("json", Commons.ACCENTS, new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.ISO_8859_1));
        JsonHandler jsonHandler = new JsonHandler();
        jsonHandler.writeContent(multipartEntityBuilder, part);

        HttpEntity entity = multipartEntityBuilder.build();
        byte[] bytes = EntityUtils.toByteArray(entity);
        Assertions.assertTrue(Commons.find(Commons.ACCENTS_ISO_8859_1, bytes));
        Assertions.assertFalse(Commons.find(Commons.ACCENTS_UTF8, bytes));
    }

    @Test
    public void multipartUTF8() throws IOException {
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

        Part part = new Part("json", Commons.ACCENTS, new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        JsonHandler jsonHandler = new JsonHandler();
        jsonHandler.writeContent(multipartEntityBuilder, part);

        HttpEntity entity = multipartEntityBuilder.build();
        byte[] bytes = EntityUtils.toByteArray(entity);
        Assertions.assertTrue(Commons.find(Commons.ACCENTS_UTF8, bytes));
        Assertions.assertFalse(Commons.find(Commons.ACCENTS_ISO_8859_1, bytes));
    }

    @Test
    public void multipartModelUTF8() throws IOException {
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        Commons.Model model = new Commons.Model();
        model.setAccents(Commons.ACCENTS);

        Part part = new Part("json", model, new ContentType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        JsonHandler jsonHandler = new JsonHandler();
        jsonHandler.writeContent(multipartEntityBuilder, part);

        HttpEntity entity = multipartEntityBuilder.build();
        byte[] bytes = EntityUtils.toByteArray(entity);
        Assertions.assertTrue(Commons.find(Commons.ACCENTS_UTF8, bytes));
        Assertions.assertFalse(Commons.find(Commons.ACCENTS_ISO_8859_1, bytes));
        System.out.println(new String(bytes, StandardCharsets.UTF_8));
    }
}
