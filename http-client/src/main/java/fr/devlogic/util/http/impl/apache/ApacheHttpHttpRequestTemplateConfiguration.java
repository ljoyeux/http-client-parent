package fr.devlogic.util.http.impl.apache;

import fr.devlogic.util.http.HttpRequestTemplateConfiguration;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client qui contient la configuration pour effectuer les requêtes.
 */
public final class ApacheHttpHttpRequestTemplateConfiguration implements HttpRequestTemplateConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ApacheHttpHttpRequestTemplateConfiguration.class);

    private HttpClientBuilder clientBuilder;
    private RequestConfig.Builder requestConfigBuilder;

    /**
     * Renseigne le serveur proxy.
     *
     * @param httpProxyHost
     * Le nom de la machine
     * @param httpProxyPort
     * Le numéro de port
     * @return
     * Le client. Permet d'enchaîner les appels
     */
    @Override
    public ApacheHttpHttpRequestTemplateConfiguration setProxy(String httpProxyHost, int httpProxyPort) {
        if (requestConfigBuilder == null) {
            requestConfigBuilder = RequestConfig.custom();
        }

        requestConfigBuilder.setProxy(new HttpHost(httpProxyHost, httpProxyPort));

        return this;
    }

    /**
     * Règle le timeout d'une requête
     * @param timeout
     * Temps en milli-seconde
     * @return
     * Le client. Permet d'enchaîner les appels
     */
    @Override
    public ApacheHttpHttpRequestTemplateConfiguration setTimeout(int timeout) {
        if (requestConfigBuilder == null) {
            requestConfigBuilder = RequestConfig.custom();
        }

        requestConfigBuilder
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout);

        return this;
    }

    CloseableHttpClient getClient() {
        if (requestConfigBuilder != null) {
            clientBuilder = HttpClients.custom();
            RequestConfig config = requestConfigBuilder.build();
            log.trace("{}", config);
            clientBuilder.setDefaultRequestConfig(config);
        }

        return (clientBuilder != null) ? clientBuilder.build() : HttpClients.createDefault();
    }
}
