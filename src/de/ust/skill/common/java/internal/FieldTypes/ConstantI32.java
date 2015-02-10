package de.ust.skill.common.java.internal.FieldTypes;

public final class ConstantI32 extends ConstantIntegerType<Integer> {
    public final int value;

    public ConstantI32(int value) {
        super(2);
        this.value = value;
    }

    @Override
    public Integer value() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("const i32 = %08X", value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConstantI32)
            return value == ((ConstantI32) obj).value;
        return false;
    }
}
