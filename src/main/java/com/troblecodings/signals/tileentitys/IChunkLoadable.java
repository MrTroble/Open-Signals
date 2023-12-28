package com.troblecodings.signals.tileentitys;

import java.util.function.BiConsumer;

import com.troblecodings.signals.OpenSignalsMain;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunk.EntityCreationType;
import net.minecraftforge.common.world.ForgeChunkManager;

public interface IChunkLoadable {

    @SuppressWarnings("unchecked")
    default <T> boolean loadChunkAndGetTile(final Class<T> clazz, final ServerLevel world,
            final BlockPos pos, final BiConsumer<T, LevelChunk> consumer) {
        if (pos == null)
            return false;
        try {
            final LevelChunk chunk = world.getChunkAt(pos);
            final ChunkPos chunkPos = chunk.getPos();
            ForgeChunkManager.forceChunk(world, OpenSignalsMain.MODID, pos, chunkPos.x, chunkPos.z,
                    true, true);
            final BlockEntity entity = chunk.getBlockEntity(pos, EntityCreationType.IMMEDIATE);
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