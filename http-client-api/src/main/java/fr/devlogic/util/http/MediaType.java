package fr.devlogic.util.http;

import fr.devlogic.util.http.annotation.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum MediaType {
    APPLICATION(Constants.MEDIA_TYPE_APPLICATION, Constants.MEDIA_SUBTYPE_WILDCARD),
    APPLICATION_ATOM_XML(Constants.MEDIA_TYPE_APPLICATION, "atom+xml"),
    APPLICATION_XHTML_XML(Constants.MEDIA_TYPE_APPLICATION, "xhtml+xml"),
    APPLICATION_SVG_XML(Constants.MEDIA_TYPE_APPLICATION, "svg+xml"),
    APPLICATION_JSON(Constants.MEDIA_TYPE_APPLICATION, "json"),
    APPLICATION_FORM_URLENCODED(Constants.MEDIA_TYPE_APPLICATION, "x-www-form-urlencoded"),
    MULTIPART_FORM_DATA(Constants.MEDIA_TYPE_MULTIPART, "form-data"),
    APPLICATION_OCTET_STREAM(Constants.MEDIA_TYPE_APPLICATION, "octet-stream"),
    TEXT(Constants.MEDIA_TYPE_TEXT, Constants.MEDIA_SUBTYPE_WILDCARD),
    TEXT_PLAIN(Constants.MEDIA_TYPE_TEXT, "plain"),
    TEXT_XML(Constants.MEDIA_TYPE_TEXT, "xml"),
    TEXT_HTML(Constants.MEDIA_TYPE_TEXT, "html"),
    SERVER_SENT_EVENTS(Constants.MEDIA_TYPE_TEXT, "event-stream"),
    APPLICATION_JSON_PATCH_JSON(Constants.MEDIA_TYPE_APPLICATION, "json-patch+json"),
    IMAGE_PNG(Constants.MEDIA_TYPE_IMAGE, "png"),
    IMAGE_JPEG(Constants.MEDIA_TYPE_IMAGE, "jpeg"),
    IMAGE_GIF(Constants.MEDIA_TYPE_IMAGE, "gif"),
    IMAGE_BITMAP(Constants.MEDIA_TYPE_IMAGE, "bmp"),
    WILDCARD(Constants.MEDIA_TYPE_WILDCARD, Constants.MEDIA_SUBTYPE_WILDCARD);

    final String type;
    final String subType;

    MediaType(String type, String subType) {
        this.type = type;
        this.subType = subType;
    }

    @Override
    public String toString() {
        return type + "/" + subType;
    }

    public boolean isType(String type) {
        return this.type.equals(type);
    }

    private static final Map<String, MediaType> STRING_TO_MEDIA_TYPE;
    static {
        STRING_TO_MEDIA_TYPE = new HashMap<>();
        Arrays.stream(MediaType.values()).forEach(mt -> STRING_TO_MEDIA_TYPE.put(mt.toString(), mt));
    }

    /**
     * Cette méthode est à utiliser en lieu et place de {@link #valueOf(String)}.
     *
     * {@link #valueOf(String)} repose que le nom du l'énumération (e.g. {@link #TEXT_PLAIN}
     *
     * @param value
     * La valeur (e.g. "text/plain"). La valeur peut être nulle
     * @return
     * Le fr.devlogic.util.http.MediaType correspondant à la valeur. Retourne null si aucune valeur est trouvé.
     */
    @Nullable
    public static MediaType getMediaType(@Nullable String value) {
        return value!=null ? STRING_TO_MEDIA_TYPE.get(value) : null;
    }
}
