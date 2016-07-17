package de.ust.skill.common.jforeign.internal;

/**
 * The root of the hierarchy of instances of skill user types. Annotations can store arbitrary objects, thus this type
 * has to exist explicitly.
 * 
 * @author Timm Felden
 * @note This type definition is in internal, because we have to protect setSkillID from the user
 */
// TODO create a builder for skill objects
public interface ISkillObject {

    /**
     * @return the skill name of this type
     */
    public abstract String skillName();

    /**
     * @return whether the object has been deleted
     */
    public default boolean isDeleted() {
        return 0 == getSkillID();
    }

    /**
     * Do not rely on skill ID if you do not know exactly what you are doing.
     */
    public long getSkillID();

    public void setSkillID(long skillID); //TODO: want this?

    /**
     * reflective setter
     *
     * @param field
     *            a field declaration instance as obtained from the storage pools iterator
     * @param value
     *            the new value of the field
     * @note if field is not a distributed field of this type, then anything may happen
     */
    public default <T> void set(de.ust.skill.common.jforeign.api.FieldDeclaration<T> field, T value) {
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
    public default <T> T get(de.ust.skill.common.jforeign.api.FieldDeclaration<T> field) {
        return field.getR(this);
    }

    /**
     * potentially expensive but more pretty representation of this instance.
     */
    public default String prettyString() {
        StringBuilder sb = new StringBuilder("SkillObject(this: ").append(this);
        return sb.append(")").toString();
    }

    public static final class SubType implements NamedType, ISkillObject {
        private final StoragePool<?, ?> τPool;

        SubType(StoragePool<?, ?> τPool, long skillID) {
            setSkillID(skillID);
            this.τPool = τPool;
        }

        @Override
        public StoragePool<?, ?> τPool() {
            return τPool;
        }

        @Override
        public String toString() {
            return skillName() + "#" + getSkillID();
        }

        @Override
        public String skillName() {
            return τPool.name;
        }

        @Override
        public long getSkillID() {
            //TODO: what?
            return 0;
        }

        @Override
        public void setSkillID(long skillID) {
            //TODO: what?
        }
    }
}
