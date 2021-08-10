package eu.gir.girsignals.tileentitys;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import eu.gir.girsignals.EnumSignals.EnumMode;
import eu.gir.girsignals.EnumSignals.EnumMuxMode;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.items.Linkingtool;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class SignalControllerTileEntity extends TileEntity implements SimpleComponent, IWorldNameable {

	private BlockPos linkedSignalPosition = null;
	private int[] listOfSupportedIndicies;
	private Map<String, Integer> tableOfSupportedSignalTypes;
	private int signalTypeCache = -1;
	private String signame = null;
	private EnumRedstoneMode rsMode = EnumRedstoneMode.SINGLE;
	private final int[] facingRedstoneModes = new int[EnumFacing.values().length];
	public EnumMode mode = EnumMode.MANUELL;
	public EnumFacing face = EnumFacing.DOWN;
	public EnumMuxMode muxmode = EnumMuxMode.MUX_CONTROL;
	public int nextSignal = 0;
	public int indexUsed = 0;
	public int lastMuxState = 0;

	private static final String ID_X = "xLinkedPos";
	private static final String ID_Y = "yLinkedPos";
	private static final String ID_Z = "zLinkedPos";
	private static final String RS_MODE = "rsMode";
	private static final String FACEING_MODES = "faceModes";

	private static final String UI_FACE = "uiface";
	private static final String UI_MODE = "uimode";
	private static final String UI_INDEX = "uiindex";
	private static final String UI_MUX = "uimux";

	public SignalControllerTileEntity() {
		Arrays.fill(facingRedstoneModes, 0xFF);
	}

	public static enum EnumRedstoneMode {
		SINGLE, MUX
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
		rsMode = EnumRedstoneMode.values()[compound.getInteger(RS_MODE)];
		int[] newArr = compound.getIntArray(FACEING_MODES);
		this.face = EnumFacing.values()[compound.getInteger(UI_FACE)];
		this.indexUsed = compound.getInteger(UI_INDEX);
		this.mode = EnumMode.values()[compound.getInteger(UI_MODE)];
		this.muxmode = EnumMuxMode.values()[compound.getInteger(UI_MUX)];
		if(newArr != null && newArr.length == facingRedstoneModes.length)
			System.arraycopy(newArr, 0, facingRedstoneModes, 0, facingRedstoneModes.length);
		super.readFromNBT(compound);
		if (world != null && world.isRemote && linkedSignalPosition != null)
			onLink();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		writeBlockPosToNBT(linkedSignalPosition, compound);
		compound.setInteger(RS_MODE, rsMode.ordinal());
		compound.setIntArray(FACEING_MODES, facingRedstoneModes);
		compound.setInteger(UI_FACE, face.ordinal());
		compound.setInteger(UI_INDEX, indexUsed);
		compound.setInteger(UI_MODE, mode.ordinal());
		compound.setInteger(UI_MUX, muxmode.ordinal());
		super.writeToNBT(compound);
		return compound;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound());
		if (hasLinkImpl())
			onLink();
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	public void onLink() {
		new Thread(() -> {
			while (!world.isBlockLoaded(pos))
				continue;
			loadChunkAndGetTile((sigtile, ch) -> {
				Signal b = Signal.SIGNALLIST.get(sigtile.getBlockID());

				HashMap<String, Integer> supportedSignaleStates = new HashMap<>();
				sigtile.accumulate((bs, prop, obj) -> {
					if (prop instanceof SEProperty && obj != null) {
						SEProperty<?> p = ((SEProperty<?>) prop);
						if (p.isChangabelAtStage(ChangeableStage.APISTAGE)
								|| p.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG))
							supportedSignaleStates.put(prop.getName(), b.getIDFromProperty(prop));
					}
					return null;
				}, null);
				listOfSupportedIndicies = supportedSignaleStates.values().stream().mapToInt(Integer::intValue)
						.toArray();
				tableOfSupportedSignalTypes = supportedSignaleStates;
				signalTypeCache = ((Signal) ch.getBlockState(linkedSignalPosition).getBlock()).getID();
				signame = sigtile.getName();
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

	public boolean link(ItemStack stack) {
		if (stack.getItem() instanceof Linkingtool) {
			BlockPos old = linkedSignalPosition;
			boolean flag = (linkedSignalPosition = readBlockPosFromNBT(stack.getTagCompound())) != null
					&& (old == null || !old.equals(linkedSignalPosition));
			if (flag) {
				onLink();
				IBlockState state = world.getBlockState(pos);
				this.world.notifyBlockUpdate(pos, state, state, 3);
			}
			return flag;
		}
		return false;
	}

	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] hasLink(Context context, Arguments args) {
		return new Object[] { hasLinkImpl() };
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

	public boolean hasLinkImpl() {
		if (linkedSignalPosition == null)
			return false;
		if (loadChunkAndGetTile((x, y) -> {
		}))
			return true;
		if (!world.isRemote)
			unlink();
		return false;
	}

	public void unlink() {
		linkedSignalPosition = null;
		tableOfSupportedSignalTypes = null;
		listOfSupportedIndicies = null;
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
		for (int x : arr)
			if (x == i)
				return true;
		return false;
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
			if(!prop.isValid(newSignal)) {
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
		return this.signame;
	}

	@Override
	public boolean hasCustomName() {
		return this.signame != null;
	}

	public EnumRedstoneMode getRsMode() {
		return rsMode;
	}

	public void setRsMode(EnumRedstoneMode rsMode) {
		this.rsMode = rsMode;
		Arrays.fill(facingRedstoneModes, rsMode == EnumRedstoneMode.MUX ? 3:0);
	}

	public void setFacingData(final EnumFacing face, final int data) {
		facingRedstoneModes[face.ordinal()] = data;
	}

	public int[] getFacingData() {
		return facingRedstoneModes;
	}

	public static int[] unpack(final int x) {
		return new int[] {
				 (x & 0b00000000000000000000000000001111),
				((x & 0b00000000000000000000001111110000) >> 4),
				((x & 0b00000000000000001111110000000000) >> 10)
		};
	}

	public void redstoneUpdate(final EnumFacing face, final boolean state) {
		if(listOfSupportedIndicies == null)
			return;
		if (rsMode == EnumRedstoneMode.SINGLE) {
			final int id = facingRedstoneModes[face.ordinal()];
			if (id < 0)
				return;
			final int[] unpacked = unpack(id);
			final int signalTypeId = unpacked[0];
			if (signalTypeId < listOfSupportedIndicies.length) {
				final int signalData = unpacked[1];
				final int signalDataOff = unpacked[2];
				final int sigType = listOfSupportedIndicies[signalTypeId];
				this.changeSignalImpl(sigType, state ? signalData : signalDataOff);
			}
		} else if (rsMode == EnumRedstoneMode.MUX) {
			final int id = facingRedstoneModes[face.ordinal()];
			if (id < 0 || id >= EnumMuxMode.values().length)
				return;
			final EnumMuxMode muxmode = EnumMuxMode.values()[id];
			if (muxmode == EnumMuxMode.MUX_CONTROL) {
				final boolean lastState = (lastMuxState & 1) != 0;
				if(lastState == state)
					return;
				lastMuxState = state ? 1 + (lastMuxState & 2):lastMuxState & 2;
				nextSignal++;
				if (nextSignal >= listOfSupportedIndicies.length)
					nextSignal = 0;
			} else if(muxmode == EnumMuxMode.SIGNAL_CONTROL) {			
				final boolean lastState = (lastMuxState & 2) != 0;
				if(lastState == state)
					return;
				lastMuxState = state ? 2 + (lastMuxState & 1):lastMuxState & 1;
				final int sigType = listOfSupportedIndicies[nextSignal];
				final int newSignal = this.getSignalStateImpl(sigType) + 1;
				if(!this.changeSignalImpl(sigType, newSignal))
					this.changeSignalImpl(sigType, 0);
			}
		}
	}

	public void setUIState(final EnumMode mode, final EnumFacing face, final int indexUsed, final EnumMuxMode muxmode) {
		this.mode = mode;
		this.face = face;
		this.indexUsed = indexUsed;
		this.muxmode = muxmode;
	}
}
