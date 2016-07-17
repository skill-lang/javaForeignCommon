package de.ust.skill.common.jforeign.iterators;

import java.util.Iterator;

/**
 * Array to iterator conversion.
 * 
 * @author Timm Felden
 */
public final class ArrayIterator<T> implements Iterator<T> {
    private final T[] target;
    private int index;

    ArrayIterator(T[] target) {
        this.target = target;
    }

    @Override
    public boolean hasNext() {
        return index < target.length;
    }

    @Override
    public T next() {
        return target[index++];
    }

}
