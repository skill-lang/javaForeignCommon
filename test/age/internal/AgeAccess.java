package age.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import age.Age;
import de.ust.skill.common.java.internal.BasePool;
import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.TypeMissmatchError;
import de.ust.skill.common.java.restrictions.FieldRestriction;

public class AgeAccess extends BasePool<Age> {

    @Override
    protected Age[] emptyArray() {
        return new Age[0];
    }

    /**
     * Can only be constructed by the SkillFile in this package.
     */
    AgeAccess(long poolIndex) {
        super(poolIndex, "age", new HashSet<String>(Arrays.asList(new String[] { "age" })));
    }

    @Override
    public boolean insertInstance(int skillID) {
        int i = skillID - 1;
        if (null != data[i])
            return false;

        Age r = new age.Age(skillID);
        data[i] = r;
        staticData.add(r);
        return true;
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

    @SuppressWarnings("unchecked")
    @Override
    public <R> FieldDeclaration<R, Age> addField(int ID, FieldType<R> type, String name,
            HashSet<FieldRestriction<?>> restrictions) {
        final FieldDeclaration<R, Age> f;
        switch (name) {
        case "age":
            f = (FieldDeclaration<R, Age>) new KnownField_Age_age(ID, this);
            break;

        default:
            return super.addField(ID, type, name, restrictions);
        }

        // override preliminary type
        if (!type.equals(f.type()))
            throw new TypeMissmatchError(type, f.type().toString(), f.name(), name);
        // TODO add if we reintroduce named preliminary stypes f.t = t

        for (FieldRestriction<?> r : restrictions)
            f.addRestriction(r);
        fields.add(f);
        return f;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addKnownField(String name) {
        final FieldDeclaration<?, Age> f;
        switch (name) {
        case "age":
            f = new KnownField_Age_age(fields.size(), this);
            break;

        default:
            super.addKnownField(name);
            return;
        }
        f.eliminatePreliminaryTypes((ArrayList<StoragePool<?, ?>>) owner.allTypes());
        fields.add(f);
    }
}
