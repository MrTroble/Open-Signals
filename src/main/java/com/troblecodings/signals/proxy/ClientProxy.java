package com.troblecodings.signals.proxy;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.blocks.SignalController;
import com.troblecodings.signals.guis.GuiPlacementtool;
import com.troblecodings.signals.guis.GuiRedstoneIO;
import com.troblecodings.signals.guis.GuiSignal;
import com.troblecodings.signals.guis.GuiSignalBox;
import com.troblecodings.signals.guis.GuiSignalController;
import com.troblecodings.signals.init.OSModels;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ClientProxy extends CommonProxy {

	@Override
	public void preinit(final FMLCommonSetupEvent event) {
		super.preinit(event);
		OpenSignalsMain.handler.addGui(Placementtool.class, GuiPlacementtool::new);
		OpenSignalsMain.handler.addGui(SignalController.class, GuiSignalController::new);
		OpenSignalsMain.handler.addGui(SignalBox.class, GuiSignalBox::new);
		OpenSignalsMain.handler.addGui(RedstoneIO.class, GuiRedstoneIO::new);
		OpenSignalsMain.handler.addGui(Signal.class, GuiSignal::new);

		MinecraftForge.EVENT_BUS.register(OSModels.class);
	}

}
