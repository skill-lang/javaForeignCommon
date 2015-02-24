package de.ust.skill.common.java.internal;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import de.ust.skill.common.java.internal.fieldTypes.ConstantLengthArray;
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
    protected final HashMap<String, Long> stringIDs = new HashMap<String, Long>();

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
    }

    protected final void annotation(SkillObject ref, OutStream out) throws IOException {
        if (null == ref) {
            // magic trick!
            out.i16((short) 0);
            return;
        }

        if (ref instanceof NamedType)
            string(((NamedType) ref).τName(), out);
        else
            string(ref.getClass().getSimpleName().toLowerCase(), out);
        out.v64(ref.getSkillID());
    }

    protected final void string(String v, OutStream out) throws IOException {
        out.v64(stringIDs.get(v));
    }

    /**
     * ************************************************ UTILITY FUNCTIONS REQUIRED FOR PARALLEL ENCODING
     * ************************************************
     */

    @SuppressWarnings("unchecked")
    protected final <T extends B, B extends SkillObject> long offset(StoragePool<T, B> p, FieldDeclaration<?, T> f) {
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
            return encodeV64(p, (FieldDeclaration<Long, T>) f);

            // case s : Annotation ⇒ p.all.map(_.get(f).asInstanceOf[SkillType]).foldLeft(0L)((r : Long, v : SkillType)
            // ⇒ r + encodeSingleV64(1 + state.poolByName(v.getClass.getName.toLowerCase).poolIndex) +
            // encodeSingleV64(v.getSkillID))
        case 5: {
            FieldDeclaration<SkillObject, T> field = (FieldDeclaration<SkillObject, T>) f;
            long result = 0L;
            for (T i : p) {
                SkillObject ref = i.get(field);
                result += encodeSingleV64(1 + state.poolByName().get(ref.getClass().getName().toLowerCase()).typeID - 32);
                result += encodeSingleV64(ref.getSkillID());
            }
            return result;
        }

        // case s : StringType ⇒ p.all.map(_.get(f).asInstanceOf[String]).foldLeft(0L)((r : Long, v : String) ⇒ r +
        // encodeSingleV64(stringIDs(v)))
        case 14: {
            FieldDeclaration<String, T> field = (FieldDeclaration<String, T>) f;
            long result = 0L;
            for (T i : p) {
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
            Iterator<T> is = Iterators.fakeArray(p.basePool.data, (int) b.bpo, (int) (b.bpo + b.count));
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
            Iterator<T> is = Iterators.fakeArray(p.basePool.data, (int) b.bpo, (int) (b.bpo + b.count));
            FieldType<?> g = ((SingleArgumentType<?, ?>) f.type).groundType;
            while (is.hasNext()) {
                Collection<?> xs = (Collection<?>) is.next().get(f);
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
            Iterator<T> is = Iterators.fakeArray(p.basePool.data, (int) b.bpo, (int) (b.bpo + b.count));
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

            return encodeRefs(p, (FieldDeclaration<? extends SkillObject, T>) f);
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
    private final static <T extends B, B extends SkillObject> long encodeV64(StoragePool<T, B> p,
            FieldDeclaration<Long, T> f) {
        long result = 0L;
        Block b = p.blocks.getLast();
        Iterator<T> is = Iterators.fakeArray(p.basePool.data, (int) b.bpo, (int) (b.bpo + b.count));
        while (is.hasNext()) {
            T i = is.next();
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

    private final static <T extends B, B extends SkillObject> long encodeRefs(StoragePool<T, B> p,
            FieldDeclaration<? extends SkillObject, T> f) {
        long result = 0L;
        Block b = p.blocks.getLast();
        Iterator<T> is = Iterators.fakeArray(p.basePool.data, (int) b.bpo, (int) (b.bpo + b.count));
        while (is.hasNext()) {
            T i = is.next();
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
    //
    // // TODO this is not a good solution! (slow and fucked up, but funny)
    // def typeToSerializationFunction(t : FieldType[_]) : (Any, OutStream) ⇒ Unit = {
    // implicit def lift[T](f : (T, OutStream) ⇒ Unit) : (Any, OutStream) ⇒ Unit = { case (x, out) ⇒
    // f(x.asInstanceOf[T], out) }
    // t match {
    // case ConstantI8(_) | ConstantI16(_) | ConstantI32(_) | ConstantI64(_) | ConstantV64(_) ⇒ { case (x, out) ⇒ }
    //
    // case BoolType ⇒ bool
    // case I8 ⇒ i8
    // case I16 ⇒ i16
    // case I32 ⇒ i32
    // case I64 ⇒ i64
    // case V64 ⇒ v64
    // case F32 ⇒ f32
    // case F64 ⇒ f64
    //
    // case Annotation(_) ⇒ annotation
    // case StringType(_) ⇒ string
    //
    // case ConstantLengthArray(len, sub) ⇒ lift(writeConstArray(typeToSerializationFunction(sub)))
    // case VariableLengthArray(sub) ⇒ lift(writeVarArray(typeToSerializationFunction(sub)))
    // case ListType(sub) ⇒ lift(writeList(typeToSerializationFunction(sub)))
    // case SetType(sub) ⇒ lift(writeSet(typeToSerializationFunction(sub)))
    //
    // case MapType(k, v) ⇒ lift(writeMap(typeToSerializationFunction(k), typeToSerializationFunction(v)))
    //
    // case s : StoragePool[_, _] ⇒ userRef
    //
    // case TypeDefinitionIndex(_) | TypeDefinitionName(_) ⇒
    // throw new
    // IllegalStateException("trying to serialize an intermediary type representation can never be successful")
    // }
    // }
    // }
    //
    // object SerializationFunctions {
    //
    // @inline final def userRef[T <: SkillType](ref : T, out : OutStream) {
    // if (null == ref) out.i8(0.toByte)
    // else out.v64(ref.getSkillID)
    // }
    //
    // @inline def bool(v : Boolean, out : OutStream) = out.i8(if (v) -1.toByte else 0.toByte)
    //
    // @inline def i8(v : Byte, out : OutStream) = out.i8(v)
    // @inline def i16(v : Short, out : OutStream) = out.i16(v)
    // @inline def i32(v : Int, out : OutStream) = out.i32(v)
    // @inline def i64(v : Long, out : OutStream) = out.i64(v)
    // @inline def v64(v : Long, out : OutStream) = out.v64(v)
    //
    // @inline def f32(v : Float, out : OutStream) = out.f32(v)
    // @inline def f64(v : Double, out : OutStream) = out.f64(v)
    //
    // @inline def writeConstArray[T, S >: T](trans : (S, OutStream) ⇒ Unit)(elements :
    // scala.collection.mutable.ArrayBuffer[T], out : OutStream) {
    // for (e ← elements)
    // trans(e, out)
    // }
    // @inline def writeVarArray[T, S >: T](trans : (S, OutStream) ⇒ Unit)(elements :
    // scala.collection.mutable.ArrayBuffer[T], out : OutStream) {
    // out.v64(elements.size)
    // for (e ← elements)
    // trans(e, out)
    // }
    // @inline def writeList[T, S >: T](trans : (S, OutStream) ⇒ Unit)(elements :
    // scala.collection.mutable.ListBuffer[T], out : OutStream) {
    // out.v64(elements.size)
    // for (e ← elements)
    // trans(e, out)
    // }
    // @inline def writeSet[T, S >: T](trans : (S, OutStream) ⇒ Unit)(elements : scala.collection.mutable.HashSet[T],
    // out : OutStream) {
    // out.v64(elements.size)
    // for (e ← elements)
    // trans(e, out)
    // }
    // def writeMap[T, U](keys : (T, OutStream) ⇒ Unit, vals : (U, OutStream) ⇒ Unit)(elements :
    // scala.collection.mutable.HashMap[T, U], out : OutStream) {
    // out.v64(elements.size)
    // for ((k, v) ← elements) {
    // keys(k, out)
    // vals(v, out)
    // }
    // }
    //
    // /**
    // * TODO serialization of restrictions
    // */
    // def restrictions(p : StoragePool[_, _], out : OutStream) = out.i8(0.toByte)
    // /**
    // * TODO serialization of restrictions
    // */
    // def restrictions(f : FieldDeclaration[_], out : OutStream) = out.i8(0.toByte)
    // /**
    // * serialization of types is fortunately independent of state, because field types know their ID
    // */
    // def writeType(t : FieldType[_], out : OutStream) : Unit = t match {
    // case ConstantI8(v) ⇒
    // v64(t.typeID, out)
    // i8(v, out)
    // case ConstantI16(v) ⇒
    // v64(t.typeID, out)
    // i16(v, out)
    // case ConstantI32(v) ⇒
    // v64(t.typeID, out)
    // i32(v, out)
    // case ConstantI64(v) ⇒
    // v64(t.typeID, out)
    // i64(v, out)
    // case ConstantV64(v) ⇒
    // v64(t.typeID, out)
    // v64(v, out)
    //
    // case ConstantLengthArray(l, t) ⇒
    // out.i8(0x0F.toByte)
    // v64(l, out)
    // v64(t.typeID, out)
    //
    // case VariableLengthArray(t) ⇒
    // out.i8(0x11.toByte)
    // v64(t.typeID, out)
    // case ListType(t) ⇒
    // out.i8(0x12.toByte)
    // v64(t.typeID, out)
    // case SetType(t) ⇒
    // out.i8(0x13.toByte)
    // v64(t.typeID, out)
    //
    // case MapType(k, v) ⇒
    // out.i8(0x14.toByte)
    // writeType(k, out)
    // writeType(v, out)
    //
    // case _ ⇒
    // v64(t.typeID, out)
    // }
    //
    // /**
    // * creates an lbpo map by recursively adding the local base pool offset to the map and adding all sub pools
    // * afterwards
    // */
    // final def makeLBPOMap[T <: B, B <: SkillType](pool : StoragePool[T, B], lbpsiMap : Array[Long], next : Long, size
    // : StoragePool[_, _] ⇒ Long) : Long = {
    // lbpsiMap(pool.poolIndex.toInt) = next
    // var result = next + size(pool)
    // for (sub ← pool.subPools) {
    // result = makeLBPOMap(sub, lbpsiMap, result, size)
    // }
    // result
    // }
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
