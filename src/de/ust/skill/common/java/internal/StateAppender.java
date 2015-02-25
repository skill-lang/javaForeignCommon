package de.ust.skill.common.java.internal;

import de.ust.skill.common.jvm.streams.FileOutputStream;

final public class StateAppender extends SerializationFunctions {

    public StateAppender(SkillState state, FileOutputStream out) {
        super(state);

        // release data structures
        state.stringType.clearIDs();
    }

}
