package de.ust.skill.common.java.internal;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.ust.skill.common.java.internal.parts.Chunk;
import de.ust.skill.common.java.iterators.Iterators;

/**
 * The base of a type hierarchy. Contains optimized representations of data compared to sub pools.
 * 
 * @author Timm Felden
 * @param <T>
 */
public class BasePool<T extends SkillObject> extends StoragePool<T, T> {

    /**
     * workaround for fucked-up generic array types
     * 
     * @return an empty array that is used as initial value of data
     * @note has to be overridden by each concrete base pool
     */
    @SuppressWarnings({ "static-method", "unchecked" })
    protected T[] newArray(int size) {
        return (T[]) new SkillObject[size];
    }

    /**
     * instances read from disk
     * 
     * @note manual type erasure required for consistency
     */
    protected T[] data = newArray(0);

    /**
     * the owner is set once by the SkillState.finish method!
     */
    protected SkillState owner = null;

    public BasePool(int poolIndex, String name, Set<String> knownFields, FieldDeclaration<?, T>[] autoFields) {
        super(poolIndex, name, null, knownFields, autoFields);
    }

    @Override
    public SkillState owner() {
        return owner;
    }

    /**
     * can only be invoked once by the skill state constructor!
     */
    public void setOwner(SkillState owner) {
        assert null == this.owner : "owner can only be set once";
        assert null != owner : "owner can not be null";
        this.owner = owner;

    }

    @SuppressWarnings("null")
    @Override
    public T getByID(long index) {
        if (0 == index)
            return null;
        return data[(int) index - 1];
    }

    /**
     * increase size of data array. Invoked by file parser only!
     */
    void resizeData(int increase) {
        data = Arrays.copyOf(data, data.length + increase);
    }

    /**
     * Static instances of base pool deal with unknown types only!
     */
    @Override
    public boolean insertInstance(int skillID) {
        int i = skillID - 1;
        if (null != data[i])
            return false;

        @SuppressWarnings("unchecked")
        T r = (T) (new SkillObject.SubType(this, skillID));
        data[i] = r;
        staticData.add(r);
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.<T> concatenate(Iterators.<T> array(basePool.data), newDynamicInstances());

    }

    /**
     * Internal use only!
     */
    public Iterator<T> dataViewIterator(int begin, int end) {
        return Iterators.<T> array(data, begin, end);
    }

    /**
     * compress new instances into the data array and update skillIDs
     */
    final void compress(int[] lbpoMap) {
        T[] d = newArray(size());
        int p = 0;
        Iterator<T> is = typeOrderIterator();
        while (is.hasNext()) {
            final T i = is.next();
            d[p++] = i;
            i.setSkillID(p);
        }
        data = d;
        updateAfterCompress(lbpoMap);
    }

    final void prepareAppend(Map<FieldDeclaration<?, ?>, Chunk> chunkMap) {
        boolean newInstances = newDynamicInstances().hasNext();

        // check if we have to append at all
        if (!newInstances && !blocks.isEmpty() && !fields.isEmpty()) {
            boolean done = true;
            for (FieldDeclaration<?, T> f : fields) {
                if (f.noDataChunk()) {
                    done = false;
                    break;
                }
            }
            if (done)
                return;
        }

        if (newInstances) {
            // we have to resize
            final T[] d = Arrays.copyOf(data, size());
            int i = data.length;

            final Iterator<T> is = newDynamicInstances();
            while (is.hasNext()) {
                final T instance = is.next();
                d[i++] = instance;
                instance.setSkillID(i);
            }
            data = d;
        }
        updateAfterPrepareAppend(chunkMap);
    }

}
