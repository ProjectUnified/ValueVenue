package io.github.projectunified.valuevenue.core;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * A {@link CachedValueGenerator} whose source values are all constants of an enum.
 *
 * @param <K> type of the key used to select values
 * @param <SV> enum type of the source value
 * @param <FV> type of the final value
 */
public abstract class EnumValueGenerator<K, SV extends Enum<SV>, FV> extends CachedValueGenerator<K, SV, FV> {
    private volatile SV[] constants;

    /**
     * Enum whose constants are the source values. {@link #update()} creates the cache entries of all of
     * them upfront. Must return the same type on every call; it is read once to cache the constants.
     *
     * @return enum type of the source values
     */
    public abstract Class<SV> getEnumType();

    @Override
    public Stream<SV> getSourceValueStream() {
        if (constants == null) {
            constants = getEnumType().getEnumConstants();
        }
        return Arrays.stream(constants);
    }
}
