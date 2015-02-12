package de.ust.skill.common.java.internal.fieldTypes;

import de.ust.skill.common.java.internal.FieldType;

/**
 * Mutable floats.
 * 
 * @author Timm Felden
 */
public abstract class FloatType<T> extends FieldType<T> {
    protected FloatType(long typeID) {
        super(typeID);
    }
}
