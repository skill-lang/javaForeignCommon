package de.ust.skill.common.java.iterators;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Combines a list of iterators into a single iterator, that will iterate over
 * all iterators in order
 * 
 * @author Timm Felden
 */
public class CombinedIterator<T> implements Iterator<T> {
    /**
     * iterator list is linked to allow for gc of unused iterators
     */
    private LinkedList<Iterator<? extends T>> is;

    @SafeVarargs
    public CombinedIterator(Iterator<? extends T>... is) {
        this.is = new LinkedList<>(Arrays.asList(is));
    }

    @Override
    public boolean hasNext() {
        return !is.isEmpty();
    }

    @Override
    public T next() {
        T next = is.getFirst().next();
        // we have to pop empty iterators now in order to fulfill hasNext's
        // contract
        while (!is.isEmpty() && !is.getFirst().hasNext())
            is.removeFirst();

        return next;
    }
}
