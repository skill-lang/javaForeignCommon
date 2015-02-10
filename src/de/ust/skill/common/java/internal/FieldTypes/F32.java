package de.ust.skill.common.java.internal.FieldTypes;

import de.ust.skill.common.jvm.streams.InStream;

public final class F32 extends IntegerType<Float> {
    private final static F32 instance = new F32();

    public static F32 get() {
        return instance;
    }

    private F32() {
        super(12);
    }

    @Override
    public Float readSingleField(InStream in) {
        return in.f32();
    }

    @Override
    public String toString() {
        return "f32";
    }
}
