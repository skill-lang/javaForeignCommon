package age.internal;

import java.util.Iterator;

import age.Age;
import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.fieldDeclarations.KnownLongField;
import de.ust.skill.common.java.internal.fieldTypes.V64;
import de.ust.skill.common.java.internal.parts.Chunk;
import de.ust.skill.common.java.internal.parts.SimpleChunk;
import de.ust.skill.common.jvm.streams.MappedInStream;

public class KnownField_Age_age extends FieldDeclaration<Long, Age> implements KnownLongField<Age> {

    public KnownField_Age_age(long index, AgeAccess owner) {
        super(V64.get(), "age", index, owner);
    }

    @Override
    public void read(MappedInStream in) {
        final Iterator<Age> is;
        Chunk last = dataChunks.getLast();
        if (last instanceof SimpleChunk) {
            SimpleChunk c = (SimpleChunk) last;
            is = ((AgeAccess) owner).dataViewIterator((int) c.bpo, (int) (c.bpo + c.count));
        } else
            is = owner.iterator();

        while (is.hasNext()) {
            long v = in.v64();
            System.out.println(v);
            is.next().setAge(v);
        }
    }

    @Override
    public Long getR(Age ref) {
        return ref.getAge();
    }

    @Override
    public void setR(Age ref, Long value) {
        ref.setAge(value);
    }

    @Override
    public long get(Age ref) {
        return ref.getAge();
    }

    @Override
    public void set(Age ref, long value) {
        ref.setAge(value);
    }

}
