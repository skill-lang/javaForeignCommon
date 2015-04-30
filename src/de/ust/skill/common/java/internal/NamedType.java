package de.ust.skill.common.java.internal;

/**
 * named types have a type name (using a tau symbol to avoid clashes in the
 * namespace)
 * 
 * @author Timm Felden
 */
public interface NamedType {
    /**
     * @return the skill name of the type
     */
    public String τName();

    /**
     * @return the pool that is managing instances of this type
     */
    public StoragePool<?, ?> τPool();
}
