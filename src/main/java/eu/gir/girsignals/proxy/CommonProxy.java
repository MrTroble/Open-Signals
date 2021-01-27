package eu.gir.girsignals.proxy;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.guis.GUIHandler;
import eu.gir.girsignals.init.GIRBlocks;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.init.GIRNetworkHandler;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
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
