package de.ust.skill.common.java.internal;

import java.util.Collection;
import java.util.Iterator;

import de.ust.skill.common.java.api.Access;
import de.ust.skill.common.java.api.FieldDeclaration;
import de.ust.skill.common.java.api.SkillFile;

/**
 * Toplevel implementation of all storage pools.
 * 
 * @author Timm Felden
 * @param <T1>
 * @param <T2>
 */
abstract public class StoragePool<T extends B, B extends SkillObject> extends FieldType<T> implements Access<T> {

    final String name;
    final StoragePool<? super T, B> superPool;

    @Override
    final public String name() {
        return name;
    }

    @Override
    public String superName() {
        if (null != superPool)
            return superPool.name;
        return null;
    }

    StoragePool(long poolIndex, String name, StoragePool<? super T, B> superPool) {
        super(32L + poolIndex);
        this.name = name;
        this.superPool = superPool;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean contains(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object[] toArray() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean add(T e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean remove(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public SkillFile owner() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<T> typeOrderIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<FieldDeclaration<?, T>> fields() {
        // TODO Auto-generated method stub
        return null;
    }

}
