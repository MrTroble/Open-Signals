package net.gir.girsignals.tileentitys;

import java.util.HashMap;
import java.util.Map;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.gir.girsignals.SEProperty;
import net.gir.girsignals.blocks.SignalBlock;
import net.gir.girsignals.items.Linkingtool;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class SignalControllerTileEntity extends TileEntity implements SimpleComponent {

	private BlockPos linkedSignalPosition = null;
	private Integer[] listOfSupportedIndicies;
	private Map<String, Integer> tableOfSupportedSignalTypes;

	private static final String ID_X = "xLinkedPos";
	private static final String ID_Y = "yLinkedPos";
	private static final String ID_Z = "zLinkedPos";

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
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		writeBlockPosToNBT(linkedSignalPosition, compound);
		return super.writeToNBT(compound);
	}
	
	private void onLink() {
		IBlockState state = world.getBlockState(linkedSignalPosition);
		Block block = state.getBlock();
		if(!(block instanceof SignalBlock)) {
			unlink();
			return;
		}
		
		SignalBlock b = (SignalBlock) block;
		
		HashMap<String, Integer> supportedSignaleStates = new HashMap<>();
		((IExtendedBlockState) b.getExtendedState(state, world, linkedSignalPosition)).getUnlistedProperties().forEach(
				(prop, opt) -> opt.ifPresent(x -> supportedSignaleStates.put(prop.getName(), b.getIDFromProperty(prop))));

		listOfSupportedIndicies = supportedSignaleStates.values().toArray(new Integer[supportedSignaleStates.size()]);
		
		tableOfSupportedSignalTypes = supportedSignaleStates;
	}
	
	@Override
	public void onLoad() {
		if(linkedSignalPosition != null && !world.isRemote) {
			onLink();
		}
	}

	public boolean link(ItemStack stack) {
		if (stack.getItem() instanceof Linkingtool) {
			BlockPos old = linkedSignalPosition;
			boolean flag = (linkedSignalPosition = readBlockPosFromNBT(stack.getTagCompound())) != null
					&& (old == null || !old.equals(linkedSignalPosition));
			if (flag) {
				onLink();
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

	public boolean hasLinkImpl() {
		if(linkedSignalPosition == null) return false;
		if(world.getBlockState(linkedSignalPosition).getBlock() instanceof SignalBlock)
			return true;
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

	public Integer[] getSupportedSignalTypesImpl() {
		if (!hasLinkImpl())
			return new Integer[] {};
		return listOfSupportedIndicies;
	}

	public static boolean find(Integer[] arr, int i) {
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
		if (!hasLinkImpl() || !find(getSupportedSignalTypesImpl(), type))
			return false;
		SignalTileEnity tile = (SignalTileEnity) world.getTileEntity(linkedSignalPosition);
		IBlockState blockstate = world.getBlockState(linkedSignalPosition);
		SignalBlock block = (SignalBlock) blockstate.getBlock();
		SEProperty prop = SEProperty.cst(block.getPropertyFromID(type));
		tile.setProperty(prop, prop.getObjFromID(newSignal));
		world.markAndNotifyBlock(linkedSignalPosition, null, blockstate, blockstate, 3);
		return true;
	}

	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] getSignalType(Context context, Arguments args) {
		return new Object[] { getSignalTypeImpl() };
	}

	public String getSignalTypeImpl() {
		return ((SignalBlock)world.getBlockState(linkedSignalPosition).getBlock()).getSignalTypeName();
	}

	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] getSignalState(Context context, Arguments args) {
		return new Object[] { getSignalStateImpl(args.checkInteger(0)) };
	}
	
	@SuppressWarnings("rawtypes")
	public int getSignalStateImpl(int type) {
		if (!hasLinkImpl() || !find(getSupportedSignalTypesImpl(), type))
			return -1;
		SignalTileEnity tile = (SignalTileEnity) world.getTileEntity(linkedSignalPosition);
		IBlockState blockstate = world.getBlockState(linkedSignalPosition);
		SignalBlock block = (SignalBlock) blockstate.getBlock();
		SEProperty prop = SEProperty.cst(block.getPropertyFromID(type));
		java.util.Optional bool = tile.getProperty(prop);
		if(bool.isPresent())
			return SEProperty.getIDFromObj(bool.get());
		return -1;
	}
	
	@Override
	public String getComponentName() {
		return "signalcontroller";
	}

}
