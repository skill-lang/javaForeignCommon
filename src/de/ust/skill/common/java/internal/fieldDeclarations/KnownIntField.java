package de.ust.skill.common.java.internal.fieldDeclarations;

/**
 * Specialized field access for int.
 * 
 * @author Timm Felden
 */
public interface KnownIntField<T> {
    public int get(T ref);

    public void set(T ref, int value);
}
