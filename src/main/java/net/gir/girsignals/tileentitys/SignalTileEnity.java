package net.gir.girsignals.tileentitys;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.gir.girsignals.SEProperty;
import net.gir.girsignals.blocks.SignalBlock;
import net.gir.girsignals.init.GIRBlocks;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SignalTileEnity extends TileEntity implements IWorldNameable {

	private HashMap<SEProperty<?>, Object> map = new HashMap<>();

	private static final String PROPERTIES = "properties";
	private static final String CUSTOMNAME = "customname";

	private String formatCustomName = null;
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound comp = new NBTTagCompound();
		map.forEach((prop, in) -> prop.writeToNBT(comp, in));
		if(formatCustomName != null)
			comp.setString(CUSTOMNAME, formatCustomName);
		compound.setTag(PROPERTIES, comp);
		return super.writeToNBT(compound);
	}

	private NBTTagCompound __tmp = null;

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		NBTTagCompound comp = compound.getCompoundTag(PROPERTIES);
		if (world == null) {
			__tmp = comp;
		} else {
			read(comp);
		}
		super.readFromNBT(compound);
	}

	private void read(NBTTagCompound comp) {
		((ExtendedBlockState) world.getBlockState(pos).getBlock().getBlockState()).getUnlistedProperties()
				.parallelStream().forEach(prop -> {
					SEProperty<?> sep = SEProperty.cst(prop);
					sep.readFromNBT(comp).toJavaUtil().ifPresent(obj -> map.put(sep, obj));
				});
		if(comp.hasKey(CUSTOMNAME))
			setCustomName(comp.getString(CUSTOMNAME));
	}

	@Override
	public void onLoad() {
		if (__tmp != null) {
			read(__tmp);
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
		return formatCustomName != null;
	}
	
	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(String.format(formatCustomName));
	}

	public void setCustomName(String str) {
		this.formatCustomName = str;
		if(str == null && map.containsKey(SignalBlock.CUSTOMNAME)) {
			map.remove(SignalBlock.CUSTOMNAME);
		} else if(str != null) {
			map.put(SignalBlock.CUSTOMNAME, true);
		}
		this.markDirty();
		world.markBlockRangeForRenderUpdate(pos, pos);
	}
	
	private float renderHeight = 0;
	
	@SideOnly(Side.CLIENT)
	public float getCustomNameRenderHeight() {
		if(renderHeight == 0) {
			int id = ((SignalBlock)world.getBlockState(pos).getBlock()).getID();
			if(id == GIRBlocks.HV_SIGNAL.getID()) renderHeight = 2.775f;
			else if(id == GIRBlocks.HL_SIGNAL.getID()) renderHeight = 1.15f;
			else if(id == GIRBlocks.KS_SIGNAL.getID()) renderHeight = 4.95f;
			else throw new IllegalArgumentException("Signal has not been added to the height list!");
		}
		return renderHeight;
	}
}
