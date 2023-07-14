package fr.devlogic.util.http;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Classe capturant le type passé par le paramètre générique.
 *
 * La classe facilite la construction du type attendu en retour.
 *
 * @param <T>
 *     Le type attendu en retour
 */
@SuppressWarnings("all")
public abstract class GenericType<T> implements Type {
    private final Type actualTypeArgument;

    protected GenericType() {
        Class<?> aClass = this.getClass();
        actualTypeArgument = ((ParameterizedType) (aClass.getGenericSuperclass())).getActualTypeArguments()[0];
    }
    public Type getType() {
        return actualTypeArgument;
    }
}
