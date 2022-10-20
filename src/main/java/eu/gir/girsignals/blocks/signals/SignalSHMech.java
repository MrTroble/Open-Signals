package eu.gir.girsignals.blocks.signals;

import java.util.Map;

import eu.gir.girsignals.ChangeableStage;
import eu.gir.girsignals.EnumSignals.SHMech;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.signalbox.config.RSSignalConfig;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.client.gui.FontRenderer;

public class SignalSHMech extends Signal {
    
    public SignalSHMech() {
        super(builder(GIRItems.PLACEMENT_TOOL, "SHMech").height(0).offsetX(7.0f).offsetY(-9.8f)
                .signHeight(1.04f).config(RSSignalConfig.RS_CONFIG).build());
    }

    public static final SEProperty<Boolean> SH_HIGH = SEProperty.of("sh_high", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<SHMech> SH_MECH = SEProperty.of("sh_mech",
            SHMech.SH0, ChangeableStage.APISTAGE_NONE_CONFIG);

    @Override
    public int getHeight(final Map<SEProperty<?>, Object> map) {
        if ((Boolean) map.getOrDefault(SH_HIGH, false)) {
            return 2;
        }
        return super.getHeight(map);
    }
    
    @Override
    public void renderOverlay(final double x, final double y, final double z,
            final SignalTileEnity te, final FontRenderer font) {
        super.renderOverlay(x, y, z, te, font,
                te.getProperty(SH_HIGH).filter(b -> (Boolean) b).isPresent() ? 3.04f
                        : this.prop.customNameRenderHeight);
    }
}
