package age.api;

import java.util.Arrays;
import java.util.HashSet;

import age.Age;
import de.ust.skill.common.java.internal.BasePool;
import de.ust.skill.common.java.internal.StoragePool;

public class AgeAccess extends BasePool<Age> {

    /**
     * Can only be constructed by the SkillFile in this package.
     */
    AgeAccess(long poolIndex) {
        super(poolIndex, "age", new HashSet<String>(Arrays.asList(new String[] { "age" })));
    }

    /**
     * @return a new age instance with default field values
     */
    public Age make() {
        Age rval = new Age();
        add(rval);
        return rval;
    }

    /**
     * @return a new age instance with the argumen field values
     */
    public Age make(long age) {
        Age rval = new Age(-1, age);
        add(rval);
        return rval;
    }

    public AgeBuilder build() {
        return new AgeBuilder(this, new Age());
    }

    /**
     * Builder for new age instances.
     * 
     * @author Timm Felden
     */
    public static final class AgeBuilder extends Builder<Age> {

        protected AgeBuilder(StoragePool<Age, ? super Age> pool, Age instance) {
            super(pool, instance);
        }

        public AgeBuilder age(long age) {
            instance.setAge(age);
            return this;
        }

    }
}
