package eu.gir.girsignals.blocks;

import eu.gir.girsignals.tileentitys.TrackIOTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TrackIOBlock extends Block implements ITileEntityProvider {

	public TrackIOBlock() {
		super(Material.GROUND);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TrackIOTileEntity();
	}

}
