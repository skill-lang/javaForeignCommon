package de.ust.skill.common.java.internal.FieldTypes;

import de.ust.skill.common.jvm.streams.InStream;

public final class V64 extends IntegerType<Long> {
    private final static V64 instance = new V64();

    public static V64 get() {
        return instance;
    }

    private V64() {
        super(11);
    }

    @Override
    public Long readSingleField(InStream in) {
        return in.v64();
    }

    @Override
    public String toString() {
        return "v64";
    }
}
