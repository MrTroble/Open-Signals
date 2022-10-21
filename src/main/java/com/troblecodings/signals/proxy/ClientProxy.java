package com.troblecodings.signals.proxy;

import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.blocks.SignalController;
import com.troblecodings.signals.guis.GuiPlacementtool;
import com.troblecodings.signals.guis.GuiRedstoneIO;
import com.troblecodings.signals.guis.GuiSignal;
import com.troblecodings.signals.guis.GuiSignalBox;
import com.troblecodings.signals.guis.GuiSignalController;
import com.troblecodings.signals.init.GIRModels;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.models.CustomModelLoader;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;
import com.troblecodings.signals.tileentitys.SignalSpecialRenderer;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

import eu.gir.guilib.ecs.GuiHandler;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preinit(final FMLPreInitializationEvent event) {
        super.preinit(event);
        GuiHandler.addGui(Placementtool.class,
                (p, w, bp) -> new GuiPlacementtool(p.getHeldItemMainhand()));
        GuiHandler.addGui(SignalController.class, SignalControllerTileEntity.class,
                GuiSignalController::new);
        GuiHandler.addGui(SignalBox.class, SignalBoxTileEntity.class, GuiSignalBox::new);
        GuiHandler.addGui(RedstoneIO.class, RedstoneIOTileEntity.class, GuiRedstoneIO::new);
        GuiHandler.addGui(Signal.class, SignalTileEnity.class, GuiSignal::new);

        MinecraftForge.EVENT_BUS.register(GIRModels.class);
        ModelLoaderRegistry.registerLoader(new CustomModelLoader());
        ClientRegistry.bindTileEntitySpecialRenderer(SignalTileEnity.class,
                new SignalSpecialRenderer());
    }

    @Override
    public void init(final FMLInitializationEvent event) {
        super.init(event);

    }

    @Override
    public void postinit(final FMLPostInitializationEvent event) {
        super.postinit(event);

    }

}
