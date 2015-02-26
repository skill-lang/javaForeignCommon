package de.ust.skill.common.java.internal.fieldTypes;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.java.internal.parts.Block;
import de.ust.skill.common.jvm.streams.OutStream;

/**
 * Super class of all container types with one type argument.
 * 
 * @author Timm Felden
 */
public abstract class SingleArgumentType<T extends Collection<Base>, Base> extends CompoundType<T> {
    public final FieldType<Base> groundType;

    public SingleArgumentType(int typeID, FieldType<Base> groundType) {
        super(typeID);
        this.groundType = groundType;
    }

    @Override
    public long calculateOffset(Collection<T> xs, Block range) {
        Iterator<T> is = xs.iterator();

        // skip begin
        if (null != range)
            for (int i = (int) range.bpo; i != 0; i--)
                is.next();

        long result = 0L;
        for (int i = null == range ? xs.size() : (int) range.count; i != 0; i--) {
            T x = is.next();
            result += V64.singleV64Offset(x.size());
            result += groundType.calculateOffset(x, null);
        }

        return result;
    }

    @Override
    public void writeSingleField(T elements, OutStream out) throws IOException {
        out.v64(elements.size());
        for (Base e : elements)
            groundType.writeSingleField(e, out);
    }
}
