package eu.gir.girsignals.blocks;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

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
			
	default boolean loadChunkAndGetTile(World w, BlockPos pos, BiConsumer<T, Chunk> consumer) {
		if (pos == null)
			return false;
		try {
			@SuppressWarnings("unchecked")
			Callable<Boolean> call = () -> {
				TileEntity entity = null;
				Chunk ch = w.getChunkFromBlockCoords(pos);
				boolean flag = !ch.isLoaded();
				if (flag) {
					if (w.isRemote) {
						ChunkProviderClient client = (ChunkProviderClient) w.getChunkProvider();
						ch = client.loadChunk(ch.x, ch.z);
					} else {
						ChunkProviderServer server = (ChunkProviderServer) w.getChunkProvider();
						ch = server.loadChunk(ch.x, ch.z);
					}
				}
				if (ch == null)
					return false;
				entity = ch.getTileEntity(pos, EnumCreateEntityType.IMMEDIATE);
				final boolean flag2 = entity != null;
				if (flag2) {
					consumer.accept((T) entity, ch);
				}
				
				if (flag) {
					if (w.isRemote) {
						ChunkProviderClient client = (ChunkProviderClient) w.getChunkProvider();
						client.unloadChunk(ch.x, ch.z);
					} else {
						ChunkProviderServer server = (ChunkProviderServer) w.getChunkProvider();
						server.queueUnload(ch);
					}
				}
				return flag2;
			};
			final MinecraftServer mcserver = w.getMinecraftServer();
			if (mcserver == null)
				return Minecraft.getMinecraft().addScheduledTask(call).get();
			return mcserver.callFromMainThread(call).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
}
