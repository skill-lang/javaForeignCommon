package de.ust.skill.common.java.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import de.ust.skill.common.java.api.Access;
import de.ust.skill.common.java.api.SkillFile;
import de.ust.skill.common.java.internal.fieldTypes.ReferenceType;
import de.ust.skill.common.java.internal.parts.Block;
import de.ust.skill.common.java.iterators.Iterators;
import de.ust.skill.common.java.restrictions.FieldRestriction;
import de.ust.skill.common.jvm.streams.InStream;

/**
 * Top level implementation of all storage pools.
 * 
 * @author Timm Felden
 * @param <T>
 *            static type of instances
 * @param <B>
 *            base type of this hierarchy
 * @note Storage pools must be created in type order!
 * @note We do not guarantee functional correctness if instances from multiple
 *       skill files are mixed. Such usage will likely break at least one of the
 *       files.
 */
abstract public class StoragePool<T extends B, B extends SkillObject> extends FieldType<T> implements Access<T>,
        ReferenceType {

    /**
     * Builder for new instances of the pool.
     * 
     * @author Timm Felden
     * @todo revisit implementation after the pool is completely implemented.
     *       Having an instance as constructor argument is questionable.
     */
    protected static abstract class Builder<T> {
        protected StoragePool<T, ? super T> pool;
        protected T instance;

        protected Builder(StoragePool<T, ? super T> pool, T instance) {
            this.pool = pool;
            this.instance = instance;
        }

        public T make() {
            pool.add(instance);
            return instance;
        }
    }

    final String name;
    final StoragePool<? super T, B> superPool;
    final BasePool<B> basePool;
    final ArrayList<SubPool<? extends T, B>> subPools = new ArrayList<>();
    public final Set<String> knownFields;

    /**
     * used by generated file parsers
     */
    public StoragePool<? super T, B> superPool() {
        return superPool;
    }

    /**
     * used by generated file parsers / known fields
     */
    public BasePool<B> basePool() {
        return basePool;
    }

    /**
     * @note the fieldIndex is either identical to the position in fields or it
     *       is an auto field
     */
    protected final ArrayList<FieldDeclaration<?, T>> fields;

    /**
     * The block layout of instances of this pool.
     */
    ArrayList<Block> blocks = new ArrayList<>();

    /**
     * All stored objects, which have exactly the type T. Objects are stored as
     * arrays of field entries. The types of the respective fields can be
     * retrieved using the fieldTypes map.
     */
    final ArrayList<T> newObjects = new ArrayList<>();

    protected final Iterator<T> newDynamicInstances() {
        LinkedList<Iterator<? extends T>> is = new LinkedList<>();
        if(!newObjects.isEmpty())
            is.add(newObjects.iterator());
        for (SubPool<? extends T, B> sub : subPools) {
            Iterator<? extends T> subIter = sub.newDynamicInstances();
            if (!subIter.hasNext())
                is.add(subIter);
        }
        return Iterators.<T> concatenate(is);
    }

    /**
     * the number of instances of exactly this type, excluding sub-types
     * 
     * @return size excluding subtypes
     */
    final public long staticSize() {
        return staticData.size() + newObjects.size();
    }

    final Iterator<T> staticInstances() {
        return Iterators.<T> concatenate(staticData.iterator(), newObjects.iterator());
    }

    /**
     * the number of static instances loaded from the file
     */
    final protected ArrayList<T> staticData = new ArrayList<>();

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

    /**
     * @note the unchecked cast is required, because we can not supply this as
     *       an argument in a super constructor, thus the base pool can not be
     *       an argument to the constructor. The cast will never fail anyway.
     */
    @SuppressWarnings("unchecked")
    StoragePool(long poolIndex, String name, StoragePool<? super T, B> superPool, Set<String> knownFields) {
        super(32L + poolIndex);
        this.name = name;
        this.superPool = superPool;
        this.basePool = null == superPool ? (BasePool<B>) this : superPool.basePool;
        this.knownFields = knownFields;
        fields = new ArrayList<>(1 + knownFields.size());
    }

    /**
     * @return the instance matching argument skill id
     */
    public abstract T getByID(long index);

    @Override
    public final T readSingleField(InStream in) {
        return getByID(in.v64());
    }

    /**
     * @return size including subtypes
     */
    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return size() != 0;
    }

    @Override
    public boolean contains(Object o) {
        throw new Error("TODO");
    }

    @Override
    public Object[] toArray() {
        throw new Error("TODO");
    }

    @Override
    public <U> U[] toArray(U[] a) {
        throw new Error("TODO");
    }

    @Override
    public boolean add(T e) {
        throw new Error("TODO");
    }

    @Override
    public boolean remove(Object o) {
        throw new Error("TODO");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object i : c)
            if (!contains(i))
                return false;
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        for (T i : c)
            changed |= add(i);
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object i : c)
            changed |= remove(i);
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        // TODO provide an implementation that works for single state usage
        // scenario
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        // TODO there are more efficient implementations then that
        removeAll(this);
    }

    @Override
    public SkillFile owner() {
        return basePool.owner();
    }

    @Override
    public Iterator<T> typeOrderIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<? extends de.ust.skill.common.java.api.FieldDeclaration<?, T>> fields() {
        return fields;
    }

    /**
     * insert a new T with default values and the given skillID into the pool
     *
     * @note implementation relies on an ascending order of insertion
     */
    abstract boolean insertInstance(int skillID);

    <R> FieldDeclaration<R, T> addField(int ID, FieldType<R> type, String name,
            HashSet<FieldRestriction<?>> restrictions) {
        FieldDeclaration<R, T> f = new LazyField<R, T>(type, name, ID, this);
        for (FieldRestriction<?> r : restrictions)
            f.addRestriction(r);
        fields.add(f);
        return f;

    }

    /**
     * used internally for state allocation
     */
    @SuppressWarnings("static-method")
    public void addKnownField(String name) {
        throw new Error("Arbitrary storage pools know no fields!");
    }

    /**
     * used internally for type forest construction
     */
    public <S extends T> StoragePool<S, B> makeSubPool(int index, String name) {
        return new SubPool<S, B>(index, name, this, Collections.emptySet());
    }
}
