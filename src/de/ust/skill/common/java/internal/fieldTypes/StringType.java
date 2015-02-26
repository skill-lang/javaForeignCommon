package de.ust.skill.common.java.internal.fieldTypes;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.java.internal.StringPool;
import de.ust.skill.common.java.internal.parts.Block;
import de.ust.skill.common.jvm.streams.InStream;
import de.ust.skill.common.jvm.streams.OutStream;

/**
 * String types are instantiated once per state.
 * 
 * @author Timm Felden
 */
public final class StringType extends FieldType<String> implements ReferenceType {

    private final StringPool strings;
    protected HashMap<String, Integer> stringIDs = null;

    public StringType(StringPool strings) {
        super(14);
        this.strings = strings;
        assert strings != null;
    }

    @Override
    public String readSingleField(InStream in) {
        return strings.get(in.v64());
    }

    @Override
    public long calculateOffset(Collection<String> xs, Block range) {
        // shortcut for small string pools
        if (stringIDs.size() < 128)
            return range.count;

        Iterator<String> is = xs.iterator();

        // skip begin
        if (null != range)
            for (int i = (int) range.bpo; i != 0; i--)
                is.next();

        long result = 0L;
        for (int i = null == range ? xs.size() : (int) range.count; i != 0; i--) {
            String s = is.next();
            result += V64.singleV64Offset(stringIDs.get(s));
        }

        return result;
    }

    long singleOffset(String name) {
        return V64.singleV64Offset(stringIDs.get(name));
    }

    @Override
    public void writeSingleField(String v, OutStream out) throws IOException {
        out.v64(stringIDs.get(v));

    }

    /**
     * internal use only!
     * 
     * @note invoked at begin of serialization
     */
    public HashMap<String, Integer> resetIDs() {
        stringIDs = new HashMap<>();
        return stringIDs;
    }

    /**
     * internal use only!
     * 
     * @note invoked at end of serialization
     */
    public void clearIDs() {
        stringIDs = null;
    }

    @Override
    public String toString() {
        return "annotation";
    }
}
