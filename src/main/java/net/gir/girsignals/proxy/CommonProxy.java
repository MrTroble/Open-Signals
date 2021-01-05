package net.gir.girsignals.proxy;

import net.gir.girsignals.GirsignalsMain;
import net.gir.girsignals.blocks.SignalTileEnity;
import net.gir.girsignals.controllers.SignalControllerTileEntity;
import net.gir.girsignals.guis.GUIHandler;
import net.gir.girsignals.init.GIRBlocks;
import net.gir.girsignals.init.GIRItems;
import net.gir.girsignals.init.GIRNetworkHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy {

	public FMLEventChannel CHANNEL; 
	
	public void preinit(FMLPreInitializationEvent event) {
		CHANNEL = NetworkRegistry.INSTANCE.newEventDrivenChannel(GIRNetworkHandler.CHANNELNAME);
		CHANNEL.register(new GIRNetworkHandler());
		TileEntity.register("SignalControllerTileEntity", SignalControllerTileEntity.class);
		TileEntity.register("SignalTileEntity", SignalTileEnity.class);
		NetworkRegistry.INSTANCE.registerGuiHandler(GirsignalsMain.MODID, new GUIHandler());

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
