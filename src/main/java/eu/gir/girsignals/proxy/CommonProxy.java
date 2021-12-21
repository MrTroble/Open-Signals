package eu.gir.girsignals.proxy;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.guis.ContainerSignalController;
import eu.gir.girsignals.guis.guilib.GuiHandler;
import eu.gir.girsignals.guis.guilib.UIInit;
import eu.gir.girsignals.init.GIRBlocks;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

	public void preinit(FMLPreInitializationEvent event) {
		UIInit.initCommon(GirsignalsMain.MODID);
		GuiHandler.addServer((p, w, bp) -> {
			final TileEntity entity = w.getTileEntity(bp);
			if (entity == null || !(entity instanceof SignalControllerTileEntity))
				return null;
			return new ContainerSignalController((SignalControllerTileEntity) entity);
		});

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
