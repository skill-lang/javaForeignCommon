package de.ust.skill.common.java.internal;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import de.ust.skill.common.java.internal.fieldDeclarations.IgnoredField;
import de.ust.skill.common.java.internal.fieldTypes.Annotation;
import de.ust.skill.common.java.internal.fieldTypes.BoolType;
import de.ust.skill.common.java.internal.fieldTypes.ConstantI16;
import de.ust.skill.common.java.internal.fieldTypes.ConstantI32;
import de.ust.skill.common.java.internal.fieldTypes.ConstantI64;
import de.ust.skill.common.java.internal.fieldTypes.ConstantI8;
import de.ust.skill.common.java.internal.fieldTypes.ConstantLengthArray;
import de.ust.skill.common.java.internal.fieldTypes.ConstantV64;
import de.ust.skill.common.java.internal.fieldTypes.F32;
import de.ust.skill.common.java.internal.fieldTypes.F64;
import de.ust.skill.common.java.internal.fieldTypes.I16;
import de.ust.skill.common.java.internal.fieldTypes.I32;
import de.ust.skill.common.java.internal.fieldTypes.I64;
import de.ust.skill.common.java.internal.fieldTypes.I8;
import de.ust.skill.common.java.internal.fieldTypes.ListType;
import de.ust.skill.common.java.internal.fieldTypes.MapType;
import de.ust.skill.common.java.internal.fieldTypes.ReferenceType;
import de.ust.skill.common.java.internal.fieldTypes.SetType;
import de.ust.skill.common.java.internal.fieldTypes.StringType;
import de.ust.skill.common.java.internal.fieldTypes.V64;
import de.ust.skill.common.java.internal.fieldTypes.VariableLengthArray;
import de.ust.skill.common.java.internal.parts.Block;
import de.ust.skill.common.java.internal.parts.BulkChunk;
import de.ust.skill.common.java.internal.parts.Chunk;
import de.ust.skill.common.java.internal.parts.SimpleChunk;
import de.ust.skill.common.java.restrictions.FieldRestriction;
import de.ust.skill.common.java.restrictions.NonNull;
import de.ust.skill.common.java.restrictions.TypeRestriction;
import de.ust.skill.common.jvm.streams.FileInputStream;
import de.ust.skill.common.jvm.streams.MappedInStream;

/**
 * The parser implementation is based on the denotational semantics given in
 * TR14§6.
 *
 * @author Timm Felden
 */
public abstract class FileParser<State extends SkillState> {
    protected FileInputStream in;

    // ERROR REPORTING
    protected int blockCounter = 0;
    protected HashSet<String> seenTypes = new HashSet<>();

    // strings
    protected final StringPool Strings;

    // types
    protected final ArrayList<StoragePool<?, ?>> types = new ArrayList<>();
    protected final HashMap<String, StoragePool<?, ?>> poolByName = new HashMap<>();
    final Annotation Annotation = new Annotation(types);
    final StringType StringType;

    /**
     * creates a new storage pool of matching name
     * 
     * @note implementation depends heavily on the specification
     */
    protected abstract <T extends B, B extends SkillObject> StoragePool<T, B> newPool(String name,
            StoragePool<? super T, B> superPool, HashSet<TypeRestriction> restrictions);

    protected FileParser(FileInputStream in) {
        this.in = in;
        Strings = new StringPool(in);
        StringType = new StringType(Strings);
    }

    final protected void stringBlock() throws ParseException {
        try {
            int count = (int) in.v64();

            if (0 != count) {
                // read offsets
                int[] offsets = new int[count];
                for (int i = 0; i < count; i++) {
                    offsets[i] = in.i32();
                }

                // store offsets
                int last = 0;
                for (int i = 0; i < count; i++) {
                    Strings.stringPositions.add(new StringPool.Position(in.position() + last, offsets[i] - last));
                    Strings.idMap.add(null);
                    last = offsets[i];
                }
                in.jump(in.position() + last);
            }
        } catch (Exception e) {
            throw new ParseException(in, blockCounter, e, "corrupted string block");
        }
    }

