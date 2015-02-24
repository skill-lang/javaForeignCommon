package de.ust.skill.common.java.internal.fieldTypes;

import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.jvm.streams.InStream;

/**
 * Constant Integers.
 * 
 * @author Timm Felden
 */
public abstract class ConstantIntegerType<T> extends FieldType<T> {
    protected ConstantIntegerType(int typeID) {
        super(typeID);
    }

    public abstract T value();

    public final void expect(T arg) {
        assert value() == arg;
    }

    @Override
    public final T readSingleField(InStream in) {
        return value();
    }
}
