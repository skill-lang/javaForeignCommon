package de.ust.skill.common.java.internal.fieldTypes;

import de.ust.skill.common.jvm.streams.InStream;

public final class I16 extends IntegerType<Short> {
    private final static I16 instance = new I16();

    public static I16 get() {
        return instance;
    }

    private I16() {
        super(8);
    }

    @Override
    public Short readSingleField(InStream in) {
        return in.i16();
    }

    @Override
    public String toString() {
        return "i16";
    }
}