    // deferred pool resize requests
    private final LinkedList<StoragePool<?, ?>> resizeQueue = new LinkedList<>();
    // deferred field declaration appends: pool, ID, type, name, block
    private final LinkedList<InsertionEntry> fieldInsertionQueue = new LinkedList<>();

    private final static class InsertionEntry {
        public InsertionEntry(StoragePool<?, ?> owner, int ID, FieldType<?> t, HashSet<FieldRestriction<?>> rest,
                String name, BulkChunk bulkChunkInfo) {
            this.owner = owner;
            // TODO Auto-generated constructor stub
            this.ID = ID;
            type = t;
            restrictions = rest;
            this.name = name;
            bci = bulkChunkInfo;
        }

        final StoragePool<?, ?> owner;
        final int ID;
        final HashSet<FieldRestriction<?>> restrictions;
        final FieldType<?> type;
        final String name;
        final BulkChunk bci;
    }

    // field data updates: pool x fieldID
    private final LinkedList<DataEntry> fieldDataQueue = new LinkedList<>();

    private final static class DataEntry {
        public DataEntry(StoragePool<?, ?> owner, int fieldID) {
            this.owner = owner;
            this.fieldID = fieldID;
        }

        final StoragePool<?, ?> owner;
        final int fieldID;
    }

    private long offset = 0L;

    /**
     * Turns a field type into a preliminary type information. In case of user
     * types, the declaration of the respective user type may follow after the
     * field declaration.
     */
    FieldType<?> fieldType() {
        final int typeID = (int) in.v64();
        switch (typeID) {
        case 0:
            return new ConstantI8(in.i8());
        case 1:
            return new ConstantI16(in.i16());
        case 2:
            return new ConstantI32(in.i32());
        case 3:
            return new ConstantI64(in.i64());
        case 4:
            return new ConstantV64(in.v64());
        case 5:
            return Annotation;
        case 6:
            return BoolType.get();
        case 7:
            return I8.get();
        case 8:
            return I16.get();
        case 9:
            return I32.get();
        case 10:
            return I64.get();
        case 11:
            return V64.get();
        case 12:
            return F32.get();
        case 13:
            return F64.get();
        case 14:
            return StringType;
        case 15:
            return new ConstantLengthArray<>(in.v64(), fieldType());
        case 17:
            return new VariableLengthArray<>(fieldType());
        case 18:
            return new ListType<>(fieldType());
        case 19:
            return new SetType<>(fieldType());
        case 20:
            return new MapType<>(fieldType(), fieldType());
        default:
            if (typeID >= 32)
                return new TypeDefinitionIndex<>(typeID - 32);

            throw new ParseException(in, blockCounter, null, "Invalid type ID: %d", typeID);
        }
    }

    private HashSet<TypeRestriction> typeRestrictions() {
        final HashSet<TypeRestriction> rval = new HashSet<>();
        // parse count many entries
        for (int i = (int) in.v64(); i != 0; i--) {
            final int id = (int) in.v64();
            switch (id) {
            case 0:
                // Unique
            case 1:
                // Singleton
            case 2:
                // Monotone
            default:
                if (id <= 5 || 1 == (id % 2))
                    throw new ParseException(in, blockCounter, null,
                            "Found unknown type restriction %d. Please regenerate your binding, if possible.", id);
                System.err
                        .println("Skiped unknown skippable type restriction. Please update the SKilL implementation.");
                break;
            }
        }
        return rval;
    }

