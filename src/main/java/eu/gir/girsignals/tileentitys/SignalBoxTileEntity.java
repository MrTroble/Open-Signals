package eu.gir.girsignals.tileentitys;

import java.util.ArrayList;

import eu.gir.girsignals.linkableApi.ILinkableTile;
import net.minecraft.block.BlockLever;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class SignalBoxTileEntity extends TileEntity implements ILinkableTile {

	private final ArrayList<BlockPos> linkedPositions = new ArrayList<>();
	private final ArrayList<SignalControllerTileEntity> controller = new ArrayList<>();
	private final ArrayList<BlockPos> rsInput = new ArrayList<>();
	private final ArrayList<BlockPos> rsOutput = new ArrayList<>();

	private static final String POS_LIST = "poslist";
	private static final String RS_IN_LIST = "rsIn";
	private static final String RS_OUT_LIST = "rsOut";

	private static void writeList(ArrayList<BlockPos> posList, NBTTagCompound compound, String name) {
		final NBTTagList list = new NBTTagList();
		posList.forEach(pos -> list.appendTag(NBTUtil.createPosTag(pos)));
		compound.setTag(name, list);
	}

	private static void readList(ArrayList<BlockPos> posList, NBTTagCompound compound, String name) {
		final NBTTagList list = compound.getTagList(POS_LIST, 10);
		list.forEach(nbt -> posList.add(NBTUtil.getPosFromTag((NBTTagCompound) nbt)));
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		readList(linkedPositions, compound, POS_LIST);
		readList(rsInput, compound, RS_IN_LIST);
		readList(rsOutput, compound, RS_OUT_LIST);
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		writeList(linkedPositions, compound, POS_LIST);
		writeList(rsInput, compound, RS_IN_LIST);
		writeList(rsOutput, compound, RS_OUT_LIST);
		return super.writeToNBT(compound);
	}

	private void onLink(final BlockPos pos) {
		final SignalControllerTileEntity entity = new SignalControllerTileEntity();
		entity.setWorld(world);
		entity.setPos(this.pos);
		entity.link(pos);
		controller.add(entity);
	}

	@Override
	public void onLoad() {
		controller.clear();
		linkedPositions.forEach(this::onLink);
	}

	@Override
	public boolean hasLink() {
		return !linkedPositions.isEmpty();
	}

	@Override
	public boolean link(BlockPos pos) {
		if (world.getTileEntity(pos) instanceof SignalTileEnity) {
			if (linkedPositions.contains(pos))
				return false;
			linkedPositions.add(pos);
			this.onLink(pos);
		} else {
			if (rsInput.contains(pos) || rsOutput.contains(pos))
				return false;
			// TODO compatibility with RS Mod
			if (world.getBlockState(pos).getBlock() instanceof BlockLever)
				rsOutput.add(pos);
			else
				rsInput.add(pos);
		}
		System.out.println(rsOutput);
		System.out.println(rsInput);
		return true;
	}

	@Override
	public boolean unlink() {
		return false;
	}

}
