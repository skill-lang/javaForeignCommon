package de.ust.skill.common.java.api;

import java.io.IOException;
import java.nio.file.Path;

import de.ust.skill.common.java.internal.SkillObject;

/**
 * A SKilL file that can be used to access types stored in a skill file and
 * persist changes.
 * 
 * @author Timm Felden
 */
public interface SkillFile {
    /**
     * Modes for file handling.
     * 
     * @author Timm Felden
     */
    public static enum Mode {
        Create, Read, Write, Append;
    }

    /**
     * Actual mode after processing.
     * 
     * @author Timm Felden
     */
    static class ActualMode {
        public final Mode open;
        public final Mode close;

        public ActualMode(Mode... modes) throws IOException {
            // determine open mode
            // @note read is preferred over create, because empty files are
            // legal and the file has been created by now if it did not exist
            // yet
            // @note write is preferred over append, because usage is more
            // inuitive
            Mode openMode = null, closeMode = null;
            for (Mode m : modes)
                switch (m) {
                case Create:
                case Read:
                    if (null == openMode)
                        openMode = m;
                    else if (openMode != m)
                        throw new IOException("You can either create or read a file.");
                    break;
                case Append:
                case Write:
                    if (null == closeMode)
                        closeMode = m;
                    else if (closeMode != m)
                        throw new IOException("You can either write or append to a file.");
                    break;
                }
            if (null == openMode)
                openMode = Mode.Read;
            if (null == closeMode)
                closeMode = Mode.Write;

            this.open = openMode;
            this.close = closeMode;
        }
    }

    /**
     * @return access to known strings
     */
    public StringAccess Strings();

    /**
     * @return iterator over all user types
     */
    public Iterable<? extends Access<? extends SkillObject>> allTypes();

    /**
     * Set a new path for the file. This will influence the next flush/close
     * operation.
     * 
     * @note (on implementation) memory maps for lazy evaluation must have been
     *       created before invocation of this method
     */
    public void changePath(Path path);

    /**
     * Checks consistency of the current state of the file.
     * 
     * @note if check is invoked manually, it is possible to fix the
     *       inconsistency and re-check without breaking the on-disk
     *       representation
     * @throws SkillException
     *             if an inconsistency is found
     */
    public void check() throws SkillException;

    /**
     * Check consistency and write changes to disk.
     * 
     * @note this will not sync the file to disk, but it will block until all
     *       in-memory changes are written to buffers.
     * @note if check fails, then the state is guaranteed to be unmodified
     *       compared to the state before flush
     * @throws SkillException
     *             if check fails
     */
    public void flush() throws SkillException;

    /**
     * Same as flush, but will also sync and close file, thus the state must not
     * be used afterwards.
     */
    public void close() throws SkillException;
}
