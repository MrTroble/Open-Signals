package com.troblecodings.signals.tileentitys;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public interface IChunkloadable {

    default <T> boolean loadChunkAndGetTile(final Class<T> clazz, final Level world,
            final BlockPos pos, final BiConsumer<T, LevelChunk> consumer) {
        if (pos == null)
            return false;
        try {
            @SuppressWarnings("unchecked")
            final Callable<Boolean> call = () -> {
                BlockEntity entity = null;
                LevelChunk chunk = world.getChunkAt(pos);
                final boolean flag = !chunk.isLoaded();
                if (flag) {
                    if (world.isClientSide) {
                        final ChunkProviderClient client = (ChunkProviderClient) world
                                .getChunkProvider();
                        chunk = client.loadChunk(chunk.x, chunk.z);
                    } else {
                        final ChunkProviderServer server = (ChunkProviderServer) world
                                .getChunkProvider();
                        chunk = server.loadChunk(chunk.x, chunk.z);
                    }
                }
                if (chunk == null)
                    return false;
                entity = chunk.getTileEntity(pos, EnumCreateEntityType.IMMEDIATE);

                final boolean flag2 = entity != null && clazz.isInstance(entity);
                if (flag2) {
                    consumer.accept((T) entity, chunk);
                }

                if (flag) {
                    if (world.isClientSide) {
                        final ChunkProviderClient client = (ChunkProviderClient) world
                                .getChunkProvider();
                        client.unloadChunk(chunk.x, chunk.z);
                    } else {
                        final ChunkProviderServer server = (ChunkProviderServer) world
                                .getChunkProvider();
                        server.queueUnload(chunk);
                    }
                }
                return flag2;
            };
            final MinecraftServer mcserver = world.getMinecraftServer();
            if (mcserver == null)
                return Minecraft.getMinecraft().addScheduledTask(call).get();
            return mcserver.callFromMainThread(call).get();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    default boolean loadChunkAndGetBlock(final Level world, final BlockPos pos,
            final BiConsumer<BlockState, Chunk> consumer) {
        if (pos == null)
            return false;
        try {
            final Callable<Boolean> call = () -> {
                BlockState entity = null;
                Chunk chunk = world.getChunkFromBlockCoords(pos);
                final boolean flag = !chunk.isLoaded();
                if (flag) {
                    if (world.isClientSide) {
                        final ChunkProviderClient client = (ChunkProviderClient) world
                                .getChunkProvider();
                        chunk = client.loadChunk(chunk.x, chunk.z);
                    } else {
                        final ChunkProviderServer server = (ChunkProviderServer) world
                                .getChunkProvider();
                        chunk = server.loadChunk(chunk.x, chunk.z);
                    }
                }
                if (chunk == null)
                    return false;
                entity = chunk.getBlockState(pos);

                final boolean flag2 = entity != null;
                if (flag2) {
                    consumer.accept(entity, chunk);
                }

                if (flag) {
                    if (world.isClientSide) {
                        final ChunkProviderClient client = (ChunkProviderClient) world
                                .getChunkProvider();
                        client.unloadChunk(chunk.x, chunk.z);
                    } else {
                        final ChunkProviderServer server = (ChunkProviderServer) world
                                .getChunkProvider();
                        server.queueUnload(chunk);
                    }
                }
                return flag2;
            };
            final MinecraftServer mcserver = world.getMinecraftServer();
            if (mcserver == null)
                return Minecraft.getMinecraft().addScheduledTask(call).get();
            return mcserver.callFromMainThread(call).get();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
