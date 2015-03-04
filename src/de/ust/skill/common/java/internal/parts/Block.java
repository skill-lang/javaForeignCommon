package de.ust.skill.common.java.internal.parts;

/**
 * A block contains information about instances in a type. A StoragePool holds
 * blocks in order of appearance in a file with the invariant, that the latest
 * block in the list will be the latest block in the file. If a StoragePool
 * holds no block, then it has no instances in a file.
 *
 * @author Timm Felden
 * @note While writing a Pool to disk, the latest block is the block currently
 *       written.
 */
public final class Block {
    public final long bpo;
    public final long count;

    /**
     * @param bpo
     *            the offset of the first instance
     * @param count
     *            the number of instances in this chunk
     */
    public Block(long bpo, long count) {
        this.bpo = bpo;
        this.count = count;
    }

	public boolean contains(long skillID) {
		return bpo <= skillID  && skillID < bpo + count;
	}

}
