package de.ust.skill.common.jforeign.internal.fieldTypes;

import de.ust.skill.common.jforeign.internal.FieldType;

/**
 * Mutable integers.
 * 
 * @author Timm Felden
 */
public abstract class IntegerType<T> extends FieldType<T> {
    protected IntegerType(int typeID) {
        super(typeID);
    }
}
