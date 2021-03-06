package de.ust.skill.common.jforeign.internal;

import de.ust.skill.common.jforeign.api.FieldType;
import de.ust.skill.common.jforeign.api.SkillException;

/**
 * Thrown in case of a type miss-match on a field type.
 * 
 * @author Timm Felden
 */
public class TypeMissmatchError extends SkillException {

    public TypeMissmatchError(de.ust.skill.common.jforeign.api.FieldType<?> type, String expected, String field, String pool) {
        super(String.format("During construction of %s.%s: Encountered incompatible type \"%s\" (expected: %s)", pool,
                field, type.toString(), expected));
    }

}
