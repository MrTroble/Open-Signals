package eu.gir.girsignals.tileentitys;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.guis.guilib.ISyncable;
import eu.gir.girsignals.linkableApi.ILinkableTile;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class SignalControllerTileEntity extends TileEntity implements ISyncable, SimpleComponent, IWorldNameable, ILinkableTile {
	
	private BlockPos linkedSignalPosition = null;
	private int[] listOfSupportedIndicies;
	private Map<String, Integer> tableOfSupportedSignalTypes;
	private int signalTypeCache = -1;
	private NBTTagCompound compound = new NBTTagCompound();
	
	public static final String UPDATE_FLAG = "updateflag";
	
	private static final String ID_X = "xLinkedPos";
	private static final String ID_Y = "yLinkedPos";
	private static final String ID_Z = "zLinkedPos";
	
	private static final String ID_COMP = "COMP";
	
	public SignalControllerTileEntity() {
	}
	
	public static BlockPos readBlockPosFromNBT(NBTTagCompound compound) {
		if (compound != null && compound.hasKey(ID_X) && compound.hasKey(ID_Y) && compound.hasKey(ID_Z)) {
			return new BlockPos(compound.getInteger(ID_X), compound.getInteger(ID_Y), compound.getInteger(ID_Z));
		}
		return null;
	}
	
	public static void writeBlockPosToNBT(BlockPos pos, NBTTagCompound compound) {
		if (pos != null && compound != null) {
			compound.setInteger(ID_X, pos.getX());
			compound.setInteger(ID_Y, pos.getY());
			compound.setInteger(ID_Z, pos.getZ());
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		linkedSignalPosition = readBlockPosFromNBT(compound);
		this.compound = compound.getCompoundTag(ID_COMP);
		super.readFromNBT(compound);
		if (world != null && world.isRemote && linkedSignalPosition != null)
			onLink();
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		writeBlockPosToNBT(linkedSignalPosition, compound);
		super.writeToNBT(compound);
		compound.setTag(ID_COMP, this.compound);
		return compound;
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound());
		if (hasLink())
			onLink();
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}
	
	public void onLink() {
		new Thread(() -> {
			loadChunkAndGetTile((sigtile, ch) -> {
				Signal b = Signal.SIGNALLIST.get(sigtile.getBlockID());
				
				HashMap<String, Integer> supportedSignaleStates = new HashMap<>();
				sigtile.accumulate((bs, prop, obj) -> {
					if (prop instanceof SEProperty && obj != null) {
						SEProperty<?> p = ((SEProperty<?>) prop);
						if (p.isChangabelAtStage(ChangeableStage.APISTAGE) || p.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG))
							supportedSignaleStates.put(prop.getName(), b.getIDFromProperty(prop));
					}
					return null;
				}, null);
				listOfSupportedIndicies = supportedSignaleStates.values().stream().mapToInt(Integer::intValue).toArray();
				tableOfSupportedSignalTypes = supportedSignaleStates;
				signalTypeCache = ((Signal) ch.getBlockState(linkedSignalPosition).getBlock()).getID();
				IBlockState nstate = this.world.getBlockState(pos);
				this.markDirty();
				this.world.notifyBlockUpdate(pos, nstate, nstate, 3);
			});
		}).start();
	}
	
	@Override
	public void onLoad() {
		if (linkedSignalPosition != null) {
			onLink();
			IBlockState state = world.getBlockState(pos);
			this.world.notifyBlockUpdate(pos, state, state, 3);
		}
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] hasLink(Context context, Arguments args) {
		return new Object[] { hasLink() };
	}
	
	public boolean loadChunkAndGetTile(BiConsumer<SignalTileEnity, Chunk> consumer) {
		if (linkedSignalPosition == null)
			return false;
		try {
			Callable<Boolean> call = () -> {
				TileEntity entity = null;
				Chunk ch = world.getChunkFromBlockCoords(linkedSignalPosition);
				boolean flag = !ch.isLoaded();
				if (flag) {
					if (world.isRemote) {
						ChunkProviderClient client = (ChunkProviderClient) world.getChunkProvider();
						ch = client.loadChunk(ch.x, ch.z);
					} else {
						ChunkProviderServer server = (ChunkProviderServer) world.getChunkProvider();
						ch = server.loadChunk(ch.x, ch.z);
					}
				}
				if (ch == null)
					return false;
				entity = ch.getTileEntity(linkedSignalPosition, EnumCreateEntityType.IMMEDIATE);
				boolean flag2 = entity instanceof SignalTileEnity && ((SignalTileEnity) entity).getBlockID() != -1;
				if (flag2) {
					consumer.accept((SignalTileEnity) entity, ch);
				}
				
				if (flag) {
					if (world.isRemote) {
						ChunkProviderClient client = (ChunkProviderClient) world.getChunkProvider();
						client.unloadChunk(ch.x, ch.z);
					} else {
						ChunkProviderServer server = (ChunkProviderServer) world.getChunkProvider();
						server.queueUnload(ch);
					}
				}
				return flag2;
			};
			MinecraftServer mcserver = world.getMinecraftServer();
			if (mcserver == null)
				return Minecraft.getMinecraft().addScheduledTask(call).get();
			return mcserver.callFromMainThread(call).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] getSupportedSignalTypes(Context context, Arguments args) {
		return new Object[] { tableOfSupportedSignalTypes };
	}
	
	public int[] getSupportedSignalTypesImpl() {
		return listOfSupportedIndicies;
	}
	
	public static boolean find(int[] arr, int i) {
		return Arrays.stream(arr).anyMatch(x -> i == x);
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] changeSignal(Context context, Arguments args) {
		return new Object[] { changeSignalImpl(args.checkInteger(0), args.checkInteger(1)) };
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean changeSignalImpl(int type, int newSignal) {
		if (!find(getSupportedSignalTypesImpl(), type))
			return false;
		final AtomicBoolean rtc = new AtomicBoolean(true);
		loadChunkAndGetTile((tile, chunk) -> {
			IBlockState state = chunk.getBlockState(linkedSignalPosition);
			Signal block = (Signal) state.getBlock();
			SEProperty prop = SEProperty.cst(block.getPropertyFromID(type));
			if (!prop.isValid(newSignal)) {
				rtc.set(false);
				return;
			}
			tile.setProperty(prop, prop.getObjFromID(newSignal));
			world.markAndNotifyBlock(linkedSignalPosition, chunk, state, state, 3);
		});
		return rtc.get();
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] getSignalType(Context context, Arguments args) {
		return new Object[] { Signal.SIGNALLIST.get(getSignalTypeImpl()).getSignalTypeName() };
	}
	
	public int getSignalTypeImpl() {
		return signalTypeCache;
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] getSignalState(Context context, Arguments args) {
		return new Object[] { getSignalStateImpl(args.checkInteger(0)) };
	}
	
	@SuppressWarnings("rawtypes")
	public int getSignalStateImpl(int type) {
		if (!find(getSupportedSignalTypesImpl(), type))
			return -1;
		final AtomicReference<SignalTileEnity> entity = new AtomicReference<SignalTileEnity>();
		loadChunkAndGetTile((sig, ch) -> entity.set(sig));
		final SignalTileEnity tile = entity.get();
		if (tile == null)
			return -1;
		Signal block = (Signal) Signal.SIGNALLIST.get(tile.getBlockID());
		SEProperty prop = SEProperty.cst(block.getPropertyFromID(type));
		java.util.Optional bool = tile.getProperty(prop);
		if (bool.isPresent())
			return SEProperty.getIDFromObj(bool.get());
		return -1;
	}
	
	public BlockPos getLinkedPosition() {
		return linkedSignalPosition;
	}
	
	@Override
	public String getComponentName() {
		return "signalcontroller";
	}
	
	@Override
	public String getName() {
		return ""; // TODO Replace with loading variant
	}
	
	@Override
	public boolean hasCustomName() {
		return false; // TODO Replace with loading variant
	}
	
	@Override
	public boolean hasLink() {
		if (linkedSignalPosition == null)
			return false;
		if (loadChunkAndGetTile((x, y) -> {
		}))
			return true;
		if (!world.isRemote)
			unlink();
		return false;
	}
	
	@Override
	public boolean link(final BlockPos pos) {
		final IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof Signal) {
			this.linkedSignalPosition = pos;
			onLink();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean unlink() {
		linkedSignalPosition = null;
		tableOfSupportedSignalTypes = null;
		listOfSupportedIndicies = null;
		return true;
	}
	
	@Override
	public void updateTag(NBTTagCompound compound) {
		if (compound == null)
			return;
		compound.getKeySet().forEach(str -> {
			if (tableOfSupportedSignalTypes.containsKey(str)) {
				int type = compound.getInteger(str);
				int id = tableOfSupportedSignalTypes.get(str);
				changeSignalImpl(id, type);
			}
		});
		this.compound = compound;
	}
	
	@Override
	public NBTTagCompound getTag() {
		return this.compound;
	}
}
