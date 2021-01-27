package eu.gir.girsignals.proxy;

import eu.gir.girsignals.init.GIRModels;
import eu.gir.girsignals.models.GIRCustomModelLoader;
import eu.gir.girsignals.tileentitys.SignalSpecialRenderer;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

	public void preinit(FMLPreInitializationEvent event) {
		super.preinit(event);
		MinecraftForge.EVENT_BUS.register(GIRModels.class);
		ModelLoaderRegistry.registerLoader(new GIRCustomModelLoader());
		ClientRegistry.bindTileEntitySpecialRenderer(SignalTileEnity.class, new SignalSpecialRenderer());
	}

	public void init(FMLInitializationEvent event) {
		super.init(event);

	}

	public void postinit(FMLPostInitializationEvent event) {
		super.postinit(event);

	}

}
