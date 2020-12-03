package net.gir.girsignals;

import net.gir.girsignals.proxy.CommonProxy;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

//Modstruktur
@Mod(modid = GIRSignals.MODID, name = GIRSignals.NAME, version = GIRSignals.VERSION)
public class GIRSignals
{
    public static final String MODID = "girsignals";
    public static final String NAME = "GIRSignals";
    public static final String VERSION = "0.0.1";
    
    //Instanz
    @Instance
    private static GIRSignals instance;
    
   	public static GIRSignals getInstance() {
   		return instance;
   	}
    
   	//Proxy
    @SidedProxy(serverSide = "net.gir.girsignals.proxy.CommonProxy", clientSide = "net.gir.girsignals.proxy.ClientProxy")
    private static CommonProxy proxy;
    
    @EventHandler
    public void preinit(FMLPreInitializationEvent event) {
    	proxy.preinit(event);
    	
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	proxy.init(event);
    	
    }
    
    @EventHandler
    public void postinit(FMLPostInitializationEvent event) {
    	proxy.postinit(event);
    	
    }
}
