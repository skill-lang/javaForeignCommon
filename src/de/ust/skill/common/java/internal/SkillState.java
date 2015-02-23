package de.ust.skill.common.java.internal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;

import de.ust.skill.common.java.api.Access;
import de.ust.skill.common.java.api.SkillException;
import de.ust.skill.common.java.api.SkillFile;
import de.ust.skill.common.java.api.StringAccess;
import de.ust.skill.common.java.internal.fieldTypes.Annotation;
import de.ust.skill.common.java.internal.fieldTypes.StringType;

/**
 * Implementation common to all skill states independent of type declarations.
 * 
 * @author Timm Felden
 */
public abstract class SkillState implements SkillFile {

    /**
     * This pool is used for all asynchronous (de)serialization operations.
     */
    static ExecutorService pool = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            final Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("SkillStatePoolThread");
            return t;
        }
    });

    /**
     * Barrier used to synchronize concurrent read operations.
     * 
     * @author Timm Felden
     */
    public static class ReadBarrier extends Semaphore {
        public ReadBarrier() {
            super(1);
        }

        /**
         * called at the beginning of a read operation to ensure main thread will wait for it
         */
        public void beginRead() {
            reducePermits(1);
        }

    }

    @SuppressWarnings("unchecked")
    protected final void finalizePools() {
        StringType ts = new StringType((StringPool) Strings());
        Annotation as = new Annotation((ArrayList<StoragePool<?, ?>>) allTypes());
        ReadBarrier barrier = new ReadBarrier();
        // async reads will post their errors in this queue
        final ConcurrentLinkedQueue<SkillException> readErrors = new ConcurrentLinkedQueue<SkillException>();

        for (StoragePool<?, ?> p : (ArrayList<StoragePool<?, ?>>) allTypes()) {
            // @note this loop must happen in type order!

            // set owners
            if (p instanceof BasePool<?>)
                ((BasePool<?>) p).setOwner(this);

            // add missing field declarations
            HashSet<String> fieldNames = new HashSet<>();
            for (de.ust.skill.common.java.api.FieldDeclaration<?, ?> f : p.fields())
                fieldNames.add(f.name());

            // ensure existence of known fields
            for (String n : p.knownFields) {
                if (!fieldNames.contains(n))
                    p.addKnownField(n, ts, as);
            }

            // read known fields
            for (FieldDeclaration<?, ?> f : p.fields)
                f.finish(barrier, readErrors);
        }

        // await async reads
        try {
            barrier.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (SkillException e : readErrors) {
            e.printStackTrace();
        }
        if (!readErrors.isEmpty())
            throw readErrors.peek();
    }

    private final StringPool strings;

    /**
     * Path and mode management can be done for arbitrary states.
     */
    protected SkillState(StringPool strings, Path path, Mode mode) {
        this.strings = strings;
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
        // TODO type restrictions
        // TODO make pools check fields, because they can optimize checks per instance and remove redispatching, if no
        // restrictions apply anyway
        for (StoragePool<?, ?> p : types)
            for (FieldDeclaration<?, ?> f : p.fields)
                try {
                    f.check();
                } catch (SkillException e) {
                    throw new SkillException(String.format("check failed in %s.%s:\n  %s", p.name, f.name,
                            e.getMessage()), e);
                }

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

    // types in type order
    protected ArrayList<StoragePool<?, ?>> types;

    @Override
    final public Iterable<? extends Access<? extends SkillObject>> allTypes() {
        return types;
    }
}
