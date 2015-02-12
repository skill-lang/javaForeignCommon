package de.ust.skill.common.java.internal.fieldTypes;

import de.ust.skill.common.jvm.streams.InStream;

public final class I8 extends IntegerType<Byte> {
    private final static I8 instance = new I8();

    public static I8 get() {
        return instance;
    }

    private I8() {
        super(7);
    }

    @Override
    public Byte readSingleField(InStream in) {
        return in.i8();
    }

    @Override
    public String toString() {
        return "i8";
    }
}
