package eu.gir.girsignals.proxy;

import com.troblecodings.guilib.ecs.UIInit;
import com.troblecodings.signals.contentpacks.OneSignalConfigParser;
import com.troblecodings.signals.contentpacks.TwoSignalConfigParser;

import eu.gir.girsignals.GIRSignalsMain;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.blocks.SignalBox;
import eu.gir.girsignals.blocks.SignalController;
import eu.gir.girsignals.guis.ContainerSignalBox;
import eu.gir.girsignals.guis.ContainerSignalController;
import eu.gir.girsignals.init.GIRBlocks;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.init.GIRSounds;
import eu.gir.girsignals.items.Placementtool;
import eu.gir.girsignals.signalbox.SignalBoxTileEntity;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preinit(final FMLPreInitializationEvent event) {
        GIRSignalsMain.handler = UIInit.initCommon(GIRSignalsMain.MODID, GIRSignalsMain.log,
                GIRSignalsMain.isDebug());
        GIRSignalsMain.handler.addServer(Placementtool.class, (p, w, bp) -> null);
        GIRSignalsMain.handler.addServer(SignalController.class,
                (p, w, bp) -> new ContainerSignalController(
                        (SignalControllerTileEntity) w.getTileEntity(bp)));
        GIRSignalsMain.handler.addServer(SignalBox.class,
                (p, w, bp) -> new ContainerSignalBox((SignalBoxTileEntity) w.getTileEntity(bp)));
        GIRSignalsMain.handler.addServer(Signal.class, (p, w, bp) -> null);

        GIRItems.init();
        GIRBlocks.init();
        GIRSounds.init();

        OneSignalConfigParser.loadInternConfigs();
        TwoSignalConfigParser.loadInternConfigs();

        MinecraftForge.EVENT_BUS.register(GIRItems.class);
        MinecraftForge.EVENT_BUS.register(GIRBlocks.class);
        MinecraftForge.EVENT_BUS.register(GIRSounds.class);
    }

    public void init(final FMLInitializationEvent event) {

    }

    public void postinit(final FMLPostInitializationEvent event) {

    }

}
