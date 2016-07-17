package de.ust.skill.common.jforeign.internal.fieldDeclarations;

/**
 * Specialized field access for boolean.
 * 
 * @author Timm Felden
 */
public interface KnownBooleanField<T> {
    public boolean get(T ref);

    public void set(T ref, boolean value);
}
