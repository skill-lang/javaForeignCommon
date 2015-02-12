package de.ust.skill.common.java.internal.fieldDeclarations;

/**
 * Specialized field access for long.
 * 
 * @author Timm Felden
 */
public interface KnownLongField<T> {
    public long get(T ref);

    public void set(T ref, long value);
}
