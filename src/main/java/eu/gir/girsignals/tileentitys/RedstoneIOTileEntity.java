package eu.gir.girsignals.tileentitys;

import java.util.ArrayList;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldNameable;

public class RedstoneIOTileEntity extends TileEntity implements IWorldNameable, IChunkloadable {

	private String name = null;
	private ArrayList<BlockPos> linkedPositions = new ArrayList<>();
	
	@Override
	public String getName() {
		if(hasCustomName())
			return name;
		return this.blockType.getLocalizedName();
	}

	@Override
	public boolean hasCustomName() {
		return this.name != null;
	}
	
	public void link(BlockPos pos) {
		if(!linkedPositions.contains(pos))
			linkedPositions.add(pos);
	}
	
	public void unlink(BlockPos pos) {
		if(linkedPositions.contains(pos))
			linkedPositions.remove(pos);
	}
}
