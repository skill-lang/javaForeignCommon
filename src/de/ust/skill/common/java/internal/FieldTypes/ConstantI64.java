package de.ust.skill.common.java.internal.FieldTypes;

public final class ConstantI64 extends ConstantIntegerType<Long> {
    public final long value;

    public ConstantI64(long value) {
        super(3);
        this.value = value;
    }

    @Override
    public Long value() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("const i64 = %016X", value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConstantI64)
            return value == ((ConstantI64) obj).value;
        return false;
    }
}
