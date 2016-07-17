package de.ust.skill.common.jforeign.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An empty iterator.
 * 
 * @author Timm Felden
 */
public final class EmptyIterator<T> implements Iterator<T> {
    static final EmptyIterator<?> instance = new EmptyIterator<Object>();

    @SuppressWarnings("unchecked")
    static <T> EmptyIterator<T> get() {
        return (EmptyIterator<T>) instance;
    }

    private EmptyIterator() {
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public T next() {
        throw new NoSuchElementException("empty iterator");
    }

}
