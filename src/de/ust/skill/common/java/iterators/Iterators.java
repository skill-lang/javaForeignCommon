package de.ust.skill.common.java.iterators;

import java.util.Collection;
import java.util.Iterator;

/**
 * Advanced iterator handling.
 * 
 * @author Timm Felden
 */
public class Iterators {
    private Iterators() {
        // there is no instance
    }

    public static <T> Iterator<T> array(T[] target) {
        if (null == target || 0 == target.length)
            return EmptyIterator.<T> get();
        return new ArrayIterator<T>(target);
    }

    public static <T> Iterator<T> empyt() {
        return EmptyIterator.<T> get();
    }

    @SafeVarargs
    public static <T> Iterator<T> concatenate(Iterator<? extends T>... is) {
        if (0 == is.length)
            return EmptyIterator.<T> get();
        return new CombinedIterator<T>(is);
    }

    public static <T> Iterator<T> concatenate(Collection<Iterator<? extends T>> is) {
        if (is.isEmpty())
            return EmptyIterator.<T> get();
        return new CombinedIterator<T>(is);
    }
}
