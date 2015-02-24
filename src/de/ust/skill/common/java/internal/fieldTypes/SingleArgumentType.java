package de.ust.skill.common.java.internal.fieldTypes;

import java.util.Collection;

import de.ust.skill.common.java.internal.FieldType;

/**
 * Super class of all container types with one type argument.
 * 
 * @author Timm Felden
 */
public abstract class SingleArgumentType<T extends Collection<Base>, Base> extends CompoundType<T> {
    public final FieldType<Base> groundType;

    public SingleArgumentType(int typeID, FieldType<Base> groundType) {
        super(typeID);
        this.groundType = groundType;
    }
}
