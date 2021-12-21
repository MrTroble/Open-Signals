package eu.gir.girsignals.proxy;

import eu.gir.girsignals.guis.GuiPlacementtool;
import eu.gir.girsignals.guis.GuiSignalController;
import eu.gir.girsignals.guis.guilib.GuiHandler;
import eu.gir.girsignals.init.GIRModels;
import eu.gir.girsignals.models.GIRCustomModelLoader;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import eu.gir.girsignals.tileentitys.SignalSpecialRenderer;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

	public void preinit(FMLPreInitializationEvent event) {
		super.preinit(event);
		GuiHandler.addGui(GuiPlacementtool.class, (p, w, bp) -> new GuiPlacementtool(p.getHeldItemMainhand()));
		GuiHandler.addGui(GuiSignalController.class, (p, w, bp) -> {
			final TileEntity entity = w.getTileEntity(bp);
			if (entity == null || !(entity instanceof SignalControllerTileEntity))
				return null;
			return new GuiSignalController((SignalControllerTileEntity) entity);
		});
		
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
