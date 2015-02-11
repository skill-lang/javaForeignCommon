package de.ust.skill.common.java.api;

import java.util.Collection;
import java.util.Iterator;

import de.ust.skill.common.java.internal.SkillObject;

public interface Access<T extends SkillObject> extends Collection<T> {

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
     * @note do not invoke this function, if you do not know what "type order"
     *       means
     */
    public Iterator<T> typeOrderIterator();

    public Iterable<? extends FieldDeclaration<?, T>> fields();
}
