package de.ust.skill.common.java.internal.fieldDeclarations;

/**
 * Specialized field access for float.
 * 
 * @author Timm Felden
 */
public interface KnownFloatField<T> {
    public float get(T ref);

    public void set(T ref, float value);
}
