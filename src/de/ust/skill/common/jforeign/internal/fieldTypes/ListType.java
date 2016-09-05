package de.ust.skill.common.jforeign.internal.fieldTypes;

import java.util.LinkedList;
import java.util.List;

import de.ust.skill.common.jforeign.internal.FieldType;
import de.ust.skill.common.jvm.streams.InStream;

public final class ListType<T> extends SingleArgumentType<List<T>, T> {

    public ListType(FieldType<T> groundType) {
        super(18, groundType);
    }

    @Override
    public List<T> readSingleField(InStream in) {
        LinkedList<T> rval = new LinkedList<>();
        for (int i = (int) in.v64(); i != 0; i--)
            rval.add(groundType.readSingleField(in));
        return rval;
    }

    public void readSingleField(InStream in, List<T> rval) {
        for (int i = (int) in.v64(); i != 0; i--)
            rval.add(groundType.readSingleField(in));
    }

    @Override
    public String toString() {
        return "list<" + groundType.toString() + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ListType<?>)
            return groundType.equals(((ListType<?>) obj).groundType);
        return false;
    }
}
