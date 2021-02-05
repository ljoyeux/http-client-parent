package fr.devlogic.util.http.annotation;

import java.lang.annotation.*;

/**
 * Indique que l'objet est une instance indépendante
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Documented
public @interface Instance {
}
