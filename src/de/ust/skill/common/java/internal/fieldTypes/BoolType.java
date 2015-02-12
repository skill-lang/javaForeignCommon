package de.ust.skill.common.java.internal.fieldTypes;

import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.jvm.streams.InStream;

public final class BoolType extends FieldType<Boolean> {
    private static final BoolType instance = new BoolType();

    public static BoolType get() {
        return instance;
    }

    private BoolType() {
        super(6);
    }


    @Override
    public Boolean readSingleField(InStream in) {
        return in.bool();
    }

    @Override
    public String toString() {
        return "bool";
    }
}