    private HashSet<FieldRestriction<?>> fieldRestrictions(FieldType<?> t) {
        HashSet<FieldRestriction<?>> rval = new HashSet<FieldRestriction<?>>();
        for (int count = (int) in.v64(); count != 0; count--) {
            final int id = (int) in.v64();
            switch (id) {

            case 0:
                if (t instanceof ReferenceType)
                    rval.add(NonNull.get());
                else
                    throw new ParseException(in, blockCounter, null, "Nonnull restriction on non-refernce type: %s.",
                            t.toString());
                break;

            case 3:
                // TODO provide translation
                // t match {
                // case I8 ⇒ Range.make(in.i8, in.i8)
                // case I16 ⇒ Range.make(in.i16, in.i16)
                // case I32 ⇒ Range.make(in.i32, in.i32)
                // case I64 ⇒ Range.make(in.i64, in.i64)
                // case V64 ⇒ Range.make(in.v64, in.v64)
                // case F32 ⇒ Range.make(in.f32, in.f32)
                // case F64 ⇒ Range.make(in.f64, in.f64)
                // case t ⇒ throw new ParseException(in, blockCounter,
                // s"Type $t can not be range restricted!", null)
                // }
            case 5:
                // case 5 ⇒ Coding(String.get(in.v64))
            case 7:
                // case 7 ⇒ ConstantLengthPointer
            default:
                if (id <= 9 || 1 == (id % 2))
                    throw new ParseException(in, blockCounter, null,
                            "Found unknown field restriction %d. Please regenerate your binding, if possible.", id);
                System.err
                        .println("Skipped unknown skippable type restriction. Please update the SKilL implementation.");
            }
        }
        return rval;
    }

    @SuppressWarnings("unchecked")
    private <B extends SkillObject, T extends B> void typeDefinition() {
        // read type part
        final String name = Strings.get(in.v64());
        if (null == name)
            throw new ParseException(in, blockCounter, null, "corrupted file: nullptr in typename");

        // type duplication error detection
        if (seenTypes.contains(name))
            throw new ParseException(in, blockCounter, null, "Duplicate definition of type %s", name);
        seenTypes.add(name);

        // try to parse the type definition
        try {
            long count = in.v64();

            StoragePool<T, B> definition = null;
            if (poolByName.containsKey(name)) {
                definition = (StoragePool<T, B>) poolByName.get(name);
            } else {
                // restrictions
                final HashSet<TypeRestriction> rest = typeRestrictions();
                // super
                final StoragePool<? super T, B> superDef;
                {
                    final int superID = (int) in.v64();
                    if (0 == superID)
                        superDef = null;
                    else if (superID > types.size())
                        throw new ParseException(
                                in,
                                blockCounter,
                                null,
                                "Type %s refers to an ill-formed super type.\n          found: %d; current number of other types %d",
                                name, superID, types.size());
                    else
                        superDef = (StoragePool<? super T, B>) types.get(superID - 1);
                }

                // allocate pool
                definition = newPool(name, superDef, rest);
            }

            final long bpo = definition.basePool.data.length
                    + ((0L != count && null != definition.superPool) ? in.v64() : 0L);

            // store block info and prepare resize
            definition.blocks.add(new Block(bpo, count));
            resizeQueue.add(definition);

            // read field part
            final ArrayList<FieldDeclaration<?, T>> fields = definition.fields;
            int totalFieldCount = fields.size();

            final int localFieldCount = (int) in.v64();
            for (int fieldCounter = 0; fieldCounter < localFieldCount; fieldCounter++) {
                final int ID = (int) in.v64();
                if (ID > totalFieldCount || ID < 0)
                    throw new ParseException(in, blockCounter, null, "Found an illegal field ID: %d", ID);

                final long end;
                if (ID == totalFieldCount) {
                    // new field
                    final String fieldName = Strings.get(in.v64());
                    if (null == fieldName)
                        throw new ParseException(in, blockCounter, null, "corrupted file: nullptr in fieldname");

                    FieldType<?> t = fieldType();
                    HashSet<FieldRestriction<?>> rest = fieldRestrictions(t);
                    end = in.v64();

                    fieldInsertionQueue.add(new InsertionEntry(definition, ID, t, rest, name, new BulkChunk(offset,
                            end, count + definition.size())));
                    totalFieldCount += 1;

                } else {
                    // field already seen
                    end = in.v64();
                    fields.get(ID).addChunk(new SimpleChunk(offset, end, bpo, count));

                }
                offset = end;
                fieldDataQueue.add(new DataEntry(definition, ID));
            }
        } catch (java.nio.BufferUnderflowException e) {
            throw new ParseException(in, blockCounter, e, "unexpected end of file");
        }
    }

