package eu.gir.girsignals.tileentitys;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonPrimitive;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.debug.NetworkDebug;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IWorldNameable;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class SignalTileEnity extends TileEntity implements IWorldNameable {

	private HashMap<SEProperty<?>, Object> map = new HashMap<>();

	private static final String PROPERTIES = "properties";
	private static final String CUSTOMNAME = "customname";
	private static final String BLOCKID = "blockid";

	private String formatCustomName = null;
	private int blockID = -1;

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound comp = new NBTTagCompound();
		map.forEach((prop, in) -> prop.writeToNBT(comp, in));
		if (formatCustomName != null)
			comp.setString(CUSTOMNAME, formatCustomName);
		compound.setInteger(BLOCKID, blockID);
		compound.setTag(PROPERTIES, comp);
		super.writeToNBT(compound);
		NetworkDebug.networkWriteHook(compound, world, this);
		return compound;
	}

	private NBTTagCompound __tmp = null;

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		NBTTagCompound comp = compound.getCompoundTag(PROPERTIES);
		super.readFromNBT(compound);
		blockID = comp.getInteger(BLOCKID);
		if (world == null) {
			__tmp = comp.copy();
		} else {
			read(comp);
		}
	}

	private void read(NBTTagCompound comp) {
		ImmutableSet.of().parallelStream();
		((ExtendedBlockState) world.getBlockState(pos).getBlock().getBlockState()).getUnlistedProperties().stream()
				.forEach(prop -> {
					SEProperty<?> sep = SEProperty.cst(prop);
					sep.readFromNBT(comp).toJavaUtil().ifPresent(obj -> map.put(sep, obj));
				});
		if (comp.hasKey(CUSTOMNAME))
			setCustomName(comp.getString(CUSTOMNAME));
		NetworkDebug.networkReadHook(comp, world, new Object[] { this, new JsonPrimitive(__tmp == null) } );
	}

	@Override
	public void onLoad() {
		if (__tmp != null) {
			read(__tmp);
			if (!world.isRemote) {
				IBlockState state = world.getBlockState(pos);
				world.notifyBlockUpdate(pos, state, state, 3);
			}
			__tmp = null;
		}
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound());
		world.markBlockRangeForRenderUpdate(pos, pos);
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	public <T extends Comparable<T>> void setProperty(SEProperty<T> prop, T opt) {
		map.put(prop, opt);
		this.markDirty();
	}

	public interface BiAccumulater<T, U, V> {

		T accept(T t, U u, V v);

	}

	@SuppressWarnings("rawtypes")
	public IExtendedBlockState accumulate(BiAccumulater<IExtendedBlockState, IUnlistedProperty, Object> bic,
			IExtendedBlockState bs) {
		for (Map.Entry<SEProperty<?>, Object> entry : map.entrySet()) {
			bs = bic.accept(bs, entry.getKey(), entry.getValue());
		}
		return bs;
	}

	public Optional<?> getProperty(SEProperty<?> prop) {
		if (map.containsKey(prop))
			return Optional.of(map.get(prop));
		return Optional.empty();
	}

	@Override
	public String getName() {
		return formatCustomName;
	}

	@Override
	public boolean hasCustomName() {
		return formatCustomName != null && getCustomNameRenderHeight() != -1;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(String.format(formatCustomName));
	}

	public void setCustomName(String str) {
		if(getCustomNameRenderHeight() == -1)
			return;
		this.formatCustomName = str;
		if (str == null && map.containsKey(Signal.CUSTOMNAME)) {
			map.remove(Signal.CUSTOMNAME);
		} else if (str != null) {
			map.put(Signal.CUSTOMNAME, true);
		}
		this.markDirty();
		world.markBlockRangeForRenderUpdate(pos, pos);
	}

	private float renderHeight = 0;

	public float getCustomNameRenderHeight() {
		if (renderHeight == 0) {
			renderHeight = ((Signal) world.getBlockState(pos).getBlock()).getCustomnameRenderHeight();
		}
		return renderHeight;
	}
	
	public float getSignWidth() {
		return 22;
	}
	
	public float getOffset() {
		return 8.8f;
	}
	
	public void setBlockID() {
		blockID = ((Signal)world.getBlockState(pos).getBlock()).getID();
	}
	
	public int getBlockID() {
		return blockID;
	}
}
