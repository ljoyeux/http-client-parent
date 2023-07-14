package fr.devlogic.util.http.part;

import fr.devlogic.util.http.ContentType;

public class Part {
    protected final String name;
    protected final Object content;
    protected final ContentType contentType;

    public Part(String name, Object content, ContentType contentType) {
        this.contentType = contentType;
        this.content = content;
        this.name = name;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public Object getContent() {
        return content;
    }

    public String getName() {
        return name;
    }
}
