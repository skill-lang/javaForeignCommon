package de.ust.skill.common.java.internal;

import java.nio.BufferUnderflowException;
import java.util.HashMap;

import de.ust.skill.common.java.internal.parts.Block;
import de.ust.skill.common.java.internal.parts.Chunk;
import de.ust.skill.common.java.internal.parts.SimpleChunk;
import de.ust.skill.common.jvm.streams.MappedInStream;

/**
 * The fields data is distributed into an array (for now its a hash map) holding
 * its instances.
 */
public class DistributedField<T, Obj extends SkillObject> extends FieldDeclaration<T, Obj> {

    public DistributedField(FieldType<T> type, String name, int index, StoragePool<Obj, ? super Obj> owner) {
        super(type, name, index, owner);
    }

    // data held as in storage pools
    // @note see paper notes for O(1) implementation
    protected HashMap<SkillObject, T> data = new HashMap<>(); // Array[T]()
    protected HashMap<SkillObject, T> newData = new HashMap<>();

    @Override
    public void read(MappedInStream in, Chunk last) {
        SkillObject[] d = owner.basePool.data;
        final long firstPosition = in.position();
        try {
            if (last instanceof SimpleChunk) {
                SimpleChunk c = (SimpleChunk) last;
                int low = (int) c.bpo;
                int high = (int) (c.bpo + c.count);
                for (int i = low; i < high; i++) {
                    data.put(d[i], type.readSingleField(in));
                }
            } else {
                for (Block bi : owner.blocks) {
                    final int end = (int) (bi.bpo + bi.count);
                    for (int i = (int) bi.bpo; i < end; i++) {
                        data.put(d[i], type.readSingleField(in));
                    }
                }
            }
        } catch (BufferUnderflowException e) {
            throw new PoolSizeMissmatchError(dataChunks.size() - 1, last.begin, last.end, this);
        }
        final long lastPosition = in.position();
        if (lastPosition - firstPosition != last.end - last.begin)
            throw new PoolSizeMissmatchError(dataChunks.size() - 1, last.begin, last.end, this);

    }

    @Override
    public T getR(Obj ref) {
        if (-1 == ref.skillID)
            return newData.get(ref);

        return data.get(ref);
    }

    @Override
    public void setR(Obj ref, T value) {
        if (-1 == ref.skillID)
            newData.put(ref, value);
        else
            data.put(ref, value);

    }
}
