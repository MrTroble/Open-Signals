package net.gir.girsignals.proxy;

import net.gir.girsignals.blocks.SignalTileEnity;
import net.gir.girsignals.controllers.SignalControllerTileEntity;
import net.gir.girsignals.init.GIRBlocks;
import net.gir.girsignals.init.GIRItems;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

	public void preinit(FMLPreInitializationEvent event) {
		TileEntity.register("SignalControllerTileEntity", SignalControllerTileEntity.class);
		TileEntity.register("SignalTileEntity", SignalTileEnity.class);

		GIRItems.init();
		GIRBlocks.init();

		MinecraftForge.EVENT_BUS.register(GIRItems.class);
		MinecraftForge.EVENT_BUS.register(GIRBlocks.class);
	}

	public void init(FMLInitializationEvent event) {

	}

	public void postinit(FMLPostInitializationEvent event) {

	}

}
