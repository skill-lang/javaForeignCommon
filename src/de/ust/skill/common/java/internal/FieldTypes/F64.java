package de.ust.skill.common.java.internal.FieldTypes;

import de.ust.skill.common.jvm.streams.InStream;

public final class F64 extends IntegerType<Double> {
    private final static F64 instance = new F64();

    public static F64 get() {
        return instance;
    }

    private F64() {
        super(13);
    }

    @Override
    public Double readSingleField(InStream in) {
        return in.f64();
    }

    @Override
    public String toString() {
        return "f64";
    }
}
