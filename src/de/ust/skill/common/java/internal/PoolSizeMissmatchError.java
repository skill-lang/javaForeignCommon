package de.ust.skill.common.java.internal;

import de.ust.skill.common.java.api.SkillException;

/**
 * Thrown, if field deserialization consumes less bytes then specified by the
 * header.
 *
 * @author Timm Felden
 */
public class PoolSizeMissmatchError extends SkillException {

    public PoolSizeMissmatchError(int block, long begin, long end, DistributedField<?, ?> field) {
        super(String.format("Corrupted data chunk in block %d between 0x%X and 0x%X in Field %s.%s of type: %s",
                block + 1, begin, end, field.owner.name, field.name, field.type.toString()));
    }

}
