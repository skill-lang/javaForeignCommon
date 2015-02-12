package de.ust.skill.common.java.internal.fieldTypes;

import de.ust.skill.common.jvm.streams.InStream;

public final class I64 extends IntegerType<Long> {
    private final static I64 instance = new I64();

    public static I64 get() {
        return instance;
    }

    private I64() {
        super(10);
    }

    @Override
    public Long readSingleField(InStream in) {
        return in.i64();
    }

    @Override
    public String toString() {
        return "i64";
    }
}
