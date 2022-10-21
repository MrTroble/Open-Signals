package com.troblecodings.signals.proxy;

import com.troblecodings.signals.GirsignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.blocks.SignalController;
import com.troblecodings.signals.blocks.SignalLoader;
import com.troblecodings.signals.guis.ContainerSignalBox;
import com.troblecodings.signals.guis.ContainerSignalController;
import com.troblecodings.signals.init.SignalBlocks;
import com.troblecodings.signals.init.SignaIItems;
import com.troblecodings.signals.init.SignalSounds;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;

import eu.gir.guilib.ecs.GuiHandler;
import eu.gir.guilib.ecs.UIInit;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preinit(final FMLPreInitializationEvent event) {
        UIInit.initCommon(GirsignalsMain.MODID, GirsignalsMain.isDebug());
        GuiHandler.addServer(Placementtool.class, (p, w, bp) -> null);
        GuiHandler.addServer(SignalController.class, (p, w, bp) -> new ContainerSignalController(
                (SignalControllerTileEntity) w.getTileEntity(bp)));
        GuiHandler.addServer(SignalBox.class,
                (p, w, bp) -> new ContainerSignalBox((SignalBoxTileEntity) w.getTileEntity(bp)));
        GuiHandler.addServer(Signal.class, (p, w, bp) -> null);

        SignalLoader.loadInternSignals();
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
