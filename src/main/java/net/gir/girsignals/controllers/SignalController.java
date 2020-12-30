package net.gir.girsignals.controllers;

import net.gir.girsignals.init.Tabs;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class SignalController extends Block implements ITileEntityProvider{
	
	public SignalController() {
		super(Material.ROCK);
		setCreativeTab(Tabs.tab);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new SignalControllerTileEntity();
	}

}
