package de.ust.skill.common.jforeign.internal;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.ust.skill.common.jforeign.internal.fieldDeclarations.AutoField;
import de.ust.skill.common.jforeign.internal.parts.Block;
import de.ust.skill.common.jforeign.internal.parts.Chunk;
import de.ust.skill.common.jforeign.iterators.Iterators;

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

    public BasePool(int poolIndex, String name, Set<String> knownFields, AutoField<?, T>[] autoFields) {
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

    @Override
    final public T getByID(long ID) {
        int index = (int) ID - 1;
        if (index < 0 || data.length <= index)
            return null;
        return data[index];
    }

    /**
     * increase size of data array. Invoked by file parser only!
     */
    void resizeData() {
        data = Arrays.copyOf(data, data.length + (int) blocks.getLast().count);
    }

    /**
     * Static instances of base pool deal with unknown types only!
     */
    @Override
    public void insertInstances() {
        final Block last = blocks.getLast();
        int i = (int) last.bpo;
        int high = (int) (last.bpo + last.count);
        while (i < high) {
            if (null != data[i])
                return;

            @SuppressWarnings("unchecked")
            T r = (T) (new SkillObject.SubType(this, i + 1));
            data[i] = r;
            staticData.add(r);

            i += 1;
        }
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
        // fix to calculate correct size in acceptable time
        fixed(true);

        // create our part of the lbpo map
        makeLBPOMap(this, lbpoMap, 0);

        // from now on, size will take deleted objects into account, thus d may in fact be smaller then data!
        T[] d = newArray(size());
        int p = 0;
        Iterator<T> is = typeOrderIterator();
        while (is.hasNext()) {
            final T i = is.next();
            if (i.getSkillID() != 0) {
                d[p++] = i;
                i.setSkillID(p);
            }
        }
        data = d;
        updateAfterCompress(lbpoMap);
    }

    /**
     * creates an lbpo map by recursively adding the local base pool offset to the map and adding all sub pools
     * afterwards
     */
    private final static int makeLBPOMap(StoragePool<?, ?> p, int[] lbpoMap, int next) {
        lbpoMap[p.typeID - 32] = next;
        int result = next + p.staticSize() - p.deletedCount;
        for (SubPool<?, ?> sub : p.subPools) {
            result = makeLBPOMap(sub, lbpoMap, result);
        }
        return result;
    }

    final void prepareAppend(Map<FieldDeclaration<?, ?>, Chunk> chunkMap) {
        boolean newInstances = newDynamicInstances().hasNext();

        // check if we have to append at all
        if (!newInstances && !blocks.isEmpty() && !dataFields.isEmpty()) {
            boolean done = true;
            for (FieldDeclaration<?, T> f : dataFields) {
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
