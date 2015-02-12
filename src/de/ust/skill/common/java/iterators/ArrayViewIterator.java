package de.ust.skill.common.java.iterators;

import java.util.Iterator;

/**
 * Array to iterator conversion.
 * 
 * @author Timm Felden
 */
public class ArrayViewIterator<T> implements Iterator<T> {
    private final T[] target;
    private int index;
    private final int end;

    ArrayViewIterator(T[] target, int begin, int end) {
        this.target = target;
        index = begin;
        this.end = end;
    }

    @Override
    public boolean hasNext() {
        return index < end;
    }

    @Override
    public T next() {
        return target[index++];
    }

}
