package de.ust.skill.common.jforeign.internal.fieldDeclarations;

/**
 * Specialized field access for long.
 * 
 * @author Timm Felden
 */
public interface KnownLongField<T> {
    public long get(T ref);

    public void set(T ref, long value);
}
