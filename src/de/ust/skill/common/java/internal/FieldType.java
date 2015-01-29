package de.ust.skill.common.java.internal;

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

    final long typeID;

    FieldType(long typeID) {
        this.typeID = typeID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FieldType<?>)
            return ((FieldType<?>) obj).typeID == typeID;
        return false;
    }

    @Override
    public int hashCode() {
        return (int) typeID;
    }
}
