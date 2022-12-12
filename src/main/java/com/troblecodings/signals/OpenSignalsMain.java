package com.troblecodings.signals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import com.troblecodings.guilib.ecs.GuiHandler;
import com.troblecodings.signals.proxy.ClientProxy;
import com.troblecodings.signals.proxy.CommonProxy;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(OpenSignalsMain.MODID)
public class OpenSignalsMain {

    private static OpenSignalsMain instance;
    public static final String MODID = "girsignals";

    public static OpenSignalsMain getInstance() {
        return instance;
    }

    public OpenSignalsMain() {
    	instance = this;
    	FMLJavaModLoadingContext.get().getModEventBus().addListener(null);
	}
    
    public static CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    public static Logger log = null;
    public static GuiHandler handler = null;
    private static boolean debug;

    /**
     * @return the debug
     */
    public static boolean isDebug() {
        return debug;
    }

    public static final int GUI_SIGNAL_CONTROLLER = 1;

    
    public void preinit(final FMLCommonSetupEvent event) {
        debug = true;
        log = LoggerContext.getContext().getLogger(MODID);
        proxy.preinit(event);
    }

    public static Logger getLogger() {
        if (log == null)
            log = LogManager.getLogger(MODID);
        return log;
    }
}
