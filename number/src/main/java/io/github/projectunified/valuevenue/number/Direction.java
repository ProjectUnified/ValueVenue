package io.github.projectunified.valuevenue.number;

/**
 * The direction a number range steps in.
 */
public enum Direction {
    /**
     * Step from the start to a higher end. A start higher than the end gives no value.
     */
    ASCENDING,
    /**
     * Step from the start to a lower end. A start lower than the end gives no value.
     */
    DESCENDING,
    /**
     * Step in the direction the bounds tell, so a start higher than the end steps down.
     */
    AUTO
}
