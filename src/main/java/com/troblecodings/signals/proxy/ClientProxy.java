package com.troblecodings.signals.proxy;

import com.troblecodings.signalbridge.SignalBridgeBasicBlock;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.blocks.PathwayRequester;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.blocks.SignalController;
import com.troblecodings.signals.blocks.TrainNumberBlock;
import com.troblecodings.signals.guis.GuiPlacementtool;
import com.troblecodings.signals.guis.GuiSignalBox;
import com.troblecodings.signals.guis.GuiSignalController;
import com.troblecodings.signals.guis.NamableGui;
import com.troblecodings.signals.guis.PathwayRequesterGui;
import com.troblecodings.signals.guis.SignalBridgeGui;
import com.troblecodings.signals.guis.TrainNumberGui;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.handler.ClientSignalStateHandler;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.items.Placementtool;
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
        OpenSignalsMain.handler.addGui(Placementtool.class, GuiPlacementtool::new);
        OpenSignalsMain.handler.addGui(SignalController.class, GuiSignalController::new);
        OpenSignalsMain.handler.addGui(SignalBox.class, GuiSignalBox::new);
        OpenSignalsMain.handler.addGui(RedstoneIO.class, NamableGui::new);
        OpenSignalsMain.handler.addGui(Signal.class, NamableGui::new);
        OpenSignalsMain.handler.addGui(PathwayRequester.class, PathwayRequesterGui::new);
        OpenSignalsMain.handler.addGui(TrainNumberBlock.class, TrainNumberGui::new);
        OpenSignalsMain.handler.addGui(SignalBridgeBasicBlock.class, SignalBridgeGui::new);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void preinit(final FMLCommonSetupEvent event) {
        super.preinit(event);

        BlockEntityRenderers.register(
                (BlockEntityType<SignalTileEntity>) BasicBlock.BLOCK_ENTITYS.get(Signal.SUPPLIER),
                SignalSpecialRenderer::new);
    }
}