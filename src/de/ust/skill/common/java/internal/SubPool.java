package de.ust.skill.common.java.internal;

import java.util.Iterator;
import java.util.Set;

import de.ust.skill.common.java.iterators.Iterators;

/**
 * Management of sub types is a bit different.
 * 
 * @author Timm Felden
 */
public class SubPool<T extends B, B extends SkillObject> extends StoragePool<T, B> {

    public SubPool(long poolIndex, String name, StoragePool<? super T, B> superPool, Set<String> knownFields) {
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
    public boolean insertInstance(int skillID) {
        int i = skillID - 1;
        if (null != basePool.data[i])
            return false;

        @SuppressWarnings("unchecked")
        T r = (T) (new SkillObject.SubType(this, skillID));
        basePool.data[i] = r;
        staticData.add(r);
        return true;
    }

    /**
     * Internal use only!
     */
    public Iterator<T> dataViewIterator(int begin, int end) {
        return Iterators.<T, B> fakeArray(basePool.data, begin, end);
    }
}
