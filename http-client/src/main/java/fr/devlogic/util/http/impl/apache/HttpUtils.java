package fr.devlogic.util.http.impl.apache;

import fr.devlogic.util.http.ContentType;
import fr.devlogic.util.http.MediaType;
import fr.devlogic.util.http.annotation.Nullable;
import fr.devlogic.util.http.exception.HttpRuntimeException;
import fr.devlogic.util.http.exception.IORuntimeException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HttpUtils {
    private HttpUtils() {
    }

    public static String encodeBasicAuth(String username, String password, @Nullable Charset charset) {
        if (charset == null) {
            charset = StandardCharsets.ISO_8859_1;
        }

        String credentialsString = username + ":" + password;
        byte[] encodedBytes = Base64.getEncoder().encode(credentialsString.getBytes(charset));
        return new String(encodedBytes, charset);
    }

    public static <O extends OutputStream> O copyStream(InputStream is, O os) throws IOException {
        byte[] buffer = new byte[1024];

        for(;;) {
            int read = is.read(buffer);
            if(read<0) {
                break;
            }

            if(read>0) {
                os.write(buffer, 0, read);
            }
        }

        try {
            is.close();
        } catch (IOException ex) {
            throw new IORuntimeException(ex);
        }

        return os;
    }

    private static final Pattern PATH_PARAM_PATTERN = Pattern.compile("(\\{[^{]+})");

    static String mapPathParamsKeyValue(String uri, Map<String, Object> map) {
        Matcher matcher = PATH_PARAM_PATTERN.matcher(uri);
        if (matcher.find()) {
            int nbGroups = matcher.groupCount();
            for (int i = 0; i < nbGroups; i++) {
                String group = matcher.group(i + 1);
                String name = group.substring(1, group.length() - 1);
                if (map.containsKey(name)) {
                    uri = uri.replace(group, map.get(name).toString());
                }
            }
        }

        return uri;
    }

    static String mapPathParamsValues(String uri, Object... values) {
        Matcher matcher = PATH_PARAM_PATTERN.matcher(uri);
        int j = 0;
        while (matcher.find()) {
            int nbGroups = matcher.groupCount();
            for (int i = 0; i < nbGroups && j < values.length; i++, j++) {
                String group = matcher.group(i + 1);
                uri = uri.replace(group, values[j].toString());
            }
        }

        return uri;
    }

    public static Reader getReader(InputStream is, ContentType contentType) {
        Charset encoding = contentType.getEncoding();
        return encoding!=null ? new InputStreamReader(is, encoding) : new InputStreamReader(is);
    }

    private static String CHARSET = "charset";

    public static ContentType getContentType(String contentType) {
        return getContentType(contentType, null);
    }

    public static ContentType getContentType(String contentType, @Nullable Charset defaultEncoding) {
        MediaType mediaType = null;
        Charset encoding = null;
        if (contentType != null) {
            String[] parts = contentType.split(";"); // mediaType;encoding
            mediaType = MediaType.getMediaType(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                String str = parts[i];
                str = str.trim().toLowerCase();
                if (str.contains(CHARSET)) {
                    int index = str.indexOf('=');
                    if(index==-1) {
                        throw new HttpRuntimeException( str + " should contains =");
                    }
                    str = str.substring(index+1).trim();
                    if (str.startsWith("\"") || str.startsWith("'")) {
                        str = str.substring(1, str.length() - 1);
                    }
                    encoding = Charset.forName(str);
                    break;
                }
            }
        }

        if ((mediaType == null) && (contentType != null && contentType.length() > 0)) {
            throw new HttpRuntimeException(String.format("Cannot get media type from %s", contentType));
        }

        if (encoding == null && mediaType!=null && Boolean.TRUE.equals(MediaTypeEncoding.getEncoding(mediaType))) {
            encoding = defaultEncoding;
        }

        return new ContentType(mediaType, encoding);
    }

    public static Charset getDefaultEncoding() {
        String property = System.getProperty("file.encoding");
        return (property != null) ? Charset.forName(property) : Charset.defaultCharset();
    }

    public static org.apache.http.entity.ContentType convertInternalContentType(ContentType contentType) {
        org.apache.http.entity.ContentType httpContentType = org.apache.http.entity.ContentType.create(contentType.getMediaType().toString());
        Charset encoding = contentType.getEncoding();
        if(encoding!=null) {
            httpContentType = httpContentType.withCharset(encoding);
        }

        return httpContentType;
    }
}
