package fr.devlogic.util.http.json.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import fr.devlogic.util.http.impl.HttpUtils;
import fr.devlogic.util.http.MediaType;
import fr.devlogic.util.http.MediaTypeProcessor;
import fr.devlogic.util.http.part.Part;
import fr.devlogic.util.http.exception.HttpRuntimeException;
import fr.devlogic.util.http.exception.IORuntimeException;
import org.apache.http.HttpEntity;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.Set;

/**
 * Implémentation de la lecture et l'écriture au format json {@link MediaType#APPLICATION_JSON}
 */
public class JsonHandler implements MediaTypeProcessor {
    @Override
    public Set<MediaType> handledMediaTypes() {
        return EnumSet.of(MediaType.APPLICATION_JSON);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getContent(InputStream contentStream, fr.devlogic.util.http.ContentType contentType, Class<T> c) {
        T object;
        try {
            if (contentStream == null) {
                return null;
            }
            if (String.class.isAssignableFrom(c)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                HttpUtils.copyStream(contentStream, baos);
                Charset encoding = contentType.getEncoding();
                return (T) ((encoding != null) ? baos.toString(encoding.name()) : baos.toString());
            } else if (JsonNode.class.isAssignableFrom(c)) {
                return (T) JsonUtils.OBJECT_MAPPER.readTree(HttpUtils.getReader(contentStream, contentType));
            }

            object = JsonUtils.OBJECT_MAPPER.readValue(HttpUtils.getReader(contentStream, contentType), c);
            contentStream.close();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }

        return object;

    }

    @Override
    public <C> C getContent(InputStream contentStream, fr.devlogic.util.http.ContentType contentType, Type t) {
        if (!(t instanceof JavaType)) {
            t = JsonUtils.getType(t);
        }

        try {
            if (contentStream == null) {
                return null;
            }

            C object = JsonUtils.OBJECT_MAPPER.readValue(HttpUtils.getReader(contentStream, contentType), (JavaType) t);
            contentStream.close();

            return object;
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    @Override
    public HttpEntity writeContent(Object content, fr.devlogic.util.http.ContentType contentType) {
        ContentType httpContentType = HttpUtils.convertInternalContentType(contentType);

        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        try {
            if (content != null) {
                String str = JsonUtils.OBJECT_MAPPER.writeValueAsString(content);
                Charset encoding = contentType.getEncoding();
                byte[] data = (encoding != null) ? str.getBytes(encoding) : str.getBytes();

                basicHttpEntity.setContent(new ByteArrayInputStream(data));
            }
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }

        basicHttpEntity.setContentType(httpContentType.toString());

        return basicHttpEntity;
    }

    @Override
    public void writeContent(MultipartEntityBuilder multipartEntityBuilder, Part part) {
        Object content = part.getContent();
        ContentType contentType = HttpUtils.convertInternalContentType(part.getContentType());

        if (content instanceof String) {
            multipartEntityBuilder.addTextBody(part.getName(), (String) content, contentType);
        } else if (content instanceof JsonNode) {
            multipartEntityBuilder.addTextBody(part.getName(), ((JsonNode) content).asText(), contentType);
        } else if (content == null) {
            multipartEntityBuilder.addTextBody(part.getName(), null, contentType);
        } else {
            try {
                multipartEntityBuilder.addTextBody(part.getName(), JsonUtils.OBJECT_MAPPER.writeValueAsString(content), contentType);
            } catch (JsonProcessingException ex) {
                throw new HttpRuntimeException(ex);
            }
        }
    }
}
