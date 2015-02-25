package de.ust.skill.common.java.internal.fieldTypes;

import java.io.IOException;
import java.util.HashMap;

import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.java.internal.StringPool;
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
    public String toString() {
        return "annotation";
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
}
