package de.ust.skill.common.java.internal.fieldTypes;

import de.ust.skill.common.java.internal.FieldType;

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
