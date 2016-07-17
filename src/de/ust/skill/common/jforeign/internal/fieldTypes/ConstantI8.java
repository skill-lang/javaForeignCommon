package de.ust.skill.common.jforeign.internal.fieldTypes;

public final class ConstantI8 extends ConstantIntegerType<Byte> {
    public final byte value;

    public ConstantI8(byte value) {
        super(0);
        this.value = value;
    }

    @Override
    public Byte value() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("const i8 = %02X", value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConstantI8)
            return value == ((ConstantI8) obj).value;
        return false;
    }
}
