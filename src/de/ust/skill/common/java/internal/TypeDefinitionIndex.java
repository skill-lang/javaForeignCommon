package de.ust.skill.common.java.internal;

import de.ust.skill.common.jvm.streams.InStream;

/**
 * This is an intermediate representation of a type until the header is
 * processed completely.
 */
public final class TypeDefinitionIndex<T> extends FieldType<T> {

    /**
     * @param index
     *            the index of the argument type in a file starting from 0
     */
    protected TypeDefinitionIndex(long index) {
        super(index + 32L);
    }

    @Override
    public T readSingleField(InStream in) {
        throw new Error("intended to be dead code!");
    }

    @Override
    public String toString() {
        return "<type definition index: " + typeID + ">";
    }

    @Override
    public boolean equals(Object obj) {
        throw new Error("intended to be dead code!");
    }
}
