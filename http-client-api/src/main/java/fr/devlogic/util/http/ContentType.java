package fr.devlogic.util.http;

import fr.devlogic.util.http.annotation.Nullable;

import java.nio.charset.Charset;
import java.util.Objects;

public class ContentType {
    private final MediaType mediaType;
    private final Charset encoding;

    public ContentType(MediaType mediaType, @Nullable Charset encoding) {
        this.mediaType = mediaType;
        this.encoding = encoding;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public Charset getEncoding() {
        return encoding;
    }

    @Override
    public String toString() {
        return (encoding != null) ? mediaType + "; charset=" + encoding : mediaType.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentType that = (ContentType) o;
        return mediaType == that.mediaType && Objects.equals(encoding, that.encoding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mediaType, encoding);
    }
}
