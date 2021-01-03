package net.gir.girsignals.blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.gir.girsignals.init.GIRBlocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class SignalTileEnity extends TileEntity {

	private HashMap<IUnlistedProperty<?>, Object> map = new HashMap<>();

	private static final String PROPERTIES = "properties";

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound comp = new NBTTagCompound();
		map.forEach((prop, in) -> {
			if (in instanceof IStringSerializable)
				comp.setString(prop.getName(), ((IStringSerializable) in).getName());
			if (in instanceof Boolean)
				comp.setBoolean(prop.getName(), ((Boolean) in).booleanValue());
		});
		compound.setTag(PROPERTIES, comp);
		System.out.println(world != null ? world.isRemote:null);
		System.out.println(comp.toString());
		return super.writeToNBT(compound);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		NBTTagCompound comp = compound.getCompoundTag(PROPERTIES);
		// TODO FIX ME Block may not have been placed
		((ExtendedBlockState)GIRBlocks.HV_SIGNAL.getBlockState()).getUnlistedProperties().parallelStream().forEach(prop -> {
			if(comp.hasKey(prop.getName())) {
				Object opt = null;
				if(prop.getType().isEnum()) {
					opt = Enum.valueOf((Class) prop.getType(), comp.getString(prop.getName()));
				} else if(prop.getType().equals(Boolean.class)) {
					opt = Optional.of(comp.getBoolean(prop.getName()));
				}
				map.put(prop, opt);
			}
		});		
		System.out.println(world != null ? world.isRemote:null);
		System.out.println(comp.toString());
		super.readFromNBT(compound);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		System.out.println("Update package!");
		return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		System.out.println("On data packet!");
		this.readFromNBT(pkt.getNbtCompound());
		world.markBlockRangeForRenderUpdate(pos, pos);
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		System.out.println("Handle tag!");
		this.readFromNBT(tag);
	}
	
	@Override
	public boolean receiveClientEvent(int id, int type) {
		return true;
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		System.out.println("Update tag!");
		return writeToNBT(new NBTTagCompound());
	}
	
	public void setProperty(IUnlistedProperty<?> prop, Object opt) {
		map.put(prop, opt);
		this.markDirty();
	}
	
	public interface BiAccumulater<T, U, V> {
		
		T accept(T t, U u, V v);
		
	}
	
	public IExtendedBlockState accumulate(BiAccumulater<IExtendedBlockState, IUnlistedProperty<?>, Object> bic, IExtendedBlockState bs) {
		for(Map.Entry<IUnlistedProperty<?>, Object> entry : map.entrySet()) {
			bs = bic.accept(bs, entry.getKey(), entry.getValue());
		}
		return bs;
	}

	public Optional<?> getProperty(IUnlistedProperty<?> prop) {
		if (map.containsKey(prop))
			return Optional.of(map.get(prop));
		return Optional.empty();
	}

}
