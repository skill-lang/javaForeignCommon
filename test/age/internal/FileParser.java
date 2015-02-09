package age.internal;

import de.ust.skill.common.java.api.SkillFile.Mode;
import de.ust.skill.common.java.internal.ParseException;
import de.ust.skill.common.jvm.streams.FileInputStream;

final public class FileParser extends de.ust.skill.common.java.internal.FileParser<SkillState> {

    /**
     * Constructs a parser that parses the file from in and constructs the
     * state. State is valid immediately after construction.
     */
    private FileParser(FileInputStream in, Mode writeMode) throws ParseException {
        super(in);

        while (!in.eof()) {
            stringBlock();
            typeBlock();
        }
    }

    /**
     * turns a file into a state.
     * 
     * @note this method is abstract, because some methods, including state
     *       allocation depend on the specification
     */
    public static SkillState read(FileInputStream in, Mode writeMode) throws ParseException {
        de.ust.skill.common.java.internal.FileParser<SkillState> p = new FileParser(in, writeMode);
        // TODO make
        throw new Error("TODO");
    }
}
