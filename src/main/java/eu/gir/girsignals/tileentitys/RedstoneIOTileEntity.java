package eu.gir.girsignals.tileentitys;

import java.util.ArrayList;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldNameable;

public class RedstoneIOTileEntity extends TileEntity implements IWorldNameable {

	private String name = null;
	private ArrayList<BlockPos> linkedPositions;
	
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
	
}
