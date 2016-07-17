package de.ust.skill.common.jforeign.internal;

import java.nio.BufferUnderflowException;

import de.ust.skill.common.jforeign.api.SkillException;

/**
 * Thrown, if field deserialization consumes less bytes then specified by the header.
 *
 * @author Timm Felden
 */
public class PoolSizeMissmatchError extends SkillException {

    public PoolSizeMissmatchError(int block, long position, long begin, long end, FieldDeclaration<?, ?> field) {
        super(String.format("Corrupted data chunk in block %d at 0x%X between 0x%X and 0x%X in Field %s.%s of type: %s",
                block + 1, position, begin, end, field.owner.name, field.name, field.type.toString()));
    }

    public PoolSizeMissmatchError(int block, long begin, long end, FieldDeclaration<?, ?> field,
            BufferUnderflowException e) {
        super(String.format("Corrupted data chunk in block %d between 0x%X and 0x%X in Field %s.%s of type: %s",
                block + 1, begin, end, field.owner.name, field.name, field.type.toString()), e);
    }

}
