package de.ust.skill.common.java.internal.fieldDeclarations;

/**
 * Specialized field access for boolean.
 * 
 * @author Timm Felden
 */
public interface KnownBooleanField<T> {
    public boolean get(T ref);

    public void set(T ref, boolean value);
}
