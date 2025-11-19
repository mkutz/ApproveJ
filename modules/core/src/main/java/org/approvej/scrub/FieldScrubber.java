package org.approvej.scrub;

/**
 * Generic {@link Scrubber} to set the value of a certain field via reflection.
 *
 * <p>Note that this requires that the field is generally mutable. Immutable fields will cause a
 * {@link ScrubbingError} when this is {@link #apply(Object) applied}.
 *
 * @param <T> the type of value to scrub
 */
public interface FieldScrubber<T> extends Scrubber<FieldScrubber<T>, T, Object> {}
