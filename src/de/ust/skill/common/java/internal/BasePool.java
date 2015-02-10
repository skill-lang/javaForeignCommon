package de.ust.skill.common.java.internal;

import java.util.Set;

import de.ust.skill.common.java.api.SkillFile;

/**
 * The base of a type hierarchy. Contains optimized representations of data
 * compared to sub pools.
 * 
 * @author Timm Felden
 * @param <T>
 */
public class BasePool<T extends SkillObject> extends StoragePool<T, T> {

    /**
     * instances read from disk
     */
    T[] data;

    /**
     * the owner is set once by the SkillState.finish method!
     */
    private SkillFile owner = null;

    public BasePool(long poolIndex, String name, Set<String> knownFields) {
        super(poolIndex, name, null, knownFields);
    }

    @Override
    public SkillFile owner() {
        return owner;
    }

    void setOwner(SkillFile owner) {
        assert null == this.owner : "owner can only be set once";
        assert null != owner : "owner can not be null";
        this.owner = owner;

    }

    @SuppressWarnings("null")
    @Override
    public T getByID(long index) {
        if (0 == index)
            return null;
        return data[(int) index - 1];
    }
}
