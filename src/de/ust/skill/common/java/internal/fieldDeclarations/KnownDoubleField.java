package de.ust.skill.common.java.internal.fieldDeclarations;

/**
 * Specialized field access for double.
 * 
 * @author Timm Felden
 */
public interface KnownDoubleField<T> {
    public double get(T ref);

    public void set(T ref, double value);
}
