package fr.devlogic.util.http.impl.apache;

import fr.devlogic.util.http.MediaType;
import fr.devlogic.util.http.exception.IORuntimeException;
import fr.devlogic.util.http.part.Part;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.Set;

import static fr.devlogic.util.http.MediaType.TEXT_HTML;
import static fr.devlogic.util.http.MediaType.TEXT_PLAIN;

public class TextHandler extends ApacheHttpMediaTypeProcessor {
    @Override
    public Set<MediaType> handledMediaTypes() {
        return EnumSet.of(TEXT_PLAIN, TEXT_HTML);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getContent(InputStream is, fr.devlogic.util.http.ContentType contentType, Class<T> c) {
        if (String.class.isAssignableFrom(c)) {
            try {
                ByteArrayOutputStream byteArrayOutputStream = HttpUtils.copyStream(is, new ByteArrayOutputStream());
                Charset encoding = contentType.getEncoding();
                return (T) ((encoding != null) ? byteArrayOutputStream.toString(encoding.toString()) : byteArrayOutputStream.toString());
            } catch (IOException ex) {
                throw new IORuntimeException(ex);
            }
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public <C> C getContent(InputStream is, fr.devlogic.util.http.ContentType contentType, Type t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeContent(Object content, fr.devlogic.util.http.ContentType contentType) {
        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        if (content != null) {
            Charset encoding = contentType.getEncoding();
            String str = content.toString();
            if (encoding != null) {
                basicHttpEntity.setContentEncoding(encoding.toString());
            }
            byte[] data = (encoding != null) ? str.getBytes(encoding) : str.getBytes();

            basicHttpEntity.setContentLength(data.length);
            basicHttpEntity.setContent(new ByteArrayInputStream(data));
        }

        basicHttpEntity.setContentType(contentType.toString());
        setHttpEntity(basicHttpEntity);
    }

    @Override
    public void writeContent(Part part) {
        MultipartEntityBuilder multipartEntityBuilder = getMultipartEntityBuilder();
        Object content = part.getContent();
        ContentType contentType = HttpUtils.convertInternalContentType(part.getContentType());

        if (content instanceof String) {
            multipartEntityBuilder.addTextBody(part.getName(), (String) content, contentType);
        } else if (content != null) {
            multipartEntityBuilder.addTextBody(part.getName(), content.toString(), contentType);
        }
    }
}
