package fr.devlogic.util.http;

import java.util.*;

public final class MediaTypeProcessorFatory {
    public static final Map<MediaType, List<MediaTypeProcessor>> PROVIDERS;

    static {
        PROVIDERS = new EnumMap<>(MediaType.class);
        ServiceLoader<MediaTypeProcessor> load = ServiceLoader.load(MediaTypeProcessor.class);
        load.iterator()
                .forEachRemaining(p -> p.handledMediaTypes()
                        .forEach(m -> PROVIDERS.computeIfAbsent(m, u -> new ArrayList<>())
                                .add(p)));
    }
}
