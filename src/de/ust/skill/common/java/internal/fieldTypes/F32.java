package de.ust.skill.common.java.internal.fieldTypes;

import java.io.IOException;

import de.ust.skill.common.jvm.streams.InStream;
import de.ust.skill.common.jvm.streams.OutStream;

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

    @Override
    public void writeSingleField(Float data, OutStream out) throws IOException {
        out.f32(data);
    }
}
