package de.ust.skill.common.java.internal.fieldTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.java.internal.NamedType;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.parts.Block;
import de.ust.skill.common.jvm.streams.InStream;
import de.ust.skill.common.jvm.streams.OutStream;

/**
 * Annotation types are instantiated once per state.
 * 
 * @author Timm Felden
 */
public final class Annotation extends FieldType<SkillObject> implements ReferenceType {

    private final StringType strings;
    private final ArrayList<StoragePool<?, ?>> types;

    /**
     * @param types
     *            the array list containing all types valid inside of a state
     * @note types can grow after passing the pointer to the annotation type. This behavior is required in order to
     *       implement reflective annotation parsing correctly.
     * @note can not take a state as argument, because it may not exist yet
     */
    public Annotation(ArrayList<StoragePool<?, ?>> types, StringType strings) {
        super(5);
        this.types = types;
        this.strings = strings;
        assert types != null;
        assert strings != null;
    }

    @Override
    public SkillObject readSingleField(InStream in) {
        final int t = (int) in.v64();
        final long f = in.v64();
        if (0 == t)
            return null;
        return types.get(t - 1).getByID(f);
    }

    @Override
    public long calculateOffset(Collection<SkillObject> xs, Block range) {
        Iterator<SkillObject> is = xs.iterator();

        // skip begin
        if (null != range)
            for (int i = (int) range.bpo; i != 0; i--)
                is.next();

        long result = 0L;
        for (int i = null == range ? xs.size() : (int) range.count; i != 0; i--) {
            SkillObject ref = is.next();

            if (ref instanceof NamedType)
                result += strings.singleOffset(((NamedType) ref).τName());
            else
                result += strings.singleOffset(ref.getClass().getSimpleName().toLowerCase());

            result += V64.singleV64Offset(ref.getSkillID());
        }

        return result;
    }

    @Override
    public void writeSingleField(SkillObject ref, OutStream out) throws IOException {
        if (null == ref) {
            // magic trick!
            out.i16((short) 0);
            return;
        }

        if (ref instanceof NamedType)
            strings.writeSingleField(((NamedType) ref).τName(), out);
        else
            strings.writeSingleField(ref.getClass().getSimpleName().toLowerCase(), out);
        out.v64(ref.getSkillID());

    }

    @Override
    public String toString() {
        return "annotation";
    }
}
