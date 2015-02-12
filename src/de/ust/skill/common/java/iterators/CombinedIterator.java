package de.ust.skill.common.java.iterators;

import java.util.Collection;
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
     * iterator list is linked to allow for gc of unused iterators. All
     * iterators in the list are non-empty
     */
    private LinkedList<Iterator<? extends T>> is;

    /**
     * Constructs a combined iterator, ignoring all empty iterators in the list
     * 
     * @param is
     *            a list of iterators
     */
    @SafeVarargs
    CombinedIterator(Iterator<? extends T>... is) {
        this.is = new LinkedList<>();
        for (Iterator<? extends T> i : is)
            if (i.hasNext())
                this.is.addLast(i);
    }

    /**
     * Constructs a combined iterator, ignoring all empty iterators in the list
     * 
     * @param is
     *            a list of iterators
     */
    CombinedIterator(Collection<Iterator<? extends T>> is) {
        this.is = new LinkedList<>();
        for (Iterator<? extends T> i : is)
            if (i.hasNext())
                this.is.addLast(i);
    }

    @Override
    public boolean hasNext() {
        return !is.isEmpty();
    }

    @Override
    public T next() {
        T next = is.getFirst().next();
        if (!is.isEmpty() && !is.getFirst().hasNext())
            is.removeFirst();

        return next;
    }
}
