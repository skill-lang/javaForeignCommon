package de.ust.skill.common.java.internal;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.ToIntFunction;

import de.ust.skill.common.java.internal.fieldTypes.ConstantI16;
import de.ust.skill.common.java.internal.fieldTypes.ConstantI32;
import de.ust.skill.common.java.internal.fieldTypes.ConstantI64;
import de.ust.skill.common.java.internal.fieldTypes.ConstantI8;
import de.ust.skill.common.java.internal.fieldTypes.ConstantLengthArray;
import de.ust.skill.common.java.internal.fieldTypes.ConstantV64;
import de.ust.skill.common.java.internal.fieldTypes.MapType;
import de.ust.skill.common.java.internal.fieldTypes.SingleArgumentType;
import de.ust.skill.common.java.internal.fieldTypes.StringType;
import de.ust.skill.common.java.internal.parts.Block;
import de.ust.skill.common.java.iterators.Iterators;
import de.ust.skill.common.jvm.streams.OutStream;

/**
 * Provides serialization functions;
 *
 * @see SKilL §6.4
 * @author Timm Felden
 */
abstract public class SerializationFunctions {

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
            for (FieldDeclaration<?, ?> f : p.fields) {
                if (0 == f.index)
                    continue;

                strings.add(f.name);
                if (f.type instanceof StringType) {
                    for (SkillObject i : p)
                        strings.add((String) i.get(f));
                }
            }
        }

        stringIDs = state.stringType.resetIDs();
    }

    protected final void string(String v, OutStream out) throws IOException {
        out.v64(stringIDs.get(v));
    }

    /**
     * ******************** UTILITY FUNCTIONS REQUIRED FOR PARALLEL ENCODING ********************
     */

    @SuppressWarnings("unchecked")
    protected final long offset(StoragePool<?, ?> p, FieldDeclaration<?, ?> f) {
        switch (f.type.typeID) {
        // case ConstantI8(_) | ConstantI16(_) | ConstantI32(_) | ConstantI64(_) | ConstantV64(_) ⇒ 0
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
            return 0;

            // case BoolType | I8 ⇒ p.blockInfos.last.count
        case 6:
        case 7:
            return p.blocks.getLast().count;

            // case I16 ⇒ 2 * p.blockInfos.last.count
        case 8:
            return 2 * p.blocks.getLast().count;

            // case F32 | I32 ⇒ 4 * p.blockInfos.last.count
        case 9:
        case 12:
            return 4 * p.blocks.getLast().count;

            // case F64 | I64 ⇒ 8 * p.blockInfos.last.count
        case 10:
        case 13:
            return 8 * p.blocks.getLast().count;

            // case V64 ⇒ encodeV64(p, f)
        case 11:
            return encodeV64(p, (FieldDeclaration<Long, ?>) f);

            // case s : Annotation ⇒ p.all.map(_.get(f).asInstanceOf[SkillType]).foldLeft(0L)((r : Long, v : SkillType)
            // ⇒ r + encodeSingleV64(1 + state.poolByName(v.getClass.getName.toLowerCase).poolIndex) +
            // encodeSingleV64(v.getSkillID))
        case 5: {
            FieldDeclaration<SkillObject, ?> field = (FieldDeclaration<SkillObject, ?>) f;
            long result = 0L;
            for (SkillObject i : p) {
                SkillObject ref = i.get(field);
                result += encodeSingleV64(1 + state.poolByName().get(ref.getClass().getName().toLowerCase()).typeID - 32);
                result += encodeSingleV64(ref.getSkillID());
            }
            return result;
        }

        // case s : StringType ⇒ p.all.map(_.get(f).asInstanceOf[String]).foldLeft(0L)((r : Long, v : String) ⇒ r +
        // encodeSingleV64(stringIDs(v)))
        case 14: {
            FieldDeclaration<String, ?> field = (FieldDeclaration<String, ?>) f;
            long result = 0L;
            for (SkillObject i : p) {
                String v = i.get(field);
                result += encodeSingleV64(stringIDs.get(v));
            }
            return result;
        }

        // case ConstantLengthArray(l, t) ⇒
        // val b = p.blockInfos.last
        // p.basePool.data.view(b.bpo.toInt, (b.bpo + b.count).toInt).foldLeft(0L) {
        // case (sum, i) ⇒
        // val xs = i.get(f).asInstanceOf[Iterable[_]];
        // sum + encode(xs, t)
        // }
        case 15: {
            long result = 0L;
            Block b = p.blocks.getLast();
            Iterator<? extends SkillObject> is = Iterators.fakeArray(p.basePool.data, (int) b.bpo,
                    (int) (b.bpo + b.count));
            while (is.hasNext()) {
                result += encode((Collection<?>) is.next().get(f), ((ConstantLengthArray<?>) f.type).groundType);
            }

            return result;
        }

        // case VariableLengthArray(t) ⇒ [...]
        // case ListType(t) ⇒ [...]
        // case SetType(t) ⇒
        // val b = p.blockInfos.last
        // p.basePool.data.view(b.bpo.toInt, (b.bpo + b.count).toInt).foldLeft(0L) {
        // case (sum, i) ⇒
        // val xs = i.get(f).asInstanceOf[Iterable[_]];
        // sum + encodeSingleV64(xs.size) + encode(xs, t)
        // }
        case 17:
        case 18:
        case 19: {
            long result = 0L;
            Block b = p.blocks.getLast();
            Iterator<? extends SkillObject> is = Iterators.fakeArray(p.basePool.data, (int) b.bpo,
                    (int) (b.bpo + b.count));
            FieldType<?> g = ((SingleArgumentType<?, ?>) f.type).groundType;
            while (is.hasNext()) {
                Collection<?> xs = is.next().get((FieldDeclaration<? extends Collection<?>, ?>) f);
                result += encodeSingleV64(xs.size());
                result += encode(xs, g);
            }

            return result;
        }

        //
        // case MapType(k, v) ⇒
        // val b = p.blockInfos.last
        // p.basePool.data.view(b.bpo.toInt, (b.bpo + b.count).toInt).foldLeft(0L) {
        // case (sum, i) ⇒
        // val m = i.get(f).asInstanceOf[HashMap[_, _]];
        // sum + encodeSingleV64(m.size) + encode(m.keys, k) + encode(m.values, v)
        // }
        case 20: {
            long result = 0L;
            Block b = p.blocks.getLast();
            Iterator<? extends HashMap<?, ?>> is = Iterators.fakeArray(p.basePool.data, (int) b.bpo,
                    (int) (b.bpo + b.count));
            FieldType<?> kt = ((MapType<?, ?>) f.type).keyType;
            FieldType<?> vt = ((MapType<?, ?>) f.type).valueType;
            while (is.hasNext()) {
                final HashMap<?, ?> m = (HashMap<?, ?>) is.next().get(f);
                result += encodeSingleV64(m.size());
                result += encode(m.keySet(), kt);
                result += encode(m.values(), vt);
            }

            return result;
        }

        // case s : StoragePool[_, _] ⇒
        // if (s.base.size < 128) p.blockInfos.last.count // quick solution for small pools
        // else {
        // encodeRefs(p, f)
        // }
        default:
            StoragePool<?, ?> target = (StoragePool<?, ?>) f.type;
            // shortcut if references are small anyway
            if (target.basePool.size() < 128)
                return p.blocks.getLast().count;

            return encodeRefs(p, (FieldDeclaration<? extends SkillObject, ?>) f);
        }
    }

    // TODO turn this into dead code or die a performance death
    private final static long encodeSingleV64(long v) {
        if (0L == (v & 0xFFFFFFFFFFFFFF80L)) {
            return 1;
        } else if (0L == (v & 0xFFFFFFFFFFFFC000L)) {
            return 2;
        } else if (0L == (v & 0xFFFFFFFFFFE00000L)) {
            return 3;
        } else if (0L == (v & 0xFFFFFFFFF0000000L)) {
            return 4;
        } else if (0L == (v & 0xFFFFFFF800000000L)) {
            return 5;
        } else if (0L == (v & 0xFFFFFC0000000000L)) {
            return 6;
        } else if (0L == (v & 0xFFFE000000000000L)) {
            return 7;
        } else if (0L == (v & 0xFF00000000000000L)) {
            return 8;
        } else {
            return 9;
        }
    }

    // TODO create interface VariableLengthDataField and move this method to the field, because generic access is going
    // to kill us
    private final static long encodeV64(StoragePool<?, ?> p, FieldDeclaration<Long, ?> f) {
        long result = 0L;
        Block b = p.blocks.getLast();
        Iterator<? extends SkillObject> is = Iterators.fakeArray(p.basePool.data, (int) b.bpo, (int) (b.bpo + b.count));
        while (is.hasNext()) {
            SkillObject i = is.next();
            long v = i.get(f);
            if (0L == (v & 0xFFFFFFFFFFFFFF80L)) {
                result += 1;
            } else if (0L == (v & 0xFFFFFFFFFFFFC000L)) {
                result += 2;
            } else if (0L == (v & 0xFFFFFFFFFFE00000L)) {
                result += 3;
            } else if (0L == (v & 0xFFFFFFFFF0000000L)) {
                result += 4;
            } else if (0L == (v & 0xFFFFFFF800000000L)) {
                result += 5;
            } else if (0L == (v & 0xFFFFFC0000000000L)) {
                result += 6;
            } else if (0L == (v & 0xFFFE000000000000L)) {
                result += 7;
            } else if (0L == (v & 0xFF00000000000000L)) {
                result += 8;
            } else {
                result += 9;
            }
        }
        return result;
    }

    private final static long encodeV64(Iterable<Long> vs) {
        long result = 0L;
        for (long v : vs) {
            if (0L == (v & 0xFFFFFFFFFFFFFF80L)) {
                result += 1;
            } else if (0L == (v & 0xFFFFFFFFFFFFC000L)) {
                result += 2;
            } else if (0L == (v & 0xFFFFFFFFFFE00000L)) {
                result += 3;
            } else if (0L == (v & 0xFFFFFFFFF0000000L)) {
                result += 4;
            } else if (0L == (v & 0xFFFFFFF800000000L)) {
                result += 5;
            } else if (0L == (v & 0xFFFFFC0000000000L)) {
                result += 6;
            } else if (0L == (v & 0xFFFE000000000000L)) {
                result += 7;
            } else if (0L == (v & 0xFF00000000000000L)) {
                result += 8;
            } else {
                result += 9;
            }
        }
        return result;
    }

    private final static long encodeRefs(StoragePool<?, ?> p, FieldDeclaration<? extends SkillObject, ?> f) {
        long result = 0L;
        Block b = p.blocks.getLast();
        Iterator<? extends SkillObject> is = Iterators.fakeArray(p.basePool.data, (int) b.bpo, (int) (b.bpo + b.count));
        while (is.hasNext()) {
            SkillObject i = is.next();
            long v = i.get(f).getSkillID();
            if (0L == (v & 0xFFFFFFFFFFFFFF80L)) {
                result += 1;
            } else if (0L == (v & 0xFFFFFFFFFFFFC000L)) {
                result += 2;
            } else if (0L == (v & 0xFFFFFFFFFFE00000L)) {
                result += 3;
            } else if (0L == (v & 0xFFFFFFFFF0000000L)) {
                result += 4;
            } else if (0L == (v & 0xFFFFFFF800000000L)) {
                result += 5;
            } else if (0L == (v & 0xFFFFFC0000000000L)) {
                result += 6;
            } else if (0L == (v & 0xFFFE000000000000L)) {
                result += 7;
            } else if (0L == (v & 0xFF00000000000000L)) {
                result += 8;
            } else {
                result += 9;
            }
        }
        return result;
    }

    private final static long encodeRefs(Iterable<? extends SkillObject> is) {
        long result = 0L;
        for (SkillObject i : is) {
            long v = i.getSkillID();
            if (0L == (v & 0xFFFFFFFFFFFFFF80L)) {
                result += 1;
            } else if (0L == (v & 0xFFFFFFFFFFFFC000L)) {
                result += 2;
            } else if (0L == (v & 0xFFFFFFFFFFE00000L)) {
                result += 3;
            } else if (0L == (v & 0xFFFFFFFFF0000000L)) {
                result += 4;
            } else if (0L == (v & 0xFFFFFFF800000000L)) {
                result += 5;
            } else if (0L == (v & 0xFFFFFC0000000000L)) {
                result += 6;
            } else if (0L == (v & 0xFFFE000000000000L)) {
                result += 7;
            } else if (0L == (v & 0xFF00000000000000L)) {
                result += 8;
            } else {
                result += 9;
            }
        }
        return result;
    }

    /**
     * same as offset, but operates on collections
     */
    @SuppressWarnings("unchecked")
    private final <T> long encode(Collection<T> xs, FieldType<?> f) {
        switch (f.typeID) {

        // case ConstantI8(_) | ConstantI16(_) | ConstantI32(_) | ConstantI64(_) | ConstantV64(_) ⇒ 0
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
            return 0;

            // case BoolType | I8 ⇒ p.blockInfos.last.count
            // case BoolType | I8 ⇒ xs.size
        case 6:
        case 7:
            return xs.size();

            // case I16 ⇒ 2 * xs.size
        case 8:
            return 2 * xs.size();

            // case F32 | I32 ⇒ 4 * xs.size
        case 9:
        case 12:
            return 4 * xs.size();

            // case F64 | I64 ⇒ 8 * xs.size
        case 10:
        case 13:
            return 8 * xs.size();

            // case V64 ⇒ encodeV64(xs.asInstanceOf[Iterable[Long]])
        case 11:
            return encodeV64((Iterable<Long>) xs);

            // case s : Annotation ⇒ xs.asInstanceOf[Iterable[SkillType]].foldLeft(0L)((r : Long, v : SkillType) ⇒ r +
            // encodeSingleV64(1 + state.poolByName(v.getClass.getName.toLowerCase).poolIndex) +
            // encodeSingleV64(v.getSkillID))
        case 5: {
            long result = 0L;
            Iterable<? extends SkillObject> rs = (Iterable<? extends SkillObject>) xs;
            for (SkillObject ref : rs) {
                result += encodeSingleV64(1 + state.poolByName().get(ref.getClass().getName().toLowerCase()).typeID - 32);
                result += encodeSingleV64(ref.getSkillID());
            }
            return result;
        }

        // case s : StringType ⇒ xs.asInstanceOf[Iterable[String]].foldLeft(0L)((r : Long, v : String) ⇒ r +
        // encodeSingleV64(stringIDs(v)))
        case 14: {
            Iterable<String> vs = (Iterable<String>) xs;
            long result = 0L;
            for (String v : vs) {
                result += encodeSingleV64(stringIDs.get(v));
            }
            return result;
        }

        //
        // case ConstantLengthArray(l, t) ⇒ xs.asInstanceOf[Iterable[ArrayBuffer[_]]].foldLeft(0L) {
        // case (sum, i) ⇒ sum + encode(i, t)
        // }
        // case VariableLengthArray(t) ⇒ xs.asInstanceOf[Iterable[ArrayBuffer[_]]].foldLeft(0L) {
        // case (sum, i) ⇒ sum + encodeSingleV64(i.size) + encode(i, t)
        // }
        // case ListType(t) ⇒ xs.asInstanceOf[Iterable[ListBuffer[_]]].foldLeft(0L) {
        // case (sum, i) ⇒ sum + encodeSingleV64(i.size) + encode(i, t)
        // }
        // case SetType(t) ⇒ xs.asInstanceOf[Iterable[HashSet[_]]].foldLeft(0L) {
        // case (sum, i) ⇒ sum + encodeSingleV64(i.size) + encode(i, t)
        // }
        //
        case 17:
        case 18:
        case 19: {
            long result = 0L;
            Collection<Collection<?>> is = (Collection<Collection<?>>) xs;
            FieldType<?> g = ((SingleArgumentType<?, ?>) f).groundType;
            for (Collection<?> i : is) {
                result += encodeSingleV64(i.size());
                result += encode(i, g);
            }

            return result;
        }

        // case MapType(k, v) ⇒ xs.asInstanceOf[Iterable[HashMap[_, _]]].foldLeft(0L) {
        // case (sum, i) ⇒ sum + encodeSingleV64(i.size) + encode(i.keys, k) + encode(i.values, v)
        // }
        case 20: {
            long result = 0L;
            Collection<HashMap<?, ?>> ms = (Collection<HashMap<?, ?>>) xs;
            FieldType<?> kt = ((MapType<?, ?>) f).keyType;
            FieldType<?> vt = ((MapType<?, ?>) f).valueType;
            for (final HashMap<?, ?> m : ms) {
                result += encodeSingleV64(m.size());
                result += encode(m.keySet(), kt);
                result += encode(m.values(), vt);
            }

            return result;
        }

        // case t : StoragePool[_, _] ⇒
        // if (t.base.size < 128) xs.size // quick solution for small pools
        // else encodeRefs(xs.asInstanceOf[Iterable[SkillType]])
        default:
            StoragePool<?, ?> target = (StoragePool<?, ?>) f;
            // shortcut if references are small anyway
            if (target.basePool.size() < 128)
                return xs.size();

            return encodeRefs((Iterable<? extends SkillObject>) xs);
        }
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
