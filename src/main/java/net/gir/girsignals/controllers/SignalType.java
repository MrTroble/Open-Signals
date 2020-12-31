package net.gir.girsignals.controllers;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalType {
	
	public static long[] getSupportedHVSignals(World world, BlockPos pos, IBlockState state) {
		//TODO ZS/ZSV in LS /// Maybe even FS
		return new long[] {0, 1, 2};
	}
	
	public static IBlockState getChangedBlockStateHV(World world, BlockPos pos, IBlockState state, int newid, int type) {
		// TODO Change statet of LS//FS
		return state;
	}

	public static final SignalType HV_TYPE = new SignalType("HV", SignalType::getSupportedHVSignals, SignalType::getChangedBlockStateHV);
	
	public final String name;
	public final SupportedSignalCallback supportedSignalStates;
	public final SignalChangeCallback onSignalChange;
	
	private SignalType(String name, SupportedSignalCallback supportedSignalStates, SignalChangeCallback onSignalChange) {
		this.name = name;
		this.supportedSignalStates = supportedSignalStates;
		this.onSignalChange = onSignalChange;
	}
	
	
	@FunctionalInterface
	public interface SignalChangeCallback {
		
		public IBlockState getNewState(World world, BlockPos pos, IBlockState state, int newid, int type);
		
	}
	
	@FunctionalInterface
	public interface SupportedSignalCallback {
		
		public long[] getSupportedSignalStates(World world, BlockPos pos, IBlockState state); 
		
	}
	
}
