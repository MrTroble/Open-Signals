package eu.gir.girsignals.tileentitys;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraft.world.gen.ChunkProviderServer;

public interface IChunkloadable<T> {
	
	default boolean loadChunkAndGetTile(World world, BlockPos pos, BiConsumer<T, Chunk> consumer) {
		if (pos == null)
			return false;
		try {
			@SuppressWarnings("unchecked")
			final Callable<Boolean> call = () -> {
				TileEntity entity = null;
				Chunk chunk = world.getChunkFromBlockCoords(pos);
				final boolean flag = !chunk.isLoaded();
				if (flag) {
					if (world.isRemote) {
						ChunkProviderClient client = (ChunkProviderClient) world.getChunkProvider();
						chunk = client.loadChunk(chunk.x, chunk.z);
					} else {
						ChunkProviderServer server = (ChunkProviderServer) world.getChunkProvider();
						chunk = server.loadChunk(chunk.x, chunk.z);
					}
				}
				if (chunk == null)
					return false;
				entity = chunk.getTileEntity(pos, EnumCreateEntityType.IMMEDIATE);
				
				final boolean flag2 = entity != null;
				if (flag2) {
					consumer.accept((T) entity, chunk);
				}
				
				if (flag) {
					if (world.isRemote) {
						ChunkProviderClient client = (ChunkProviderClient) world.getChunkProvider();
						client.unloadChunk(chunk.x, chunk.z);
					} else {
						ChunkProviderServer server = (ChunkProviderServer) world.getChunkProvider();
						server.queueUnload(chunk);
					}
				}
				return flag2;
			};
			final MinecraftServer mcserver = world.getMinecraftServer();
			if (mcserver == null)
				return Minecraft.getMinecraft().addScheduledTask(call).get();
			return mcserver.callFromMainThread(call).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	default boolean loadChunkAndGetBlock(World world, BlockPos pos, BiConsumer<IBlockState, Chunk> consumer) {
		if (pos == null)
			return false;
		try {
			final Callable<Boolean> call = () -> {
				IBlockState entity = null;
				Chunk chunk = world.getChunkFromBlockCoords(pos);
				final boolean flag = !chunk.isLoaded();
				if (flag) {
					if (world.isRemote) {
						ChunkProviderClient client = (ChunkProviderClient) world.getChunkProvider();
						chunk = client.loadChunk(chunk.x, chunk.z);
					} else {
						ChunkProviderServer server = (ChunkProviderServer) world.getChunkProvider();
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
					if (world.isRemote) {
						ChunkProviderClient client = (ChunkProviderClient) world.getChunkProvider();
						client.unloadChunk(chunk.x, chunk.z);
					} else {
						ChunkProviderServer server = (ChunkProviderServer) world.getChunkProvider();
						server.queueUnload(chunk);
					}
				}
				return flag2;
			};
			final MinecraftServer mcserver = world.getMinecraftServer();
			if (mcserver == null)
				return Minecraft.getMinecraft().addScheduledTask(call).get();
			return mcserver.callFromMainThread(call).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
