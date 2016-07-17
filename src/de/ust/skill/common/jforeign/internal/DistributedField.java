package de.ust.skill.common.jforeign.internal;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import de.ust.skill.common.jforeign.api.SkillException;
import de.ust.skill.common.jforeign.internal.parts.Block;
import de.ust.skill.common.jforeign.internal.parts.Chunk;
import de.ust.skill.common.jforeign.internal.parts.SimpleChunk;
import de.ust.skill.common.jvm.streams.MappedInStream;
import de.ust.skill.common.jvm.streams.MappedOutStream;

/**
 * The fields data is distributed into an array (for now its a hash map) holding its instances.
 */
public class DistributedField<T, Obj extends ISkillObject> extends FieldDeclaration<T, Obj> {

    public DistributedField(FieldType<T> type, String name, int index, StoragePool<Obj, ? super Obj> owner) {
        super(type, name, index, owner);
    }

    // data held as in storage pools
    // @note see paper notes for O(1) implementation
    protected HashMap<ISkillObject, T> data = new HashMap<>(); // Array[T]()
    protected HashMap<ISkillObject, T> newData = new HashMap<>();

    @Override
    public void read(ChunkEntry ce) {
        final MappedInStream in = ce.in;
        final Chunk last = ce.c;
        final ISkillObject[] d = owner.basePool.data;
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
            throw new PoolSizeMissmatchError(dataChunks.size() - 1, last.begin, last.end, this, e);
        }
        final long lastPosition = in.position();
        if (lastPosition - firstPosition != last.end - last.begin)
            throw new PoolSizeMissmatchError(dataChunks.size() - 1, in.position(), last.begin, last.end, this);

    }

    // TODO distributed fields need to be compressed as well!

    @SuppressWarnings("unchecked")
    @Override
    public long offset() {
        final Block range = owner.blocks.getLast();
        // @note order is not important, because we calculate offsets only!!!
        if (range.count == data.size())
            return type.calculateOffset(data.values());

        // we have to filter the right values
        return type.calculateOffset((Collection<T>) Arrays.asList(data.entrySet().stream()
                .filter(e -> range.contains(e.getKey().getSkillID())).map(e -> e.getValue()).toArray()));
    }

    @Override
    public void write(MappedOutStream out) {
        try {
            final ISkillObject[] d = owner.basePool.data;
            final Chunk last = lastChunk();
            if (last instanceof SimpleChunk) {
                final SimpleChunk c = (SimpleChunk) last;
                int low = (int) c.bpo;
                int high = (int) (c.bpo + c.count);
                for (int i = low; i < high; i++) {
                    type.writeSingleField(data.get(d[i]), out);
                }
            } else {
                for (Block bi : owner.blocks) {
                    final int end = (int) (bi.bpo + bi.count);
                    for (int i = (int) bi.bpo; i < end; i++) {
                        type.writeSingleField(data.get(d[i]), out);
                    }
                }
            }

        } catch (IOException e) {
            throw new SkillException("serialization of field " + name + " failed", e);
        }
    }

    @Override
    public T getR(ISkillObject ref) {
        if (-1 == ref.getSkillID())
            return newData.get(ref);

        return data.get(ref);
    }

    @Override
    public void setR(ISkillObject ref, T value) {
        if (-1 == ref.getSkillID())
            newData.put(ref, value);
        else
            data.put(ref, value);

    }

}
