package com.troblecodings.signals.proxy;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Monitor;
import com.troblecodings.signals.blocks.MonitorTEBlock;
import com.troblecodings.signals.blocks.PathwayRequester;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.blocks.SignalController;
import com.troblecodings.signals.blocks.SignalReader;
import com.troblecodings.signals.blocks.TrainNumberBlock;
import com.troblecodings.signals.guis.GuiMonitor;
import com.troblecodings.signals.guis.GuiMonitorSelection;
import com.troblecodings.signals.guis.GuiPathwayRequester;
import com.troblecodings.signals.guis.GuiPlacementtool;
import com.troblecodings.signals.guis.GuiSignalBox;
import com.troblecodings.signals.guis.GuiSignalBridge;
import com.troblecodings.signals.guis.GuiSignalController;
import com.troblecodings.signals.guis.GuiSignalReader;
import com.troblecodings.signals.guis.GuiTrainNumber;
import com.troblecodings.signals.guis.NamableGui;
import com.troblecodings.signals.guis.UISignalBoxProfile;
import com.troblecodings.signals.handler.ClientMonitorNetworkHandler;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.handler.ClientRenderUpdate;
import com.troblecodings.signals.handler.ClientSignalStateHandler;
import com.troblecodings.signals.handler.MonitorNetworkHandler;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.init.OSModels;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.models.CustomModelLoader;
import com.troblecodings.signals.signalbridge.SignalBridgeBasicBlock;
import com.troblecodings.signals.tileentitys.MonitorSpecialRenderer;
import com.troblecodings.signals.tileentitys.MonitorTileEntity;
import com.troblecodings.signals.tileentitys.SignalSpecialRenderer;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void initModEvent(final FMLPreInitializationEvent event) {
        super.initModEvent(event);
        SignalStateHandler.registerToNetworkChannel(new ClientSignalStateHandler());
        NameHandler.registerToNetworkChannel(new ClientNameHandler());
        MonitorNetworkHandler.registerToNetworkChannel(new ClientMonitorNetworkHandler());
        OpenSignalsMain.handler.addGui(Placementtool.class, GuiPlacementtool::new);
        OpenSignalsMain.handler.addGui(SignalController.class, GuiSignalController::new);
        OpenSignalsMain.handler.addGui(SignalBox.class, GuiSignalBox::new);
        OpenSignalsMain.handler.addGui(RedstoneIO.class, NamableGui::new);
        OpenSignalsMain.handler.addGui(Signal.class, NamableGui::new);
        OpenSignalsMain.handler.addGui(PathwayRequester.class, GuiPathwayRequester::new);
        OpenSignalsMain.handler.addGui(TrainNumberBlock.class, GuiTrainNumber::new);
        OpenSignalsMain.handler.addGui(SignalBridgeBasicBlock.class, GuiSignalBridge::new);
        OpenSignalsMain.handler.addGui(Monitor.class, GuiMonitorSelection::new);
        OpenSignalsMain.handler.addGui(MonitorTEBlock.class, GuiMonitor::new);
        OpenSignalsMain.handler.addGui(SignalReader.class, GuiSignalReader::new);

        ModelLoaderRegistry.registerLoader(CustomModelLoader.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(SignalTileEntity.class,
                new SignalSpecialRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(MonitorTileEntity.class,
                new MonitorSpecialRenderer());
        MinecraftForge.EVENT_BUS.register(OSModels.class);
        MinecraftForge.EVENT_BUS.register(ClientRenderUpdate.INSTANCE);

        UISignalBoxProfile.loadSignalBoxUIProfiles();
    }
}