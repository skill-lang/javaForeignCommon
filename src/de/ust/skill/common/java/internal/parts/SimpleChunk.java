package de.ust.skill.common.java.internal.parts;

/**
 * A chunk used for regular appearances of fields.
 * 
 * @author Timm Felden
 */
public final class SimpleChunk extends Chunk {

    public final long bpo;

    public SimpleChunk(long begin, long end, long bpo, long count) {
        super(begin, end, count);
        this.bpo = bpo;
    }

}
