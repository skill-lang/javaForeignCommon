package de.ust.skill.common.java.internal;

/**
 * named types store a reference to their type, so that they can be distinguished from another
 * 
 * @author Timm Felden
 */
public interface NamedType {

    /**
     * @return the pool that is managing instances of this type
     */
    public StoragePool<?, ?> τPool();
}
