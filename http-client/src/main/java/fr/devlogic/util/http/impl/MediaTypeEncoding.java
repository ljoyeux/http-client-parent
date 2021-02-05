package fr.devlogic.util.http.impl;

import fr.devlogic.util.http.MediaType;
import fr.devlogic.util.http.exception.HttpRuntimeException;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import static fr.devlogic.util.http.MediaType.*;

public enum MediaTypeEncoding {

    APPLICATION_ENCODING(APPLICATION, null),
    APPLICATION_ATOM_XML_ENCODING(APPLICATION_ATOM_XML,true),
    APPLICATION_XHTML_XML_ENCODING(APPLICATION_XHTML_XML,true),
    APPLICATION_SVG_XML_ENCODING(APPLICATION_SVG_XML, true),
    APPLICATION_JSON_ENCODING(APPLICATION_JSON,  true),
    APPLICATION_FORM_URLENCODED_ENCODING(APPLICATION_FORM_URLENCODED,  true),
    MULTIPART_FORM_DATA_ENCODING(MULTIPART_FORM_DATA,  false),
    APPLICATION_OCTET_STREAM_ENCODING(APPLICATION_OCTET_STREAM,  false),
    TEXT_ENCODING(TEXT,  true),
    TEXT_PLAIN_ENCODING(TEXT_PLAIN,  true),
    TEXT_XML_ENCODING(TEXT_XML, true),
    TEXT_HTML_ENCODING(TEXT_HTML, true),
    SERVER_SENT_EVENTS_ENCODING(SERVER_SENT_EVENTS, null),
    APPLICATION_JSON_PATCH_JSON_ENCODING(APPLICATION_JSON_PATCH_JSON, true),
    IMAGE_PNG_ENCODING(IMAGE_PNG, false),
    IMAGE_JPEG_ENCODING(IMAGE_JPEG,  false),
    IMAGE_GIF_ENCODING(IMAGE_GIF, false),
    IMAGE_BITMAP_ENCODING(IMAGE_BITMAP, false),
    WILDCARD_ENCODING(WILDCARD, null);
    final MediaType type;
    final Boolean encoding;

    private static final Map<MediaType, Boolean> ENCODINGS;

    static {
        ENCODINGS = new EnumMap<>(MediaType.class);
        Arrays.stream(MediaTypeEncoding.values()).forEach(v -> ENCODINGS.put(v.type, v.encoding));
    }

    MediaTypeEncoding(MediaType type, Boolean encoding) {
        this.type = type;
        this.encoding = encoding;
    }

    public static Boolean getEncoding(MediaType mediaType) {
        if (!ENCODINGS.containsKey(mediaType)){
           throw new HttpRuntimeException("Internal error : " + mediaType + " is missing in " + MediaTypeEncoding.class.getSimpleName());
        }

        return ENCODINGS.get(mediaType);
    }
}
