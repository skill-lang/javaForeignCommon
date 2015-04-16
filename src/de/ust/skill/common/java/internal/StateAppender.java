package de.ust.skill.common.java.internal;

import java.util.ArrayList;
import java.util.HashMap;

import de.ust.skill.common.java.internal.parts.Chunk;
import de.ust.skill.common.jvm.streams.FileOutputStream;

final public class StateAppender extends SerializationFunctions {

    public StateAppender(SkillState state, FileOutputStream out) {
        super(state);

        // save the index of the first new pool
        final int newPoolIndex;
        {
            int i = 0;
            for (StoragePool<?, ?> t : state.types) {
                if (t.blocks.isEmpty())
                    break;
                i++;
            }
            newPoolIndex = i;
        }

        // make lbpsi map, update data map to contain dynamic instances and create serialization skill IDs for
        // serialization
        // index → bpsi
        final int[] lbpoMap = new int[state.types.size()];
        final HashMap<FieldDeclaration<?, ?>, Chunk> chunkMap = new HashMap<>();
        state.types.stream().parallel().forEach(p -> {
            if (p instanceof BasePool<?>) {
                makeLBPOMap(p, lbpoMap, 0);
                ((BasePool<?>) p).prepareAppend(chunkMap);
                p.fixed(true);
            }
        });

         // locate relevant pools
        final ArrayList<StoragePool<?, ?>> rPools = new ArrayList<>(state.types.size());
        for(StoragePool<?, ?> p : state.types){
            // new index?
            if(p.typeID - 32 >= newPoolIndex)
                rPools.add(p);
            // new instance or field?
            else if(p.size() > 0){
                boolean exists = false;
                for(FieldDeclaration<?, ?> f : p.fields){
                    if(chunkMap.containsKey(f)){
                        exists = true;
                        break;
                    }
                }
                if(exists)
                    rPools.add(p);
            }
        }
        
         // locate relevant fields
        final FieldDeclaration<?, ?>[] rFields = chunkMap.keySet().toArray(new FieldDeclaration<?, ?>[0]);

        /**
         * **************** PHASE 3: WRITE * ****************
         */

        // TODO TBD

        /**
         * **************** PHASE 4: CLEANING * ****************
         */

        // release data structures
        state.stringType.clearIDs();
        // unfix pools
        for (StoragePool<?, ?> p : state.types) {
            p.fixed(false);
        }
    }

    private final static int makeLBPOMap(StoragePool<?, ?> p, int[] lbpoMap, int next) {
        lbpoMap[p.typeID - 32] = next;
        int result = next + p.newObjects.size();
        for (SubPool<?, ?> sub : p.subPools) {
            result = makeLBPOMap(sub, lbpoMap, result);
        }
        return result;

    }
}
