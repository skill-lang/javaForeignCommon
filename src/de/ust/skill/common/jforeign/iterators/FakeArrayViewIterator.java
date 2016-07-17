package de.ust.skill.common.jforeign.iterators;

import java.util.Iterator;

/**
 * Array to iterator conversion converting to a sub type as well.
 * 
 * @author Timm Felden
 */
public final class FakeArrayViewIterator<T extends B, B> implements Iterator<T> {
    private final B[] target;
    private int index;
    private final int end;

    FakeArrayViewIterator(B[] target, int begin, int end) {
        this.target = target;
        index = begin;
        this.end = end;
    }

    @Override
    public boolean hasNext() {
        return index < end;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T next() {
        return (T) target[index++];
    }

}
