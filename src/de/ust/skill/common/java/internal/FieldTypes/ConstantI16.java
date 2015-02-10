package de.ust.skill.common.java.internal.FieldTypes;

public final class ConstantI16 extends ConstantIntegerType<Short> {
    public final short value;

    public ConstantI16(short value) {
        super(1);
        this.value = value;
    }

    @Override
    public Short value() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("const i16 = %04X", value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConstantI16)
            return value == ((ConstantI16) obj).value;
        return false;
    }
}
