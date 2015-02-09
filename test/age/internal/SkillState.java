package age.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import age.api.AgeAccess;
import age.api.SkillFile;
import de.ust.skill.common.java.api.SkillException;
import de.ust.skill.common.jvm.streams.FileInputStream;

public final class SkillState extends de.ust.skill.common.java.internal.SkillState implements SkillFile {

    /**
     * Create a new skill file based on argument path and mode.
     * 
     * @throws IOException
     *             on IO and mode related errors
     * @throws SkillException
     *             on file or specification consistency errors
     */
    public static SkillState open(String path, Mode... mode) throws IOException, SkillException {
        File f = new File(path);
        assert f.exists() : "can only open files that already exist in genarel, because of java.nio restrictions";
        return open(f, mode);
    }

    /**
     * Create a new skill file based on argument path and mode.
     * 
     * @throws IOException
     *             on IO and mode related errors
     * @throws SkillException
     *             on file or specification consistency errors
     */
    public static SkillState open(File path, Mode... mode) throws IOException, SkillException {
        assert path.exists() : "can only open files that already exist in genarel, because of java.nio restrictions";
        return open(path.toPath(), mode);
    }

    /**
     * Create a new skill file based on argument path and mode.
     * 
     * @throws IOException
     *             on IO and mode related errors
     * @throws SkillException
     *             on file or specification consistency errors
     */
    public static SkillState open(Path path, Mode... mode) throws IOException, SkillException {
        ActualMode actualMode = new ActualMode(mode);
        switch (actualMode.open) {
        case Create:
        case Read:
            return FileParser.read(FileInputStream.open(path), actualMode.close);

        default:
            throw new IllegalStateException("may not happen");
        }
    }

    private AgeAccess ages;

    @Override
    public AgeAccess Ages() {
        return ages;
    }

}
