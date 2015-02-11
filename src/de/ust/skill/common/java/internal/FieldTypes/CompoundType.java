package de.ust.skill.common.java.internal.FieldTypes;

import java.util.ArrayList;

import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.java.internal.StoragePool;

/**
 * Super class of all container types.
 * 
 * @author Timm Felden
 */
public abstract class CompoundType<T> extends FieldType<T> {
    protected CompoundType(long typeID) {
        super(typeID);
    }

    /**
     * used for state construction only!
     */
    public abstract CompoundType<T> eliminatePreliminaryTypes(ArrayList<StoragePool<?, ?>> types);
}
