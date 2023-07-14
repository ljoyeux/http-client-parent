package fr.devlogic.util.http.annotation;

import java.lang.annotation.*;

/**
 * Indique que l'instance peut être partagée
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Documented
public @interface Shared {
}
