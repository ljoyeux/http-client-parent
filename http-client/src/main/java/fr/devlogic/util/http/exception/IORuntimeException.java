package fr.devlogic.util.http.exception;

import java.io.IOException;

/**
 * Version RuntimeException de l'exception {@link IOException}
 */
public class IORuntimeException extends RuntimeException {

    private static final long serialVersionUID = -822240453641894672L;

    public IORuntimeException(IOException ex) {
        super(ex);
    }
}
