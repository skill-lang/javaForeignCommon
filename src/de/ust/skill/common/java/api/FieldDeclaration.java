package de.ust.skill.common.java.api;

import de.ust.skill.common.java.internal.SkillObject;

/**
 * An abstract Field declaration, used for the runtime representation of types.
 * It can be used for reflective access of types.
 * 
 * @author Timm Felden
 * @param <T>
 *            runtime type of the field modulo boxing
 * @param <Obj>
 *            runtime type of the object having this field
 * @note Obj may appear in some places as "? extends T" or "? super T" do not
 *       get confused by this
 */
public interface FieldDeclaration<T, Obj extends SkillObject> {
    /**
     * @return skill name of this field
     */
    public String name();

    /**
     * @return enclosing type
     */
    public Access<Obj> owner();

    /**
     * Generic getter for an object.
     */
    public T getR(Obj ref);

    public void setR(Obj ref, T value);
}
