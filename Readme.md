# Client Http

This library is based on the Apache commons httpclient implementation and offers features similar to RestTemplate. RestTemplate contains some bugs which forced the development of this library

## Concept

An HTTP request consists of an url, a method, a header and possibly some data. The header can often be factored between
different requests. Therefore, the request is mutualized, and information specific to the request is used to create the
httpResponse.

The query relies on the httpRequestTemplateConfiguration's configuration. It is the httpRequestTemplateConfiguration who
determines how the request is made.

The httpResponse is the materialization of the request which results in obtaining the requested content.

## Utilisation

The library uses Apache commons httpclient as well as spring-boot-starter-json (for the JSON part). The current
implementation is mainly oriented to perform json requests.

The entry point into the library is RequestTemplate. The RequestTemplate instance can be shared. Most methods can be
chained. A httpRequestTemplateConfiguration can be provided to RestTemplate to configure, for example, a proxy.

Here is an example of instantiating RestTemplate:

```java
HttpRequestTemplateFactory httpRequestTemplateFactory=new HttpRequestTemplateFactory();

        HttpRequestTemplate httpRequestTemplate=httpRequestTemplateFactory.getObject():

        HttpRequestTemplateConfiguration httpRequestTemplateConfiguration=httpRequestTemplate.configuration().setTimeout(TIMEOUT); // create a httpRequestTemplateConfiguration for httpRequestTemplate instance and configure the httpRequestTemplateConfiguration
        if(proxy.getAddress()!=null&&proxy.getPort()!=null){
        log.info("Utilisation d'un proxy pour les requetes vers SPICE : {}, {}",proxy.getAddress(),proxy.getPort());
        httpRequestTemplateConfiguration.setProxy(proxy.getAddress(),proxy.getPort());
        }

        jsonRequestTemplate=
        httpRequestTemplate
        .basicAuth(credentials.getUsername(),credentials.getPassword())
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON);
```

Implemented requests are GET, POST, PUT and DELETE. There are two scenarios: with or without parameters.

In the case without parameter, the methods to call contain the URL of the request. The content of the request is optionally passed. Methods return an instance of Response.
```java
jsonRequestTemplate.get(uri)
```

In the case with parameters, the url is passed by the url() method. Parameters are provided through the pathParam and queryParam methods. At the end of parameter passing, the methods without parameters are called.

```java
jsonRequestTemplate.url(uri).pathParam("dateDebut", dateDebut).pathParam("dateFin", dateFin).get();
```

The httpResponse contains the header, status and data. The supported data types are application/json when a json model
is returned, any type when you want the information in the form of a string or a stream.

Regarding the json type, the desired model is passed as a getContent parameter:

```java
String str = jsonRequestTemplate.get(uri).getContent(String.class);

// explicit type
Map<String, String> map = jsonRequestTemplate.get(uri).getContent(new GenericType<Map<String, String>>(){});

// Implicit type (type inference)
Map<String, String> map = jsonRequestTemplate.get(uri).getContent(new GenericType<>(){});

// Dynamically
Map<String, String> map = jsonRequestTemplate.get(uri).getContent(TYPE_FACTORY.constructMapType(Map.class, String.class, String.class));

// Stream
InputStream is = jsonRequestTemplate.get(uri).getContentStream();
```

`GenericType` instance **cannot be shared** across the code. The type, passed as a generic parameter, explicitly or implicitly (by inference), is captured by the instantiation of a subclass of `GenericType`.
