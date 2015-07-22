package de.ust.skill.common.java.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import de.ust.skill.common.java.internal.fieldDeclarations.AutoField;
import de.ust.skill.common.java.internal.parts.Block;
import de.ust.skill.common.java.iterators.Iterators;

/**
 * Management of sub types is a bit different.
 * 
 * @author Timm Felden
 */
public class SubPool<T extends B, B extends SkillObject> extends StoragePool<T, B> {

    public SubPool(int poolIndex, String name, StoragePool<? super T, B> superPool, Set<String> knownFields,
            AutoField<?, T>[] autoFields) {
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
    public void insertInstances() {
        final Block last = blocks.getLast();
        int i = (int) last.bpo;
        int high = (int) (last.bpo + last.count);
        B[] data = basePool.data;
        while (i < high) {
            if (null != data[i])
                return;

            @SuppressWarnings("unchecked")
            T r = (T) (new SkillObject.SubType(this, i + 1));
            data[i] = r;
            staticData.add(r);

            i += 1;
        }
    }

    /**
     * Internal use only!
     */
    public Iterator<T> dataViewIterator(int begin, int end) {
        return Iterators.<T, B> fakeArray(basePool.data, begin, end);
    }
}
