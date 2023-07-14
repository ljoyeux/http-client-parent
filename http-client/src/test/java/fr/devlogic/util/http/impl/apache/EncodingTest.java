package fr.devlogic.util.http.impl.apache;

import fr.devlogic.util.http.Commons;
import fr.devlogic.util.http.ContentType;
import fr.devlogic.util.http.MediaType;
import fr.devlogic.util.http.part.Part;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class EncodingTest {

    private static final String ACCENTS = "éàù";
    private static final byte[] ACCENTS_UTF8 = new byte[]{(byte)0xc3, (byte)0xa9, (byte)0xc3, (byte)0xa0, (byte)0xc3, (byte)0xb9};
    private static final byte[] ACCENTS_ISO_8859_1 = new byte[]{(byte) 0xE9,(byte) 0xE0, (byte) 0xf9};

    @Test
    public void encoding() {
        byte[] lettersISo8859 = ACCENTS.getBytes(StandardCharsets.ISO_8859_1);
        Assertions.assertArrayEquals(ACCENTS_UTF8, ACCENTS.getBytes(), "Echoue si le projet n'est pas compilé en utf-8");
        Assertions.assertArrayEquals(ACCENTS_ISO_8859_1, lettersISo8859);

        String strISO8859 = new String(ACCENTS.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1);
        char c = strISO8859.charAt(0);
        Assertions.assertEquals(0xe9, c);
        byte[] bytes = strISO8859.getBytes(StandardCharsets.ISO_8859_1); // strISO8859.getBytes() fail
        Assertions.assertEquals(0xe9, 0xff & strISO8859.getBytes(StandardCharsets.ISO_8859_1)[0]);
        Assertions.assertEquals(0xc3, 0xff & strISO8859.getBytes()[0]);
    }


    @Test
    public void getEncoding() {
        String property = System.getProperty("file.encoding");
        Charset defaultCharset = Charset.defaultCharset();

        System.out.printf("file.encoding %s default %s%n", Charset.forName(property), defaultCharset);
        Assertions.assertEquals(HttpUtils.getDefaultEncoding(), StandardCharsets.UTF_8);
    }

    @Test
    public void writeContent() throws IOException {
        TextHandler textHandler = new TextHandler();
        textHandler.writeContent(ACCENTS, new ContentType(MediaType.TEXT_PLAIN, StandardCharsets.ISO_8859_1));
        HttpEntity httpEntity = textHandler.getHttpEntity();
        Assertions.assertArrayEquals(ACCENTS_ISO_8859_1, EntityUtils.toByteArray(httpEntity));

        textHandler.writeContent(ACCENTS, new ContentType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8));
        httpEntity = textHandler.getHttpEntity();
        byte[] bytes = EntityUtils.toByteArray(httpEntity);
        Assertions.assertArrayEquals(ACCENTS_UTF8, bytes);
        System.out.println(new String(bytes));


        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

        Part part = new Part("json", ACCENTS, new ContentType(MediaType.TEXT_PLAIN, StandardCharsets.ISO_8859_1));
        textHandler.setMultipartEntityBuilder(multipartEntityBuilder);
        textHandler.writeContent(part);
        HttpEntity multipartEntity = multipartEntityBuilder.build();
        bytes = EntityUtils.toByteArray(multipartEntity);
        Assertions.assertTrue(Commons.find(ACCENTS_ISO_8859_1, bytes));
        Assertions.assertFalse(Commons.find(ACCENTS_UTF8, bytes));
        String str = new String(bytes, StandardCharsets.ISO_8859_1);
        Assertions.assertTrue(str.toLowerCase().contains(StandardCharsets.ISO_8859_1.toString().toLowerCase()));
        System.out.println(str);
    }

}
