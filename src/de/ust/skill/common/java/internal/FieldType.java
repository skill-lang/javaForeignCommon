package de.ust.skill.common.java.internal;

import de.ust.skill.common.jvm.streams.InStream;

/**
 * Top level implementation of a field type, the runtime representation of a
 * fields type.
 * 
 * @param <T>
 *            the Java type to represent instances of this field type
 * @note representation of the type system relies on invariants and heavy abuse
 *       of type erasure
 * @author Timm Felden
 */
abstract public class FieldType<T> {

    final int typeID;

    protected FieldType(int typeID) {
        this.typeID = typeID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FieldType<?>)
            return ((FieldType<?>) obj).typeID == typeID;
        return false;
    }

    @Override
    public final int hashCode() {
        return typeID;
    }

    /**
     * Takes one T out of the stream.
     *
     * @note this function has to be implemented by FieldTypes because of limits
     *       of the Java type system (and any other sane type system)
     * @note intended for internal usage only!
     */
    public abstract T readSingleField(InStream in);
}
