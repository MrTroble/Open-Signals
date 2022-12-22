package com.troblecodings.signals.proxy;

import com.troblecodings.guilib.ecs.UIInit;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.blocks.SignalController;
import com.troblecodings.signals.contentpacks.ChangeConfigParser;
import com.troblecodings.signals.contentpacks.DefaultConfigParser;
import com.troblecodings.signals.contentpacks.OneSignalConfigParser;
import com.troblecodings.signals.guis.ContainerSignalBox;
import com.troblecodings.signals.guis.ContainerSignalController;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.init.OSSounds;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonProxy {

    public void preinit(final FMLCommonSetupEvent event) {
        OpenSignalsMain.handler = UIInit.initCommon(OpenSignalsMain.MODID, OpenSignalsMain.log,
                OpenSignalsMain.isDebug());
        OpenSignalsMain.handler.addServer(Placementtool.class, (p, w, bp) -> null);
        OpenSignalsMain.handler.addServer(SignalController.class,
                (p, w, bp) -> new ContainerSignalController(
                        (SignalControllerTileEntity) w.getBlockEntity(bp)));
        OpenSignalsMain.handler.addServer(SignalBox.class,
                (p, w, bp) -> new ContainerSignalBox((SignalBoxTileEntity) w.getBlockEntity(bp)));
        OpenSignalsMain.handler.addServer(Signal.class, (p, w, bp) -> null);

        OSSounds.init();
        OSItems.init();
        OSBlocks.init();

        OneSignalConfigParser.loadInternConfigs();
        ChangeConfigParser.loadInternConfigs();
        DefaultConfigParser.loadInternConfigs();

        MinecraftForge.EVENT_BUS.register(OSItems.class);
        MinecraftForge.EVENT_BUS.register(OSBlocks.class);
        MinecraftForge.EVENT_BUS.register(OSSounds.class);
    }


}
