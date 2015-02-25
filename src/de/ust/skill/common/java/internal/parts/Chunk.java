package de.ust.skill.common.java.internal.parts;

/**
 * Chunks contain information on where field data can be found.
 *
 * @author Timm Felden
 * @note indices of recipient of the field data is not necessarily continuous;
 *       make use of staticInstances!
 * @note begin and end are mutable, because they will contain relative offsets
 *       while parsing a type block
 * @note this is a POJO that shall not be passed to users!
 */
public abstract class Chunk {
    public long begin;
    public long end;
    public final long count;

    /**
     * @param begin
     *            position of the first byte of the first instance's data
     * @param end
     *            position of the last byte, i.e. the first byte that is not
     *            read
     * @param count
     *            the number of instances in this chunk
     */
    protected Chunk(long begin, long end, long count) {
        this.begin = begin;
        this.end = end;
        this.count = count;
    }
}
