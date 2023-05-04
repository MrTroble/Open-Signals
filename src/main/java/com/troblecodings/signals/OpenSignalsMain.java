package com.troblecodings.signals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import com.troblecodings.contentpacklib.FileReader;
import com.troblecodings.core.net.NetworkHandler;
import com.troblecodings.guilib.ecs.GuiHandler;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.init.OSModels;
import com.troblecodings.signals.init.OSSounds;
import com.troblecodings.signals.proxy.ClientProxy;
import com.troblecodings.signals.proxy.CommonProxy;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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
        MinecraftForge.EVENT_BUS.register(SignalBoxHandler.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> eventBus.register(OSModels.class));
    }

    public static CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new,
            () -> CommonProxy::new);
    private static Logger log = null;
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

    @SubscribeEvent
    public void preInit(final FMLConstructModEvent event) {
        debug = false;
        log = LoggerContext.getContext().getLogger(MODID);
        contentPacks = new FileReader(MODID, "assets/" + MODID, log,
                name -> getRessourceLocation(name), OSBlocks.class);
        proxy.initModEvent(event);
    }

    @SubscribeEvent
    public void client(final FMLClientSetupEvent event) {
        /*
         * OSBlocks.BLOCKS_TO_REGISTER.forEach(block -> {
         * ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped()); });
         */
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

    private static FileSystem fileSystemCache;

    public static Path getRessourceLocation(final String location) {
        String filelocation = location;
        final URL url = OSBlocks.class.getResource("/assets/opensignals");
        try {
            if (url != null) {
                final URI uri = url.toURI();
                if ("file".equals(uri.getScheme())) {
                    if (!location.startsWith("/"))
                        filelocation = "/" + filelocation;
                    final URL resource = OSBlocks.class.getResource(filelocation);
                    if (resource == null)
                        return null;
                    return Paths.get(resource.toURI());
                } else {
                    if (!"jar".equals(uri.getScheme())) {
                        return null;
                    }
                    if (fileSystemCache == null) {
                        fileSystemCache = FileSystems.newFileSystem(uri, Collections.emptyMap());
                    }
                    return fileSystemCache.getPath(filelocation);
                }
            }
        } catch (final IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }
}
