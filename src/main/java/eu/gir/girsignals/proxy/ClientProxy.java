package eu.gir.girsignals.proxy;

import eu.gir.girsignals.blocks.RedstoneIO;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.blocks.SignalBox;
import eu.gir.girsignals.blocks.SignalController;
import eu.gir.girsignals.guis.GuiPlacementtool;
import eu.gir.girsignals.guis.GuiRedstoneIO;
import eu.gir.girsignals.guis.GuiSignal;
import eu.gir.girsignals.guis.GuiSignalBox;
import eu.gir.girsignals.guis.GuiSignalController;
import eu.gir.girsignals.init.GIRModels;
import eu.gir.girsignals.items.Placementtool;
import eu.gir.girsignals.models.GIRCustomModelLoader;
import eu.gir.girsignals.signalbox.SignalBoxTileEntity;
import eu.gir.girsignals.tileentitys.RedstoneIOTileEntity;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import eu.gir.girsignals.tileentitys.SignalSpecialRenderer;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
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
        GuiHandler.addGui(SignalController.class, SignalControllerTileEntity.class, GuiSignalController::new);
        GuiHandler.addGui(SignalBox.class, SignalBoxTileEntity.class, GuiSignalBox::new);
        GuiHandler.addGui(RedstoneIO.class, RedstoneIOTileEntity.class, GuiRedstoneIO::new);
        GuiHandler.addGui(Signal.class, SignalTileEnity.class, GuiSignal::new);

        MinecraftForge.EVENT_BUS.register(GIRModels.class);
        ModelLoaderRegistry.registerLoader(new GIRCustomModelLoader());
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
