package de.ust.skill.common.java.internal.FieldTypes;

import de.ust.skill.common.java.internal.FieldType;

/**
 * Super class of all container types.
 * 
 * @author Timm Felden
 */
public abstract class CompoundType<T> extends FieldType<T> {
    protected CompoundType(long typeID) {
        super(typeID);
    }
}
