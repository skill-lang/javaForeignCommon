package de.ust.skill.common.java.internal.FieldTypes;

import de.ust.skill.common.jvm.streams.InStream;

public final class I32 extends IntegerType<Integer> {
    private final static I32 instance = new I32();

    public static I32 get() {
        return instance;
    }

    private I32() {
        super(9);
    }

    @Override
    public Integer readSingleField(InStream in) {
        return in.i32();
    }

    @Override
    public String toString() {
        return "i32";
    }
}
