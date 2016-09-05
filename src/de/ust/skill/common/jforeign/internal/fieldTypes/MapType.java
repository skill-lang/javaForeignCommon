package de.ust.skill.common.jforeign.internal.fieldTypes;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.ust.skill.common.jforeign.internal.FieldType;
import de.ust.skill.common.jvm.streams.InStream;
import de.ust.skill.common.jvm.streams.OutStream;

public final class MapType<K, V> extends CompoundType<Map<K, V>> {
    public final FieldType<K> keyType;
    public final FieldType<V> valueType;

    public MapType(FieldType<K> keyType, FieldType<V> valueType) {
        super(20);
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    public Map<K, V> readSingleField(InStream in) {
        Map<K, V> rval = new HashMap<>();
        for (int i = (int) in.v64(); i != 0; i--)
            rval.put(keyType.readSingleField(in), valueType.readSingleField(in));
        return rval;
    }

    public void readSingleField(InStream in, Map<K, V> rval) {
        for (int i = (int) in.v64(); i != 0; i--)
            rval.put(keyType.readSingleField(in), valueType.readSingleField(in));
    }

    @Override
    public long calculateOffset(Collection<Map<K, V>> xs) {
        long result = 0L;
        for (Map<K, V> x : xs) {
            result += V64.singleV64Offset(x.size());
            result += keyType.calculateOffset(x.keySet());
            result += valueType.calculateOffset(x.values());
        }

        return result;
    }

    @Override
    public void writeSingleField(Map<K, V> data, OutStream out) throws IOException {
        out.v64(data.size());
        for (Entry<K, V> e : data.entrySet()) {
            keyType.writeSingleField(e.getKey(), out);
            valueType.writeSingleField(e.getValue(), out);
        }
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
