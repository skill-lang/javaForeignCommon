package de.ust.skill.common.java.internal;

import java.util.HashSet;
import java.util.LinkedList;

import de.ust.skill.common.java.api.Access;
import de.ust.skill.common.java.internal.parts.Chunk;
import de.ust.skill.common.java.restrictions.FieldRestriction;
import de.ust.skill.common.jvm.streams.MappedInStream;

/**
 * Actual implementation as used by all bindings.
 * 
 * @author Timm Felden
 */
abstract public class FieldDeclaration<T, Obj extends SkillObject> implements
        de.ust.skill.common.java.api.FieldDeclaration<T, Obj> {

    /**
     * @note types may change during file parsing. this may seem like a hack,
     *       but it makes file parser implementation a lot easier, because there
     *       is no need for two mostly similar type hierarchy implementations
     */
    FieldType<T> type;

    /**
     * skill name of this
     */
    final String name;

    @Override
    public String name() {
        return name;
    }

    /**
     * index as used in the file
     * 
     * @note this is 0 iff the field will not be serialized (auto & skillID)
     */
    final long index;

    /**
     * the enclosing storage pool
     */
    final StoragePool<Obj, ? super Obj> owner;

    /**
     * Restriction handling.
     */
    public final HashSet<FieldRestriction<T>> restrictions = new HashSet<>();

    @SuppressWarnings("unchecked")
    public <U> void addRestriction(FieldRestriction<U> r) {
        restrictions.add((FieldRestriction<T>) r);
    }

    /**
     * Check consistency of restrictions on this field.
     */
    void check() {
        if (!restrictions.isEmpty())
            for (Obj x : owner)
                for (FieldRestriction<T> r : restrictions)
                    r.check(x.get(this));
    }

    @Override
    public Access<Obj> owner() {
        return owner;
    }

    public FieldDeclaration(FieldType<T> type, String name, long index, StoragePool<Obj, ? super Obj> owner) {
        this.type = type;
        this.name = name.intern(); // we will switch on names, thus we need to
                                   // intern them
        this.index = index;
        this.owner = owner;
    }

    @Override
    public String toString() {
        return type.toString() + " " + name;
    }

    /**
     * Field declarations are equal, iff their names and types are equal.
     * 
     * @note This makes fields of unequal enclosing types equal!
     */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof FieldDeclaration) {
            return ((FieldDeclaration<?, ?>) obj).name().equals(name)
                    && ((FieldDeclaration<?, ?>) obj).type.equals(type);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return type.hashCode() ^ name.hashCode();
    }

    /**
     * Data chunk information, as it is required for parsing of field data.
     */
    protected final LinkedList<Chunk> dataChunks = new LinkedList<>();

    public final void addChunk(Chunk chunk) {
        dataChunks.add(chunk);
    }

    final void addOffsetToLastChunk(long offset) {
        Chunk c = dataChunks.getLast();
        c.begin += offset;
        c.end += offset;
    }

    final boolean noDataChunk() {
        return dataChunks.isEmpty();
    }

    final Chunk lastChunk() {
        return dataChunks.getLast();
    }

    /**
     * Read data from a mapped input stream and set it accordingly
     */
    public abstract void read(MappedInStream in);
}
