package de.ust.skill.common.java.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.function.ToIntFunction;

import de.ust.skill.common.java.api.SkillException;
import de.ust.skill.common.java.internal.fieldTypes.ConstantI16;
import de.ust.skill.common.java.internal.fieldTypes.ConstantI32;
import de.ust.skill.common.java.internal.fieldTypes.ConstantI64;
import de.ust.skill.common.java.internal.fieldTypes.ConstantI8;
import de.ust.skill.common.java.internal.fieldTypes.ConstantLengthArray;
import de.ust.skill.common.java.internal.fieldTypes.ConstantV64;
import de.ust.skill.common.java.internal.fieldTypes.MapType;
import de.ust.skill.common.java.internal.fieldTypes.SingleArgumentType;
import de.ust.skill.common.java.internal.fieldTypes.StringType;
import de.ust.skill.common.jvm.streams.FileOutputStream;
import de.ust.skill.common.jvm.streams.MappedOutStream;
import de.ust.skill.common.jvm.streams.OutStream;

/**
 * Provides serialization functions;
 *
 * @see SKilL §6.4
 * @author Timm Felden
 */
abstract public class SerializationFunctions {

    /**
     * Data structure used for parallel serialization scheduling
     */
    protected static final class Task<B extends SkillObject> {
        public final FieldDeclaration<?, ?> f;
        public final long begin;
        public final long end;

        Task(FieldDeclaration<?, ?> f, long begin, long end) {
            this.f = f;
            this.begin = begin;
            this.end = end;
        }
    }

    protected final SkillState state;
    protected final HashMap<String, Integer> stringIDs;

    public SerializationFunctions(SkillState state) {
        this.state = state;

        /**
         * collect String instances from known string types; this is needed, because strings are something special on
         * the jvm
         * 
         * @note this is a O(σ) operation:)
         * @note we do no longer make use of the generation time type info, because we want to treat generic fields as
         *       well
         */
        StringPool strings = (StringPool) state.Strings();

        for (StoragePool<?, ?> p : state.types) {
            strings.add(p.name);
            for (FieldDeclaration<?, ?> f : p.dataFields) {

                strings.add(f.name);
                if (f.type instanceof StringType) {
                    for (SkillObject i : p)
                        strings.add((String) i.get(f));
                }
            }
        }

        /**
         * check consistency of the state, now that we aggregated all instances
         */
        state.check();

        stringIDs = state.stringType.resetIDs();
    }

    protected final void string(String v, OutStream out) throws IOException {
        out.v64(stringIDs.get(v));
    }

    /**
     * TODO serialization of restrictions
     */
    protected static final void restrictions(StoragePool<?, ?> p, OutStream out) throws IOException {
        out.i8((byte) 0);
    }

    /**
     * TODO serialization of restrictions
     */
    protected static final void restrictions(FieldDeclaration<?, ?> f, OutStream out) throws IOException {
        out.i8((byte) 0);
    }

    /**
     * serialization of types is fortunately independent of state, because field types know their ID
     */
    protected static final void writeType(FieldType<?> t, OutStream out) throws IOException {
        switch (t.typeID) {
        // case ConstantI8(v) ⇒
        case 0:
            out.i8((byte) 0);
            out.i8(((ConstantI8) t).value);
            return;

            // case ConstantI16(v) ⇒
        case 1:
            out.i8((byte) 1);
            out.i16(((ConstantI16) t).value);
            return;

            // case ConstantI32(v) ⇒
        case 2:
            out.i8((byte) 2);
            out.i32(((ConstantI32) t).value);
            return;

            // case ConstantI64(v) ⇒
        case 3:
            out.i8((byte) 3);
            out.i64(((ConstantI64) t).value);
            return;

            // case ConstantV64(v) ⇒
        case 4:
            out.i8((byte) 4);
            out.v64(((ConstantV64) t).value);
            return;

            // case ConstantLengthArray(l, t) ⇒
        case 15:
            out.i8((byte) 0x0F);
            out.v64(((ConstantLengthArray<?>) t).length);
            out.v64(((SingleArgumentType<?, ?>) t).groundType.typeID);
            return;

            // case VariableLengthArray(t) ⇒
            // case ListType(t) ⇒
            // case SetType(t) ⇒
        case 17:
        case 18:
        case 19:
            out.i8((byte) t.typeID);
            out.v64(((SingleArgumentType<?, ?>) t).groundType.typeID);
            return;

            // case MapType(k, v) ⇒
        case 20:
            out.i8((byte) 0x14);
            writeType(((MapType<?, ?>) t).keyType, out);
            writeType(((MapType<?, ?>) t).valueType, out);
            return;

        default:
            out.v64(t.typeID);
            return;
        }
    }

    protected final static void writeFieldData(SkillState state, FileOutputStream out, ArrayList<Task<?>> data)
            throws IOException, InterruptedException {

        final Semaphore barrier = new Semaphore(0);
        // async reads will post their errors in this queue
        final ConcurrentLinkedQueue<SkillException> writeErrors = new ConcurrentLinkedQueue<SkillException>();

        long baseOffset = out.position();
        for (Task<?> t : data) {
            final FieldDeclaration<?, ?> f = t.f;
            final MappedOutStream outMap = out.map(baseOffset, t.begin, t.end);
            // @note use semaphore instead of data.par, because map is not thread-safe
            SkillState.pool.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        f.write(outMap);
                    } catch (SkillException e) {
                        writeErrors.add(e);
                    } catch (IOException e) {
                        writeErrors.add(new SkillException("failed to write field " + f.toString(), e));
                    } catch (Throwable e) {
                        writeErrors.add(new SkillException("unexpected failure while writing field " + f.toString(), e));
                    } finally {
                        // ensure that writer can terminate, errors will be printed to command line anyway, and we wont
                        // be able to recover, because errors can only happen if the skill implementation itself is
                        // broken
                        barrier.release(1);
                    }
                }
            });
        }
        barrier.acquire(data.size());
        out.close();

        // report errors
        for (SkillException e : writeErrors) {
            e.printStackTrace();
        }
        if (!writeErrors.isEmpty())
            throw writeErrors.peek();

        /**
         * **************** PHASE 4: CLEANING * ****************
         */
        // release data structures
        state.stringType.clearIDs();
        // unfix pools
        for (StoragePool<?, ?> p : state.types) {
            p.fixed(false);
        }
    }

    /**
     * creates an lbpo map by recursively adding the local base pool offset to the map and adding all sub pools
     * afterwards TODO remove size function, because there are only two cases!
     */
    protected final static int makeLBPOMap(StoragePool<?, ?> p, int[] lbpoMap, int next,
            ToIntFunction<StoragePool<?, ?>> size) {
        lbpoMap[p.typeID - 32] = next;
        int result = next + size.applyAsInt(p);
        for (SubPool<?, ?> sub : p.subPools) {
            result = makeLBPOMap(sub, lbpoMap, result, size);
        }
        return result;
    }
    //
    // /**
    // * concatenates array buffers in the d-map. This will in fact turn the d-map from a map pointing from names to
    // static
    // * instances into a map pointing from names to dynamic instances.
    // */
    // final def concatenateDataMap[T <: B, B <: SkillType](pool : StoragePool[T, B], data : HashMap[String,
    // ArrayBuffer[SkillType]]) : Unit = for (sub ← pool.subPools) {
    // data(pool.basePool.name) ++= data(sub.name)
    // data(sub.name) = data(pool.basePool.name)
    // concatenateDataMap(sub, data)
    // }
}
