package com.troblecodings.signals.proxy;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.blocks.SignalController;
import com.troblecodings.signals.guis.GuiPlacementtool;
import com.troblecodings.signals.guis.GuiSignalBox;
import com.troblecodings.signals.guis.GuiSignalController;
import com.troblecodings.signals.guis.NamableGui;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.handler.ClientSignalStateHandler;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.models.CustomModelLoader;
import com.troblecodings.signals.tileentitys.SignalSpecialRenderer;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void initModEvent(FMLPreInitializationEvent event) {
        super.initModEvent(event);
        SignalStateHandler.add(new ClientSignalStateHandler());
        NameHandler.add(new ClientNameHandler());
        OpenSignalsMain.handler.addGui(Placementtool.class, GuiPlacementtool::new);
        OpenSignalsMain.handler.addGui(SignalController.class, GuiSignalController::new);
        OpenSignalsMain.handler.addGui(SignalBox.class, GuiSignalBox::new);
        OpenSignalsMain.handler.addGui(RedstoneIO.class, NamableGui::new);
        OpenSignalsMain.handler.addGui(Signal.class, NamableGui::new);
        ModelLoaderRegistry.registerLoader(CustomModelLoader.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(SignalTileEntity.class,
                new SignalSpecialRenderer());
    }
}