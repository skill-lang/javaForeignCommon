package de.ust.skill.common.java.internal;

import de.ust.skill.common.java.internal.fieldDeclarations.AutoField;
import de.ust.skill.common.java.internal.fieldDeclarations.KnownLongField;
import de.ust.skill.common.java.internal.fieldTypes.V64;
import de.ust.skill.common.java.internal.parts.Chunk;
import de.ust.skill.common.jvm.streams.MappedInStream;

/**
 * SKilL IDs behave as if they were auto fields of type V64
 * 
 * @author Timm Felden
 */
public class KnownField_SkillID<T extends SkillObject> extends FieldDeclaration<Long, T> implements KnownLongField<T>,
        AutoField {

    public KnownField_SkillID(BasePool<T> owner) {
        super(V64.get(), "skillid", 0, owner);
    }

    @Override
    public Long getR(T ref) {
        return ref.skillID;
    }

    @Override
    public void setR(T ref, Long value) {
        ref.skillID = value;
    }

    @Override
    public long get(T ref) {
        return ref.skillID;
    }

    @Override
    public void set(T ref, long value) {
        ref.skillID = value;
    }

    @Override
    protected void read(MappedInStream in, Chunk last) {
        throw new NoSuchMethodError("one can not read auto fields!");
    }
}
