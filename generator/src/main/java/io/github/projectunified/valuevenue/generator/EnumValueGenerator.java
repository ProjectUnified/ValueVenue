package io.github.projectunified.valuevenue.generator;

import java.util.Arrays;
import java.util.stream.Stream;

public abstract class EnumValueGenerator<K, SV extends Enum<SV>, FV> extends SharedValueGenerator<K, SV, FV> {
    public abstract Class<SV> getEnumType();

    @Override
    public Stream<SV> getSourceValueStream() {
        return Arrays.stream(getEnumType().getEnumConstants());
    }
}
