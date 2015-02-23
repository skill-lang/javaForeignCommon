package de.ust.skill.common.java.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import de.ust.skill.common.java.internal.parts.Block;
import de.ust.skill.common.java.iterators.Iterators;

/**
 * Management of sub types is a bit different.
 * 
 * @author Timm Felden
 */
public class SubPool<T extends B, B extends SkillObject> extends StoragePool<T, B> {

    public SubPool(long poolIndex, String name, StoragePool<? super T, B> superPool, Set<String> knownFields,
            FieldDeclaration<?, T>[] autoFields) {
        super(poolIndex, name, superPool, knownFields, autoFields);
        superPool.subPools.add(this);
    }

    @Override
    public Iterator<T> iterator() {
        B[] data = basePool.data;
        ArrayList<Iterator<? extends T>> is = new ArrayList<>(1 + blocks.size());
        is.add(newDynamicInstances());
        for (Block b : blocks)
            is.add(Iterators.fakeArray(data, (int) b.bpo, (int) (b.bpo + b.count)));

        return Iterators.concatenate(is);
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
