package net.gir.girsignals.controllers;

import net.gir.girsignals.init.Tabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class HVSignalController extends Block {

	public HVSignalController() {
		super(Material.CACTUS);
		setCreativeTab(Tabs.tab);

	}

}
