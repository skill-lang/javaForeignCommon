package age.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import age.internal.SkillState;
import de.ust.skill.common.java.api.SkillException;

/**
 * An abstract skill file that is hiding all the dirty implementation details
 * from you.
 * 
 * @author Timm Felden
 */
public interface SkillFile extends de.ust.skill.common.java.api.SkillFile {

    /**
     * Create a new skill file based on argument path and mode.
     * 
     * @throws IOException
     *             on IO and mode related errors
     * @throws SkillException
     *             on file or specification consistency errors
     */
    public static SkillFile open(String path, Mode... mode) throws IOException, SkillException {
        return SkillState.open(path, mode);
    }

    /**
     * Create a new skill file based on argument path and mode.
     * 
     * @throws IOException
     *             on IO and mode related errors
     * @throws SkillException
     *             on file or specification consistency errors
     */
    public static SkillFile open(File path, Mode... mode) throws IOException, SkillException {
        return SkillState.open(path, mode);
    }

    /**
     * Create a new skill file based on argument path and mode.
     * 
     * @throws IOException
     *             on IO and mode related errors
     * @throws SkillException
     *             on file or specification consistency errors
     */
    public static SkillFile open(Path path, Mode... mode) throws IOException, SkillException {
        return SkillState.open(path, mode);
    }

    /**
     * @return an access for all Ages in this state
     */
    public AgeAccess Ages();
}
