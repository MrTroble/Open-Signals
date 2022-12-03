package eu.gir.girsignals.proxy;

import com.troblecodings.guilib.ecs.UIInit;
import com.troblecodings.signals.contentpacks.OneSignalConfigParser;
import com.troblecodings.signals.contentpacks.TwoSignalConfigParser;

import eu.gir.girsignals.SignalsMain;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.blocks.SignalBox;
import eu.gir.girsignals.blocks.SignalController;
import eu.gir.girsignals.guis.ContainerSignalBox;
import eu.gir.girsignals.guis.ContainerSignalController;
import eu.gir.girsignals.init.SignalBlocks;
import eu.gir.girsignals.init.SignalItems;
import eu.gir.girsignals.init.SignalSounds;
import eu.gir.girsignals.items.Placementtool;
import eu.gir.girsignals.signalbox.SignalBoxTileEntity;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preinit(final FMLPreInitializationEvent event) {
        SignalsMain.handler = UIInit.initCommon(SignalsMain.MODID, SignalsMain.log,
                SignalsMain.isDebug());
        SignalsMain.handler.addServer(Placementtool.class, (p, w, bp) -> null);
        SignalsMain.handler.addServer(SignalController.class,
                (p, w, bp) -> new ContainerSignalController(
                        (SignalControllerTileEntity) w.getTileEntity(bp)));
        SignalsMain.handler.addServer(SignalBox.class,
                (p, w, bp) -> new ContainerSignalBox((SignalBoxTileEntity) w.getTileEntity(bp)));
        SignalsMain.handler.addServer(Signal.class, (p, w, bp) -> null);

        SignalItems.init();
        SignalBlocks.init();
        SignalSounds.init();

        OneSignalConfigParser.loadInternConfigs();
        TwoSignalConfigParser.loadInternConfigs();

        MinecraftForge.EVENT_BUS.register(SignalItems.class);
        MinecraftForge.EVENT_BUS.register(SignalBlocks.class);
    }

    public void init(final FMLInitializationEvent event) {

    }

    public void postinit(final FMLPostInitializationEvent event) {

    }

}
