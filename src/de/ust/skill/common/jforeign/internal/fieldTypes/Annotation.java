package de.ust.skill.common.jforeign.internal.fieldTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import de.ust.skill.common.jforeign.internal.FieldType;
import de.ust.skill.common.jforeign.internal.NamedType;
import de.ust.skill.common.jforeign.internal.SkillObject;
import de.ust.skill.common.jforeign.internal.StoragePool;
import de.ust.skill.common.jvm.streams.InStream;
import de.ust.skill.common.jvm.streams.OutStream;

/**
 * Annotation types are instantiated once per state.
 * 
 * @author Timm Felden
 */
public final class Annotation extends FieldType<SkillObject>implements ReferenceType {

    private final ArrayList<StoragePool<?, ?>> types;
    private HashMap<String, StoragePool<?, ?>> typeByName = null;

    /**
     * @param types
     *            the array list containing all types valid inside of a state
     * @note types can grow after passing the pointer to the annotation type. This behavior is required in order to
     *       implement reflective annotation parsing correctly.
     * @note can not take a state as argument, because it may not exist yet
     */
    public Annotation(ArrayList<StoragePool<?, ?>> types) {
        super(5);
        this.types = types;
        assert types != null;
    }

    public void fixTypes(HashMap<String, StoragePool<?, ?>> poolByName) {
        assert typeByName == null;
        typeByName = poolByName;
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
            if (null == ref)
                result += 2;
            else {
                if (ref instanceof NamedType)
                    result += V64.singleV64Offset(((NamedType) ref).τPool().typeID() - 31);
                else
                    result += V64.singleV64Offset(
                            typeByName.get(ref.getClass().getSimpleName().toLowerCase()).typeID() - 31);

                result += V64.singleV64Offset(ref.getSkillID());
            }
        }

        return result;
    }

    /**
     * used for simple offset calculation
     */
    public long singleOffset(SkillObject ref) {
        if (null == ref)
            return 2L;

        final long name;
        if (ref instanceof NamedType)
            name = V64.singleV64Offset(((NamedType) ref).τPool().typeID() - 31);
        else
            name = V64.singleV64Offset(typeByName.get(ref.getClass().getSimpleName().toLowerCase()).typeID() - 31);

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
            out.v64(((NamedType) ref).τPool().typeID() - 31);
        else
            out.v64(typeByName.get(ref.getClass().getSimpleName().toLowerCase()).typeID() - 31);
        out.v64(ref.getSkillID());

    }

    @Override
    public String toString() {
        return "annotation";
    }

    /**
     * required for proper treatment of Interface types (because Java can not have interfaces inherit from classes)
     */
    public static <T> Annotation cast(FieldType<T> f) {
        return (Annotation) f;
    }
}
