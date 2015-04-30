package de.ust.skill.common.java.internal;

/**
 * The root of the hierarchy of instances of skill user types. Annotations can store arbitrary objects, thus this type
 * has to exist explicitly.
 * 
 * @author Timm Felden
 * @note This type definition is in internal, because we have to protect setSkillID from the user
 * @param <This>
 *            this.type
 */
// TODO create a builder for skill objects
public class SkillObject {

    /**
     * The constructor is protected to ensure that users do not break states accidentally
     */
    protected SkillObject(long skillID) {
        this.skillID = skillID;
    }

    /**
     * -1 for new objects<br>
     * 0 for deleted objects<br>
     * everything else is the ID of an object inside of a file
     */
    protected long skillID;

    /**
     * create a delete mark, thus pointers to this instance will be replaced by null a pointer on flush
     */
    public void delete() {
        skillID = 0;
    }

    /**
     * @return whether the object has been deleted
     */
    public boolean isDeleted() {
        return 0 == skillID;
    }

    /**
     * Do not rely on skill ID if you do not know exactly what you are doing.
     */
    public final long getSkillID() {
        return skillID;
    }

    final void setSkillID(long skillID) {
        this.skillID = skillID;
    }

    /**
     * reflective setter
     *
     * @param field
     *            a field declaration instance as obtained from the storage pools iterator
     * @param value
     *            the new value of the field
     * @note if field is not a distributed field of this type, then anything may happen
     */
    public <T> void set(de.ust.skill.common.java.api.FieldDeclaration<T> field, T value) {
        field.setR(this, value);
    }

    /**
     * reflective getter
     *
     * @param field
     *            a field declaration instance as obtained from the storage pools iterator
     * @note if field is not a distributed field of this type, then anything may happen
     * @note the second type parameter of field has to be this.type. Unfortunately Java wont let us override the type
     *       parameter on each overload, although this pattern would automagically make everything work as intended and
     *       the user would always know whether using a field declaration on a specific instance would work well.
     */
    public <T> T get(de.ust.skill.common.java.api.FieldDeclaration<T> field) {
        return field.getR(this);
    }

    /**
     * potentially expensive but more pretty representation of this instance.
     */
    public String prettyString() {
        StringBuilder sb = new StringBuilder("SkillObject(this: ").append(this);
        return sb.append(")").toString();
    }

    public static final class SubType extends SkillObject implements NamedType {
        private final StoragePool<?, ?> τPool;

        SubType(StoragePool<?, ?> τPool, long skillID) {
            super(skillID);
            this.τPool = τPool;
        }

        @Override
        public StoragePool<?, ?> τPool() {
            return τPool;
        }

        @Override
        public String τName() {
            return τPool.name;
        }

        @Override
        public String toString() {
            return τName() + "#" + skillID;
        }
    }
}
