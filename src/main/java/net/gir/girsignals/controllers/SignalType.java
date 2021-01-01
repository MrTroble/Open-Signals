package net.gir.girsignals.controllers;

import java.util.HashMap;

import net.gir.girsignals.blocks.SignalTileEnity;
import net.gir.girsignals.init.GIRBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class SignalType {

	public static void getSupportedHVSignals(World world, BlockPos pos, IExtendedBlockState state,
			HashMap<String, Integer> map) {
		SignalTileEnity entity = (SignalTileEnity) world.getTileEntity(pos);
		if (entity != null) {
			state.getUnlistedProperties().forEach((prop, opt) -> {
				if (opt.isPresent())
					map.put(prop.getName(), GIRBlocks.HV_SIGNAL.getIDFromProperty(prop));
			});
		}
	}

	public static IBlockState getChangedBlockStateHV(World world, BlockPos pos, IExtendedBlockState state, int newid,
			int type) {
		SignalTileEnity entity = (SignalTileEnity) world.getTileEntity(pos);
		if (entity != null) {
			IUnlistedProperty<?> prop = GIRBlocks.HV_SIGNAL.getPropertyFromID(type);
			if (prop.getType().equals(Boolean.class))
				entity.setProprty(prop, newid == 0 ? Boolean.FALSE : Boolean.TRUE);
			else if (prop.getType().isEnum())
				entity.setProprty(prop, (IStringSerializable) prop.getType().getEnumConstants()[newid]);
		}
		return state;
	}

	public static final SignalType HV_TYPE = new SignalType("HV", SignalType::getSupportedHVSignals,
			SignalType::getChangedBlockStateHV);

	public final String name;
	public final SupportedSignalCallback supportedSignalStates;
	public final SignalChangeCallback onSignalChange;

	private SignalType(String name, SupportedSignalCallback supportedSignalStates,
			SignalChangeCallback onSignalChange) {
		this.name = name;
		this.supportedSignalStates = supportedSignalStates;
		this.onSignalChange = onSignalChange;
	}

	@FunctionalInterface
	public interface SignalChangeCallback {

		public IBlockState getNewState(World world, BlockPos pos, IExtendedBlockState state, int newid, int type);

	}

	@FunctionalInterface
	public interface SupportedSignalCallback {

		public void getSupportedSignalStates(World world, BlockPos pos, IExtendedBlockState state,
				HashMap<String, Integer> map);

	}

}
