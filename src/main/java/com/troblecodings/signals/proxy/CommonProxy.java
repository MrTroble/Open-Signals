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
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.init.OSSounds;
import com.troblecodings.signals.items.Placementtool;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

public class CommonProxy {

    public void initModEvent(final FMLConstructModEvent event) {
        OpenSignalsMain.handler = UIInit.initCommon(OpenSignalsMain.MODID,
                OpenSignalsMain.getLogger(), OpenSignalsMain.isDebug());
        OpenSignalsMain.handler.addServer(Placementtool.class, _u -> null);
        OpenSignalsMain.handler.addServer(SignalController.class, ContainerSignalController::new);
        OpenSignalsMain.handler.addServer(SignalBox.class, ContainerSignalBox::new);
        OpenSignalsMain.handler.addServer(Signal.class, _u -> null);
    }

    public void preinit(final FMLCommonSetupEvent event) {

        OSSounds.init();
        OSItems.init();

        OneSignalConfigParser.loadOneSignalConfigs();
        ChangeConfigParser.loadChangeConfigs();
        DefaultConfigParser.loadDefaultConfigs();
    }
}