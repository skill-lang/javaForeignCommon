package de.ust.skill.common.java.iterators;

import java.util.Collection;
import java.util.Iterator;

/**
 * Adds a size to fake array views and makes them iterable using the respective iterator.s
 * 
 * @author Timm Felden
 */
final public class IterableArrayView<T> implements Collection<T> {

    final private T[] target;
    final private int begin;
    final private int end;

    public IterableArrayView(T[] target, int begin, int end) {
        this.target = target;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public int size() {
        return end - begin;
    }

    @Override
    public boolean isEmpty() {
        throw new NoSuchMethodError("not provided by array views");
    }

    @Override
    public boolean contains(Object o) {
        throw new NoSuchMethodError("not provided by array views");
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.array(target, begin, end);
    }

    @Override
    public Object[] toArray() {
        throw new NoSuchMethodError("not provided by array views");
    }

    @Override
    public <R> R[] toArray(R[] a) {
        throw new NoSuchMethodError("not provided by array views");
    }

    @Override
    public boolean add(T e) {
        throw new NoSuchMethodError("not provided by array views");
    }

    @Override
    public boolean remove(Object o) {
        throw new NoSuchMethodError("not provided by array views");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new NoSuchMethodError("not provided by array views");
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new NoSuchMethodError("not provided by array views");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new NoSuchMethodError("not provided by array views");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new NoSuchMethodError("not provided by array views");
    }

    @Override
    public void clear() {
        throw new NoSuchMethodError("not provided by array views");

    }

}
