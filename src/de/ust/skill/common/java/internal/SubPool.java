package de.ust.skill.common.java.internal;

import java.util.Iterator;
import java.util.Set;

/**
 * Management of sub types is a bit different.
 * 
 * @author Timm Felden
 */
public class SubPool<T extends B, B extends SkillObject> extends StoragePool<T, B> {

    SubPool(long poolIndex, String name, StoragePool<? super T, B> superPool, Set<String> knownFields) {
        super(poolIndex, name, superPool, knownFields);
        superPool.subPools.add(this);
    }

    @Override
    public Iterator<T> iterator() {
        // TODO Auto-generated method stub
        throw new Error("TODO");
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getByID(long index) {
        return (T) basePool.getByID(index);
    }

    @Override
    boolean insertInstance(int skillID) {
        // TODO Auto-generated method stub
        throw new Error("TODO");
    }

}
