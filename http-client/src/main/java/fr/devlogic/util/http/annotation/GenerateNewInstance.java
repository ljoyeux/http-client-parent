package fr.devlogic.util.http.annotation;

import java.lang.annotation.*;

/**
 * Indique que la méthode génère une instance indépendante
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Documented
public @interface GenerateNewInstance {
}
