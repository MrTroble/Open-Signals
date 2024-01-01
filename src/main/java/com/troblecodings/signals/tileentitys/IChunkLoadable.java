package com.troblecodings.signals.tileentitys;

import java.util.function.BiConsumer;

import com.troblecodings.signals.OpenSignalsMain;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.world.ForgeChunkManager;

public interface IChunkLoadable {

    @SuppressWarnings("unchecked")
    default <T> boolean loadChunkAndGetTile(final Class<T> clazz, final ServerWorld world,
            final BlockPos pos, final BiConsumer<T, Chunk> consumer) {
        if (pos == null)
            return false;
        try {
            final Chunk chunk = world.getChunkAt(pos);
            final ChunkPos chunkPos = chunk.getPos();
            ForgeChunkManager.forceChunk(world, OpenSignalsMain.MODID, pos, chunkPos.x, chunkPos.z,
                    true, true);
            final TileEntity entity = chunk.getBlockEntity(pos, Chunk.CreateEntityType.IMMEDIATE);
            final boolean flag = entity != null && clazz.isInstance(entity);
            if (flag) {
                consumer.accept((T) entity, chunk);
            }
            ForgeChunkManager.forceChunk(world, OpenSignalsMain.MODID, pos, chunkPos.x, chunkPos.z,
                    false, true);
            return flag;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
