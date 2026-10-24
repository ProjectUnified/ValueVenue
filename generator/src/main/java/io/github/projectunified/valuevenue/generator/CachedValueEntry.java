package io.github.projectunified.valuevenue.generator;

import java.util.concurrent.atomic.AtomicBoolean;

class CachedValueEntry<FV> {
    final FV finalValue;
    final AtomicBoolean activated = new AtomicBoolean();

    CachedValueEntry(FV finalValue) {
        this.finalValue = finalValue;
    }
}
