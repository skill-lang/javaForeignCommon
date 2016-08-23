package de.ust.skill.common.jforeign.internal.fieldTypes;

import de.ust.skill.common.jforeign.internal.FieldType;

/**
 * Mutable floats.
 * 
 * @author Timm Felden
 */
public abstract class FloatType<T> extends FieldType<T> {
    protected FloatType(int typeID) {
        super(typeID);
    }
}
