package de.ust.skill.common.java.internal;

import java.util.HashSet;

import de.ust.skill.common.jvm.streams.FileInputStream;

/**
 * The parser implementation is based on the denotational semantics given in
 * TR14§6.
 *
 * @author Timm Felden
 */
public abstract class FileParser<State extends SkillState> {
    private FileInputStream in;

    // ERROR REPORTING
    protected int blockCounter = 0;
    protected HashSet<String> seenTypes = new HashSet<>();

    // strings
    final StringPool Strings;

    protected FileParser(FileInputStream in) {
        this.in = in;
        Strings = new StringPool(in);
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

    protected void typeBlock() {
        // TODO Auto-generated method stub
        throw new Error("TODO");
    }

}
