package io.github.projectunified.valuevenue.core;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The source values a value generator works on, before they are turned into final values.
 * <p>
 * A generator caches one final value per source value, so the source values are the identity of what is generated.
 * They can depend on the key or be shared by every key.
 * <p>
 * It is a {@link Function} from a key to the active source values of that key, so it can be handed to a
 * {@link ValueVenue} as it is.
 *
 * @param <K> type of the key
 * @param <SV> type of the source value
 */
public interface ValueSource<K, SV> extends Function<K, List<SV>> {
    /**
     * All source values that do not depend on a key.
     * <p>
     * Empty by default: a source that only knows its values through {@link #getSourceValueStream(Object)} needs no
     * override.
     *
     * @return stream of every key-independent source value
     */
    default Stream<SV> getSourceValueStream() {
        return Stream.empty();
    }

    /**
     * Source values to expose for the given key. Defaults to {@link #getSourceValueStream()}.
     *
     * @param key key whose source values are requested
     * @return stream of source values for the key
     */
    default Stream<SV> getSourceValueStream(K key) {
        return getSourceValueStream();
    }

    /**
     * Whether a source value is currently active.
     *
     * @param value source value to check
     * @return true if the value may be exposed
     */
    default boolean isSourceValueActivated(SV value) {
        return true;
    }

    /**
     * The source values exposed for the key: the stream of the key, without the inactive values.
     *
     * @param key key whose source values are requested
     * @return list of the active source values for the key
     */
    @Override
    default List<SV> apply(K key) {
        return getSourceValueStream(key).filter(this::isSourceValueActivated).collect(Collectors.toList());
    }

    /**
     * Resolves a variable of the source value.
     *
     * @param input name of the variable, without its markers
     * @param value source value the variable is resolved for
     * @return the replacement, or null if the variable is not known
     */
    Object replace(String input, SV value);
}
