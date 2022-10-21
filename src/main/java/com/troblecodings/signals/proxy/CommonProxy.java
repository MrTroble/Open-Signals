package com.troblecodings.signals.proxy;

import com.troblecodings.signals.GirsignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.blocks.SignalController;
import com.troblecodings.signals.blocks.SignalLoader;
import com.troblecodings.signals.guis.ContainerSignalBox;
import com.troblecodings.signals.guis.ContainerSignalController;
import com.troblecodings.signals.init.GIRBlocks;
import com.troblecodings.signals.init.GIRItems;
import com.troblecodings.signals.init.GIRSounds;
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
        GIRItems.init();
        GIRBlocks.init();
        GIRSounds.init();

        MinecraftForge.EVENT_BUS.register(GIRItems.class);
        MinecraftForge.EVENT_BUS.register(GIRBlocks.class);
    }

    public void init(final FMLInitializationEvent event) {

    }

    public void postinit(final FMLPostInitializationEvent event) {

    }

}
