package de.ust.skill.common.java.internal.fieldTypes;

import java.util.ArrayList;

import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.jvm.streams.InStream;

/**
 * Annotation types are instantiated once per state.
 * 
 * @author Timm Felden
 */
public final class Annotation extends FieldType<SkillObject> implements ReferenceType {

    private final ArrayList<StoragePool<?, ?>> types;

    /**
     * @param types
     *            the array list containing all types valid inside of a state
     * @note types can grow after passing the pointer to the annotation type. This behavior is required in order to
     *       implement reflective annotation parsing correctly.
     */
    public Annotation(ArrayList<StoragePool<?, ?>> types) {
        super(5);
        this.types = types;
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
    public String toString() {
        return "annotation";
    }
}
