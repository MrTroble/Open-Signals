package eu.gir.girsignals;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresMcRestart;

@Config(modid = GirsignalsMain.MODID)
public final class GIRSignalsConfig {

    private GIRSignalsConfig() {
    }

    @RangeInt(min = 0, max = 15)
    @RequiresMcRestart
    @Comment(value = {
            "", "Make sure to set this value", "before you start playing as",
            "you might need to reset some signals", "after changing this value!"
    })
    public static int signalLightValue = 15;

}
