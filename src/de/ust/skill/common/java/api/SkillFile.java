package de.ust.skill.common.java.api;

import java.nio.file.Path;
import java.util.Iterator;

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
     * @return access to known strings
     */
    public StringAccess Strings();

    /**
     * @return iterator over all user types
     */
    public Iterator<Access<? extends SkillObject>> all();

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
