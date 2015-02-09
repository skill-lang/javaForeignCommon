package age;

import de.ust.skill.common.java.internal.NamedType;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;

/**
 * The age of a person.
 * 
 * @author Timm Felden
 */
public class Age extends SkillObject {

    /**
     * Create a new unmanaged age. Allocation of objects without using the
     * access factory method is discouraged.
     */
    public Age() {
        super(-1);
    }

    /**
     * Used for internal construction only!
     * 
     * @param skillID
     */
    public Age(long skillID) {
        super(skillID);
    }

    /**
     * Used for internal construction, full allocation.
     * 
     * @param skillID
     */
    public Age(long skillID, long age) {
        super(skillID);
        this.age = age;
    }

    protected long age;

    /**
     * People have a small positive age, but maybe they will start to live
     * longer in the future, who knows
     * 
     * @note type := min(0) v64
     */
    public long getAge() {
        return age;
    }

    /**
     * People have a small positive age, but maybe they will start to live
     * longer in the future, who knows
     * 
     * @note type := min(0) v64
     */
    public void setAge(long age) {
        this.age = age;
    }

    /**
     * potentially expensive but more pretty representation of this instance.
     */
    @Override
    public String prettyString() {
        StringBuilder sb = new StringBuilder("Age(this: ").append(this);
        sb.append(", age: ").append(age);
        return sb.append(")").toString();
    }

    /**
     * Generic sub types of this type.
     * 
     * @author Timm Felden
     */
    public static final class SubType extends Age implements NamedType {
        private final StoragePool<?, ?> τPool;

        SubType(StoragePool<?, ?> τPool, long skillID) {
            super(skillID);
            this.τPool = τPool;
        }

        @Override
        public String τName() {
            return τPool.name();
        }

        @Override
        public String toString() {
            return τName() + "#" + skillID;
        }
    }
}
