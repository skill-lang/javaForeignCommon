package de.ust.skill.common.java.internal.parts;

/**
 * A chunk that is used iff a field is appended to a preexisting type in a
 * block.
 * 
 * @author Timm Felden
 */
public class BulkChunk extends Chunk {

    public BulkChunk(long begin, long end, long count) {
        super(begin, end, count);
    }

}
