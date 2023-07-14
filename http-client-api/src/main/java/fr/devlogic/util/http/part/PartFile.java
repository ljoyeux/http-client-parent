package fr.devlogic.util.http.part;

import fr.devlogic.util.http.ContentType;
import fr.devlogic.util.http.MediaType;

public class PartFile extends Part {
    private final String filename;

    public PartFile(String entry, Object content, MediaType mediaType, String filename) {
        super(entry, content, new ContentType(mediaType, null));
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
