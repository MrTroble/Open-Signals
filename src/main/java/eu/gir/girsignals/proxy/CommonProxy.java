package eu.gir.girsignals.proxy;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.blocks.SignalBox;
import eu.gir.girsignals.blocks.SignalController;
import eu.gir.girsignals.guis.ContainerSignalBox;
import eu.gir.girsignals.guis.ContainerSignalController;
import eu.gir.girsignals.init.GIRBlocks;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.items.Placementtool;
import eu.gir.girsignals.signalbox.SignalBoxTileEntity;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import eu.gir.guilib.ecs.GuiHandler;
import eu.gir.guilib.ecs.UIInit;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preinit(final FMLPreInitializationEvent event) {
        UIInit.initCommon(GirsignalsMain.MODID);
        GuiHandler.addServer(Placementtool.class, (p, w, bp) -> null);
        GuiHandler.addServer(SignalController.class, (p, w, bp) -> new ContainerSignalController(
                (SignalControllerTileEntity) w.getTileEntity(bp)));
        GuiHandler.addServer(SignalBox.class,
                (p, w, bp) -> new ContainerSignalBox((SignalBoxTileEntity) w.getTileEntity(bp)));
        GuiHandler.addServer(Signal.class, (p, w, bp) -> null);

        GIRItems.init();
        GIRBlocks.init();

        MinecraftForge.EVENT_BUS.register(GIRItems.class);
        MinecraftForge.EVENT_BUS.register(GIRBlocks.class);
    }

    public void init(final FMLInitializationEvent event) {

    }

    public void postinit(final FMLPostInitializationEvent event) {

    }

}
