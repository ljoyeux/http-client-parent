package fr.devlogic.util.http.impl.apache;

import fr.devlogic.util.http.*;
import fr.devlogic.util.http.annotation.Nullable;
import fr.devlogic.util.http.exception.HttpRuntimeException;
import fr.devlogic.util.http.exception.IORuntimeException;
import fr.devlogic.util.http.part.Part;
import fr.devlogic.util.http.part.PartFile;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static fr.devlogic.util.http.impl.apache.HttpUtils.getContentType;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;


/**
 * Classe qui exécute la réponse HTTP.
 */
public final class ApacheHttpHttpResponse implements HttpResponse {
    private static final Logger LOG = LoggerFactory.getLogger(ApacheHttpHttpResponse.class);

    private static final Map<MediaType, ApacheHttpMediaTypeProcessor> PROVIDERS;
    private static final String PREDICATE_IS_MISSING = "Predicate is missing";

    static {
        PROVIDERS = new EnumMap<>(MediaType.class);
        MediaTypeProcessorFatory.PROVIDERS.forEach((k, v) -> PROVIDERS.put(k, (ApacheHttpMediaTypeProcessor) (v.stream().filter(o -> ApacheHttpMediaTypeProcessor.class.isAssignableFrom(o.getClass())).findFirst()).get()));
    }

    private static final String CONTENT = "content";
    private CloseableHttpResponse closeableHttpResponse;
    private final CloseableHttpClient httpClient;
    private final HttpRequestBase requestBase;
    private final ApacheHttpRequestTemplate genericRequestTemplate;
    private Function<HttpResponse, ?> fct;
    private fr.devlogic.util.http.ContentType contentMediaType;
    private Predicate<HttpResponse> predicate;


