package de.ust.skill.common.jforeign.internal.fieldDeclarations;

import de.ust.skill.common.jforeign.api.SkillException;
import de.ust.skill.common.jforeign.internal.FieldDeclaration;
import de.ust.skill.common.jforeign.internal.FieldType;
import de.ust.skill.common.jforeign.internal.ISkillObject;
import de.ust.skill.common.jforeign.internal.StoragePool;
import de.ust.skill.common.jvm.streams.MappedOutStream;

/**
 * This trait marks auto fields.
 * 
 * @author Timm Felden
 */
public abstract class AutoField<T, Obj extends ISkillObject> extends FieldDeclaration<T, Obj> {
    protected AutoField(FieldType<T> type, String name, int index, StoragePool<Obj, ? super Obj> owner) {
        super(type, name, index, owner);
    }

    @Override
    protected final void read(ChunkEntry last) {
        throw new NoSuchMethodError("one can not read auto fields!");
    }

    @Override
    public final long offset() {
        throw new NoSuchMethodError("one get the offset of an auto fields!");
    }

    @Override
    public final void write(MappedOutStream out) throws SkillException {
        throw new NoSuchMethodError("one can not write auto fields!");
    }
}
