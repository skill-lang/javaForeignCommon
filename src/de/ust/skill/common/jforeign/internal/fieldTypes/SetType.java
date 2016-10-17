package de.ust.skill.common.jforeign.internal.fieldTypes;

import java.util.HashSet;
import java.util.Set;

import de.ust.skill.common.jforeign.internal.FieldType;
import de.ust.skill.common.jvm.streams.InStream;

public final class SetType<T, U extends Set<T>> extends SingleArgumentType<U, T> {

    public SetType(FieldType<T> groundType) {
        super(19, groundType);
    }

    @Override
    public U readSingleField(InStream in) {
        U rval = (U) new HashSet<>();
        for (int i = (int) in.v64(); i != 0; i--)
            rval.add(groundType.readSingleField(in));
        return rval;
    }

    public void readSingleField(InStream in, Set<T> rval) {
        for (int i = (int) in.v64(); i != 0; i--)
            rval.add(groundType.readSingleField(in));
    }

    @Override
    public String toString() {
        return "set<" + groundType.toString() + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SetType<?, ?>)
            return groundType.equals(((SetType<?, ?>) obj).groundType);
        return false;
    }
}
