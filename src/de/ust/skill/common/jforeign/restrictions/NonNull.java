package de.ust.skill.common.jforeign.restrictions;

import de.ust.skill.common.jforeign.api.SkillException;
import de.ust.skill.common.jforeign.internal.SkillObject;

/**
 * A nonnull restricition. It will ensure that field data is non null.
 * 
 * @author Timm Felden
 */
public class NonNull<T extends SkillObject> implements FieldRestriction<T> {
    private static final NonNull<?> instance = new NonNull<>();

    private NonNull() {
    }

    public static NonNull<?> get() {
        return instance;
    }

    @Override
    public void check(SkillObject value) throws SkillException {
        if (value == null)
            throw new SkillException("Null value violates @NonNull.");
    }
}
