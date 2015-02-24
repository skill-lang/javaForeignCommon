package de.ust.skill.common.java.internal.fieldTypes;

import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.java.internal.StringPool;
import de.ust.skill.common.jvm.streams.InStream;

/**
 * String types are instantiated once per state.
 * 
 * @author Timm Felden
 */
public final class StringType extends FieldType<String> implements ReferenceType {

    private final StringPool strings;

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
}
