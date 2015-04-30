package de.ust.skill.common.java.internal.fieldTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.java.internal.NamedType;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;
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
    public long calculateOffset(Collection<SkillObject> xs) {
        long result = 0L;
        for (SkillObject ref : xs) {

            if (ref instanceof NamedType)
                result += strings.singleOffset(((NamedType) ref).τName());
            else
                result += strings.singleOffset(ref.getClass().getSimpleName().toLowerCase());

            result += V64.singleV64Offset(ref.getSkillID());
        }

        return result;
    }

    public long singleOffset(SkillObject ref) {
        final long name;
        if (ref instanceof NamedType)
            name = strings.singleOffset(((NamedType) ref).τName());
        else
            name = strings.singleOffset(ref.getClass().getSimpleName().toLowerCase());

        return name + V64.singleV64Offset(ref.getSkillID());
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
