package fr.devlogic.util.http.impl.apache;

import fr.devlogic.util.http.*;
import fr.devlogic.util.http.annotation.GenerateNewInstance;
import fr.devlogic.util.http.annotation.Instance;
import fr.devlogic.util.http.annotation.Nullable;
import fr.devlogic.util.http.annotation.Shared;
import fr.devlogic.util.http.exception.HttpRuntimeException;
import org.apache.http.HttpHeaders;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.apache.http.client.config.AuthSchemes.BASIC;

/**
 * Classe de génération d'une requête HTTP.
 * <p>
 * L'instance de cette classe peut être partagée. Certains appels sont effectués sur la même instance,
 * d'autres génère une sous instance. Les méthodes sont annotées pour indiqués si l'instance retournée est la même que
 * celle de l'appel ({@link Shared}) ou si c'est une nouvelle instance ({@link Instance})
 * <p>
 * {@link ApacheHttpRequestTemplate} s'utilise de 2 manières, avec ou sans paramètres. Dans le second cas, on utilisera les
 * méthodes où l'on peut passer l'url ({@link #get(String)}, {@link #post(String, Object)} , {@link #put(String, Object)}, {@link #delete(String, Object)}.
 * Si la requête nécessite le passage de paramètre, il faudra utiliser la méthode {@link #url(String)} qui retourne une nouvelle instance de
 * {@link ApacheHttpRequestTemplate}}. Les paramètres sont fournis par {@link #pathParam(String, Object)} ou {@link #queryParam(String, Object)}. A l'issu de
 * la configuration, l'une des méthodes {@link #get()}, {@link #post(Object)}, {@link #put(Object)} ou {@link #delete(Object)} est appelée.
 */
