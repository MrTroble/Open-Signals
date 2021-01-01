package net.gir.girsignals.blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.gir.girsignals.init.GIRBlocks;
import net.minecraft.nbt.NBTTagCompound;
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
		return super.writeToNBT(compound);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		NBTTagCompound comp = compound.getCompoundTag(PROPERTIES);
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
		super.readFromNBT(compound);
	}

	public void setProperty(IUnlistedProperty<?> prop, Object opt) {
		map.put(prop, opt);
	}
	
	public interface BiAccumulater<T, U, V> {
		
		T accept(T t, U u, V v);
		
	}
	
	public IExtendedBlockState foreach(BiAccumulater<IExtendedBlockState, IUnlistedProperty<?>, Object> bic, IExtendedBlockState bs) {
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
