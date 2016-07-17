package de.ust.skill.common.jforeign.internal.fieldDeclarations;

/**
 * Specialized field access for short.
 * 
 * @author Timm Felden
 */
public interface KnownShortField<T> {
    public short get(T ref);

    public void set(T ref, short value);
}
