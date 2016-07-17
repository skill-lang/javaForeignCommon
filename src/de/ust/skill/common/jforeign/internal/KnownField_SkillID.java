package de.ust.skill.common.jforeign.internal;

import de.ust.skill.common.jforeign.internal.fieldDeclarations.AutoField;
import de.ust.skill.common.jforeign.internal.fieldDeclarations.KnownLongField;
import de.ust.skill.common.jforeign.internal.fieldTypes.V64;

/**
 * SKilL IDs behave as if they were auto fields of type V64
 * 
 * @author Timm Felden
 */
public class KnownField_SkillID<T extends ISkillObject> extends AutoField<Long, T> implements KnownLongField<T> {

    public KnownField_SkillID(StoragePool<T, ? super T> storagePool) {
        super(V64.get(), "skillid", 0, storagePool);
    }

    @Override
    public Long getR(ISkillObject ref) {
        return ref.getSkillID();
    }

    @Override
    public void setR(ISkillObject ref, Long value) {
        ref.setSkillID(value);
    }

    @Override
    public long get(T ref) {
        return ref.getSkillID();
    }

    @Override
    public void set(T ref, long value) {
        ref.setSkillID(value);
    }

    @Override
    void check() {
        // always correct
    }
}
