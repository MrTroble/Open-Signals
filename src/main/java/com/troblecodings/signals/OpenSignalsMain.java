package com.troblecodings.signals;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import com.troblecodings.contentpacklib.FileReader;
import com.troblecodings.core.net.NetworkHandler;
import com.troblecodings.guilib.ecs.GuiHandler;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.init.OSModels;
import com.troblecodings.signals.init.OSSounds;
import com.troblecodings.signals.proxy.ClientProxy;
import com.troblecodings.signals.proxy.CommonProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.Pack.Position;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathResourcePack;

@Mod(OpenSignalsMain.MODID)
public class OpenSignalsMain {

    private static OpenSignalsMain instance;
    public static final String MODID = "opensignals";

    public static OpenSignalsMain getInstance() {
        return instance;
    }

    public OpenSignalsMain() {
        instance = this;
        final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.register(this);
        eventBus.register(OSBlocks.class);
        eventBus.register(OSItems.class);
        eventBus.register(OSSounds.class);
        MinecraftForge.EVENT_BUS.register(SignalStateHandler.class);
        MinecraftForge.EVENT_BUS.register(NameHandler.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> eventBus.register(OSModels.class));
    }

    public static CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new,
            () -> CommonProxy::new);
    public static Logger log = null;
    public static GuiHandler handler = null;
    public static NetworkHandler network = null;
    public static FileReader contentPacks = null;
    private static boolean debug;

    /**
     * @return the debug
     */
    public static boolean isDebug() {
        return debug;
    }

    public static final int GUI_SIGNAL_CONTROLLER = 1;

    public IModFile file;

    @SubscribeEvent
    public void preInit(final FMLConstructModEvent event) {
        debug = true;
        log = LoggerContext.getContext().getLogger(MODID);
        final IModInfo modInfo = ModLoadingContext.get().getActiveContainer().getModInfo();
        file = modInfo.getOwningFile().getFile();
        contentPacks = new FileReader(MODID, "assets/" + MODID, log,
                name -> file.findResource(name));
        proxy.initModEvent(event);
    }

    @SubscribeEvent
    public void packEvent(AddPackFindersEvent event) {
        Map<String, Pack> packs = new HashMap<>();
        event.addRepositorySource((consumer, instance) -> {
            if(!packs.isEmpty()) {
                packs.values().forEach(consumer);
                return;
            }
            for (Path path : contentPacks.getPaths()) {
                String fileName = MODID + "internal" + packs.size();
                Component component = new TextComponent(fileName);
                consumer.accept(instance.create(fileName, component, true,
                        () -> new PathResourcePack(fileName, path),
                        new PackMetadataSection(component, 8), Position.TOP, PackSource.DEFAULT,
                        !debug));
            }
        });
    }

    @SubscribeEvent
    public void client(final FMLClientSetupEvent event) {
        OSBlocks.blocksToRegister.forEach(block -> {
            ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped());
        });
    }

    @SubscribeEvent
    public void init(final FMLCommonSetupEvent event) {
        proxy.preinit(event);
    }

    public static Logger getLogger() {
        if (log == null)
            log = LogManager.getLogger(MODID);
        return log;
    }
}
