package net.pl3x.map.claimchunk.hook;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.data.newdata.IClaimChunkDataHandler;

import java.lang.reflect.Field;

public class ClaimChunkHook {
    public static DataChunk[] getClaims() {
        ClaimChunk cc = ClaimChunk.getInstance();

        IClaimChunkDataHandler dataHandler;
        try {
            Field field = ClaimChunk.class.getDeclaredField("dataHandler");
            field.setAccessible(true);
            dataHandler = (IClaimChunkDataHandler) field.get(cc);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

        return dataHandler.getClaimedChunks();
    }
}
