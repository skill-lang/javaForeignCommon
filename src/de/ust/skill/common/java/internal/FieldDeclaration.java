package de.ust.skill.common.java.internal;

import de.ust.skill.common.java.api.SkillObject;

/**
 * Actual implementation as used by all bindings.
 * 
 * @author Timm Felden
 */
abstract public class FieldDeclaration<T, Obj extends SkillObject> implements
        de.ust.skill.common.java.api.FieldDeclaration<T, Obj> {

    /**
     * @note types may change during file parsing. this may seem like a hack,
     *       but it makes file parser implementation a lot easier, because there
     *       is no need for two mostly similar type hierarchy implementations
     */
    FieldType<T> type;

    /**
     * skill name of this
     */
    final String name;

    /**
     * index as used in the file
     * 
     * @note this is 0 iff the field will not be serialized (auto & skillID)
     */
    final long index;

    /**
     * the enclosing storage pool
     */
    final StoragePool<Obj, ? super Obj> owner;

    public FieldDeclaration(FieldType<T> type, String name, long index, StoragePool<Obj, ? super Obj> owner) {
        this.type = type;
        this.name = name;
        this.index = index;
        this.owner = owner;
    }

    @Override
    public String toString() {
        return type.toString() + " " + name;
    }

    /**
     * Field declarations are equal, iff their names and types are equal.
     * 
     * @note This makes fields of unequal enclosing types equal!
     */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof FieldDeclaration) {
            return ((FieldDeclaration<?, ?>) obj).name().equals(name)
                    && ((FieldDeclaration<?, ?>) obj).type.equals(type);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return type.hashCode() ^ name.hashCode();
    }
}
