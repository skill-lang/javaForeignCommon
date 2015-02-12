package de.ust.skill.common.java.internal.fieldTypes;

import java.util.ArrayList;

import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.jvm.streams.InStream;

public final class ConstantLengthArray<T> extends IntegerType<ArrayList<T>> {
    public final FieldType<T> groundType;
    public final long length;

    public ConstantLengthArray(long length, FieldType<T> groundType) {
        super(15);
        this.groundType = groundType;
        this.length = length;
    }

    @Override
    public ArrayList<T> readSingleField(InStream in) {
        ArrayList<T> rval = new ArrayList<>();
        for (int i = (int) length; i != 0; i--)
            rval.add(groundType.readSingleField(in));
        return rval;
    }

    @Override
    public String toString() {
        return groundType.toString() + "[" + length + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConstantLengthArray<?>)
            return length == ((ConstantLengthArray<?>) obj).length
                    && groundType.equals(((ConstantLengthArray<?>) obj).groundType);
        return false;
    }
}
