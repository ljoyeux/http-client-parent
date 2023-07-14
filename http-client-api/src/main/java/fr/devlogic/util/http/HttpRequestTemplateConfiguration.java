package fr.devlogic.util.http;

public interface HttpRequestTemplateConfiguration {
    HttpRequestTemplateConfiguration setProxy(String httpProxyHost, int httpProxyPort);

    HttpRequestTemplateConfiguration setTimeout(int timeout);
}
