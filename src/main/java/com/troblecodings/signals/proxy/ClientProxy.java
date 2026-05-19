package com.troblecodings.signals.proxy;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.blocks.Monitor;
import com.troblecodings.signals.blocks.MonitorTEBlock;
import com.troblecodings.signals.blocks.PathwayRequester;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.blocks.SignalController;
import com.troblecodings.signals.blocks.TrainNumberBlock;
import com.troblecodings.signals.guis.GuiMonitor;
import com.troblecodings.signals.guis.GuiMonitorSelection;
import com.troblecodings.signals.guis.GuiPathwayRequester;
import com.troblecodings.signals.guis.GuiPlacementtool;
import com.troblecodings.signals.guis.GuiSignalBox;
import com.troblecodings.signals.guis.GuiSignalBridge;
import com.troblecodings.signals.guis.GuiSignalController;
import com.troblecodings.signals.guis.GuiTrainNumber;
import com.troblecodings.signals.guis.NamableGui;
import com.troblecodings.signals.guis.UISignalBoxProfile;
import com.troblecodings.signals.handler.ClientMonitorNetworkHandler;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.handler.ClientSignalStateHandler;
import com.troblecodings.signals.handler.MonitorNetworkHandler;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.signalbridge.SignalBridgeBasicBlock;
import com.troblecodings.signals.tileentitys.MonitorSpecialRenderer;
import com.troblecodings.signals.tileentitys.MonitorTileEntity;
import com.troblecodings.signals.tileentitys.SignalSpecialRenderer;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void initModEvent(final FMLConstructModEvent event) {
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

        UISignalBoxProfile.loadSignalBoxUIProfiles();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void preinit(final FMLCommonSetupEvent event) {
        super.preinit(event);

        BlockEntityRenderers.register(
                (BlockEntityType<SignalTileEntity>) BasicBlock.BLOCK_ENTITYS.get(Signal.SUPPLIER),
                SignalSpecialRenderer::new);
        BlockEntityRenderers.register((BlockEntityType<MonitorTileEntity>) BasicBlock.BLOCK_ENTITYS
                .get(MonitorTEBlock.SUPPLIER), MonitorSpecialRenderer::new);
    }
}