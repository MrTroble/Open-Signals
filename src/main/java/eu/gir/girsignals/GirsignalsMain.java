package eu.gir.girsignals;

import java.nio.file.Files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.gir.girsignals.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = GirsignalsMain.MODID, dependencies = "after:opencomputers", acceptedMinecraftVersions = "[1.12.2]")
public class GirsignalsMain {

    @Instance
    private static GirsignalsMain instance;
    public static final String MODID = "girsignals";

    public static GirsignalsMain getInstance() {
        return instance;
    }

    @SidedProxy(serverSide = "eu.gir.girsignals.proxy.CommonProxy", clientSide = "eu.gir.girsignals.proxy.ClientProxy")
    public static CommonProxy proxy;
    public static Logger log = null;
    private static boolean debug;

    /**
     * @return the debug
     */
    public static boolean isDebug() {
        return debug;
    }

    @EventHandler
    public void preinit(final FMLPreInitializationEvent event) {
        debug = Files.isDirectory(event.getSourceFile().toPath());
        log = event.getModLog();
        proxy.preinit(event);
    }

    @EventHandler
    public void init(final FMLInitializationEvent event) {
        proxy.init(event);
    }

    @EventHandler
    public void postinit(final FMLPostInitializationEvent event) {
        proxy.postinit(event);
    }

    public static Logger getLogger() {
        if (log == null)
            log = LogManager.getLogger(MODID);
        return log;
    }
}