package de.ust.skill.common.java.internal.fieldTypes;

import java.io.IOException;
import java.util.Collection;

import de.ust.skill.common.java.internal.parts.Block;
import de.ust.skill.common.jvm.streams.InStream;
import de.ust.skill.common.jvm.streams.OutStream;

public final class F32 extends FloatType<Float> {
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
    public long calculateOffset(Collection<Float> xs, Block range) {
        return 4 * range.count;
    }

    @Override
    public void writeSingleField(Float data, OutStream out) throws IOException {
        out.f32(data);
    }

    @Override
    public String toString() {
        return "f32";
    }
}
