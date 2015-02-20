package de.ust.skill.common.java.iterators;

import java.util.Collection;
import java.util.Iterator;

/**
 * Advanced iterator handling.
 * 
 * @author Timm Felden
 */
public final class Iterators {
    private Iterators() {
        // there is no instance
    }

    public static <T> Iterator<T> array(T[] target) {
        if (null == target || 0 == target.length)
            return EmptyIterator.<T> get();
        return new ArrayIterator<T>(target);
    }

    /**
     * iterates over a view of an array
     * 
     * @param target
     *            viewed array
     * @param begin
     *            first index to be used
     * @param end
     *            first index _not_ to be used
     * @return iterator over the supplied range
     */
    public static <T> Iterator<T> array(T[] target, int begin, int end) {
        if (null == target || 0 == target.length || end <= begin)
            return EmptyIterator.<T> get();
        return new ArrayViewIterator<T>(target, begin, end);
    }

    /**
     * iterates over a view of an array
     * 
     * @param target
     *            viewed array
     * @param begin
     *            first index to be used
     * @param end
     *            first index _not_ to be used
     * @return iterator over the supplied range
     */
    public static <T extends B, B> Iterator<T> fakeArray(B[] target, int begin, int end) {
        if (null == target || 0 == target.length || end <= begin)
            return EmptyIterator.<T> get();
        return new FakeArrayViewIterator<T, B>(target, begin, end);
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