    public ApacheHttpHttpResponse(ApacheHttpRequestTemplate requestTemplate) {
        this.genericRequestTemplate = requestTemplate;
        httpClient = requestTemplate.getHttpClient();
        switch (requestTemplate.method) {
            case GET:
                requestBase = buildNonEnclosing(new HttpGet(requestTemplate.url));
                break;
            case DELETE:
                requestBase = (genericRequestTemplate.content != null) ?
                        buildEnclosing(new HttpDeleteWithBody(requestTemplate.url)) :
                        buildNonEnclosing(new HttpDelete(requestTemplate.url));
                break;
            case POST:
                requestBase = buildEnclosing(new HttpPost(requestTemplate.url));
                break;
            case PUT:
                requestBase = buildEnclosing(new HttpPut(requestTemplate.url));
                break;
            case TRACE:
                requestBase = buildNonEnclosing(new HttpTrace(requestTemplate.url));
                break;
            default:
                throw new UnsupportedOperationException(requestTemplate.method.toString());
        }

        try {
            closeableHttpResponse = httpClient.execute(requestBase);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    private HttpEntityEnclosingRequestBase buildEnclosing(HttpEntityEnclosingRequestBase requestBase) {
        putHeaders(requestBase, (k, v) ->  !( k.equalsIgnoreCase(CONTENT_TYPE) && (v!=null) && v.toString().equalsIgnoreCase(MediaType.MULTIPART_FORM_DATA.toString())));
        HttpEntity entity = entity();
        requestBase.setEntity(entity);
        return requestBase;
    }

    private HttpRequestBase buildNonEnclosing(HttpRequestBase requestBase) {
        putHeaders(requestBase, (k, v) -> !k.toLowerCase().startsWith(CONTENT));
        return requestBase;
    }

    private void putHeaders(HttpRequestBase request, BiPredicate<String,Object> predicate) {
        genericRequestTemplate.headers.forEach((k, v) -> {
            if (predicate.test(k, v)) {
                request.addHeader(k, v.toString());
            }
        });
    }

    private HttpEntity entity() {
        Object objectContentType = genericRequestTemplate.headers.get(CONTENT_TYPE);
        fr.devlogic.util.http.ContentType contentType;
        if (objectContentType instanceof fr.devlogic.util.http.ContentType) {
            contentType = (fr.devlogic.util.http.ContentType) objectContentType;
        } else if (objectContentType instanceof MediaType) {
            contentType = new fr.devlogic.util.http.ContentType((MediaType) objectContentType, null);
        } else if (objectContentType instanceof String) {
            contentType = HttpUtils.getContentType((String) objectContentType);
        } else {
            throw new HttpRuntimeException("Cannot handle " + objectContentType);
        }

        Object content = genericRequestTemplate.content;

        MediaType mediaType = contentType.getMediaType();
        ApacheHttpMediaTypeProcessor mediaTypeProcessor = PROVIDERS.get(mediaType);
        if (mediaTypeProcessor != null) {
            mediaTypeProcessor.writeContent(content, contentType);
            return mediaTypeProcessor.getHttpEntity();
        }

        switch (mediaType) {

            case APPLICATION_FORM_URLENCODED:
                return getUrlEncodedFormEntity(contentType, content);

            case MULTIPART_FORM_DATA:
                return getMultipartFormDataEntity(content);

            default:
                throw new UnsupportedOperationException("Encoding not supported : " + objectContentType);
        }
    }

    private HttpEntity getMultipartFormDataEntity(Object content) {
        if (content instanceof List) {
            @SuppressWarnings("unchecked")
            List<Part> parts = (List<Part>) content;

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            for (Part part : parts) {
                MediaType partMediaType = part.getContentType().getMediaType();

                // comportement spécifique
                ApacheHttpMediaTypeProcessor partMediaTypeProcessor = PROVIDERS.get(partMediaType);
                if (partMediaTypeProcessor != null) {
                    try {
                        partMediaTypeProcessor.setMultipartEntityBuilder(builder);
                        partMediaTypeProcessor.writeContent(part);
                        continue;
                    } catch (UnsupportedOperationException ex) {
                        //
                    }
                }

                // comportement par défaut
                if (part instanceof PartFile) {
                    PartFile partFile = (PartFile) part;
                    InputStream is = null;
                    byte[] data = null;
                    Object partFileContent = partFile.getContent();

                    if (partFileContent instanceof InputStream) {
                        is = (InputStream) partFileContent;
                    } else if (partFileContent instanceof byte[]) {
                        data = (byte[]) partFileContent;
                    } else if (partFileContent instanceof String) {
                        data = ((String) partFileContent).getBytes();
                    } else if (partFileContent instanceof ByteArrayOutputStream) {
                        data = ((ByteArrayOutputStream) partFileContent).toByteArray();
                    } else if (partFileContent != null) {
                        throw new HttpRuntimeException("Cannot handle content of type " + partFileContent.getClass());
                    }

                    if (is != null) {
                        builder.addBinaryBody(partFile.getName(), is, ContentType.create(partMediaType.toString()), partFile.getFilename());
                    } else if (data != null) {
                        builder.addBinaryBody(partFile.getName(), data, ContentType.create(partMediaType.toString()), partFile.getFilename());
                    }
                } else {
                    Object partContent = part.getContent();
                    String data = partContent != null ? partContent.toString() : null;
                    builder.addTextBody(part.getName(), data, HttpUtils.convertInternalContentType(part.getContentType()));
                }
            }
            return builder.build();
        } else if (content == null) {
            return MultipartEntityBuilder.create().build();
        } else {
            throw new UnsupportedOperationException("Content not supported : " + content.getClass());
        }
    }

    private UrlEncodedFormEntity getUrlEncodedFormEntity(fr.devlogic.util.http.ContentType contentType, Object content) {
        List<NameValuePair> nvps = new ArrayList<>();

        if (content instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) content;
            map.forEach((k, v) -> nvps.add(new BasicNameValuePair(k, v.toString())));

            Charset encoding = contentType.getEncoding();
            if (encoding == null) {
                encoding = StandardCharsets.UTF_8;
            }

            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nvps, encoding);
            urlEncodedFormEntity.setContentType(contentType.toString());

            return urlEncodedFormEntity;
        } else if (content == null) {
            return new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8);
        } else {
            throw new UnsupportedOperationException("Content not supported : " + contentType);
        }
    }

    /**
     * Retourne le contenu d'une entrée dans l'entête de réponse
     *
     * @param key La clef
     * @return La valeur
     */
    public String header(String key) {
        return Optional.ofNullable(closeableHttpResponse.getFirstHeader(key)).map(Header::getValue).orElse(null);
    }

    public fr.devlogic.util.http.ContentType contentType() {
        if (contentMediaType == null) {
            String contentType = header(CONTENT_TYPE);
            contentMediaType = getContentType(contentType, genericRequestTemplate.defaultCharset);
        }

        return contentMediaType;
    }

    /**
     * Status de la requête
     *
     * @return la valeur du status
     */
    public int status() {
        return closeableHttpResponse.getStatusLine().getStatusCode();
    }

    /**
     * Détermine si le statut rempli la condition.
     *
     * @param code Le code {@link HttpResponseCode#OK}) ou le type de retour ({@link HttpResponseCode#SUCCESS_2XX})
     * @return Retourne vrai lorsque le statut remplit la condition.
     */
    public boolean statusMatches(HttpResponseCode code) {
        return code.match(status());
    }

    @Nullable
    public <T> T thenReturn(Function<HttpResponse, T> fct, @Nullable Function<HttpResponse, T> defaultValue) {
        if (predicate == null) {
            throw new IllegalStateException(PREDICATE_IS_MISSING);
        }

        try {
            if (predicate.test(this)) {
                return fct.apply(this);
            } else {
                return (defaultValue != null) ? defaultValue.apply(this) : null;
            }
        } finally {
            predicate = null;
        }
    }

    public HttpResponse thenReturn(Function<HttpResponse, ?> fct) {
        this.fct = fct;
        return this;
    }

    public ApacheHttpHttpResponse thenReturnContentAs(Class<?> c) {
        this.fct = r -> r.getContent(c);
        return this;
    }

    public ApacheHttpHttpResponse thenReturnContentAs(Type t) {
        this.fct = r -> r.getContent(t);
        return this;
    }

    public <T> ApacheHttpHttpResponse thenReturnContentAs(GenericType<T> t) {
        this.fct = r -> r.getContent(t);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T orElseThrows(Supplier<RuntimeException> supplier) {
        if (predicate == null) {
            throw new IllegalStateException(PREDICATE_IS_MISSING);
        }
        try {
            if (predicate.test(this)) {
                return (T) fct.apply(this);
            }

            throw supplier.get();
        } finally {
            predicate = null;
        }
    }

    public HttpResponse when(Predicate<HttpResponse> p) {
        this.predicate = (predicate == null) ? p : predicate.and(p);
        return this;
    }

    public HttpResponse when(HttpResponseCode code) {
        Predicate<HttpResponse> responsePredicate = statusMatch(code);
        this.predicate = (predicate == null) ? responsePredicate : predicate.and(responsePredicate);
        return this;
    }

    public HttpResponse whenNot(HttpResponseCode code) {
        Predicate<HttpResponse> responsePredicate = statusMatch(code).negate();
        this.predicate = (predicate == null) ? responsePredicate : predicate.and(responsePredicate);
        return this;
    }

    public HttpResponse when(MediaType mediaType) {
        Predicate<HttpResponse> p = r -> mediaType.equals(r.contentType().getMediaType());
        this.predicate = (predicate == null) ? p : predicate.and(p);
        return this;
    }

    /**
     * Lance l'exception {@link HttpRuntimeException} lorsque la condition est avérée (e.g {@link #whenNot(HttpResponseCode)}.
     *
     * Cette méthode est utilisée principalement pour gérer les réponses qui correspondent à des erreurs.
     *
     * L'exception {@link HttpRuntimeException} contient le statut et le contenu du message d'erreur.
     *
     * Exemple :
     * <pre>
     *      httpRestTemplate.whenNot(HttpResponseCode.SUCCESS_2XX).thenThrowException()
     * </pre>
     *
     * @return
     * L'instance
     * @throws HttpRuntimeException
     * Lance potentiellement l'exception
     */
    public HttpResponse thenThrowException() throws HttpRuntimeException {
        if (predicate == null) {
            throw new IllegalStateException(PREDICATE_IS_MISSING);
        }

        try {
            if (predicate.test(this)) {
                throw new HttpRuntimeException(status(), getContent(String.class));
            }
        } finally {
            predicate = null;
        }

        return this;
    }

    public <R extends RuntimeException> HttpResponse thenThrowException(Class<R> exceptionClass) throws RuntimeException {
        if (predicate == null) {
            throw new IllegalStateException(PREDICATE_IS_MISSING);
        }

        try {
            if (predicate.test(this)) {
                try {
                    Constructor<R> constructor = exceptionClass.getConstructor(Throwable.class);
                    throw constructor.newInstance(new HttpRuntimeException(status(), getContent(String.class)));
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                         InvocationTargetException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        } finally {
            predicate = null;
        }

        return this;
    }

    public Predicate<HttpResponse> statusMatch(HttpResponseCode code) {
        return r -> code.match(r.status());
    }

    /**
     * Retourne une valeur lorsque le status ne correspond pas à celui attendu
     *
     * @param supplier Le fournisseur
     * @param <T>      Le type d'objet attendu
     * @return La valeur de retour
     */
    @SuppressWarnings("unchecked")
    public <T> T orElse(Supplier<T> supplier) {
        if (predicate == null) {
            throw new IllegalStateException(PREDICATE_IS_MISSING);
        }
        try {
            return predicate.test(this) ? (T) fct.apply(this) : supplier.get();
        } finally {
            predicate = null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T orElse(Function<HttpResponse, T> supplier) {
        if (predicate == null) {
            throw new IllegalStateException(PREDICATE_IS_MISSING);
        }
        try {
            return (T) (predicate.test(this) ? this.fct.apply(this) : supplier.apply(this));
        } finally {
            predicate = null;
        }
    }

    @Nullable
    public InputStream getContentStream() {
        HttpEntity entity = closeableHttpResponse.getEntity();
        if (entity == null) {
            return null;
        }

        try {
            return entity.getContent();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    @Nullable
    public <C> C getContent(Class<C> c) {
        try (InputStream contentStream = getContentStream()) {
            if (contentStream == null) {
                return null;
            }

            if (c.getPackage().equals(InputStream.class.getPackage())) {
                throw new UnsupportedOperationException("Cannot handle " + c.getName());
            }
            fr.devlogic.util.http.ContentType contentType = contentType();
            MediaTypeProcessor mediaTypeProcessor = PROVIDERS.get(contentType.getMediaType());
            if (mediaTypeProcessor != null) {
                return mediaTypeProcessor.getContent(contentStream, contentType, c);
            }

            throw new UnsupportedOperationException(contentMediaType + " n'est pas supporté pour le type " + c.getName());
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    @Nullable
    public <C> C getContent(GenericType<C> p) {
        fr.devlogic.util.http.ContentType contentType = contentType();
        MediaTypeProcessor mediaTypeProcessor = PROVIDERS.get(contentType.getMediaType());
        if (mediaTypeProcessor != null) {
            return mediaTypeProcessor.getContent(getContentStream(), contentType, p);
        }

        return getContent(p.getType());
    }

    @Nullable
    public <C> C getContent(Type t) {
        try (InputStream contentStream = getContentStream()) {
            if (contentStream == null) {
                return null;
            }

            fr.devlogic.util.http.ContentType contentType = contentType();
            MediaTypeProcessor mediaTypeProcessor = PROVIDERS.get(contentType.getMediaType());
            if (mediaTypeProcessor != null) {
                return mediaTypeProcessor.getContent(contentStream, contentType, t);
            }

            throw new UnsupportedOperationException(t.toString() + " non supporté pour le contenu " + contentMediaType);

        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }
}
