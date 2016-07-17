package de.ust.skill.common.jforeign.internal;

import de.ust.skill.common.jforeign.internal.fieldDeclarations.AutoField;
import de.ust.skill.common.jforeign.internal.fieldDeclarations.KnownLongField;
import de.ust.skill.common.jforeign.internal.fieldTypes.V64;

/**
 * SKilL IDs behave as if they were auto fields of type V64
 * 
 * @author Timm Felden
 */
public class KnownField_SkillID<T extends SkillObject> extends AutoField<Long, T> implements KnownLongField<T> {

    public KnownField_SkillID(StoragePool<T, ? super T> storagePool) {
        super(V64.get(), "skillid", 0, storagePool);
    }

    @Override
    public Long getR(SkillObject ref) {
        return ref.skillID;
    }

    @Override
    public void setR(SkillObject ref, Long value) {
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
    void check() {
        // always correct
    }
}
