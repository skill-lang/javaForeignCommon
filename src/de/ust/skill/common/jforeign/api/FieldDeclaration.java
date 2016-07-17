package de.ust.skill.common.jforeign.api;

import de.ust.skill.common.jforeign.internal.ISkillObject;


/**
 * An abstract Field declaration, used for the runtime representation of types.
 * It can be used for reflective access of types.
 * 
 * @author Timm Felden
 * @param <T>
 *            runtime type of the field modulo boxing
 */
public interface FieldDeclaration<T> {
    /**
     * @return the skill type of this field
     */
    public FieldType<T> type();

    /**
     * @return skill name of this field
     */
    public String name();

    /**
     * @return enclosing type
     */
    public Access<?> owner();

    /**
     * Generic getter for an object.
     */
    public T getR(ISkillObject ref);

    /**
     * Generic setter for an object.
     */
    public void setR(ISkillObject ref, T value);
}
