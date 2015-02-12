package de.ust.skill.common.java.internal;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import de.ust.skill.common.java.api.SkillException;
import de.ust.skill.common.java.api.SkillFile;
import de.ust.skill.common.java.api.StringAccess;

/**
 * Implementation common to all skill states independent of type declarations.
 * 
 * @author Timm Felden
 */
public abstract class SkillState implements SkillFile {

    /**
     * This pool is used for all asynchronous (de)serialization operations.
     */
    public static ExecutorService pool = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            final Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });

    private StringAccess strings;

    /**
     * Path and mode management can be done for arbitrary states.
     */
    public SkillState(Path path, Mode mode) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public StringAccess Strings() {
        return strings;
    }

    @Override
    public void changePath(Path path) {
        // TODO Auto-generated method stub

    }

    @Override
    public void check() throws SkillException {
        // TODO Auto-generated method stub

    }

    @Override
    public void flush() throws SkillException {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() throws SkillException {
        // TODO Auto-generated method stub

    }

    /**
     * internal use only
     */
    public abstract HashMap<String, StoragePool<?, ?>> poolByName();

}
