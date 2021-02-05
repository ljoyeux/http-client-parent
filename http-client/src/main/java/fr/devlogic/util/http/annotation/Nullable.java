package fr.devlogic.util.http.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Documented
public @interface Nullable {
}
