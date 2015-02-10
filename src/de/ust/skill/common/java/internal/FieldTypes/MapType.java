package de.ust.skill.common.java.internal.FieldTypes;

import java.util.HashMap;

import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.jvm.streams.InStream;

public final class MapType<K, V> extends IntegerType<HashMap<K, V>> {
    public final FieldType<K> keyType;
    public final FieldType<V> valueType;

    public MapType(FieldType<K> keyType, FieldType<V> valueType) {
        super(20);
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    public HashMap<K, V> readSingleField(InStream in) {
        HashMap<K, V> rval = new HashMap<>();
        for (int i = (int) in.v64(); i != 0; i--)
            rval.put(keyType.readSingleField(in), valueType.readSingleField(in));
        return rval;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("map<");
        sb.append(keyType).append(", ").append(valueType).append(">");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MapType<?, ?>)
            return keyType.equals(((MapType<?, ?>) obj).keyType) && valueType.equals(((MapType<?, ?>) obj).valueType);
        return false;
    }
}