public class ApacheHttpRequestTemplate implements HttpRequestTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(ApacheHttpRequestTemplate.class);

    private HttpRequestTemplateConfiguration httpRequestTemplateConfiguration;

    Object content;
    protected String url;
    HttpMethod method;
    final Map<String, Object> headers;
    private final List<Consumer<ApacheHttpRequestTemplate>> callbacks;
    private List<RequestParam> pathParams;
    private List<RequestParam> queryParams;
    Charset defaultCharset;

    public ApacheHttpRequestTemplate() {
        headers = new HashMap<>();
        callbacks = new ArrayList<>();
    }

    private ApacheHttpRequestTemplate(Map<String, Object> headers, List<Consumer<ApacheHttpRequestTemplate>> callbacks) {
        this.headers = headers;
        this.callbacks = callbacks;

    }

    @SuppressWarnings("CopyConstructorMissesField")
    public ApacheHttpRequestTemplate(ApacheHttpRequestTemplate ref) {
        this(ref.headers, ref.callbacks);
        this.httpRequestTemplateConfiguration = ref.httpRequestTemplateConfiguration;
        this.defaultCharset = ref.defaultCharset;
    }

    /**
     * Ajoute une entête dans la requête
     *
     * @param key
     * La clef
     * @param value
     * La valeur
     * @return
     * La même instance
     */
    @Shared
    public ApacheHttpRequestTemplate header(String key, String value) {
        headers.put(key, value);
        return this;
    }

    /**
     * Ajoute une authentification basique
     *
     * @param username
     * L'utilisateur
     * @param password
     * Le mot de passe
     * @return
     * La même instance
     */
    @Shared
    public ApacheHttpRequestTemplate basicAuth(String username, String password) {
        headers.put(HttpHeaders.AUTHORIZATION, BASIC + " " + HttpUtils.encodeBasicAuth(username, password, StandardCharsets.ISO_8859_1));
        return this;
    }

    /**
     * Spécifie le type de contenu accepté en retour
     *
     * @param mediaType
     * Le type de média
     * @return
     * La même instance
     */
    @Shared
    public ApacheHttpRequestTemplate accept(MediaType... mediaType) {
        if (mediaType.length > 0) {
            headers.put(HttpHeaders.ACCEPT, Arrays.stream(mediaType).map(MediaType::toString).collect(Collectors.joining(", ")));
        }

        return this;
    }

    /**
     * Spécifie le contenu de la requête
     * @param mediaType
     * Le type de média
     * @return
     * La même instance
     */
    @Shared
    public ApacheHttpRequestTemplate contentType(MediaType mediaType) {
        headers.put(HttpHeaders.CONTENT_TYPE, mediaType);
        return this;
    }
    
    @Shared
    public ApacheHttpRequestTemplate contentType(ContentType contentType) {
        headers.put(HttpHeaders.CONTENT_TYPE, contentType);
        return this;
    }

    /**
     * Ajoute un prétraitement effectué avant la requête.
     *
     * @param process La tâche
     * @return La même instance
     */
    @Shared
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ApacheHttpRequestTemplate addRequestPreProcess(Consumer<? extends HttpRequestTemplate> process) {
        synchronized (callbacks) {
            callbacks.add((Consumer) process);
        }

        return this;
    }

    /**
     * Renseigne le codage des caractères par défaut pour les réponses
     *
     * @param defaultCharset
     *  le {@link Charset}
     * @return
     * la même instance
     */
    @Shared
    public ApacheHttpRequestTemplate defaultCharset(Charset defaultCharset) {
        this.defaultCharset = defaultCharset;
        return this;
    }

    @GenerateNewInstance
    public ApacheHttpRequestTemplate url(String url) {
        ApacheHttpRequestTemplate r = newInstance();
        r.url = url;

        return r;
    }

    private ApacheHttpRequestTemplate newInstance() {
        @SuppressWarnings("unchecked")
        Class<ApacheHttpRequestTemplate> aClass = (Class<ApacheHttpRequestTemplate>) this.getClass();
        try {
            Constructor<ApacheHttpRequestTemplate> constructor = aClass.getDeclaredConstructor(aClass);
            constructor.setAccessible(true);
            return constructor.newInstance(this);
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException(aClass + " should define a constructor with " + aClass + " as argument. ex: reason" + e.getClass() + " " + e.getMessage() );
        }
    }

    /**
     * Renseigne un paramètre de chemin
     *
     * @param key
     * La variable dans le chemin
     * @param value
     * La valeur
     * @return
     * La même instance
     */
    @Instance
    public ApacheHttpRequestTemplate pathParam(String key, Object value) {
        if (pathParams == null) {
            pathParams = new ArrayList<>();
        }

        pathParams.add(new RequestParam(key, value));

        return this;
    }

    /**
     * Renseigne un paramètre de chemin
     *
     * @param value
     * La valeur
     * @return
     * La même instance
     */
    @Instance
    public ApacheHttpRequestTemplate pathParam(Object value) {
        return pathParam(null, value);
    }

    /**
     * Renseigne un paramètre de requête
     *
     * @param key
     * La variable de requête
     * @param value
     * La valeur
     * @return
     * La même instance
     */
    @Instance
    public ApacheHttpRequestTemplate queryParam(String key, Object value) {
        if (queryParams == null) {
            queryParams = new ArrayList<>();
        }

        queryParams.add(new RequestParam(key, value));

        return this;
    }

    @Instance
    public ApacheHttpHttpResponse get() {
        method = HttpMethod.GET;

        String builtUrl = url();
        LOG.trace("GET : {}", builtUrl);

        synchronized (callbacks) {
            callbacks.forEach(c -> c.accept(this));
        }

        return new ApacheHttpHttpResponse(this);
    }

    /**
     * Effectue une requête {@link HttpMethod#POST}
     *
     * @param content
     * La classe du contenu dépend du type de contenu :
     * <ul>
     *     <li>{@link MediaType#APPLICATION_FORM_URLENCODED}: {@link Map}</li>
     *     <li>{@link MediaType#APPLICATION_JSON}: Objet</li>
     * </ul>
     * @return
     * {@link ApacheHttpHttpResponse}
     */
    @Instance
    public ApacheHttpHttpResponse post(@Nullable Object content) {
        method = HttpMethod.POST;
        this.content = content;

        String builtUrl = url();
        LOG.trace("POST : {}", builtUrl);

        synchronized (callbacks) {
            callbacks.forEach(c -> c.accept(this));
        }

        return new ApacheHttpHttpResponse(this);
    }

    @Instance
    public ApacheHttpHttpResponse put(@Nullable Object content) {
        method = HttpMethod.PUT;
        this.content = content;

        String builtUrl = url();
        LOG.trace("PUT : {}", builtUrl);

        synchronized (callbacks) {
            callbacks.forEach(c -> c.accept(this));
        }

        return new ApacheHttpHttpResponse(this);
    }

    @Instance
    public ApacheHttpHttpResponse delete(@Nullable Object content) {
        method = HttpMethod.DELETE;
        this.content = content;

        String builtUrl = url();
        LOG.trace("DELETE : {}", builtUrl);

        synchronized (callbacks) {
            callbacks.forEach(c -> c.accept(this));
        }

        return new ApacheHttpHttpResponse(this);
    }

    @GenerateNewInstance
    public ApacheHttpHttpResponse trace() {
        method = HttpMethod.TRACE;

        String builtUrl = url();
        LOG.trace("TRACE : {}", builtUrl);

        synchronized (callbacks) {
            callbacks.forEach(c -> c.accept(this));
        }

        return new ApacheHttpHttpResponse(this);
    }

    @GenerateNewInstance
    public ApacheHttpHttpResponse get(String url) {
        synchronized (callbacks) {
            callbacks.forEach(c -> c.accept(this));
        }

        LOG.trace("GET : {}", url);

        return new ApacheHttpHttpResponse(newInstance().setUrl(url).setMethod(HttpMethod.GET));
    }

    /**
     * Effectue une requête {@link HttpMethod#POST}.
     *
     * @param url
     * L'url de la requête
     * @param content
     * La classe du contenu dépend du type de contenu :
     * <ul>
     *     <li>{@link MediaType#APPLICATION_FORM_URLENCODED}: {@link Map}</li>
     *     <li>{@link MediaType#APPLICATION_JSON}: Objet</li>
     * </ul>
     * @return
     * {@link ApacheHttpHttpResponse}
     */
    @GenerateNewInstance
    public ApacheHttpHttpResponse post(String url, Object content) {
        synchronized (callbacks) {
            callbacks.forEach(c -> c.accept(this));
        }

        LOG.trace("POST : {}", url);

        return new ApacheHttpHttpResponse(newInstance().setUrl(url).setMethod(HttpMethod.POST).setContent(content));
    }

    @GenerateNewInstance
    public ApacheHttpHttpResponse put(String url, Object content) {
        synchronized (callbacks) {
            callbacks.forEach(c -> c.accept(this));
        }

        LOG.trace("PUT : {}", url);

        return new ApacheHttpHttpResponse(newInstance().setUrl(url).setMethod(HttpMethod.PUT).setContent(content));
    }

    @GenerateNewInstance
    public ApacheHttpHttpResponse delete(String url, @Nullable Object content) {
        synchronized (callbacks) {
            callbacks.forEach(c -> c.accept(ApacheHttpRequestTemplate.this));
        }

        LOG.trace("DELETE : {}", url);

        return new ApacheHttpHttpResponse(newInstance().setUrl(url).setMethod(HttpMethod.DELETE).setContent(content));
    }

    @Shared
    public ApacheHttpHttpResponse trace(String url) {
        synchronized (callbacks) {
            callbacks.forEach(c -> c.accept(ApacheHttpRequestTemplate.this));
        }

        LOG.trace("TRACE : {}", url);

        return new ApacheHttpHttpResponse(newInstance().setUrl(url).setMethod(HttpMethod.TRACE));
    }

    protected ApacheHttpRequestTemplate setUrl(String url) {
        this.url = url;

        return this;
    }

    public String url() {
        if ((pathParams == null) && (queryParams == null)) {
            return url;
        }

        if ((pathParams != null) && !pathParams.isEmpty()) {
            Map<Boolean, List<String>> nullNotNull = pathParams.stream().map(RequestParam::getKey).collect(Collectors.groupingBy(Objects::isNull));
            List<String> notNullKeys = nullNotNull.get(false);
            List<String> nullKeys = nullNotNull.get(true);
            if ((notNullKeys != null) && (nullKeys != null) && !nullKeys.isEmpty() && !notNullKeys.isEmpty()) {
                throw new IllegalStateException("Mixing path params with null and not null keys.");
            }

            if ((notNullKeys != null) && !notNullKeys.isEmpty()) {
                url = HttpUtils.mapPathParamsKeyValue(url, pathParams.stream().collect(Collectors.toMap(RequestParam::getKey, RequestParam::getValue)));
            } else if ((nullKeys != null) && !nullKeys.isEmpty()) {
                url = HttpUtils.mapPathParamsValues(url, pathParams.stream().map(RequestParam::getValue).toArray());
            }

            pathParams = null; // les paramètres ne sont appliqués qu'une seule fois
        }

        if ((queryParams != null) && !queryParams.isEmpty()) {
            try {
                URIBuilder uriBuilder = new URIBuilder(url);
                queryParams.forEach(qp -> uriBuilder.addParameter(qp.getKey(), (qp.getValue()) !=null ? qp.getValue().toString() : null));
                url = uriBuilder.toString();
            } catch (URISyntaxException e) {
                throw new HttpRuntimeException(e);
            }

            queryParams = null;  // les paramètres ne sont appliqués qu'une seule fois
        }

        return url;
    }

    @Override
    public HttpRequestTemplateConfiguration configuration() {
        httpRequestTemplateConfiguration = new ApacheHttpHttpRequestTemplateConfiguration();
        return httpRequestTemplateConfiguration;
    }

    protected ApacheHttpRequestTemplate setMethod(HttpMethod method) {
        this.method = method;

        return this;
    }

    protected ApacheHttpRequestTemplate setContent(Object content) {
        this.content = content;

        return this;
    }

    CloseableHttpClient getHttpClient() {
        return (httpRequestTemplateConfiguration != null) ? ((ApacheHttpHttpRequestTemplateConfiguration) httpRequestTemplateConfiguration).getClient() : HttpClients.createDefault();
    }

    public ApacheHttpRequestTemplate setClient(HttpRequestTemplateConfiguration httpRequestTemplateConfiguration) {
        this.httpRequestTemplateConfiguration = httpRequestTemplateConfiguration;

        return this;
    }
}
