package de.ust.skill.common.java.iterators;

import java.util.Iterator;

/**
 * Array to iterator conversion.
 * 
 * @author Timm Felden
 */
public class ArrayIterator<T> implements Iterator<T> {
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
