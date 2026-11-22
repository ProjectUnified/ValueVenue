package io.github.projectunified.valuevenue.number;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * The numeric types a number source can produce, with the arithmetic of the type.
 * <p>
 * The integral types step in {@code long}, so they are exact and need no decimal arithmetic. The floating types step
 * in {@code BigDecimal}, because a fractional step would drift in a binary float, and resolve to plain text without
 * scientific notation.
 * <p>
 * The class is final and creates its constants itself, so it loads no other class of its own while it is being
 * initialized.
 *
 * @param <N> type of the number
 */
public final class NumberType<N extends Number> {
    /**
     * The byte type.
     */
    public static final NumberType<Byte> BYTE = integral(value -> (byte) value);
    /**
     * The short type.
     */
    public static final NumberType<Short> SHORT = integral(value -> (short) value);
    /**
     * The int type.
     */
    public static final NumberType<Integer> INT = integral(value -> (int) value);
    /**
     * The long type.
     */
    public static final NumberType<Long> LONG = integral(value -> value);
    /**
     * The float type.
     */
    public static final NumberType<Float> FLOAT = decimal(BigDecimal::floatValue);
    /**
     * The double type.
     */
    public static final NumberType<Double> DOUBLE = decimal(BigDecimal::doubleValue);

    private final Values<N> values;
    private final Function<N, String> format;

    private NumberType(Values<N> values, Function<N, String> format) {
        this.values = values;
        this.format = format;
    }

    private static <N extends Number> NumberType<N> integral(LongFunction<N> converter) {
        return new NumberType<>(
                (start, end, step, maxInclusive, direction) -> {
                    long startValue = start.longValue();
                    long endValue = end.longValue();
                    long stepValue = step.longValue();
                    if (stepValue <= 0) {
                        throw new IllegalArgumentException("The step must be positive, not " + step);
                    }
                    int sign = directionSign(direction, Long.compare(startValue, endValue));
                    if (sign == 0) {
                        return Stream.empty();
                    }
                    long signedStep = sign * stepValue;
                    return LongStream.iterate(startValue, value -> value + signedStep)
                            .limit(count(toDecimal(startValue), toDecimal(endValue), toDecimal(stepValue), maxInclusive))
                            .boxed()
                            .map(value -> converter.apply(value));
                },
                value -> String.valueOf(value)
        );
    }

    private static <N extends Number> NumberType<N> decimal(Function<BigDecimal, N> converter) {
        return new NumberType<>(
                (start, end, step, maxInclusive, direction) -> {
                    BigDecimal startValue = toDecimal(start);
                    BigDecimal endValue = toDecimal(end);
                    BigDecimal stepValue = toDecimal(step);
                    if (stepValue.signum() <= 0) {
                        throw new IllegalArgumentException("The step must be positive, not " + step);
                    }
                    int sign = directionSign(direction, startValue.compareTo(endValue));
                    if (sign == 0) {
                        return Stream.empty();
                    }
                    BigDecimal signedStep = sign < 0 ? stepValue.negate() : stepValue;
                    return Stream.iterate(startValue, value -> value.add(signedStep))
                            .limit(count(startValue, endValue, stepValue, maxInclusive))
                            .map(converter);
                },
                value -> toDecimal(value).stripTrailingZeros().toPlainString()
        );
    }

    /**
     * The sign of the step: negative when the values step down, positive when they step up, and 0 when the requested
     * direction cannot reach the end.
     */
    private static int directionSign(Direction direction, int comparison) {
        switch (direction) {
            case ASCENDING:
                return comparison > 0 ? 0 : 1;
            case DESCENDING:
                return comparison < 0 ? 0 : -1;
            default:
                return comparison > 0 ? -1 : 1;
        }
    }

    /**
     * How many steps of the range fit between the start and the end.
     */
    private static long count(BigDecimal start, BigDecimal end, BigDecimal step, boolean maxInclusive) {
        int comparison = start.compareTo(end);
        if (comparison == 0) {
            return maxInclusive ? 1 : 0;
        }
        BigDecimal distance = comparison < 0 ? end.subtract(start) : start.subtract(end);
        return maxInclusive
                ? distance.divideToIntegralValue(step).longValue() + 1
                : distance.divide(step, 0, RoundingMode.CEILING).longValue();
    }

    private static BigDecimal toDecimal(Number value) {
        return new BigDecimal(value.toString());
    }

    /**
     * The values of a range, from the start to the end.
     *
     * @param start        the first value
     * @param end          the last value, or its bound when it is not included
     * @param step         the distance between two values, positive
     * @param maxInclusive whether the end is a value of the range
     * @param direction    the direction the values step in
     * @return stream of the values, empty when the direction cannot reach the end
     */
    public Stream<N> values(Number start, Number end, Number step, boolean maxInclusive, Direction direction) {
        return values.values(start, end, step, maxInclusive, direction);
    }

    /**
     * The plain text of a number: no scientific notation, whatever its type.
     *
     * @param value the number
     * @return the plain text
     */
    public String format(N value) {
        return format.apply(value);
    }

    /**
     * Builds the values of a range.
     *
     * @param <N> type of the number
     */
    private interface Values<N extends Number> {
        Stream<N> values(Number start, Number end, Number step, boolean maxInclusive, Direction direction);
    }
}
