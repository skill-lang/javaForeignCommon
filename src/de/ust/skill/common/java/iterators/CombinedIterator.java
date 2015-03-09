package de.ust.skill.common.java.iterators;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Combines a list of iterators into a single iterator, that will iterate over all iterators in order
 * 
 * @author Timm Felden
 */
public final class CombinedIterator<T> implements Iterator<T> {
    /**
     * iterator list is linked to allow for gc of unused iterators. All iterators in the list are non-empty
     */
    private final LinkedList<Iterator<? extends T>> iterators;
    private Iterator<? extends T> current;

    /**
     * Constructs a combined iterator, ignoring all empty iterators in the list
     * 
     * @param is
     *            a non-empty list of iterators
     */
    CombinedIterator(LinkedList<Iterator<? extends T>> iterators) {
        this.iterators = iterators;
        current = iterators.removeFirst();
    }

    @Override
    public boolean hasNext() {
        return null != current;
    }

    @Override
    public T next() {
        if (null == current)
            throw new NoSuchElementException("empty iterator");

        T next = current.next();
        if (!current.hasNext())
            if (iterators.isEmpty())
                current = null;
            else
                current = iterators.removeFirst();

        return next;

    }
}
