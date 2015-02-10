package de.ust.skill.common.java.internal.FieldTypes;

import de.ust.skill.common.java.internal.FieldType;

/**
 * Mutable integers.
 * 
 * @author Timm Felden
 */
public abstract class IntegerType<T> extends FieldType<T> {
    protected IntegerType(long typeID) {
        super(typeID);
    }
}
