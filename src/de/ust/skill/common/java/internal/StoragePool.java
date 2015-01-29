package de.ust.skill.common.java.internal;

import de.ust.skill.common.java.api.Access;
import de.ust.skill.common.java.api.SkillObject;

/**
 * Toplevel implementation of all storage pools.
 * 
 * @author Timm Felden
 * @param <T1>
 * @param <T2>
 */
abstract public class StoragePool<T extends B, B extends SkillObject> extends FieldType<T> implements Access<T> {

    private final String name;
    private final StoragePool<? super T, B> superPool;

    @Override
    final public String name() {
        return name;
    }

    @Override
    public String superName() {
        if (null != superPool)
            return superPool.name;
        return null;
    }

    StoragePool(long poolIndex, String name, StoragePool<? super T, B> superPool) {
        super(32L + poolIndex);
        this.name = name;
        this.superPool = superPool;
    }

}
