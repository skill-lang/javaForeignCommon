package de.ust.skill.common.java.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import de.ust.skill.common.java.api.StringAccess;
import de.ust.skill.common.jvm.streams.FileInputStream;
import de.ust.skill.common.jvm.streams.FileOutputStream;

/**
 * @author Timm Felden
 * @note String pools use magic index 0 for faster translation of string ids to strings.
 * @note String pool may contain duplicates, if strings have been added. This is a necessary behavior, if add should be
 *       an O(1) operation and Strings are loaded from file lazily.
 */
public class StringPool implements StringAccess {
    final private FileInputStream input;

    /**
     * the set of new strings, i.e. strings which do not have an ID
     */
    private final HashSet<String> newStrings = new HashSet<>();

    /**
     * ID ⇀ (absolute offset, length) will be used if idMap contains a null reference
     *
     * @note there is a fake entry at ID 0
     */
    final ArrayList<Position> stringPositions;

    final static class Position {
        public Position(long l, int i) {
            absoluteOffset = l;
            length = i;
        }

        public long absoluteOffset;
        public int length;
    }

    /**
     * get string by ID
     */
    final ArrayList<String> idMap;

    /**
     * DO NOT CALL IF YOU ARE NOT GENERATED OR INTERNAL CODE!
     */
    public StringPool(FileInputStream input) {
        this.input = input;
        stringPositions = new ArrayList<>();
        stringPositions.add(new Position(-1L, -1));
        idMap = new ArrayList<>();
        idMap.add(null);
    }

    @Override
    public int size() {
        return stringPositions.size() + newStrings.size();
    }

    @Override
    public String get(long index) {
        if (0L == index)
            return null;

        String result;
        try {
            result = idMap.get((int) index);
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidPoolIndexException(index, stringPositions.size(), "string");
        }
        if (null != result)
            return result;

        // we have to load the string from disk
        // @note this block has to be synchronized in order to enable parallel
        // decoding of field data
        // @note this is correct, because string pool is the only one who can do
        // parallel operations on input!
        synchronized (this) {
            Position off = stringPositions.get((int) index);
            input.push(off.absoluteOffset);
            byte[] chars = input.bytes(off.length);
            input.pop();

            try {
                result = new String(chars, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // as if that would ever happen
                e.printStackTrace();
            }
            idMap.set((int) index, result);
        }
        return result;
    }

    public void prepareAndWrite(FileOutputStream out, StateWriter ws) throws IOException {
        HashMap<String, Integer> serializationIDs = ws.stringIDs;

        // ensure all strings are present
        for (int i = 1; i < stringPositions.size(); i++) {
            get(i);
        }

        // create inverse map
        for (int i = 1; i < idMap.size(); i++) {
            serializationIDs.put(idMap.get(i), i);
        }

        // instert new strings to the map;
        // this is the place where duplications with lazy strings will be detected and eliminated
        for (String s : newStrings) {
            if (!serializationIDs.containsKey(s)) {
                serializationIDs.put(s, idMap.size());
                idMap.add(s);
            }
        }

        // count
        // @note idMap access performance hack
        out.v64(idMap.size() - 1);

        // @note idMap access performance hack
        if (1 != idMap.size()) {
            // offsets
            ByteBuffer end = ByteBuffer.allocate(4 * (idMap.size() - 1));
            int off = 0;
            for (int i = 1; i < idMap.size(); i++) {
                off += idMap.get(i).getBytes().length;
                end.putInt(off);
            }
            out.put(end.array());

            // data
            for (int i = 1; i < idMap.size(); i++)
                out.put(idMap.get(i).getBytes());
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterator<String> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object[] toArray() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean add(String e) {
        return newStrings.add(e);
    }

    @Override
    public boolean remove(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }
}
