package de.ust.skill.common.java.internal.fieldDeclarations;

/**
 * Generic field access.
 * 
 * @author Timm Felden
 */
public interface KnownField<R, T> {
    public R get(T ref);

    public void set(T ref, R value);
}
