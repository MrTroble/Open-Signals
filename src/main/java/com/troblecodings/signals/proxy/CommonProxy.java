package com.troblecodings.signals.proxy;

import com.troblecodings.guilib.ecs.UIInit;
import com.troblecodings.signals.SignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.blocks.SignalController;
import com.troblecodings.signals.guis.ContainerSignalBox;
import com.troblecodings.signals.guis.ContainerSignalController;
import com.troblecodings.signals.init.SignaIItems;
import com.troblecodings.signals.init.SignalBlocks;
import com.troblecodings.signals.init.SignalSounds;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;

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

        SignaIItems.init();
        SignalBlocks.init();
        SignalSounds.init();

        MinecraftForge.EVENT_BUS.register(SignaIItems.class);
        MinecraftForge.EVENT_BUS.register(SignalBlocks.class);
    }

    public void init(final FMLInitializationEvent event) {

    }

    public void postinit(final FMLPostInitializationEvent event) {

    }

}
