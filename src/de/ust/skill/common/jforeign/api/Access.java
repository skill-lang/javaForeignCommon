package de.ust.skill.common.jforeign.api;

import java.util.Collection;
import java.util.Iterator;

import de.ust.skill.common.jforeign.internal.ISkillObject;

public interface Access<T extends ISkillObject> extends Collection<T> {

    /**
     * @return the skill file owning this access
     */
    public SkillFile owner();

    /**
     * @return the skill name of the type
     */
    public String name();

    /**
     * @return the skill name of the super type, if it exists
     */
    public String superName();

    /**
     * @return a type ordered Container iterator over all instances of T
     * @note do not invoke this function, if you do not know what "type order" means
     */
    public Iterator<T> typeOrderIterator();

    /**
     * @return an iterator over all fields of T
     */
    public Iterator<? extends FieldDeclaration<?>> fields();

    /**
     * @return a new T instance with default field values
     * @throws SkillException
     *             if no instance can be created. This is either caused by restrictions, such as @singleton, or by
     *             invocation on unknown types, which are implicitly unmodifiable in this SKilL-implementation.
     */
    public T make() throws SkillException;
}
