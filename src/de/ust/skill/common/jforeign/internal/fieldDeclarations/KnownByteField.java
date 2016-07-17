package de.ust.skill.common.jforeign.internal.fieldDeclarations;

/**
 * Specialized field access for byte.
 * 
 * @author Timm Felden
 */
public interface KnownByteField<T> {
    public byte get(T ref);

    public void set(T ref, byte value);
}
