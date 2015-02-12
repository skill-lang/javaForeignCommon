package de.ust.skill.common.java.internal.fieldDeclarations;

import de.ust.skill.common.jvm.streams.MappedInStream;

/**
 * This trait marks auto fields.
 * 
 * @author Timm Felden
 */
public interface AutoField {
    public default void read(MappedInStream in) {
        throw new NoSuchMethodError("one can not read auto fields!");
    }
}