    protected void typeBlock() {
        // reset fields
        resizeQueue.clear();
        fieldInsertionQueue.clear();
        fieldDataQueue.clear();
        offset = 0L;

        // parse block
        for (int count = (int) in.v64(); count != 0; count--)
            typeDefinition();

        // resize pools
        {
            Stack<StoragePool<?, ?>> resizeStack = new Stack<>();

            // resize base pools and push entries to stack
            for (StoragePool<?, ?> p : resizeQueue) {
                if (p instanceof BasePool<?>) {
                    final ArrayList<Block> bs = p.blocks;
                    final Block last = bs.get(bs.size() - 1);
                    ((BasePool<?>) p).resizeData((int) last.count);
                }
                resizeStack.push(p);
            }

            // create instances from stack
            for (StoragePool<?, ?> p : resizeStack) {
                final ArrayList<Block> bs = p.blocks;
                final Block last = bs.get(bs.size() - 1);
                int i = (int) last.bpo;
                int high = (int) (last.bpo + last.count);
                while (i < high && p.insertInstance(i + 1))
                    i += 1;
            }
        }
        // insert fields
        for (InsertionEntry e : fieldInsertionQueue) {
            FieldDeclaration<?, ?> f = e.owner.addField(e.ID, e.type, e.name, e.restrictions);
            f.eliminatePreliminaryTypes(types);
            f.addChunk(e.bci);
        }

        processFieldData();
    }

    private final void processFieldData() {
        // we have to add the file offset to all begins and ends we encounter
        final long fileOffset = in.position();
        long dataEnd = fileOffset;

        // awaiting async read operations
        final Semaphore readBarrier = new Semaphore(1 - fieldDataQueue.size());
        // async reads will post their errors in this queue
        final ConcurrentLinkedQueue<Throwable> readErrors = new ConcurrentLinkedQueue<Throwable>();

        // process field data declarations in order of appearance and update
        // offsets to absolute positions
        for (DataEntry e : fieldDataQueue) {
            FieldDeclaration<?, ?> f = e.owner.fields.get(e.fieldID);
            try {
                f.eliminatePreliminaryTypes(types);
            } catch (Exception ex) {
                throw new ParseException(in, blockCounter, ex, "inexistent user type %d (user types: %s)",
                        f.type.typeID, poolByName.keySet().toString());
            }

            // make begin/end absolute
            f.addOffsetToLastChunk(fileOffset);
            final Chunk last = f.lastChunk();

            final MappedInStream map;
            try {
                map = in.map(0L, last.begin, last.end);
            } catch (IOException e1) {
                throw new Error(e1);
            }

            SkillState.pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        f.read(map);
                        // map was not consumed
                        if (!map.eof() && !(f instanceof LazyField<?, ?> || f instanceof IgnoredField))
                            readErrors.add(new PoolSizeMissmatchError(blockCounter, last.begin, last.end, f));
                    } catch (BufferUnderflowException e) {
                        readErrors.add(new PoolSizeMissmatchError(blockCounter, last.begin, last.end, f));
                    } catch (Throwable t) {
                        readErrors.add(t);
                    } finally {
                        readBarrier.release();
                    }
                }
            });
            dataEnd = Math.max(dataEnd, last.end);
        }
        in.jump(dataEnd);

        // await async reads
        try {
            readBarrier.acquire();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        for (Throwable e : readErrors) {
            e.printStackTrace();
        }
        if (!readErrors.isEmpty())
            throw new ParseException(in, blockCounter, readErrors.peek(),
                    "unexpected exception(s) while reading field data (see above)");
    }
}
