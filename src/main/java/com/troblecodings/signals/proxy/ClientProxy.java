package com.troblecodings.signals.proxy;

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
import com.troblecodings.signals.guis.GuiPathwayRequester;
import com.troblecodings.signals.guis.GuiSignalBridge;
import com.troblecodings.signals.guis.GuiTrainNumber;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.handler.ClientSignalStateHandler;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.models.CustomModelLoader;
import com.troblecodings.signals.signalbridge.SignalBridgeBasicBlock;
import com.troblecodings.signals.tileentitys.SignalSpecialRenderer;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoaderRegistry;
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
        OpenSignalsMain.handler.addGui(PathwayRequester.class, GuiPathwayRequester::new);
        OpenSignalsMain.handler.addGui(TrainNumberBlock.class, GuiTrainNumber::new);
        OpenSignalsMain.handler.addGui(SignalBridgeBasicBlock.class, GuiSignalBridge::new);
        ModelLoaderRegistry.registerLoader(
                new ResourceLocation(OpenSignalsMain.MODID, "oscustommodelloader"),
                CustomModelLoader.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void preinit(final FMLCommonSetupEvent event) {
        super.preinit(event);
        TileEntityRendererDispatcher.instance.setSpecialRendererInternal(
                (TileEntityType<SignalTileEntity>) BasicBlock.BLOCK_ENTITYS.get(Signal.SUPPLIER),
                new SignalSpecialRenderer(TileEntityRendererDispatcher.instance));
    }
}