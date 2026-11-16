package com.troblecodings.signals.tileentitys;

import java.util.Comparator;
import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunk.EntityCreationType;

public interface IChunkLoadable {

    public static final TicketType<BlockPos> UPDATE =
            TicketType.create("opensignals_update", Comparator.comparingLong(BlockPos::asLong), 40);

    default <T> boolean loadChunkAndGetTile(final Class<T> clazz, final ServerLevel world,
            final BlockPos pos, final BiConsumer<T, LevelChunk> consumer) {
        if (pos == null)
            return false;
        try {
            final ChunkPos chunkPos = new ChunkPos(pos);
            world.getChunkSource().addRegionTicket(UPDATE, chunkPos, 2, pos);
            final LevelChunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
            final BlockEntity entity = chunk.getBlockEntity(pos, EntityCreationType.IMMEDIATE);
            if (entity != null && clazz.isInstance(entity)) {
                consumer.accept(clazz.cast(entity), chunk);
                return true;
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}