package de.ust.skill.common.java.internal;

import de.ust.skill.common.jvm.streams.FileOutputStream;

public class StateWriter extends SerializationFunctions {

    public StateWriter(SkillState state, FileOutputStream out) {
        super(state);
    }

}
